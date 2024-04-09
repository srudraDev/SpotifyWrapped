package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DuoWrappedActivity extends AppCompatActivity {
    private Button present_button;
    private Button duo_button;
    private Button past_button;
    private Button public_button;
    private Button settings_button;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.duo_wrapped);


        // Wrapped pages to be implemented
        // Create the different pages for the different wrapped
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
    }
}