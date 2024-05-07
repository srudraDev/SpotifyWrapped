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

public class PastWrappedFragment extends Fragment {

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;

    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    // Firebase
    private FirebaseAuth firebaseAuth;
    // Views
    private RecyclerView recyclerView;
    private Spinner typeSpinner;
    // Adapters
    private ArtistAdapter artistAdapterMedium;
    private TrackAdapter trackAdapterMedium;
    private ArtistAdapter artistAdapterLong;
    private TrackAdapter trackAdapterLong;
    // User lists
    private List<top10Artists> artistListMedium;
    private List<top10Tracks> trackListMedium;
    private List<top10Artists> artistListLong;
    private List<top10Tracks> trackListLong;
    // song stuff
    private MediaPlayer mediaPlayer;
    private top10Tracks currentlyPlayingTrack;
    public PastWrappedFragment() {
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
        View view = inflater.inflate(R.layout.past_wrapped, container, false);

        // Initialize views and variables
        Button linkSpotifyBtn = view.findViewById(R.id.refresh_btn);
        typeSpinner = view.findViewById(R.id.time_frame_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item,
                getResources().getStringArray(R.array.time_frames));
        typeSpinner.setAdapter(adapter);
        Button settings = view.findViewById(R.id.settings_btn);
        settings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsPage.class)));
        shareButtonListener(view, getContext());

        // Set up RecyclerView, adapters, and lists
        recyclerView = view.findViewById(R.id.top_artists_recycler_view);
        artistListLong = new ArrayList<>();
        trackListLong = new ArrayList<>();
        artistListMedium = new ArrayList<>();
        trackListMedium = new ArrayList<>();
        artistAdapterMedium = new ArtistAdapter(artistListMedium);
        trackAdapterMedium = new TrackAdapter(trackListMedium);
        artistAdapterLong = new ArtistAdapter(artistListLong);
        trackAdapterLong = new TrackAdapter(trackListLong);
        // Automatically sets RV to artists
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        /*
          Get data from FireBase and attempt to load profile. Will call GetUserProfile if
          data is not fully available in FireBase
        */
        Log.d("LOAD", "CALLING LOAD DATA");
        loadData();

        // The DropDown menu for selecting Artists or Tracks
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection
                Log.d("RECYCLER", "ATTEMPTING FILL");
                switch (position) {
                    case 0:
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        // Load data for top 10 artists Medium
                        recyclerView.setAdapter(artistAdapterMedium);
                        break;
                    case 1:
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.start();
                        }
                        // Load data for top 10 tracks Medium
                        recyclerView.setAdapter(trackAdapterMedium);
                        trackAdapterMedium.setOnItemClickListener(positionV -> {
                            // Handle item click
                            top10Tracks clickedTrack = trackListMedium.get(positionV);

                            // Check if the clicked track has a preview URL
                            if (clickedTrack.getPreviewUrl() != null) {
                                Log.d("TEST", "Song: " + clickedTrack + " Url: " + clickedTrack.getPreviewUrl());
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
                    case 2:
                        if (mediaPlayer != null) {
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        // Load data for top 10 artists Long
                        recyclerView.setAdapter(artistAdapterLong);
                        break;
                    case 3:
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.start();
                        }
                        // Load data for top 10 tracks Long
                        recyclerView.setAdapter(trackAdapterLong);
                        trackAdapterLong.setOnItemClickListener(positionV -> {
                            // Handle item click
                            top10Tracks clickedTrack = trackListLong.get(positionV);

                            // Check if the clicked track has a preview URL
                            if (clickedTrack.getPreviewUrl() != null) {
                                Log.d("TEST", "Song: " + clickedTrack + " Url: " + clickedTrack.getPreviewUrl());
                                if (mediaPlayer.isPlaying() && clickedTrack.equals(currentlyPlayingTrack)) {
                                    // If a preview URL exists, play the song clip
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
            }
        });
        // Send the user to spotify login screen if they do not have an account (launched by linkSpotifyBtn)
        spotifyAuthLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Call the method from pullSpotifyDataToDatabase to retrieve the authorization response
                        AuthorizationResponse response = pullSpotifyDataToDatabase.
                                getSpotifyAuthResponse(result.getResultCode(), data);

                        // Check if the response is present
                        if (response != null) {
                            mAccessToken = response.getAccessToken();
                            getUserProfile();
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

    private void getUserProfile() {
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

                    Map<String, Object> user = new HashMap<>();
                    // Set User Artist data
                    top10Artists.ArtistFetcher mediumArtistFetcher = new top10Artists.ArtistFetcher("medium_term");
                    List<top10Artists> parsedMediumArtistData = mediumArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Artists.ArtistFetcher longArtistFetcher = new top10Artists.ArtistFetcher("long_term");
                    List<top10Artists> parsedLongArtistData = longArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Set User Tracks data
                    top10Tracks.TrackFetcher mediumTrackFetcher = new top10Tracks.TrackFetcher("medium_term", mAccessToken, mOkHttpClient);
                    List<top10Tracks> parsedMediumTracksData = mediumTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Tracks.TrackFetcher longTrackFetcher = new top10Tracks.TrackFetcher("long_term", mAccessToken, mOkHttpClient);
                    List<top10Tracks> parsedLongTracksData = longTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Put all the user data in a HashMap
                    user.put("displayName", displayName);
                    user.put("ArtistsMedium10", parsedMediumArtistData);
                    user.put("TracksMedium10", parsedMediumTracksData);
                    user.put("ArtistsLong10", parsedLongArtistData);
                    user.put("TracksLong10", parsedLongTracksData);
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
                        List<Map<String, Object>> ArtistsMedium10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsMedium10");
                        List<Map<String, Object>> TracksMedium10 = (List<Map<String, Object>>) documentSnapshot.get("TracksMedium10");
                        List<Map<String, Object>> ArtistsLong10 = (List<Map<String, Object>>) documentSnapshot.get("ArtistsLong10");
                        List<Map<String, Object>> TracksLong10 = (List<Map<String, Object>>) documentSnapshot.get("TracksLong10");

                        // Set each item in both lists from firebase
                        artistListMedium = setArtists(ArtistsMedium10);
                        trackListMedium = setTracks(TracksMedium10);
                        artistListLong = setArtists(ArtistsLong10);
                        trackListLong = setTracks(TracksLong10);

                        if (artistListLong == null || trackListLong == null || artistListMedium == null || trackListMedium == null) {
                            throw new java.lang.IllegalArgumentException("artistList or trackList is null");
                        }

                        artistAdapterMedium = new ArtistAdapter(artistListMedium);
                        artistAdapterLong = new ArtistAdapter(artistListLong);
                        trackAdapterMedium = new TrackAdapter(trackListMedium);
                        trackAdapterLong = new TrackAdapter(trackListLong);

                        typeSpinner.setSelection(0);
                        recyclerView.setAdapter(artistAdapterMedium);
                    } catch (Exception e) {
                        Log.d("loadData", "Document data retrieval error");
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