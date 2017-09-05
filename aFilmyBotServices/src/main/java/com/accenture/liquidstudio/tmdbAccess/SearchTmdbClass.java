package com.accenture.liquidstudio.tmdbAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import com.accenture.liquidstudio.MaxSizeHashMap;
import com.accenture.liquidstudio.accessBotHybridApi.ParseIntentAndReplyService;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import ai.api.model.Result;

public class SearchTmdbClass implements HasMarkupProperty {

	private final String RESULT = "result";

	// query is mapped to a list of movies to reduce api calls, especially when
	// doing a callback
	Map<String, List<MovieInfo>> resultMap;

	private TheMovieDbApi movieDbApi;

	public SearchTmdbClass(TheMovieDbApi movieDbApi, int mapSize) {
		this.movieDbApi = movieDbApi;
		resultMap = new MaxSizeHashMap<>(mapSize);
	}

	public SendMessage searchMovieByName(Result result) throws MovieDbException {

		List<String> params = Utilities.getParametersAsString(result.getParameters());

		if (params.isEmpty()) {
			ParseIntentAndReplyService.setResetContextWithCount(true, 1);
			return Utilities.sendDefaultReply(result);
		}

		List<MovieInfo> movieInfo = generateMessageBodyList(params);

		return Utilities.createMessageToSend(movieInfo, 0, params.toString(), this, RESULT);
	}

	private List<MovieInfo> generateMessageBodyList(List<String> parameters) throws MovieDbException {
		List<MovieInfo> movieInfo = new ArrayList<>();

		//this way we get results for all given queries, even if more than one
		for (String query : parameters) {
			try {
				List<MovieInfo> temp = resultMap.get(query);

				if (temp == null) {
					temp = movieDbApi.searchMovie(query, 0, "", null, 0, 0, SearchType.PHRASE).getResults();
					resultMap.put(query, temp);
					System.out.println("Map entries:" + resultMap.size() + "\n");
				}

				//removes all duplicates and appends to the main list
				temp.removeAll(movieInfo);
				movieInfo.addAll(temp);
			} catch (MovieDbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (movieInfo.isEmpty())
			Utilities.throwMovieDBDummyError();

		return movieInfo;
	}

	public void resultCallBackEditer(EditMessageText message, String substring) throws MovieDbException {
//		System.out.println(substring);

		String[] tokens = substring.split("_");

		if (tokens.length != 3) {
			System.out.println("Error here :P");
		}

		List<String> parameters = Utilities.getListFromString(tokens[2]);
		List<MovieInfo> movieInfo = null;

		movieInfo = generateMessageBodyList(parameters);

		Utilities.editCallBackMessage(message, tokens, parameters.toString(), movieInfo, this, RESULT);
	}

	@Override
	public void setResultMarkup(InlineKeyboardMarkup markup, int c, String parameters, MovieBasic mi,
			boolean nextBackButton, String identifier) {

		int i = 1;
		// identifier result
		if (nextBackButton) {
			Utilities.addInlineButtonToMarkup(markup, i, "previous",
					"view_" + identifier + "_" + c + "_previous_" + parameters + "");
			Utilities.addInlineButtonToMarkup(markup, i++, "next",
					"view_" + identifier + "_" + c + "_next_" + parameters);
		}
		MovieTmdbClass.addSimilarButtonToMarkup(markup, mi, i++);
//		Utilities.addInlineButtonToMarkup(markup, i++, "view similar?", "view_similar_" + mi.getId());
		Utilities.addInlineButtonToMarkup(markup, i++, "Finish", "EndInlineMessaging");
	}

}
