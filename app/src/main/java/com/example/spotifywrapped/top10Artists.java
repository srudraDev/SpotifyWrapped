package com.example.spotifywrapped;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class top10Artists {
    private String name;
    private List<String> genres;
    private String secondImageUrl;

    public top10Artists(String name, List<String> genres, String secondImageUrl) {
        this.name = name;
        this.genres = genres;
        this.secondImageUrl = secondImageUrl;
    }

    public String getName() {
        return name;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getSecondImageUrl() {
        return secondImageUrl;
    }
    public static List<top10Artists> fetchTop10Artist(String mAccessToken, OkHttpClient mOkHttpClient) throws IOException {
        List<top10Artists> parsedData = new ArrayList<>();
        final Request top10Artists = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?limit=10")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        try (Response response = mOkHttpClient.newCall(top10Artists).execute()) {
           final JSONObject top10ArtistsJSON = new JSONObject(response.body().string());
            parsedData = parseJson(top10ArtistsJSON);
        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data: " + e);
        }
        return parsedData;
    }

    // Parse JSON and extract required information
    public static List<top10Artists> parseJson(JSONObject json) throws JSONException{
        List<top10Artists> wantedData = new ArrayList<>();
        JSONArray artists = json.getJSONArray("items");

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
        return wantedData;
    }
}
