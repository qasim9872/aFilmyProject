package com.accenture.liquidstudio.linkBotToTelegram;

import com.accenture.liquidstudio.accessBotHybridApi.ParseIntentAndReplyService;
import com.accenture.liquidstudio.accessBotHybridApi.ReplyService;
import com.accenture.liquidstudio.tmdbAccess.AccessTmdbApi;
import com.accenture.liquidstudio.tmdbAccess.IAccessTmdbApi;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class BotInjectorModule extends AbstractModule {

	//
	private final String botUsername = "<INSERT BOT USERNAME HERE>";

	// The key below is the consumer key, you can train your agent and use that key
	// or use the provided one
	private final String apiaiKey = "0e856cc6394f43e1bc34214dd9aa876d";
	
	
	private final String tmdbAPiKey = "<INSERT TMDB API KEY HERE>";
	private final String botToken = "<INSERT BOT TOKEN HERE>";

	@Override
	protected void configure() {
		// TODO Auto-generated method stub

		bind(ReplyService.class).to(ParseIntentAndReplyService.class);
		bind(IAccessTmdbApi.class).to(AccessTmdbApi.class);

		// Binding all the keys to named constants in order to initialize the classes
		// using dependency injector
		bindConstant().annotatedWith(Names.named("apiaiKey")).to(apiaiKey);
		bindConstant().annotatedWith(Names.named("tmdbApiKey")).to(tmdbAPiKey);
		bindConstant().annotatedWith(Names.named("botToken")).to(botToken);
		bindConstant().annotatedWith(Names.named("botUsername")).to(botUsername);

	}

}
