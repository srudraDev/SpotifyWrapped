package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.UtilitySpotifyFeatureMethods.*;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.db;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import okhttp3.Response;

public class WrappedFragment extends Fragment {

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;
    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    // Firebase
    private FirebaseAuth firebaseAuth;
    // Views
    private RecyclerView recyclerView;
    private Spinner mainPageName;
    // Adapters
    private ArtistAdapter artistAdapter;
    private TrackAdapter trackAdapter;
    // User lists
    private List<top10Artists> artistList;
    private List<top10Tracks> trackList;
    // song stuff
    private MediaPlayer mediaPlayer;
    private top10Tracks currentlyPlayingTrack;

    public WrappedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wrapped, container, false);

        // Initialize Refresh Button
        Button linkSpotifyBtn = view.findViewById(R.id.refresh_btn);

        // Initialize Settings Button
        Button settings = view.findViewById(R.id.settings_btn);
        settings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsPage.class)));
        shareButtonListener(view, getContext());

        // Initialize Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, getResources().getStringArray(R.array.typeOfWrapped));
        mainPageName = view.findViewById(R.id.typeOfWrapped);
        mainPageName.setAdapter(adapter);

        // Set up RecyclerView, adapters, and lists
        recyclerView = view.findViewById(R.id.recycler_view_top_artists);
        artistList = new ArrayList<>();
        trackList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(artistList);
        trackAdapter = new TrackAdapter(trackList);
        // Automatically sets RV to artists
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        /*
          Get data from FireBase and attempt to load profile. Will call GetUserProfile if
          data is not fully available in FireBase
        */
        loadData();

        // The DropDown menu for selecting Artists or Tracks
        mainPageName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection
                switch (position) {
                    case 0:
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        // Load data for top 10 artists
                        recyclerView.setAdapter(artistAdapter);
                        break;
                    case 1:
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.start();
                        }
                        // Load data for top 10 tracks
                        recyclerView.setAdapter(trackAdapter);

                        trackAdapter.setOnItemClickListener(positionV -> {
                            // Handle item click
                            top10Tracks clickedTrack = trackList.get(positionV);
                            Log.d("SONG", "SONG: " + clickedTrack);

                            // Check if the clicked track has a preview URL
                            if (clickedTrack != null && clickedTrack.getPreviewUrl() != null) {
                                if (mediaPlayer.isPlaying() && clickedTrack.equals(currentlyPlayingTrack)) {
                                    mediaPlayer.pause();
                                } else {
                                    mediaPlayer.reset();
                                    playSongClip(clickedTrack.getPreviewUrl(), mediaPlayer, requireContext());
                                    currentlyPlayingTrack = clickedTrack;
                                }
                            } else {
                                // Handle case where preview URL is not available
                                Toast.makeText(requireContext(), "Preview not available", Toast.LENGTH_SHORT).show();
                                assert clickedTrack != null;
                                Log.d("SONG", "URL: " + clickedTrack.getPreviewUrl());
                            }
                        });

                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        // Send the user to spotify login screen if they do not have an account (launched by linkSpotifyBtn)
        spotifyAuthLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Call the method from pullSpotifyDataToDatabase to retrieve the authorization response
                        AuthorizationResponse response = pullSpotifyDataToDatabase.getSpotifyAuthResponse(result.getResultCode(), data);

                        // Check if the response is present
                        if (response != null) {
                            mAccessToken = response.getAccessToken();
                            refreshUserProfile();
                        }
                    }
        });

        // Gets the token for the user and primes the spotifyAuthLauncher
        refreshLinkSpotify(spotifyAuthLauncher, linkSpotifyBtn, requireActivity());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void refreshUserProfile() {
        requestUserProfile(mAccessToken, mOkHttpClient);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    Looper.prepare();
                } catch (Exception e) {
                    Log.d("LOOPER", "LOOPER PREPARED");
                }
                try {
                    assert response.body() != null;
                    String responseBody = response.body().string();

                    final JSONObject jsonObject = new JSONObject(responseBody);

                    // Set User data
                    String displayName = jsonObject.getString("display_name");
                    String userProfileImageURL;
                    try {
                        JSONArray userProfileImageArray = jsonObject.getJSONArray("images");
                        userProfileImageURL = userProfileImageArray.getJSONObject(0).getString("url");
                    } catch (Exception e) {
                        userProfileImageURL = "@drawable/ic_default_profile_image"; //a default user profile image
                        Log.d("PROFILE IMAGE", "NO PROFILE IMAGE FOUND");
                    }

                    // Set User Artist data
                    top10Artists.ArtistFetcher shortArtistFetcher = new top10Artists.ArtistFetcher("short_term");
                    List<top10Artists> parsedShortArtistData = shortArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Set User Tracks data
                    top10Tracks.TrackFetcher shortTrackFetcher = new top10Tracks.TrackFetcher("short_term", mAccessToken, mOkHttpClient);
                    List<top10Tracks> parsedShortTracksData = shortTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Put all the user data in a HashMap
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", displayName);
                    user.put("Artists10", parsedShortArtistData);
                    user.put("Tracks10", parsedShortTracksData);
                    user.put("profilePic", userProfileImageURL);

                    getAndUpdateFromFirebase(db, user);
                    // Load new Firebase data
                    loadData();
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    private void loadData() {
        try {
            // Get Current User id from FireBase
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            assert currentUser != null;
            String userID = currentUser.getUid(); // Will never be null (must have account to log in)

            String documentPath = "users/" + userID;
            DocumentReference docRef = db.document(documentPath);

            // FILL DATA FROM FIREBASE
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d("TEST", "DOCREF SUCCESSFUL");
                    // Document exists, extract the data
                    // Assuming the structure of your document is similar to how you parsed the Spotify API response
                    try {
                        // Get artist and track arrays from firebase
                        List<Map<String, Object>> Artists10 = (List<Map<String, Object>>) documentSnapshot.get("Artists10");
                        List<Map<String, Object>> Tracks10 = (List<Map<String, Object>>) documentSnapshot.get("Tracks10");

                        // Set each item in both lists from firebase
                        artistList = setArtists(Artists10);
                        trackList = setTracks(Tracks10);

                        if (artistList == null|| trackList == null) {
                            throw new java.lang.IllegalArgumentException("artistList or trackList is null");
                        }

                        artistAdapter = new ArtistAdapter(artistList);
                        trackAdapter = new TrackAdapter(trackList);

                        mainPageName.setSelection(0);
                        recyclerView.setAdapter(artistAdapter);
                    } catch (Exception e) {
                        Log.d("loadData","Document data retrieval error");
                    }
                } else {
                    // Document does not exist
                    Log.d(TAG, "DOCREF UNSUCCESSFUL");
                }
            }).addOnFailureListener(e -> {
                // Error getting document
                Log.w(TAG, "Error getting document", e);
            });
        } catch (Exception e) {
            Log.d("LOAD", "MAJOR LOAD ERROR: " + e);
        }
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}