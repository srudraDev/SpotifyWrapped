package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText userInput;
    private EditText passInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        userInput = findViewById(R.id.userInput);
        passInput = findViewById(R.id.passInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView toSignUp = findViewById(R.id.toSignUp);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(view -> loginUser());

        toSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
    }
    private void loginUser() {
        String username = userInput.getText().toString();
        String password = passInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be left blank.", Toast.LENGTH_SHORT).show();
        }
        firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Log.d("LOGIN", "LOGIN SUCCESSFUL");
                Intent intent = new Intent(this, MainActivity.class);
                // If account was previously deleted, logging in successfully should tell wrapped fragment
                WrappedFragment.setAccountDeleted(false);
                startActivity(intent);
                //finish();
            } else {
                Toast.makeText(this, "Login Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}