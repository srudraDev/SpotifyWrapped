package com.example.spotifywrapped;

import static com.example.spotifywrapped.MainActivity.*;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.db;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;
import static com.example.spotifywrapped.top10Artists.fetchTop10Artist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import okhttp3.Call;     //added by me
import okhttp3.Callback; // added by me
import okhttp3.Request;  // added by me
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText userInput;
    private EditText passInput;
    private Button loginButton;
    private TextView toSignUp;

    //added by me
    private TextView tokenTextView, codeTextView, profileTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        userInput = findViewById(R.id.userInput);
        passInput = findViewById(R.id.passInput);
        loginButton = findViewById(R.id.loginButton);
        toSignUp = findViewById(R.id.toSignUp);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(view -> loginUser());

        toSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }
    private void loginUser() {
        String username = userInput.getText().toString();
        String password = passInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be left blank.", Toast.LENGTH_SHORT).show();
        }
        firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // move "request token" "request code" and "get user profile" functionality to this button in that order.
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(this, MainActivity.class);
                //startActivity(intent);
                // add code here:

                try {
                    pullSpotifyDataToDatabase.getToken(this); // getToken()
                    //wait for response
                    pullSpotifyDataToDatabase.getCode(this); // getCode()
                    onGetUserProfileClicked();  // method to fetch user profile data
                } catch (Exception e) {
                    Log.d("Error123", "" + e);
                }
                Intent intent = new Intent(LoginActivity.this, WrappedMainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Login Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(LoginActivity.this, "Failed to fetch data, watch Logcat for more details",
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
                    Toast.makeText(LoginActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // added
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }
}