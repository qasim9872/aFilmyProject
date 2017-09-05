package com.accenture.liquidstudio.linkBotToTelegram;

import java.io.IOException;

import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.accenture.liquidstudio.accessBotHybridApi.ReplyService;
import com.accenture.liquidstudio.tmdbAccess.IAccessTmdbApi;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.omertron.themoviedbapi.MovieDbException;

import ai.api.AIServiceException;
import ai.api.model.AIResponse;

public class MyTelegramBot extends TelegramLongPollingBot {

	@Inject
	private ReplyService replyGenerator;

	@Inject
	private IAccessTmdbApi tmdb;

	@Inject
	@Named("botToken")
	private String botToken;

	@Inject
	@Named("botUsername")
	private String botUsername;

	public String getBotUsername() {
		// TODO Auto-generated method stub
		return botUsername;
	}

	public void onUpdateReceived(Update update) {

		if (update.hasMessage()) {
			// Set variables

			String errorMessage = "";
			AIResponse response = null;
			SendMessage messageToBeSent = null;
			long chat_id = update.getMessage().getChatId();

			try {

				if (update.getMessage().hasText()) {
					
					errorMessage = "Unable to parse the given text";
					
					response = replyGenerator.getResponse(update.getMessage().getText());

				} else if (update.getMessage().getVoice() != null) {

					File voiceFile = getFile(new GetFile().setFileId(update.getMessage().getVoice().getFileId()));

					errorMessage = "Unable to handle the audio, the feature is currently under development";

					response = replyGenerator.getResponseFromAudioUrl(voiceFile.getFileUrl(botToken),
							voiceFile.getFileId());

				} else {
					// In case message has a different type of input such as image or video etc
					errorMessage = "Unable to handle the given input, the feature will soon be considered for development";
				}

				if (isResponseValid(response)) {
					System.out.println("\ngiven input       --> " + response.getResult().getResolvedQuery());
					System.out.println("api parsed output --> " + response.getResult().getFulfillment().getSpeech());
					System.out.println();
					messageToBeSent = tmdb.parseResultObject(response.getResult());
					messageToBeSent.setParseMode("Markdown");
				} else {
					// In the finally block, a message with the set error message will be sent
				}

			} catch (AIServiceException e) {
				e.printStackTrace();
				errorMessage = ("There has been some sort of issue with the api ai");
			} catch (TelegramApiException e) {
				e.printStackTrace();
				errorMessage = ("There has been some sort of issue with telegram api");
			} catch (IOException e) {
				e.printStackTrace();
				errorMessage = ("There has been some sort of issue with input output of files");
			} catch (MovieDbException e) {
				e.printStackTrace();
				errorMessage = ("We were unable to match your query to anything, please try again");
			} finally {
				if (messageToBeSent == null)
					messageToBeSent = getNewMessageWithText(errorMessage);
				messageToBeSent.setChatId(chat_id);
				sendGivenMessage(messageToBeSent);
			}

		} else if (update.hasCallbackQuery()) {
			String call_data = update.getCallbackQuery().getData();
			long message_id = update.getCallbackQuery().getMessage().getMessageId();
			long chat_id = update.getCallbackQuery().getMessage().getChatId();
			String message_content = update.getCallbackQuery().getMessage().getText();

			EditMessageText new_message = new EditMessageText().setChatId(chat_id)
					.setMessageId(java.lang.Math.toIntExact(message_id)).setParseMode("Markdown");

			try {

				if (call_data.equals("EndInlineMessaging")) {
					System.out.println("end in line messaging");

					new_message.setText(message_content).setReplyMarkup(null);

				} else if (call_data.startsWith("view_")) {

//					System.out.println("call back query --> " + call_data);
					tmdb.callBackParser(new_message, call_data.substring(5));

				} else {
					new_message.setText("Something seems to have gone wrong");
				}
					
				editMessageText(new_message);

			} catch (MovieDbException e) {
				
				sendEditMessage(new_message, "Unable to load the appropriate movie");

			} catch (TelegramApiException e) {
				
				sendEditMessage(new_message, "There seems to have been an issue handling your query");
				
			}

		}

	}

	/**
	 * @param new_message
	 * @param message
	 * @throws TelegramApiException
	 */
	private void sendEditMessage(EditMessageText new_message, String message) {
		try {
			new_message.setText(message);
			editMessageText(new_message);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param messageToBeSent
	 * @throws TelegramApiException
	 */
	private void sendGivenMessage(SendMessage messageToBeSent) {
		try {
			sendMessage(messageToBeSent);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private SendMessage getNewMessageWithText(String text) {
		return new SendMessage().setText(text);
	}

	public boolean isResponseValid(AIResponse response) {
		return (response != null && response.getStatus().getCode() == 200);
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return botToken;
	}

}
