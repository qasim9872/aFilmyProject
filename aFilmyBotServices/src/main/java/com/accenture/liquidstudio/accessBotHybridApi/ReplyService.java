package com.accenture.liquidstudio.accessBotHybridApi;

import java.io.IOException;
import ai.api.AIServiceException;
import ai.api.model.AIResponse;

public interface ReplyService {
	
	public AIResponse getResponseFromAudioUrl(String url, String string) throws IOException, AIServiceException;

	public AIResponse getResponse(String message_text) throws AIServiceException;
}