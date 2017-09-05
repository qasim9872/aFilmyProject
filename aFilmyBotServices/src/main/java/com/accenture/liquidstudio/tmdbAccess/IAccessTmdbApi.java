package com.accenture.liquidstudio.tmdbAccess;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;

import com.omertron.themoviedbapi.MovieDbException;

import ai.api.model.Result;

public interface IAccessTmdbApi {

	SendMessage parseResultObject(Result response) throws MovieDbException;

	void callBackParser(EditMessageText new_message, String substring) throws MovieDbException;

}