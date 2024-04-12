package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

public class DuoWrappedActivity extends AppCompatActivity {
    // Spotify
    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    // Recycler View
    private List<top10Artists> artistList;
    private List<top10Tracks> trackList;
    // Adapters
    private ArtistAdapter artistAdapter;
    private TrackAdapter trackAdapter;
    // Views
    private RecyclerView recyclerView;
    private FireModel fireModel;
    private Button present_button;
    private Button duo_button;
    private Button past_button;
    private Button public_button;
    private Button settings_button;
    private Spinner spinner;
    // booleans
    private static boolean isAccountDeleted = false;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MYLOG", "in duowrapped on create");
        super.onCreate(savedInstanceState);

        Log.d("MYLOG", "past super.OnCreate");


        setContentView(R.layout.duo_wrapped);
        Log.d("MYLOG", "set the content view");

        // Initialize FireStore
        db = FirebaseFirestore.getInstance();
        Log.d("MYLOG", "db made");

        // RecyclerView
        artistList = new ArrayList<>();
        Log.d("MYLOG", "artist list made");

        trackList = new ArrayList<>();
        Log.d("MYLOG", "track list made");

        artistAdapter = new ArtistAdapter(artistList);
        Log.d("MYLOG", "artist adapter made");

        initiateRecyclerView(recyclerView);
        Log.d("MYLOG", "initiated recycler view");

        // Initialize fireModel
        fireModel = new ViewModelProvider(this).get(FireModel.class);
        Log.d("MYLOG", "fire model made");

        getUserProfile();
        Log.d("MYLOG", "got user profile");

        spinner = findViewById(R.id.time_frame_spinner);
        Log.d("MYLOG", "spinner made");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection
                switch (position) {
                    case 0:
                        // Load data for top 10 artists
                        loadFireModel();
                        recyclerView.setAdapter(artistAdapter);
                        fillRecyclerViewArtists(artistList);
                        break;
                    case 1:
                        // Load data for top 10 tracks
                        loadFireModel();
                        trackAdapter = new TrackAdapter(trackList);
                        recyclerView.setAdapter(trackAdapter);
                        fillRecyclerViewTracks(trackList);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });


        // Wrapped pages to be implemented
        // Create the different pages for the different wrapped
        present_button = findViewById(R.id.present_button);
        present_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        duo_button = findViewById(R.id.duo_button);
        duo_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, DuoWrappedActivity.class);
            startActivity(intent);
        });

        past_button = findViewById(R.id.past_button);
        past_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, PastWrappedActivity.class);
            startActivity(intent);
        });

        public_button = findViewById(R.id.public_button);
        public_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, PublicWrappedActivity.class);
            startActivity(intent);
        });

        settings_button = findViewById(R.id.settings_button);
        settings_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsPage.class);
            startActivity(intent);
        });
        Log.d("MYLOG", "past the buttons being made");
    }

    @Override
    protected void onResume() {
        Log.d("MYLOG", "before the super onResume");

        super.onResume();
        Log.d("MYLOG", "past the super on resume");

        // Check if user has been deleted
        if (isAccountDeleted) {
            Log.d("ACCOUNT", "Account read as DELETED");

            isAccountDeleted = false;
            finish();
        }
        Log.d("MYLOG", "past the account being deleted");

        // Check if data is already loaded in the FireModel
        fireModel = new ViewModelProvider(this).get(FireModel.class);
        if (!fireModel.getNeedReload()) {
            // Data exists
            fillRecyclerViewArtists(fireModel.getArtists10List());
        }
    }

    public void getUserProfile() {
        fireModel.setNeedReload(false);
        Log.d("MYLOG", "set fire model reload");


        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MYLOG", "m access token null checked");

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        Log.d("MYLOG", "made request");

        cancelCall();
        Log.d("MYLOG", "cancel call");

        mCall = mOkHttpClient.newCall(request);
        Log.d("MYLOG", "mCall made, newCall(request)");


        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(DuoWrappedActivity.this, "Failed to fetch data, watch Logcat for more details",
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
                    Toast.makeText(DuoWrappedActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                try {
                    //fireModel.setUserName(documentSnapshot.getString("userName"));
                    //fireModel.setUserId(documentSnapshot.getString("userId"));
                    //fireModel.setUserImage(documentSnapshot.getString("userImage"));

                    // Assuming you have stored the parsed artist data as an array in Firestore
                    List<Map<String, Object>> ArtistsLong10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsLong10");
                    List<Map<String, Object>> ArtistsMedium10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsMedium10");
                    List<Map<String, Object>> TracksLong10 = (List<Map<String, Object>>) documentSnapshot.get("TracksLong10");
                    List<Map<String, Object>> TracksMedium10 = (List<Map<String, Object>>) documentSnapshot.get("TracksMedium10");

                    // Set each thing in fireModel to what's in firebase
                    fireModel.setArtistsMedium10List(setFireModelArtists(ArtistsMedium10));
                    fireModel.setArtistsLong10List(setFireModelArtists(ArtistsLong10));

                    fireModel.setTracksMedium10List(setFireModelTracks(TracksMedium10));
                    fireModel.setTracksLong10List(setFireModelTracks(TracksLong10));

                    fillRecyclerViewArtists(fireModel.getArtists10List());
                } catch (Exception e) {
                    getUserProfile();
                }
            } else {
                // Document does not exist
                Log.d(TAG, "DOCREF UNSUCCESSFUL");
            }
        }).addOnFailureListener(e -> {
            // Error getting document
            Log.w(TAG, "Error getting document", e);
        });
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

    public void initiateRecyclerView(RecyclerView rv) {
        recyclerView = findViewById(R.id.recycler_view_top_artists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(artistAdapter);
    }

}
