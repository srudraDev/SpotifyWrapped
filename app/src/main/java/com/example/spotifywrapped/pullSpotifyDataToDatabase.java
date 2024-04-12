package com.example.spotifywrapped;

import static com.example.spotifywrapped.secret.spotify_client_id;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import okhttp3.Call;
public class pullSpotifyDataToDatabase {
    public static final String CLIENT_ID = spotify_client_id;
    public static final String REDIRECT_URI = "spotifywrapped://auth";
    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;
    public static Call mCall;


    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public static Intent getToken(Activity activity) {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
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
    private static AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-private", "user-top-read" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    /**
     * Get the Spotify authorization response from the intent data
     * @param resultCode The result code
     * @param data The intent data
     * @return The authorization response
     */
    public static AuthorizationResponse getSpotifyAuthResponse(int resultCode, Intent data) {
        return AuthorizationClient.getResponse(resultCode, data);
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    public static Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    public static void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
