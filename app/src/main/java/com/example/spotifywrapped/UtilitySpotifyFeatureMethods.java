package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.cancelCall;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.getToken;
import static com.example.spotifywrapped.pullSpotifyDataToDatabase.mCall;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UtilitySpotifyFeatureMethods {
    protected static void refreshLinkSpotify(ActivityResultLauncher<Intent> spotifyAuthLauncher,
                                          Button linkSpotifyBtn, Activity activity) {
        linkSpotifyBtn.setOnClickListener(v -> {
            linkSpotifyBtn.setEnabled(false);
            // Call getToken() to link Spotify
            Log.d("TOKEN", "GET TOKEN");
            Intent intent = getToken(activity);
            spotifyAuthLauncher.launch(intent);
            // Re-enable the button after a 10 second delay
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                linkSpotifyBtn.setEnabled(true);
                Toast.makeText(activity.getApplicationContext(),"You can now refresh again!",Toast.LENGTH_SHORT).show();
            }, 10000); // 10000 milliseconds = 10 seconds
        });
    }
    protected static void requestUserProfile(String mAccessToken, OkHttpClient mOkHttpClient) {
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
    }
    protected static void getAndUpdateFromFirebase(FirebaseFirestore db, Map<String, Object> user) {
        // Get Current User id from FireBase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        assert currentUser != null;
        String userID = currentUser.getUid(); // Will never be null (must have account to log in)

        // Update firebase data with new user data
        db.collection("users").document(userID)
                .update(user)
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error updating user data", e));
    }
    public static List<top10Tracks> setTracks(List<Map<String, Object>> trackList) {
        List<top10Tracks> newTrackList = new ArrayList<>();
        for (Map<String, Object> trackData : trackList) {
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
    public static List<top10Artists> setArtists(List<Map<String, Object>> artistList) {
        List<top10Artists> newArtistList = new ArrayList<>();
        for (Map<String, Object> artistData : artistList) {
            String artistName = (String) artistData.get("name");
            List<String> genres = (List<String>) artistData.get("genres");
            String imageUrl = (String) artistData.get("secondImageUrl");

            top10Artists newArtist = new top10Artists(artistName, genres, imageUrl);
            newArtistList.add(newArtist);
        }
        return newArtistList;
    }
    protected static void shareButtonListener(View view, Context context) {
        Button shareButton = view.findViewById(R.id.share_btn);
        shareButton.setOnClickListener(v -> {
            // Capture the current view as an image
            Bitmap capturedBitmap = captureView(view);
            shareImage(capturedBitmap, context);
        });
    }
    private static Bitmap captureView(View view) {
        // Create a bitmap with the same dimensions as the view
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Create a canvas with the bitmap
        Canvas canvas = new Canvas(bitmap);
        // Draw the view onto the canvas
        view.draw(canvas);
        return bitmap;
    }
    private static void shareImage(Bitmap bitmap, Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Image.jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        try {
            if (imageUri != null) {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            // Add a subject and text (optional)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my Spotify Wrapped!");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here's my Spotify Wrapped from last year!");
            // Start the activity to share the image
            context.startActivity(Intent.createChooser(shareIntent, "Share"));
        } else {
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
    protected static void playSongClip(String previewUrl, MediaPlayer mediaPlayer, Context context) {
        try {
            mediaPlayer.setDataSource(previewUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(context, "Preview not available", Toast.LENGTH_SHORT).show();
            Log.d("SONG", "SONG FAILED TO PLAY");
        }

        // Optionally, you may want to release the MediaPlayer after the clip finishes playing
        mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.release());
    }
}
