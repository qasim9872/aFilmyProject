package com.accenture.liquidstudio.accessBotHybridApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.speech.gcp.GcpAIConfiguration;
import ai.api.speech.gcp.GcpAIDataService;

public class ParseIntentAndReplyService implements ReplyService {

	private AIDataService dataService;
	private GcpAIDataService gcpDataService;

	private final String TARGET_FILE_TYPE = "oga";

	@Inject
	public ParseIntentAndReplyService(@Named("apiaiKey") final String apiaiKey) {

		AIConfiguration configuration = new AIConfiguration(apiaiKey);
		GcpAIConfiguration gcpConfiguration = new GcpAIConfiguration(apiaiKey);

		gcpConfiguration.setRecognitionConfig(cloneAndAmendRecognitionObject(gcpConfiguration.getRecognitionConfig()));

		dataService = new AIDataService(configuration);
		gcpDataService = new GcpAIDataService(gcpConfiguration);

		// converter = new FileFormatConverter();
	}

	/**
	 * This method takes a RecognitionConfig object and clones it appending the
	 * AudioEncoding to match the one we are using, by default it is set to LINEAR16
	 * in this case oga since telegram holds audio files in that format
	 * 
	 * @param config
	 * @return <code>RecognitionConfig</code>
	 */
	private RecognitionConfig cloneAndAmendRecognitionObject(RecognitionConfig config) {
		return RecognitionConfig.newBuilder().setEncoding(AudioEncoding.OGG_OPUS)
				.setSampleRateHertz(config.getSampleRateHertz()).setLanguageCode(config.getLanguageCode()).build();
	}

	private static boolean resetContext = false;
	private static int resetContextTrueCount = 0;

	/**
	 * makes a request to the Api.ai to parse the given text and return a response
	 * which contains the intent alongside the parameters
	 * 
	 * @return <code>AIResponse</code>
	 * 
	 */
	@Override
	public AIResponse getResponse(String message_text) throws AIServiceException {

		AIRequest request = new AIRequest(message_text);

		// This resets contexts every request cycle if checkResetContext is true
		request.setResetContexts(checkResetContext());

		return dataService.request(request);
	}

	/**
	 * The file is downloaded from the given url and saved on disk until the end of
	 * current executing method, this method then passes the audio for it to be parsed
	 * 
	 * @return <code>AIResponse </code>
	 * 
	 */
	@Override
	public AIResponse getResponseFromAudioUrl(String url, String fileId) throws AIServiceException, IOException {

		File source = new File("audio\\" + fileId + "." + TARGET_FILE_TYPE);

		if (!source.exists())
			source.createNewFile();

		// String convertedFileUrl = converter.convert(url, TARGET_FILE_TYPE);

		downloadFile(url, source);

		return getResponseFromAudioFile(source);
	}

	/**
	 * downloads the file present at the url into the file object given
	 * 
	 * @param url
	 * @param source
	 * @throws IOException
	 */
	private static void downloadFile(String url, File source) throws IOException {
		URL website = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(source);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();

		System.out.println("file downloaded");
	}

	/**
	 * 
	 * This method parses the audio present in the given file into intent and parameters
	 * 
	 * @param file
	 * @return <code>AIResponse</code>
	 * @throws AIServiceException
	 * @throws IOException
	 */
	public AIResponse getResponseFromAudioFile(File file) throws AIServiceException, IOException {

		FileInputStream inputStream = new FileInputStream(file);

		AIResponse response = gcpDataService.voiceRequest(inputStream);

		inputStream.close();

		return response;
	}

	public static boolean checkResetContext() {
		if (isResetContext() && resetContextTrueCount > 0) {
			resetContextTrueCount--;
			return true;
		}

		return false;
	}

	public static void setResetContextWithCount(boolean resetContext, int resetContextTrueCount) {
		ParseIntentAndReplyService.resetContext = resetContext;
		ParseIntentAndReplyService.resetContextTrueCount = resetContextTrueCount;
	}

	public static boolean isResetContext() {
		return resetContext;
	}

	public static void setResetContext(boolean resetContext) {
		ParseIntentAndReplyService.resetContext = resetContext;
	}

}
