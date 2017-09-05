package com.accenture.liquidstudio.tmdbAccess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.yamj.api.common.exception.ApiExceptionType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import ai.api.model.AIOutputContext;
import ai.api.model.Result;

public class Utilities {

	static GenreHandler genreMap;
	static String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500";

	static {
		genreMap = new GenreHandler();
	}

	public static String readFileData(String fileName) {
		String data = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = in.readLine()) != null)
				data += (str) + "\n";
			in.close();
		} catch (IOException e) {
			e.getMessage();
		}
		return data;
	}

	public static ArrayList<String> getListFromString(String s) {

		String[] strValues = s.substring(1, s.length() - 1).split(",");

		/*
		 * Use asList method of Arrays class to convert Java String array to ArrayList
		 */
		ArrayList<String> aList = new ArrayList<String>(Arrays.asList(strValues));

		return aList;
	}

	public static Set<Integer> getIntSetFromString(String s) {

		String[] strValues = s.substring(1, s.length() - 1).split(",");

		/*
		 * Use asList method of Arrays class to convert Java String array to ArrayList
		 */
		Set<Integer> aSet = new HashSet<>();

		for (String n : (Arrays.asList(strValues))) {
			aSet.add(Integer.parseInt(n.trim()));
		}

		return aSet;
	}

	public static void addInlineButtonToMarkup(InlineKeyboardMarkup markup, int l, String text, String callBack) {
		List<List<InlineKeyboardButton>> rowsInline = markup.getKeyboard();

		if (l > rowsInline.size()) {
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			rowsInline.add(rowInline);
		}

		InlineKeyboardButton button = new InlineKeyboardButton().setText(text).setCallbackData(callBack);
		rowsInline.get(l - 1).add(button);
	}

	public static InlineKeyboardMarkup getNewInlineKeyboardMarkup() {
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		// return the markup
		markupInline.setKeyboard(rowsInline);
		return markupInline;
	}

	public static List<String> getParametersAsString(HashMap<String, JsonElement> parameters) {
		List<String> params = new ArrayList<>();

		if (parameters != null) {
			Collection<JsonElement> query = parameters.values();
			System.out.println(query.size());
			for (JsonElement elem : query) {
				if (elem.isJsonArray()) {
					JsonArray arr = elem.getAsJsonArray();
					for (JsonElement e : arr) {
						// System.out.println("---> " + e.getAsString());
						params.add(e.getAsString());

					}
				} else {
					params.add(elem.getAsString());
				}
			}
		}

		System.out.println("parameters--> " + params);

		return params;
	}

	public static <T extends MovieBasic> String displayString(T mi, int c) {
		String display = "Match no." + (c + 1);

		// System.out.println(mi.toString());

		// System.out.println(mi.getImages().get(0).toString());
		display += "\n\n*Title*: " + mi.getTitle();
		display += "\n*Rating*: " + mi.getVoteAverage();
		display += "\n*Genre*: _" + genreMap.getGenreNames(mi) + "_";
		display += "\n*Overview*: ```" + mi.getOverview() + "```";
		display += "\n[img](" + BASE_IMAGE_URL + mi.getPosterPath() + ")";

		return display;
	}

	public static int getCurrentDisplayItem(String cValue, String move, int size) throws MovieDbException {
		if (size == 0)
			throwMovieDBDummyError();

		int c = Integer.parseInt(cValue);
		if (move.equals("next"))
			return c < size - 1 ? c + 1 : 0;
		else if (move.equals("previous"))
			return c > 0 ? c - 1 : size - 1;
		else
			return c;
	}

	public static SendMessage sendDefaultReply(Result result) {
		String reply = result.getFulfillment().getSpeech();
		SendMessage sm = new SendMessage().setText(reply);

		List<AIOutputContext> contexts = result.getContexts();

		for (AIOutputContext context : contexts) {
			// If we dont reset the contexts, it continues to give an error
			System.out.println(context.getLifespan().intValue());
			context.setLifespan(0);

		}

		return sm;
	}

	public static Set<Integer> getGenreIdsFromList(List<String> params) {
		// using set we remove duplicates since we cannot have same genre multiple times
		// in a movie
		return genreMap.getGenreIds(params);

	}

	/**
	 * This is a generic method that creates a SendMessage instance to be returned
	 * using the classes from which they were called since they implement an
	 * interface with the method setResultMarkup which is different for each class
	 * that needs to be called. This reduces code duplication
	 * 
	 * @return <code>SendMessage</code>
	 * 
	 */
	public static <T extends MovieBasic> SendMessage createMessageToSend(List<T> movieInfo, int c, String parameters,
			HasMarkupProperty markupSetting, String identifier) {

		MovieBasic mi = movieInfo.get(c);
		SendMessage message = new SendMessage().setText(Utilities.displayString(mi, c));

		// addInlineButton(message, mi.getId() + "");?

		// Using new method
		InlineKeyboardMarkup markup = Utilities.getNewInlineKeyboardMarkup();
		markupSetting.setResultMarkup(markup, c, parameters, mi, movieInfo.size() > 1, identifier);
		message.setReplyMarkup(markup);

		return message;
	}

	/**
	 * This is a generic method that updates the EditMessageText instance using the
	 * classes from which they were called since they implement an interface with
	 * the method setResultMarkup that needs to be called. This reduces code
	 * duplication
	 * 
	 * @return <code>SendMessage</code>
	 * 
	 */
	public static <T extends MovieBasic> void editCallBackMessage(EditMessageText message, String[] tokens,
			String parametersString, List<T> movieInfo, HasMarkupProperty markupProperty, String identifier)
			throws MovieDbException {
		int c = Utilities.getCurrentDisplayItem(tokens[0], tokens[1], movieInfo.size());

		InlineKeyboardMarkup markup = Utilities.getNewInlineKeyboardMarkup();
		markupProperty.setResultMarkup(markup, c, parametersString, movieInfo.get(c), movieInfo.size() > 1, identifier);
		message.setReplyMarkup(markup);

		message.setText(Utilities.displayString(movieInfo.get(c), c));
	}

	/**
	 * @throws MovieDbException
	 */
	public static void throwMovieDBDummyError() throws MovieDbException {
		throw new MovieDbException(ApiExceptionType.UNKNOWN_CAUSE, "Dummy error to be caught by a different class");
	}

}
