package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PastWrappedActivity extends AppCompatActivity {
    private Button present_button;
    private Button duo_button;
    private Button past_button;
    private Button public_button;
    private Button settings_button;
    private RecyclerView recyclerView;
    private ArtistAdapter artistAdapter;
    private List<top10Artists> artistList;
    private List<top10Artists> artistList2;
    private Spinner time_frame_spinner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_wrapped);
//        time_frame_spinner = findViewById(R.id.time_frame_spinner);

        // RecyclerView
//        try {
//            top10Artists.ArtistFetcher artistFetcherMedium = new top10Artists.ArtistFetcher("medium_term");
//            List<top10Artists> parsedDataMedium = artistFetcherMedium.fetchTop10Items(MainActivity.mAccessToken, MainActivity.mOkHttpClient);
//            top10Artists.ArtistFetcher artistFetcherLong = new top10Artists.ArtistFetcher("long_term");
//            List<top10Artists> parsedDataLong = artistFetcherLong.fetchTop10Items(MainActivity.mAccessToken, MainActivity.mOkHttpClient);
//            artistList = parsedDataMedium;
//            artistList2 = parsedDataLong;
//        } catch (IOException e) {
//            Log.d("JSON", "Failed to parse data: " + e);
//            Toast.makeText(PastWrappedActivity.this, "Failed to parse data, watch Logcat for more details",
//                    Toast.LENGTH_SHORT).show();
//        }
//        artistList = new ArrayList<>();
//        artistAdapter = new ArtistAdapter(artistList);
//        initiateRecyclerView(recyclerView);


        // Buttons to navigate between wrapped pages
        present_button = findViewById(R.id.present_button);
        present_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        duo_button = findViewById(R.id.duo_button);
        duo_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, DuoWrappedActivity.class);
            startActivity(intent);
        });
        past_button = findViewById(R.id.past_button);
        past_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, PastWrappedActivity.class);
            startActivity(intent);
        });
        public_button = findViewById(R.id.public_button);
        public_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, PublicWrappedActivity.class);
            startActivity(intent);
        });
        settings_button = findViewById(R.id.settings_button);
        settings_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsPage.class);
            startActivity(intent);
        });


//        time_frame_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // Handle item selection
//                switch (position) {
//                    case 0:
//                        // Load data for 6 months
//                        fillRecyclerView(artistList);
//                        break;
//                    case 1:
//                        // Load data for 12 months
//                        fillRecyclerView(artistList2);
//                        break;
//                }
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                // Do nothing
//            }
//        });

    }

    /**
     * Fill RecyclerView with data
     * @param artistData List of top 10 artists
     */
//    private void fillRecyclerView(List<top10Artists> artistData) {
//        // Clear existing data
//        artistList.clear();
//        // Add new data
//        artistList.addAll(artistData);
//        // Notify adapter about data change
//        artistAdapter.notifyDataSetChanged();
//    }
//
//    public void initiateRecyclerView(RecyclerView rv) {
//        recyclerView = findViewById(R.id.top_artists_recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(artistAdapter);
//    }
}