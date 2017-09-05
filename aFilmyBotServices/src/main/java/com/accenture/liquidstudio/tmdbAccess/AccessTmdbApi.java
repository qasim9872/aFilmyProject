package com.accenture.liquidstudio.tmdbAccess;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.methods.TmdbAuthentication;
import ai.api.model.Result;

public class AccessTmdbApi implements IAccessTmdbApi {

	TmdbAuthentication auth;

	SearchTmdbClass searchClass;
	MovieTmdbClass movieClass;
	DiscoverTmdbClass discoverClass;
	TheMovieDbApi movieDbApi;

	// All sub tmdb classess have cache to keep track of previous searches

	@Inject
	public AccessTmdbApi(@Named("tmdbApiKey") final String tmdbApiKey) throws MovieDbException {
		movieDbApi = new TheMovieDbApi(tmdbApiKey);

		searchClass = new SearchTmdbClass(movieDbApi, 100);
		discoverClass = new DiscoverTmdbClass(movieDbApi, 100);
		movieClass = new MovieTmdbClass(movieDbApi, 100);
	}

	/**
	 * This receives a Result object and matches the action with the correct method
	 * to call, the reason it returns a SendMessage instance is that it allows me to
	 * correctly setup the button and callbackaction markup
	 * 
	 * @return <code>SendMessage </code>
	 * 
	 */
	@Override
	public SendMessage parseResultObject(Result result) throws MovieDbException {

		switch (result.getAction()) {
		case "searchMovieByName":
			System.out.println("looking up movie by name");
			return searchClass.searchMovieByName(result);
		case "searchMovieByGenre":
			System.out.println("looking up movie by genre");
			return discoverClass.searchMovieByGenre(result);
		case "showTopRatedFilms":
			System.out.println("showing top rated films");
			return movieClass.displayTopRatedFilms(result);
		case "showPopularFilms":
			System.out.println("showing popular films");
			return movieClass.displayPopularFilms(result);
		case "nowPlayingFilms":
			System.out.println("showing now playing films");
			return movieClass.displayNowPlayingFilms(result);
		default:
			// Send default reply from api.ai
			return Utilities.sendDefaultReply(result);
		}
	}

	/**
	 * Within the call back query there is a string containing all the information,
	 * elements inside are seperated by an underscore(_) symbol. They are split up
	 * and the values tell which flow needs to be taken
	 * 
	 * It updates the incoming message with the new data
	 * 
	 */
	@Override
	public void callBackParser(EditMessageText message, String token) throws MovieDbException {
		// System.out.println("callBackParserToken--> " + token);
		String[] tokens = token.split("_");
		switch (tokens[0]) {
		case "original":
			System.out.println("showing the original film, token-->" + token);
			movieClass.originalCallBackEditer(message, token.substring(9));
			break;
		case "similar":
			System.out.println("showing similar films, token-->" + token);
			movieClass.similarCallBackEditer(message, token.substring(8));
			break;
		case "result":
			System.out.println("showing the search result, token-->" + token);
			searchClass.resultCallBackEditer(message, token.substring(7));
			break;
		case "discover":
			System.out.println("showing the discover genre-->" + token);
			discoverClass.discoverCallBackEditer(message, token.substring(9));
			break;
		case "topRated":
			System.out.println("showing top rated films, token-->" + token);
			movieClass.topRatedCallBackEditer(message, token.substring(9));
			break;
		case "popular":
			System.out.println("showing popular films, token-->" + token);
			movieClass.popularCallBackEditer(message, token.substring(8));
			break;
		case "nowPlaying":
			System.out.println("showing now playing films, token-->" + token);
			movieClass.nowPlayingCallBackEditer(message, token.substring(11));
			break;
		default:
			System.out.println("default case, nothing done! token-->" + token);
			break;
		}
	}

}
