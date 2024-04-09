package com.example.spotifywrapped;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class top10Artists extends top10Items{
    private final List<String> genres;

    public top10Artists(String name, List<String> genres, String secondImageUrl) {
        super(name, secondImageUrl);
        this.genres = genres;
    }
    public List<String> getGenres() {
        return genres;
    }
    public static class ArtistFetcher extends Fetcher<top10Artists> {
        private String time;
        public ArtistFetcher(String time) {
            this.time = time;
        }
        public ArtistFetcher() {
            this("medium_term");
        }
        @Override
        protected String getEndpoint() {
            return "https://api.spotify.com/v1/me/top/artists?time_range=" + this.time + "&limit=10";
        }

        @Override
        protected List<top10Artists> parseJson(JSONObject json) throws JSONException {
            List<top10Artists> wantedData = new ArrayList<>();
            JSONArray artists = json.getJSONArray("items");

            if (artists.length() == 0) {
                wantedData.add(new top10Artists("You have no artists! Listen to some and come back later", null, null));
                return wantedData;
            }

            for (int i = 0; i < artists.length(); i++) {
                JSONObject artist = artists.getJSONObject(i);
                String name = artist.getString("name");
                JSONArray genresArray = artist.getJSONArray("genres");
                List<String> genresList = new ArrayList<>();
                for (int j = 0; j < genresArray.length(); j++) {
                    String genre = genresArray.getString(j);
                    genresList.add(genre);
                }
                String secondImageUrl = artist.getJSONArray("images").getJSONObject(1).getString("url");
                wantedData.add(new top10Artists(name, genresList, secondImageUrl));
            }
            Log.d("parseJSON", wantedData.get(0).name);
            return wantedData;
        }
    }
}
