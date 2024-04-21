package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
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
                    // Get artist and track arrays from firebase
                    username = (String) documentSnapshot.get("displayName");
                    // Change the name in home if the user has one
                    TextView welcomeMessage = view.findViewById(R.id.welcomeMessage);
                    if (username != null) {
                        welcomeMessage.setText("Hello " + username + "!");
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

        return view;
    }
}