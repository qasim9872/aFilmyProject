package com.accenture.liquidstudio.linkBotToTelegram;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.google.inject.Guice;
import com.google.inject.Injector;


/**
 * This class registers the bot with telegram
 * @author muhammad.qasim
 */
public class InitBot {

	public static void main(String[] param) {
		// Initialize Api Context
		ApiContextInitializer.init();

		// Instantiate Telegram Bots API
		TelegramBotsApi botsApi = new TelegramBotsApi();

		// Register our bot
		try {
			botsApi.registerBot(getBotInstance());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This initializes the dependency injector with properties from botinjectormodule class
	 * @return <code>TelegramLongPollingBot </code>
	 * 
	 */
	public static TelegramLongPollingBot getBotInstance()
	{
		Injector injector = Guice.createInjector(new BotInjectorModule());	
		
		MyTelegramBot bot = injector.getInstance(MyTelegramBot.class);
		
		return bot;
	}

}
