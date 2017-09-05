package com.accenture.liquidstudio.tmdbAccess;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import com.omertron.themoviedbapi.model.movie.MovieBasic;

public interface HasMarkupProperty 
{
	public void setResultMarkup(InlineKeyboardMarkup markup, int c, String parameters, MovieBasic mi,
			boolean nextBackButton, String viewIdentifier);
}
