//package com.example.spotifywrapped;
//
//import android.os.Bundle;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.List;
//
//public class PastWrappedActivity extends AppCompatActivity {
//    private TextView displayNameTextView;
//    private RecyclerView topArtistsRecyclerView;
//    // Add more UI elements as needed
//    private FirebaseFirestore db;
//    private List<top10Artists> artistsList;
//    private TopArtistsAdapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.wrapped_main);
//
//        // Initialize UI elements
//        //displayNameTextView = findViewById(R.id.display_name_text_view);
//        //topArtistsRecyclerView = findViewById(R.id.top_artists_recycler_view);
//        // Initialize other UI elements
//        db = FirebaseFirestore.getInstance();
//        // Retrieve data from Firebase
//        retrieveUserDataFromFirebase();
//    }
//
//    private void retrieveUserDataFromFirebase() {
//// Assuming the top artists are stored in a "topArtists" collection in Firestore
//        db.collection("Artists10")
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            top10Artists artist = documentSnapshot.toObject(top10Artists.class);
//                            artistsList.add(artist);
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(WrappedMainActivity.this, "Failed to retrieve top artists", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//}
