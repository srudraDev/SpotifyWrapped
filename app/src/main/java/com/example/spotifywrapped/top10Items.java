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

public abstract class top10Items {
    protected final String name;
    protected final String secondImageUrl;

    public top10Items(String name, String secondImageUrl) {
        this.name = name;
        this.secondImageUrl = secondImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getSecondImageUrl() {
        return secondImageUrl;
    }

    public abstract static class Fetcher<T extends top10Items> {
        protected abstract String getEndpoint();

        public List<T> fetchTop10Items(String mAccessToken, OkHttpClient mOkHttpClient) throws IOException {
            List<T> parsedData = new ArrayList<>();
            final Request request = new Request.Builder()
                    .url(getEndpoint())
                    .addHeader("Authorization", "Bearer " + mAccessToken)
                    .build();
            try (Response response = mOkHttpClient.newCall(request).execute()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                final JSONObject json = new JSONObject(responseBody);
                parsedData = parseJson(json);
            } catch (JSONException e) {
                Log.d("JSON", "Failed to parse data: " + e);
            }
            return parsedData;
        }
        protected abstract List<T> parseJson(JSONObject json) throws JSONException;
    }
}

