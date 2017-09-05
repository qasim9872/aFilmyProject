package com.accenture.liquidstudio.tmdbAccess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.movie.MovieBasic;
import com.omertron.themoviedbapi.model.movie.MovieInfo;

public class GenreHandler {
	private HashMap<Integer, String> genreMap;

	public GenreHandler() {
		genreMap = new HashMap<Integer, String>();
		loadGenreData();
	}

	private void loadGenreData() {
		String s = Utilities.readFileData("CSV\\genre.txt");
		// System.out.println(s);

		String[] tokens = s.split("\n");
		// System.out.println(Arrays.toString(tokens));

		for (int i = 0; i < tokens.length; i++) {
			String[] seperator = tokens[i].split(",");
			genreMap.put(Integer.parseInt(seperator[0]), seperator[1]);
		}

	}

	// Since tmdb api only supports a limited number of genres, to reduce the api
	// calls I placed them in a file and load them at the start
	public String getGenreNames(MovieBasic mi) {

		String s = "";
		List<Integer> genreIds = mi.getGenreIds();

		if (genreIds == null) {
			
			if(mi instanceof MovieInfo)
			{
				for(Genre genre : ((MovieInfo)mi).getGenres())
				{
					s+= genre.getName() + " | ";
				}
			}

		} else {

			for (int id : genreIds) {
				s += genreMap.get(id) + " | ";
			}
		}
		return s;
	}

	public Set<Integer> getGenreIds(List<String> genre) {
		Set<Integer> genreId = new HashSet<>();

		for (Entry<Integer, String> e : genreMap.entrySet()) {
			for (String s : genre) {
				if (e.getValue().equalsIgnoreCase(s)) {
					genreId.add(e.getKey());
					// System.out.println(s);
				}
			}
		}

		// System.out.println(genreId.toString());
		return genreId;
	}

}
