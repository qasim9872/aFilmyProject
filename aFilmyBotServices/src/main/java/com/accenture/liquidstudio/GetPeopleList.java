
package com.accenture.liquidstudio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.accenture.liquidstudio.WriteToFileUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class GetPeopleList {

	public static void main(String[] param) {
		// System.out.println(specialEntries(150));
		// createPostBody("Person", 100);
		generatePersonFile(10000, 15000);

	}

	public static JSONObject foo(String url, String jsonString) {
		JSONObject jsonObjectResp = null, json = new JSONObject(jsonString);

		try {

			MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			OkHttpClient client = new OkHttpClient();

			RequestBody body = RequestBody.create(JSON, json.toString());
			Request request = new Request.Builder().url(url)
					.addHeader("Authorization", "f708da8c8f974d3f9bc7e9c22d759c5a").post(body).build();

			Response response = client.newCall(request).execute();

			String networkResp = response.body().string();
			System.out.println(networkResp);
			if (!networkResp.isEmpty()) {
				jsonObjectResp = parseJSONStringToJSONObject(networkResp);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			String err = String.format("{\"result\":\"false\",\"error\":\"%s\"}", ex.getMessage());
			jsonObjectResp = parseJSONStringToJSONObject(err);
		}

		return jsonObjectResp;
	}

	private static JSONObject parseJSONStringToJSONObject(final String strr) {

		JSONObject response = null;
		try {
			response = new JSONObject(strr);
		} catch (Exception ex) {
			// Log.e("Could not parse malformed JSON: \"" + json + "\"");
			try {
				response = new JSONObject();
				response.put("result", "failed");
				response.put("data", strr);
				response.put("error", ex.getMessage());
			} catch (Exception exx) {
			}
		}
		return response;
	}

	public static String createPostBody(String entityName, int length) {
		String body = "{\n\t\"name\":" + entityName + "," + "\n\t" + entries(length) + "\n}";

		System.out.println(body);
		return body;
	}

	public static String entries(int length) {
		String body = "\"entries\": [";

		for (int i = 6; i <= length; i++) {
			try {
				body += getNameArray(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (i <= length - 1)
				body += ",";
		}

		body += "]";

		return body;
	}

	public static String specialEntries(int length) {
		String body = "";

		for (int i = length; i >= 59; i--) {
			try {
				body += getNameArray(i);

				if (i >= 60) {
					System.out.println("printing comma for i:" + i);
					body += ",";
				}
			} catch (Exception e) {
				System.out.println(i + " error");
			}

		}

		body += "]";

		return body;
	}

	// retrieves person info from tmdb with the given id and creates a string which
	// could be posted to api.ai
	public static String getNameArray(int id) throws Exception {
		// System.out.println(id);
		String nameArray = "\n{\n\t\"value\":";

		Response r = getPersonInfo(id);

		try {
			String result = (r.body().string());
			System.out.println(result);
			JSONObject myObject = new JSONObject(result);
			String personName;

			try {
				personName = myObject.get("name") + "";
				String gender = myObject.get("gender") + "";
				if (!gender.equals("1"))
					throw new Exception();
			} catch (Exception e) {
				throw new Exception();
			}

			System.out.println("<_____>");
			nameArray += "\"" + personName + "\",\n\t\"synonyms\":[\"" + personName + "\"";

			JSONArray json = myObject.getJSONArray("also_known_as");

			int l = json.length();
			for (int i = 0; i < l; i++) {

				nameArray += ",";
				nameArray += "\"" + json.getString(i) + "\"";

			}

			nameArray += "]\n}";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nameArray;
	}

	static WriteToFileUtil writer, writer2;

	public static void generatePersonFile(int min, int max) {

		writer = new WriteToFileUtil("actor1.txt");
		writer2 = new WriteToFileUtil("model1.txt");

		int count = min;
		boolean execute = true;
		while (execute && count < max) {
			Response r = getPersonInfo(count);
			System.out.println(count + "");

			if (r.isSuccessful()) {
				appendResponseInCSVForm(r, 2);

				count++;
			} else {
				// System.out.println("request failed: " + r.code());
				if (r.code() == 404)
					count++;
				else if (r.code() == 429) {
					// Wait a certain amount of time before continuing
					// System.out.println("\nwaiting...: " + r.code());
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
		writer2.closeWriter();

	}

	private static void appendResponseInCSVForm(Response r, int gender) {
		List<String> s = new ArrayList<>();
		try {
			JSONObject myObject = new JSONObject(r.body().string());

			String personName = "\"" + myObject.get("name") + "\"";
			s.add(personName);
			s.add(personName);

			JSONArray json = myObject.getJSONArray("also_known_as");
			for (int i = 0; i < json.length(); i++) {
				s.add("\"" + json.getString(i) + "\"");
			}

			String sentence = (Arrays.toString(s.toArray()));
			sentence = sentence.substring(1, sentence.length() - 1);

			String genderValue = myObject.get("gender") + "";
			// System.out.println(sentence);
			if (genderValue.equals(gender + "")) {
				System.out.print("  --> actor");
				writer.writeToFile(sentence + "\n");
				// ls.add(sentence);
			} else if (genderValue.equals("1")) {
				System.out.print("  --> model");
				writer2.writeToFile(sentence + "\n");
			} else {
				System.out.println(sentence);
			}

		} catch (JSONException | IOException e) {
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

	public static Response getPersonInfo(int id) {
		// System.out.println(id);
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder().url("https://api.themoviedb.org/3/person/" + id
				+ "?language=en-US&api_key=9bd86e6a97f659f0484a59ffb93a1858").get().build();

		try {
			return client.newCall(request).execute();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}