package com.example.spotifywrapped;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class top10Tracks extends top10Items{

    private final String artistName;
    private final String albumName;
    public top10Tracks(String name, String artistName, String albumName, String secondImageUrl) {
        super(name, secondImageUrl);
        this.artistName = artistName;
        this.albumName = albumName;
    }
    public String getArtistName() {
        return artistName;
    }
    public String getAlbumName() {
        return albumName;
    }
    public static class TrackFetcher extends Fetcher<top10Tracks> {
        private final String timeRange;
        public TrackFetcher (String timeRange) {
            this.timeRange = timeRange;
        }
        @Override
        protected String getEndpoint() {
            return "https://api.spotify.com/v1/me/top/tracks?limit=10&time_range=" + timeRange;
        }
        protected List<top10Tracks> parseJson(JSONObject json) throws JSONException {
            List<top10Tracks> wantedData = new ArrayList<>();
            JSONArray items = json.getJSONArray("items");
            if (items.length() == 0) {
                wantedData.add(new top10Tracks("You have no tracks! Listen to some and come back later", null, null, null) {
                });
                return wantedData;
            }
            for (int i = 0; i < items.length(); i++) {
                JSONObject individual = items.getJSONObject(i);
                String name = individual.getString("name");
                String secondImageUrl = individual.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                String albumName = individual.getJSONObject("album").getString("name");
                String artistName = "";
                JSONArray artistInfo = individual.getJSONArray("artists");
                artistName = artistInfo.getJSONObject(0).getString("name");
                if (artistInfo.length() > 1) {
                    for (int j = 1; j < artistInfo.length(); j++) {
                        artistName += ", " + artistInfo.getJSONObject(j).getString("name");
                    }
                }
                wantedData.add(new top10Tracks(name, artistName, albumName, secondImageUrl));
            }
            return wantedData;
        }
    }
}
