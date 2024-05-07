package com.example.spotifywrapped;

import static com.example.spotifywrapped.secret.spotify_client_id;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import okhttp3.Call;
public class pullSpotifyDataToDatabase {
    private static final String CLIENT_ID = spotify_client_id;
    private static final String REDIRECT_URI = "spotifywrapped://auth";
    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static Call mCall;


    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public static Intent getToken(Activity activity) {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "user-read-email", "user-top-read"});
        AuthorizationRequest request = builder.build();

        return AuthorizationClient.createLoginActivityIntent(activity, request);
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */

    /**
     * Get the Spotify authorization response from the intent data
     * @param resultCode The result code
     * @param data The intent data
     * @return The authorization response
     */
    public static AuthorizationResponse getSpotifyAuthResponse(int resultCode, Intent data) {
        return AuthorizationClient.getResponse(resultCode, data);
    }

    public static void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
