package com.example.spotifywrapped;

import static com.example.spotifywrapped.pullSpotifyDataToDatabase.db;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText userInput;
    private EditText passInput;
    private Button signupButton;
    private TextView toLogin;
    private static String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        userInput = findViewById(R.id.userInput);
        passInput = findViewById(R.id.passInput);
        signupButton = findViewById(R.id.authButton);
        toLogin = findViewById(R.id.changePage);

        firebaseAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(v -> signupUser());
        toLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    private void signupUser() {
        String username = userInput.getText().toString();
        String password = passInput.getText().toString();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(this, "Fields cannot be left blank.", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    Toast.makeText(SignUpActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                    if (currentUser != null) {
                        userID = currentUser.getUid();
                        db.collection("users").document(userID).set(new HashMap<>())
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this,
                                            "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Signup Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}