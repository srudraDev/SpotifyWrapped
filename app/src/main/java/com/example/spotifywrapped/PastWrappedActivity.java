package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PastWrappedActivity extends AppCompatActivity {

    private Button back_button;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_wrapped);

        back_button = findViewById(R.id.main_button);

        back_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }
}