package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import static com.example.spotifywrapped.pullSpotifyDataToDatabase.*;
import static com.example.spotifywrapped.top10Artists.fetchTop10Artist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.graphics.Color;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static String mAccessToken, mAccessCode;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArtistAdapter artistAdapter;
    private List<top10Artists> artistList;
    private boolean isProfileBtnClicked = false;
    private static boolean isAccountDeleted = false;
    private Button linkSpotifyBtn;
    private TextView mainPageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkSpotifyBtn = findViewById(R.id.link_spotify_btn);
        mainPageName = findViewById(R.id.mainPageName);
        // Initialize FireStore
        db = FirebaseFirestore.getInstance();

        // RecyclerView
        artistList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(artistList);
        initiateRecyclerView(recyclerView);


        // Set the click listeners for the buttons
        hide(profileBtn);

        linkSpotifyBtn.setOnClickListener(v -> {
            // Call getToken() to link Spotify
            Log.d("Token", "Getting token");
            getToken(MainActivity.this);
            Log.d("Token Done", "Got Token");
            // Disable linkSpotifyButton
            hide(linkSpotifyBtn);
            mainPageName.setVisibility(View.VISIBLE);
            isProfileBtnClicked = true;
            Log.d("Link Spotify Successful", "Linked to Spotify Account Successfully");
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MYLOGGG", "within the resume");
        //check if user has been deleted
        if (isAccountDeleted) {
            Log.d("MYLOGGG", "successfully hit true if statement");

            isAccountDeleted = false;
            finish();

        }

        // Check if profileBtn is clicked
        if (isProfileBtnClicked) {
            // Hide profileBtn and linkSpotifyBtn
            linkSpotifyBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Call the method from pullSpotifyDataToDatabase to retrieve the authorization response
        AuthorizationResponse response = pullSpotifyDataToDatabase.getSpotifyAuthResponse(resultCode, data);

        // Check which request code is present (if any)
        if (response != null) {
            if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
                mAccessToken = response.getAccessToken();
                getUserProfile();
            }
        }
    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void getUserProfile() {

        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Looper.prepare();
                } catch (Exception e) {
                    Log.d("Looper", "Looper already prepared: " + e);
                }
                try {
                    String responseBody = response.body().string();
                    //Log.d("ResponseBodyUser", responseBody);

                    final JSONObject jsonObject = new JSONObject(responseBody);
                    String displayName = jsonObject.getString("display_name");
                    String spotifyUserId = jsonObject.getString("id");
                    String userProfileImageURL;
                    try {
                        JSONArray userProfileImageArray = jsonObject.getJSONArray("images");
                        userProfileImageURL = userProfileImageArray.getJSONObject(0).getString("url");
                    } catch (Exception e) {
                        userProfileImageURL = "@drawable/ic_default_profile_image"; //a default user profile image
                        Log.d("ProfileImageError", "No Profile Image Found");
                    }

                    List<top10Artists> parsedData = fetchTop10Artist(mAccessToken,mOkHttpClient);
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", displayName);
                    user.put("spotifyId", spotifyUserId);
                    user.put("Artists10", parsedData);
                    user.put("profilePic", userProfileImageURL);
                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    String userID = currentUser.getUid();

                    db.collection("users").document(userID)
                            .update(user)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + userID);
                                // Fill RecyclerView with data
                                fillRecyclerView(parsedData);
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Fill RecyclerView with data
     * @param artistData List of top 10 artists
     */
    private void fillRecyclerView(List<top10Artists> artistData) {
        // Clear existing data
        artistList.clear();
        // Add new data
        artistList.addAll(artistData);
        // Notify adapter about data change
        artistAdapter.notifyDataSetChanged();
    }

    public void hide(Button button) {
        button.setEnabled(false);
        button.setVisibility(View.INVISIBLE);
        button.setClickable(false);
        button.setFocusable(false);
    }

    public void initiateRecyclerView(RecyclerView rv) {
        recyclerView = findViewById(R.id.recycler_view_top_artists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(artistAdapter);
    }

    public void settings_btn_click(View view) {
        startActivity(new Intent(this, SettingsPage.class));

        //finish();
    }
    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public static void setAccountDeleted(boolean input) {
        isAccountDeleted = input;
    }
}