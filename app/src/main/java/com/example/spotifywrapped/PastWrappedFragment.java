package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.getToken;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;

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

public class PastWrappedFragment extends Fragment {

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;

    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
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
    // counters
    private int loadCounter = 0;
    // booleans
    private static boolean isAccountDeleted = false;
    // song stuff
    private MediaPlayer mediaPlayer;
    private top10Tracks currentlyPlayingTrack;

    public PastWrappedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.past_wrapped, container, false);

        // Initialize views and variables
        Button linkSpotifyBtn = view.findViewById(R.id.refresh_btn);
        typeSpinner = view.findViewById(R.id.time_frame_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, getResources().getStringArray(R.array.time_frames));
        typeSpinner.setAdapter(adapter);
        recyclerView = view.findViewById(R.id.top_artists_recycler_view);
        //past_button = view.findViewById(R.id.past_button);

        // Set up RecyclerView, adapters, and lists
        artistListLong = new ArrayList<>();
        trackListLong = new ArrayList<>();
        artistListMedium = new ArrayList<>();
        trackListMedium = new ArrayList<>();
        artistAdapterMedium = new ArtistAdapter(artistListMedium);
        trackAdapterMedium = new TrackAdapter(trackListMedium);
        artistAdapterLong = new ArtistAdapter(artistListLong);
        trackAdapterLong = new TrackAdapter(trackListLong);
        // Automatically sets RV to artists
        initiateRecyclerView(recyclerView);

        /*
          Get data from FireBase and attempt to load profile. Will call GetUserProfile if
          data is not fully available in FireBase
          Also loads data for the other pages. Since this page pops up first, it will prime
          the others for usage
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
                                    playSongClip(clickedTrack.getPreviewUrl(), mediaPlayer);
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
                                    playSongClip(clickedTrack.getPreviewUrl(), mediaPlayer);
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
                            getUserProfile();
                        }
                    }
                });

        // Gets the token for the user and primes the spotifyAuthLauncher
        linkSpotifyBtn.setOnClickListener(v -> {
            // Call getToken() to link Spotify
            Log.d("TOKEN", "GET TOKEN");
            Intent intent = getToken(requireActivity());
            spotifyAuthLauncher.launch(intent);
        });

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
        initiateRecyclerView(recyclerView);
        loadData();
    }

    public void getUserProfile() {
        Log.d("GET USER", "GET USER PROFILE");

        if (mAccessToken == null) {
            Log.d("GET USER ERROR", "NO ACCESS TOKEN");
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
                    String spotifyUserId = jsonObject.getString("id");
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

                    top10Artists.ArtistFetcher mediumArtistFetcher = new top10Artists.ArtistFetcher("medium_term");
                    List<top10Artists> parsedMediumArtistData = mediumArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Artists.ArtistFetcher longArtistFetcher = new top10Artists.ArtistFetcher("long_term");
                    List<top10Artists> parsedLongArtistData = longArtistFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    // Set User Tracks data
                    top10Tracks.TrackFetcher shortTrackFetcher = new top10Tracks.TrackFetcher("short_term", mAccessToken, mOkHttpClient);
                    List<top10Tracks> parsedShortTracksData = shortTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Tracks.TrackFetcher mediumTrackFetcher = new top10Tracks.TrackFetcher("medium_term", mAccessToken, mOkHttpClient);
                    List<top10Tracks> parsedMediumTracksData = mediumTrackFetcher.fetchTop10Items(mAccessToken, mOkHttpClient);

                    top10Tracks.TrackFetcher longTrackFetcher = new top10Tracks.TrackFetcher("long_term", mAccessToken, mOkHttpClient);
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
                    assert currentUser != null;
                    String userID = currentUser.getUid(); // Will never be null (must have account to log in)

                    // Update firebase data with new user data
                    db.collection("users").document(userID)
                            .update(user)
                            .addOnFailureListener(e ->
                                    Log.w(TAG, "Error updating user data", e)
                            );
                    // Load new Firebase data
                    loadData();
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    public void loadData() {
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

                        if (artistListLong == null|| trackListLong == null || artistListMedium == null || trackListMedium == null) {
                            throw new java.lang.IllegalArgumentException("artistList or trackList is null");
                        }

                        artistAdapterMedium = new ArtistAdapter(artistListMedium);
                        artistAdapterLong = new ArtistAdapter(artistListLong);
                        trackAdapterMedium = new TrackAdapter(trackListMedium);
                        trackAdapterLong = new TrackAdapter(trackListLong);

                        typeSpinner.setVisibility(View.VISIBLE);

                        typeSpinner.setSelection(0);
                        recyclerView.setAdapter(artistAdapterMedium);
                    } catch (Exception e) {
                        System.out.println(loadCounter);
                        if (loadCounter < 10) {
                            getUserProfile();
                            loadCounter++;
                        } else {
                            Log.d("LOAD ERROR", "ATTEMPTED TO LOAD MORE THAN 10 TIMES");
                        }
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

    public void initiateRecyclerView(RecyclerView rv) {
        recyclerView = rv.findViewById(R.id.top_artists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    public List<top10Artists> setArtists(List<Map<String, Object>> ArtistList) {
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

    public List<top10Tracks> setTracks(List<Map<String, Object>> TrackList) {
        List<top10Tracks> newTrackList = new ArrayList<>();
        for (Map<String, Object> trackData : TrackList) {
            String name = (String) trackData.get("name");
            String artistName = (String) trackData.get("artistName");
            String albumName = (String) trackData.get("albumName");
            String imageUrl = (String) trackData.get("secondImageUrl");
            String previewUrl = (String) trackData.get("previewUrl");

            top10Tracks newTrack = new top10Tracks(name, artistName, albumName, imageUrl, previewUrl);
            newTrackList.add(newTrack);
        }
        return newTrackList;
    }

    private void playSongClip(String previewUrl, MediaPlayer mediaPlayer) {
        try {
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Preview not available", Toast.LENGTH_SHORT).show();
            Log.d("SONG", "SONG FAILED TO PLAY");
        }

        // Optionally, you may want to release the MediaPlayer after the clip finishes playing
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
            }
        });
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}