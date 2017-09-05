package com.accenture.liquidstudio;

import java.io.IOException;

import org.json.JSONObject;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class GetMovieList {

	public static void main(String[] params) {
		 generateMovieFile(1, 1000);
//		for (int i = 0; i < 10; i++) {
//			printResponse(getMovieInfo(i));
//		}
	}

	public static void generateMovieFile(int min, int max) {

		WriteToFileUtil writer = new WriteToFileUtil("movieInfo.txt");

		int count = min;
		boolean execute = true;
		while (execute && count < max) {
			Response r = getMovieInfo(count);
			System.out.print(count + "");

			if (r.isSuccessful()) {
				appendResponseInCSVForm(writer, r, 2);

				count++;
			} else {
				// System.out.println("request failed: " + r.code());
				if (r.code() == 404) {
					count++;
					System.out.println("\t not found");
				} else if (r.code() == 429) {
					// Wait a certain amount of time before continuing
					// System.out.println("\nwaiting...: " + r.code());
					System.out.println("\twaiting");
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("\nunknown error:\n");
					printResponse(r);
				}
				try {
					r.body().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		writer.closeWriter();
	}

	private static void appendResponseInCSVForm(WriteToFileUtil writer, Response r, int i) {
		try {

			JSONObject myObject = new JSONObject(r.body().string());

			String movieOriginalTitle = "\"" + myObject.get("original_title") + "\"";
			String movieTitle = "\"" + myObject.get("title") + "\"";
			
			String sentence = movieOriginalTitle + "," + movieOriginalTitle;
			
			if(!movieOriginalTitle.equals(movieTitle))
				sentence+=","+movieTitle;
			
			writer.writeToFile(sentence+ "\n");

			System.out.println("\t" + sentence);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printResponse(Response r) {
		try {
			System.out.println("request successful: " + r.body().string());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Response getMovieInfo(int id) {
		// System.out.println(id);
		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/octet-stream");
		RequestBody body = RequestBody.create(mediaType, "{}");
		Request request = new Request.Builder().url(
				"https://api.themoviedb.org/3/movie/" + id + "?language=en-US&api_key=9bd86e6a97f659f0484a59ffb93a1858")
				.get().build();

		try {
			return client.newCall(request).execute();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
