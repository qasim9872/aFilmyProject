package com.accenture.liquidstudio.tmdbAccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import com.accenture.liquidstudio.MaxSizeHashMap;
import com.accenture.liquidstudio.accessBotHybridApi.ParseIntentAndReplyService;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.discover.Discover;
import com.omertron.themoviedbapi.model.discover.WithBuilder;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import ai.api.model.Result;

public class DiscoverTmdbClass implements HasMarkupProperty{
	
	private final String DISCOVER = "discover";
	
	private TheMovieDbApi movieDbApi;

	
	//genreId is mapped to a list of movies to reduce api calls, especially when doing a callback
	Map<Set<Integer>, List<MovieBasic>> resultMap;

	public DiscoverTmdbClass(TheMovieDbApi movieDbApi, int mapSize) {
		this.movieDbApi = movieDbApi;
		resultMap = new MaxSizeHashMap<>(mapSize);	
	}

	public SendMessage searchMovieByGenre(Result result) throws MovieDbException {
		List<String> params = Utilities.getParametersAsString(result.getParameters());

		if (params.isEmpty()) {
			ParseIntentAndReplyService.setResetContextWithCount(true, 1);
			return Utilities.sendDefaultReply(result);
		}

		//Using a set to eliminate doubles and equality check allows me to use it in the result map to map to a certain list
		Set<Integer> genreIds = Utilities.getGenreIdsFromList(params);
		
		List<MovieBasic> movieInfo = getMovieInfoList(genreIds);

		return Utilities.createMessageToSend(movieInfo, 0, genreIds.toString(), this, DISCOVER);
	}

	private List<MovieBasic> getMovieInfoList(Set<Integer> genreIds) throws MovieDbException {
		
		List<MovieBasic> movieInfo = resultMap.get(genreIds);
		
		if(movieInfo==null)
		{
			System.out.println("getting values from api");
			Discover d = getDiscoverObject(genreIds);
//			movieInfo = createMovieInfoList(d);
			movieInfo = movieDbApi.getDiscoverMovies(d).getResults();
			resultMap.put(genreIds, movieInfo);
		}

		return movieInfo;
	}

	private Discover getDiscoverObject(Set<Integer> genreIds) {
	
		WithBuilder wb = null;

		for (Integer genreId : genreIds) {
			if (wb == null)
				wb = new WithBuilder(genreId);
			else
				wb.and(genreId);
		}
		wb.build();

		return new Discover().withGenres(wb);
	}
	
	@Override
	public void setResultMarkup(InlineKeyboardMarkup markup, int c, String parameters, MovieBasic mi,
			boolean nextBackButton, String identifier) {

		int i = 1;

		if (nextBackButton) {
			Utilities.addInlineButtonToMarkup(markup, i, "previous",
					"view_"+identifier+"_" + c + "_previous_" + parameters + "");
			Utilities.addInlineButtonToMarkup(markup, i++, "next",
					"view_"+identifier+"_" + c + "_next_" + parameters);
		}
		MovieTmdbClass.addSimilarButtonToMarkup(markup, mi, i++);
//		Utilities.addInlineButtonToMarkup(markup, i++, "view similar?", "view_similar_" + mi.getId());
		Utilities.addInlineButtonToMarkup(markup, i++, "Finish", "EndInlineMessaging");
	}

	public void discoverCallBackEditer(EditMessageText message, String substring) throws MovieDbException {
		System.out.println(substring);
		
		String[] tokens = substring.split("_");
		
		if (tokens.length != 3) {
			System.out.println("Error here :P");
		}
		
		Set<Integer> genreIds = Utilities.getIntSetFromString(tokens[2]);
		List<MovieBasic> movieInfo = null;
		
		movieInfo = getMovieInfoList(genreIds);
		
		
		Utilities.editCallBackMessage(message, tokens, genreIds.toString(),
				movieInfo, this, DISCOVER);
		
	}

}
