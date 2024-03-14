package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class SettingsPage extends AppCompatActivity {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button delete_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        delete_button = findViewById(R.id.delete_btn);

    }

    public void delete_account_button(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        currentUser.delete();

        finish();
    }

    public void edit_account_button(View view) {
        startActivity(new Intent(this, EditAccountInformation.class));
    }

    public void change_color_mode(View view) {

    }
}