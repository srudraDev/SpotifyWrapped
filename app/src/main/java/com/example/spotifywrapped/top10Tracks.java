package com.example.spotifywrapped;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class top10Tracks extends top10Items {

    private final String artistName;
    private final String albumName;
    private String previewUrl;
    public top10Tracks(String name, String artistName, String albumName, String secondImageUrl, String previewUrl) {
        super(name, secondImageUrl);
        this.artistName = artistName;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
    }
    public String getArtistName() {
        return artistName;
    }
    public String getAlbumName() {
        return albumName;
    }
    public String getPreviewUrl() {
        return previewUrl;
    }
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
    public static class TrackFetcher extends Fetcher<top10Tracks> {
        private final OkHttpClient mOkHttpClient;
        private final String mAccessToken;
        public TrackFetcher(String timeRange, String accessToken, OkHttpClient okHttpClient) {
            super(timeRange);
            this.mAccessToken = accessToken;
            this.mOkHttpClient = okHttpClient;
        }
        @Override
        protected String getEndpoint() {
            return "https://api.spotify.com/v1/me/top/tracks?limit=10&time_range=" + timeRange;
        }
        protected List<top10Tracks> parseJson(JSONObject json) throws JSONException {
            List<top10Tracks> wantedData = new ArrayList<>();
            JSONArray items = json.getJSONArray("items");
            if (items.length() == 0) {
                wantedData.add(new top10Tracks("You have no tracks! Listen to some and come back later",
                        null, null, null, null) {
                });
                return wantedData;
            }
            for (int i = 0; i < items.length(); i++) {
                JSONObject individual = items.getJSONObject(i);
                String name = individual.getString("name");
                String secondImageUrl = individual.getJSONObject("album").getJSONArray("images").
                        getJSONObject(1).getString("url");
                String albumName = individual.getJSONObject("album").getString("name");
                String artistName;
                JSONArray artistInfo = individual.getJSONArray("artists");
                artistName = artistInfo.getJSONObject(0).getString("name");
                if (artistInfo.length() > 1) {
                    for (int j = 1; j < artistInfo.length(); j++) {
                        artistName += ", " + artistInfo.getJSONObject(j).getString("name");
                    }
                }
                top10Tracks track = new top10Tracks(name, artistName, albumName, secondImageUrl, null);

                // Call getTrackPreviewUrl to fetch the track preview URL
                String previewUrl = getTrackPreviewUrl(track.name, track.artistName);

                // Set the preview URL for the track
                track.setPreviewUrl(previewUrl);
                Log.d("SONG", track.name + track.getPreviewUrl());
                wantedData.add(new top10Tracks(name, artistName, albumName, secondImageUrl, previewUrl));
            }
            return wantedData;
        }
        private String getTrackPreviewUrl(String trackName, String artistName) {
            try {
                String encodedTrackName = URLEncoder.encode(trackName, "UTF-8");
                String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
                String query = String.format("track:%s artist:%s", encodedTrackName, encodedArtistName);
                String apiUrl = "https://api.spotify.com/v1/search?type=track&q=" + query;

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .addHeader("Authorization", "Bearer " + mAccessToken)
                        .build();

                Response response = mOkHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.d("SONG", "SONG GRAB FAILED FOR" + trackName);
                    // Handle unsuccessful response
                    return null;
                }

                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray tracks = jsonResponse.getJSONObject("tracks").getJSONArray("items");
                if (tracks.length() > 0) {
                    // Assuming we just take the first track's preview URL
                    return tracks.getJSONObject(0).getString("preview_url");
                }
            } catch (IOException | JSONException e) {
                Log.d("SONG", "FATAL ERROR: " + trackName);
                e.printStackTrace();
            }
            return null;
        }
    }
}
