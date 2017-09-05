package com.accenture.liquidstudio.tmdbAccess;

import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import com.accenture.liquidstudio.MaxSizeHashMap;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import ai.api.model.Result;

public class MovieTmdbClass implements HasMarkupProperty {
	private final String TOPRATED = "topRated", POPULAR = "popular", NOWPLAYING = "nowPlaying";

	// TmdbMovies movies;

	// query is mapped to a list of movies to reduce api calls, especially when
	// doing a callback
	Map<String, List<MovieInfo>> resultMap;

	private TheMovieDbApi movieDbApi;

	public MovieTmdbClass(TheMovieDbApi movieDbApi, int mapSize) {
		this.movieDbApi = movieDbApi;
		resultMap = new MaxSizeHashMap<>(mapSize);
	}

	@Override
	public void setResultMarkup(InlineKeyboardMarkup markup, int c, String parameters, MovieBasic mi,
			boolean nextBackButton, String identifier) {
		int i = 1;

		if (nextBackButton) {
			Utilities.addInlineButtonToMarkup(markup, i, "previous", "view_" + identifier + "_" + c + "_previous");
			Utilities.addInlineButtonToMarkup(markup, i++, "next", "view_" + identifier + "_" + c + "_next");
		}
		addSimilarButtonToMarkup(markup, mi, i++);
		Utilities.addInlineButtonToMarkup(markup, i++, "Finish", "EndInlineMessaging");
	}

	// VIEW ORIGINAL

	public void originalCallBackEditer(EditMessageText message, String substring) {

		// System.out.println(substring);

		int id = Integer.parseInt(substring);

		try {
			MovieInfo mi = movieDbApi.getMovieInfo(id, "", "");

			// System.out.println(mi.toString());

			message.setText(Utilities.displayString(mi, 0));

			InlineKeyboardMarkup markup = Utilities.getNewInlineKeyboardMarkup();
			Utilities.addInlineButtonToMarkup(markup, 1, "Finish", "EndInlineMessaging");
			addSimilarButtonToMarkup(markup, mi, 2);
			message.setReplyMarkup(markup);

		} catch (MovieDbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// SIMILAR FILMS

	/**
	 * @param markup
	 * @param mi
	 * @param i
	 * @return
	 */
	public static void addSimilarButtonToMarkup(InlineKeyboardMarkup markup, MovieBasic mi, int i) {
		Utilities.addInlineButtonToMarkup(markup, i, "view similar?",
				"view_similar_" + mi.getId() + "_" + 0 + "_placeholder");
	}

	public void similarCallBackEditer(EditMessageText message, String token) throws MovieDbException {
//		System.out.println("--->>>>>> " + token);
		String[] tokens = token.split("_");

		int id = Integer.parseInt(tokens[0]);

		List<MovieInfo> movies = getSimilarMoviesList(id, null);

		int c = Utilities.getCurrentDisplayItem(tokens[1], tokens[2], movies.size());

		MovieInfo mi = movies.get(c);
		message.setText(Utilities.displayString(mi, c));

		InlineKeyboardMarkup markup = Utilities.getNewInlineKeyboardMarkup();
		setSimilarResultMarkup(markup, id, c, movies.size() > 1);
		message.setReplyMarkup(markup);

		System.out.println("new current display item value: "+c);
	}

	private void setSimilarResultMarkup(InlineKeyboardMarkup markup, int id, int c, boolean nextBackButton) {
		int i = 1;
		if (nextBackButton) {
			Utilities.addInlineButtonToMarkup(markup, i, "previous", "view_similar_" + id + "_" + c + "_previous");
			Utilities.addInlineButtonToMarkup(markup, i++, "next", "view_similar_" + id + "_" + c + "_next");
		}
		Utilities.addInlineButtonToMarkup(markup, i++, "view original", "view_original_" + id);
		Utilities.addInlineButtonToMarkup(markup, i++, "Finish", "EndInlineMessaging");
	}

	private List<MovieInfo> getSimilarMoviesList(int id, Integer numberOfResultPages) throws MovieDbException {

		List<MovieInfo> result = resultMap.get(id + "");

		if (result == null || result.isEmpty()) {
			result = movieDbApi.getSimilarMovies(id, numberOfResultPages, "").getResults();
			System.out.println("movie list retrieved here --> " + result.toString());
			resultMap.put(id + "", result);
		}

		return result;
	}

	// TOP RATED FILMS

	public SendMessage displayTopRatedFilms(Result response) throws MovieDbException {
		List<MovieInfo> movieInfo = getTopRatedMovieList();

		return Utilities.createMessageToSend(movieInfo, 0, "", this, TOPRATED);
	}

	private List<MovieInfo> getTopRatedMovieList() throws MovieDbException {
		List<MovieInfo> topRatedMovies = resultMap.get(TOPRATED);

		if (topRatedMovies == null) {
			topRatedMovies = movieDbApi.getTopRatedMovies(null, "").getResults();
			resultMap.put(TOPRATED, topRatedMovies);
		}

		return topRatedMovies;

	}

	public void topRatedCallBackEditer(EditMessageText message, String substring) throws MovieDbException {
		System.out.println(substring);

		String[] tokens = substring.split("_");

		if (tokens.length != 2) {
			System.out.println("Error here :P");
		}

		List<MovieInfo> movieInfo = getTopRatedMovieList();

		Utilities.editCallBackMessage(message, tokens, "", movieInfo, this, TOPRATED);
	}

	// POPULAR FILMS

	public SendMessage displayPopularFilms(Result response) throws MovieDbException {
		List<MovieInfo> movieInfo = getPopularMovieList();

		return Utilities.createMessageToSend(movieInfo, 0, "", this, POPULAR);
	}

	private List<MovieInfo> getPopularMovieList() throws MovieDbException {
		List<MovieInfo> popularMovies = resultMap.get(POPULAR);

		if (popularMovies == null) {
			popularMovies = movieDbApi.getPopularMovieList(null, "").getResults();
			resultMap.put(POPULAR, popularMovies);
		}

		return popularMovies;
	}

	public void popularCallBackEditer(EditMessageText message, String substring) throws MovieDbException {
		System.out.println(substring);

		String[] tokens = substring.split("_");

		if (tokens.length != 2) {
			System.out.println("Error here :P");
		}

		List<MovieInfo> movieInfo = getPopularMovieList();

		Utilities.editCallBackMessage(message, tokens, "", movieInfo, this, POPULAR);
	}

	// NOW PLAYING FILMS

	public SendMessage displayNowPlayingFilms(Result response) throws MovieDbException {
		List<MovieInfo> movieInfo = getNowPlayingMovieList();

		return Utilities.createMessageToSend(movieInfo, 0, "", this, NOWPLAYING);
	}

	private List<MovieInfo> getNowPlayingMovieList() throws MovieDbException {
		List<MovieInfo> nowPlayingMovies = resultMap.get(NOWPLAYING);

		if (nowPlayingMovies == null) {
			nowPlayingMovies = movieDbApi.getNowPlayingMovies(null, "").getResults();
			resultMap.put(NOWPLAYING, nowPlayingMovies);
		}

		return nowPlayingMovies;
	}

	public void nowPlayingCallBackEditer(EditMessageText message, String substring) throws MovieDbException {
		System.out.println(substring);

		String[] tokens = substring.split("_");

		if (tokens.length != 2) {
			System.out.println("Error here :P");
		}

		List<MovieInfo> movieInfo = getNowPlayingMovieList();

		Utilities.editCallBackMessage(message, tokens, "", movieInfo, this, NOWPLAYING);
	}

}
