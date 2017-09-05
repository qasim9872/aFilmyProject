package com.accenture.liquidstudio.linkBotToTelegram;

import com.accenture.liquidstudio.accessBotHybridApi.ParseIntentAndReplyService;
import com.accenture.liquidstudio.accessBotHybridApi.ReplyService;
import com.accenture.liquidstudio.tmdbAccess.AccessTmdbApi;
import com.accenture.liquidstudio.tmdbAccess.IAccessTmdbApi;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class BotInjectorModule extends AbstractModule {

//	private final String apiaiKey = "f708da8c8f974d3f9bc7e9c22d759c5a";
	private final String botUsername = "AFilmyFirstTryBot";
	private final String apiaiKey = "0e856cc6394f43e1bc34214dd9aa876d";
	private final String tmdbAPiKey = "9bd86e6a97f659f0484a59ffb93a1858";
	private final String botToken = "376265490:AAG3IS9bTpiQHSnnOpGZ2JTstUohNDzqXBw";
	
	
	@Override
	protected void configure() {
		// TODO Auto-generated method stub
		
		bind(ReplyService.class).to(ParseIntentAndReplyService.class);
		bind(IAccessTmdbApi.class).to(AccessTmdbApi.class);
		
		
		//Binding all the keys to named constants in order to initialize the classes using dependency injector
		bindConstant().annotatedWith(Names.named("apiaiKey")).to(apiaiKey);
		bindConstant().annotatedWith(Names.named("tmdbApiKey")).to(tmdbAPiKey);
		bindConstant().annotatedWith(Names.named("botToken")).to(botToken);
		bindConstant().annotatedWith(Names.named("botUsername")).to(botUsername);
		
	}

}
