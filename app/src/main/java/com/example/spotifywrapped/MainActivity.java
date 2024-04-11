package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import static com.example.spotifywrapped.pullSpotifyDataToDatabase.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    // Spotify
    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    // Views
    private RecyclerView recyclerView;
    private Button linkSpotifyBtn;
    private Spinner mainPageName;
    private FireModel fireModel;
    // Adapters
    private ArtistAdapter artistAdapter;
    private TrackAdapter trackAdapter;
    // User lists
    private List<top10Artists> artistList;
    private List<top10Tracks> trackList;
    // Boolean helpers
    private static boolean isAccountDeleted = false;
    private Button past_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkSpotifyBtn = findViewById(R.id.link_spotify_btn);
        mainPageName = findViewById(R.id.typeOfWrapped);

        // Initialize FireStore
        db = FirebaseFirestore.getInstance();

        // RecyclerView
        artistList = new ArrayList<>();
        trackList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(artistList);
        initiateRecyclerView(recyclerView);

        // Initialize FireView
        fireModel = new ViewModelProvider(this).get(FireModel.class);

        // if user data can be found in firebase:
        if (fireModel.getNeedReload()) {
            getUserProfile();
        } else {
            try {
                loadFireModel();
                linkSpotifyBtn.setVisibility(View.INVISIBLE);
                mainPageName.setVisibility(View.VISIBLE);
            } catch (Exception e) { // if user data cannot be found in firebase:
                // Let the user know they need to connect their spotify account
                linkSpotifyBtn.setVisibility(View.VISIBLE);
                mainPageName.setVisibility(View.INVISIBLE);
            }
        }
        // Set the click listeners for the buttons

        past_button = findViewById(R.id.past_button);
        past_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, PastWrappedActivity.class);
            startActivity(intent);
        });

        linkSpotifyBtn.setOnClickListener(v -> {
            // Call getToken() to link Spotify
            getToken(MainActivity.this);
        });
        mainPageName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection
                switch (position) {
                    case 0:
                        // Load data for top 10 artists
                        recyclerView.setAdapter(artistAdapter);
                        fillRecyclerViewArtists(fireModel.getArtists10List());
                        break;
                    case 1:
                        // Load data for top 10 tracks
                        trackAdapter = new TrackAdapter(trackList);
                        recyclerView.setAdapter(trackAdapter);
                        fillRecyclerViewTracks(fireModel.getTracks10List());
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Check if user has been deleted
        if (isAccountDeleted) {
            Log.d("ACCOUNT", "Account read as DELETED");

            isAccountDeleted = false;
            finish();
        }
        // Check if data is already loaded in the FireModel
        fireModel = new ViewModelProvider(this).get(FireModel.class);
        if (!fireModel.getNeedReload()) {
            // Data exists, hide link Spotify button
            linkSpotifyBtn.setVisibility(View.INVISIBLE);
            mainPageName.setVisibility(View.VISIBLE);
            fillRecyclerViewArtists(fireModel.getArtists10List());
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
        fireModel.setNeedReload(false);

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

                    // Set User data
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

                    // Set User Artist data
                    top10Artists.ArtistFetcher shortArtistFetcher = new top10Artists.ArtistFetcher("short_term");
                    List<top10Artists> parsedShortArtistData = shortArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Artists.ArtistFetcher mediumArtistFetcher = new top10Artists.ArtistFetcher("medium_term");
                    List<top10Artists> parsedMediumArtistData = mediumArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Artists.ArtistFetcher longArtistFetcher = new top10Artists.ArtistFetcher("long_term");
                    List<top10Artists> parsedLongArtistData = longArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Set User Tracks data
                    top10Tracks.TrackFetcher shortTrackFetcher = new top10Tracks.TrackFetcher("short_term");
                    List<top10Tracks> parsedShortTracksData = shortTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Tracks.TrackFetcher mediumTrackFetcher = new top10Tracks.TrackFetcher("medium_term");
                    List<top10Tracks> parsedMediumTracksData = mediumTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Tracks.TrackFetcher longTrackFetcher = new top10Tracks.TrackFetcher("long_term");
                    List<top10Tracks> parsedLongTracksData = longTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Put all the user data in a HashMap
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", displayName);
                    user.put("spotifyId", spotifyUserId);
                    user.put("Artists10", parsedShortArtistData);
                    user.put("Tracks10", parsedShortTracksData);
                    user.put("ArtistsMedium10", parsedMediumArtistData);
                    user.put("TracksMedium10", parsedMediumTracksData);
                    user.put("ArtistsLong10", parsedLongArtistData);
                    user.put("TracksLong10", parsedLongTracksData);
                    user.put("profilePic", userProfileImageURL);

                    // Get Current User id from FireBase
                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    String userID = currentUser.getUid(); // Will never be null (must have account to log in)

                    // Update firebase data with new user data
                    db.collection("users").document(userID)
                            .update(user)
                            .addOnFailureListener(e ->
                                    Log.w(TAG, "Error updating user data", e)
                            );

                    // Load new Firebase data into FireModel
                    loadFireModel();
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
    private void fillRecyclerViewArtists(List<top10Artists> artistData) {
        // Clear existing data
        artistList.clear();

        // Add new data
        artistList.addAll(artistData);

        // Notify adapter about data change
        artistAdapter.notifyDataSetChanged();
    }

    private void fillRecyclerViewTracks(List<top10Tracks> trackData) {
        // Clear existing data
        trackList.clear();

        // Add new data
        trackList.addAll(trackData);

        // Notify adapter about data change
        trackAdapter.notifyDataSetChanged();
    }

    public List<top10Artists> setFireModelArtists(List<Map<String, Object>> ArtistList) {
        List<top10Artists> newArtistList = new ArrayList<>();
        for (Map<String, Object> artistData : ArtistList) {
            String artistName = (String) artistData.get("name");
            List<String> genres = (List<String>) artistData.get("genres");
            String imageUrl = (String) artistData.get("secondImageUrl");

            top10Artists newArtist = new top10Artists(artistName, genres, imageUrl);
            newArtistList.add(newArtist);
        }
        return newArtistList;
    }

    public List<top10Tracks> setFireModelTracks(List<Map<String, Object>> TrackList) {
        List<top10Tracks> newTrackList = new ArrayList<>();
        for (Map<String, Object> trackData : TrackList) {
            String name = (String) trackData.get("name");
            String artistName = (String) trackData.get("artistName");
            String albumName = (String) trackData.get("albumName");
            String imageUrl = (String) trackData.get("secondImageUrl");

            top10Tracks newTrack = new top10Tracks(name, artistName, albumName, imageUrl);
            newTrackList.add(newTrack);
        }
        return newTrackList;
    }

    public void loadFireModel() {
        // Get Current User id from FireBase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userID = currentUser.getUid(); // Will never be null (must have account to log in)

        String documentPath = "users/" + userID;
        DocumentReference docRef = db.document(documentPath);

        // FILL FIREMODEL FROM FIREBASE
        Log.d("TEST", "ATTEMPTING DOCREF");
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("TEST", "DOCREF SUCCESSFUL");
                // Document exists, extract the data
                // Assuming the structure of your document is similar to how you parsed the Spotify API response
                fireModel.setUserName(documentSnapshot.getString("userName"));
                fireModel.setUserId(documentSnapshot.getString("userId"));
                fireModel.setUserImage(documentSnapshot.getString("userImage"));

                // Assuming you have stored the parsed artist data as an array in Firestore
                List<Map<String, Object>> Artists10 = (List<Map<String, Object>>) documentSnapshot.get("Artists10");
                List<Map<String, Object>> ArtistsLong10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsLong10");
                List<Map<String, Object>> ArtistsMedium10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsMedium10");
                List<Map<String, Object>> Tracks10 = (List<Map<String, Object>>) documentSnapshot.get("Tracks10");
                List<Map<String, Object>> TracksLong10 = (List<Map<String, Object>>) documentSnapshot.get("TracksLong10");
                List<Map<String, Object>> TracksMedium10 = (List<Map<String, Object>>) documentSnapshot.get("TracksMedium10");

                // Set each thing in fireModel to what's in firebase
                fireModel.setArtists10List(setFireModelArtists(Artists10));
                fireModel.setArtistsMedium10List(setFireModelArtists(ArtistsMedium10));
                fireModel.setArtistsLong10List(setFireModelArtists(ArtistsLong10));

                fireModel.setTracks10List(setFireModelTracks(Tracks10));
                fireModel.setTracksMedium10List(setFireModelTracks(TracksMedium10));
                fireModel.setTracksLong10List(setFireModelTracks(TracksLong10));

                fillRecyclerViewArtists(fireModel.getArtists10List());
            } else {
                // Document does not exist
                Log.d(TAG, "DOCREF UNSUCCESSFUL");
            }
        }).addOnFailureListener(e -> {
            // Error getting document
            Log.w(TAG, "Error getting document", e);
        });
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