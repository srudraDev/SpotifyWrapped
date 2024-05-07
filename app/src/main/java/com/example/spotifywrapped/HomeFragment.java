package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import static com.example.spotifywrapped.UtilitySpotifyFeatureMethods.*;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.db;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private ActivityResultLauncher<Intent> spotifyAuthLauncher;
    public static String mAccessToken;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private FirebaseAuth firebaseAuth;
    private String username;
    private final Handler handler = new Handler();
    private boolean wasLoadingData = false;
    private TextView loadingTextView;
    private TextView getStartedTextView;
    private Button linkSpotifyBtn;
    private ImageView profilePic;
    private String profilePicURL;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home, container, false);

        linkSpotifyBtn = view.findViewById(R.id.link_spotify_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        checkFirebase();
        profilePic = view.findViewById(R.id.profilePic);

        loadingTextView = view.findViewById(R.id.loading);
        loadingTextView.setVisibility(View.INVISIBLE);
        getStartedTextView = view.findViewById(R.id.getStarted);

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
                            username = null;
                            loadingTextView = view.findViewById(R.id.loading);
                            loadingTextView.setVisibility(View.VISIBLE);
                            getStartedTextView.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        // Gets the token for the user and primes the spotifyAuthLauncher
        refreshLinkSpotify(spotifyAuthLauncher, linkSpotifyBtn, requireActivity());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(dataLoadingRunnable);
    }

    private void setLoading(boolean loading) {
        // Access MainActivity and set loading state
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.setLoadingData(loading);
        if (loading) {
            wasLoadingData = true;
        }
        // Access text views and update visibility based on loading state
        getActivity().runOnUiThread(() -> {
            // Access text views and update visibility based on loading state
            loadingTextView = view.findViewById(R.id.loading);
            getStartedTextView = view.findViewById(R.id.getStarted);
            loadingTextView.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
            getStartedTextView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
            if (username != null) {
                linkSpotifyBtn.setVisibility(View.INVISIBLE);
                linkSpotifyBtn.setEnabled(false);
            }
        });
    }

    private final Runnable dataLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            checkFirebase(); // Check Firebase for username
            handler.postDelayed(this, 5000); // Schedule the next check after 5 seconds
        }
    };

    private void checkFirebase() {
        getUserName();
        setLoading(username == null);
    }

    private void getUserName() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String userID = currentUser.getUid();

        String documentPath = "users/" + userID;
        DocumentReference docRef = db.document(documentPath);

        // FILL DATA FROM FIREBASE
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("TEST", "DOCREF SUCCESSFUL");
                // Document exists, extract the data
                // Assuming the structure of your document is similar to how you parsed the Spotify API response
                try {
                    username = (String) documentSnapshot.get("displayName");
                    profilePicURL = (String) documentSnapshot.get("profilePic");
                    // Change the name in home if the user has one
                    TextView welcomeMessage = view.findViewById(R.id.welcomeMessage);
                    if (username != null) {
                        getActivity().runOnUiThread(() -> {
                            Glide.with(this)
                                    .load(profilePicURL)
                                    .into(profilePic);
                            profilePic.setVisibility(View.VISIBLE);
                            welcomeMessage.setText("Hello " + username + "!");
                            setLoading(false); // Update UI component
                            if (wasLoadingData) {
                                Toast.makeText(requireContext(), "Loading Success!", Toast.LENGTH_SHORT).show();
                                wasLoadingData = false;
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d("LOAD ERROR", "USERNAME NOT FOUND");
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

                    getAndUpdateFromFirebase(db, user);
                    checkFirebase();
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }
}