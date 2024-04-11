package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.getToken;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class WrappedFragment extends Fragment {

    private ActivityResultLauncher<Intent> spotifyAuthLauncher;

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
    private boolean isProfileBtnClicked = false;
    private static boolean isAccountDeleted = false;
    private Button past_button;

    public WrappedFragment() {
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
        View view = inflater.inflate(R.layout.fragment_wrapped, container, false);

        // Initialize views and variables
        linkSpotifyBtn = view.findViewById(R.id.link_spotify_btn);
        mainPageName = view.findViewById(R.id.typeOfWrapped);
        recyclerView = view.findViewById(R.id.recycler_view_top_artists);
        //past_button = view.findViewById(R.id.past_button);

        // Set up RecyclerView, adapters, and lists
        artistList = new ArrayList<>();
        trackList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(artistList);
        initiateRecyclerView(recyclerView);

        fireModel = new ViewModelProvider(this).get(FireModel.class);
        if (fireModel.getNeedReload()) {
            // Data is null, show link spotify button
            linkSpotifyBtn.setVisibility(View.VISIBLE);
            mainPageName.setVisibility(View.INVISIBLE);
        } else {
            // Data exists, hide link Spotify button
            linkSpotifyBtn.setVisibility(View.INVISIBLE);
            mainPageName.setVisibility(View.VISIBLE);
            // Load RecyclerView with data from FireModel
            fillRecyclerView(fireModel.getArtists10List());
        }
        mainPageName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection
                switch (position) {
                    case 0:
                        // Load data for top 10 artists
                        recyclerView.setAdapter(artistAdapter);
                        break;
                    case 1:
                        // Load data for top 10 tracks
                        trackAdapter = new TrackAdapter(trackList);
                        recyclerView.setAdapter(trackAdapter);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

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

        linkSpotifyBtn.setOnClickListener(v -> {
            // Call getToken() to link Spotify
            Log.d("Token", "Getting token");
            Intent intent = getToken(requireActivity());
            spotifyAuthLauncher.launch(intent);
            Log.d("Token Done", "Got Token");
            // Disable linkSpotifyButton
            isProfileBtnClicked = true;
            Log.d("Link Spotify Successful", "Linked to Spotify Account Successfully");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MYLOG", "within the resume");

        // Check if user has been deleted
        if (isAccountDeleted) {
            Log.d("MYLOG", "successfully hit true if statement");

            isAccountDeleted = false;

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);

            requireActivity().finish();
        }

        // Check if data is loaded in the FireModel
        fireModel = new ViewModelProvider(requireActivity()).get(FireModel.class);
        if (!fireModel.getNeedReload()) {
            // Data exists, hide link Spotify button
            linkSpotifyBtn.setVisibility(View.INVISIBLE);
            mainPageName.setVisibility(View.VISIBLE);
            // Reload RecyclerView with data from FireModel
            fillRecyclerView(fireModel.getArtists10List());
        }
    }

    public void getUserProfile() {
        fireModel.setNeedReload(false);

        if (mAccessToken == null) {
            Toast.makeText(this.getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details",
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

                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    String userID = currentUser.getUid(); // Will never be null (must have account to log in)
                    //DocumentReference userRef = db.collection("users").document(userID);

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

                    // Fill fireModel with data
                    fireModel.setArtists10List(parsedShortArtistData);
                    fireModel.setArtistsMedium10List(parsedMediumArtistData);
                    fireModel.setArtistsLong10List(parsedLongArtistData);
                    fireModel.setTracks10List(parsedShortTracksData);
                    fireModel.setTracksMedium10List(parsedMediumTracksData);
                    fireModel.setTracksLong10List(parsedLongTracksData);
                    fireModel.setUserName(displayName);
                    fireModel.setUserId(spotifyUserId);
                    fireModel.setUserImage(userProfileImageURL);

                    // Put all the user data in a HashMap
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", displayName);
                    user.put("spotifyId", spotifyUserId);
                    user.put("ArtistsShort10", parsedShortArtistData);
                    user.put("TracksShort10", parsedShortTracksData);
                    user.put("ArtistsMedium10", parsedMediumArtistData);
                    user.put("TracksMedium10", parsedMediumTracksData);
                    user.put("ArtistsLong10", parsedLongArtistData);
                    user.put("TracksLong10", parsedLongTracksData);
                    user.put("profilePic", userProfileImageURL);

                    // Get Current User id from FireBase

                    // Update firebase data with new user data
                    db.collection("users").document(userID)
                            .update(user)
                            .addOnSuccessListener(v ->
                                    fillRecyclerView(fireModel.getArtists10List()))
                            .addOnFailureListener(e ->
                                    Log.w(TAG, "Error updating user data", e)
                            );
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initiateRecyclerView(RecyclerView rv) {
        recyclerView = rv.findViewById(R.id.recycler_view_top_artists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(artistAdapter);
    }

    private void fillRecyclerView(List<top10Artists> artistData) {
        // Clear existing data
        artistList.clear();

        // Add new data
        artistList.addAll(artistData);
        System.out.println(artistData);

        // Notify adapter about data change
        artistAdapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public static void setAccountDeleted(boolean input) {
        isAccountDeleted = input;
    }
}