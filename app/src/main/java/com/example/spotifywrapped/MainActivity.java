package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import static com.example.spotifywrapped.pullSpotifyDataToDatabase.*;
import static com.example.spotifywrapped.top10Artists.fetchTop10Artist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static String mAccessToken, mAccessCode;
    private TextView tokenTextView, codeTextView, profileTextView;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views
        tokenTextView = (TextView) findViewById(R.id.token_text_view);
        codeTextView = (TextView) findViewById(R.id.code_text_view);
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        Button tokenBtn = (Button) findViewById(R.id.token_btn);
        Button codeBtn = (Button) findViewById(R.id.code_btn);
        Button profileBtn = (Button) findViewById(R.id.profile_btn);

        // Set the click listeners for the buttons

        tokenBtn.setOnClickListener((v) -> {
            getToken(MainActivity.this);
        });

        codeBtn.setOnClickListener((v) -> {
            getCode(MainActivity.this);
        });

        profileBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });
    }

    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Call the method from pullSpotifyDataToDatabase to retrieve the authorization response
        AuthorizationResponse response = pullSpotifyDataToDatabase.getSpotifyAuthResponse(resultCode, data);

        // Check which request code is present (if any)
        if (response != null) {
            if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
                mAccessToken = response.getAccessToken();
                setTextAsync(mAccessToken, tokenTextView);

            } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
                mAccessCode = response.getCode();
                setTextAsync(mAccessCode, codeTextView);
            }
        }
    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void onGetUserProfileClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        final Request top10Artists = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?limit=10")
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
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    String displayName = jsonObject.getString("display_name");
                    String spotifyUserId = jsonObject.getString("id");
                    //JSONArray userProfileImageArray = jsonObject.getJSONArray("images");
                    //String userProfileImageURL = userProfileImageArray.getJSONObject(0).getString("url");
                    setTextAsync(displayName, profileTextView);

                    List<top10Artists> parsedData = fetchTop10Artist(mAccessToken,mOkHttpClient);
                    Map<String, Object> user = new HashMap<>();
                    user.put("displayName", displayName);
                    user.put("spotifyId", spotifyUserId);
                    user.put("Artists10", parsedData);
                    //user.put("profilePic", userProfileImageURL);
                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    String userID = currentUser.getUid();

                    db.collection("users").document(userID)
                            .update(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + userID);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}