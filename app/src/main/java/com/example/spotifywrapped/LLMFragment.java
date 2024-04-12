package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;
import static com.example.spotifywrapped.secret.openAI_key;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LLMFragment extends Fragment {

    private TextView aiResponse;
    private String prompt_genre = "";
    private String assistant = "";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public LLMFragment() {
        // Required empty public constructor
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.personality_ai, container, false);

        aiResponse = view.findViewById(R.id.response);
        if (assistant.isEmpty())
            genreRetrieval();
        else
            aiResponse.setText(assistant);
        return view;
    }


    public void makePersonalityRequest(String genre) {
        Log.d("genre", genre);
        String url = "https://api.openai.com/v1/chat/completions";
        String prompt = "What would my personality be based on the genres of music I listen to listed below? (Respond in 50 words) " + genre;

        String requestBody = "{" +
                "\"model\": \"gpt-3.5-turbo\"," +
                "\"messages\": [" +
                "{\"role\":\"system\", \"content\": \"" + prompt + "\"}," +
                "{\"role\": \"user\", \"content\": \"Hello!\"}" +
                "]}";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body =  RequestBody.create(mediaType,requestBody);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + openAI_key)
                .addHeader("Content-Type", "application/json")
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OPENAI", "Request failed: " + e.getMessage());
                // Handle failure
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("TAG", "ARE YOU RUNNING");
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        final JSONObject jsonObject = new JSONObject(responseBody);
                        assistant = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        Log.d("assistant", assistant);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Set the extracted content to aiResponse TextView
                                aiResponse.setText(assistant);
                            }
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("OPENAI", "Request failed: " + response.code());
                    // Handle unsuccessful response
                }
            }
        });
    }
    public void genreRetrieval() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userID = currentUser.getUid();
        String documentPath = "users/" + userID;
        DocumentReference docRef = db.document(documentPath);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                try {
                    // Get artist and track arrays from firebase
                    List<Map<String,Object>> Artists10 = (List<Map<String, Object>>) documentSnapshot.get("Artists10");
                    for (int i = 0; i < 4; i++) {
                        ArrayList<String> genres = (ArrayList<String>) Artists10.get(i).get("genres");
                        if (!genres.isEmpty()) {
                            prompt_genre += genres.get(0) + ", ";
                        }
                        Log.d("SEE GENRES", prompt_genre);
                    }
                    makePersonalityRequest(prompt_genre);
                } catch (Exception e) {
                    System.out.println("E: " + e);
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
}
