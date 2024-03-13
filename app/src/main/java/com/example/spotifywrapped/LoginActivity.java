package com.example.spotifywrapped;

import static com.example.spotifywrapped.MainActivity.*;
import android.content.Intent;
import android.os.Bundle;
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
    private Button loginButton;
    private TextView toSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        userInput = findViewById(R.id.userInput);
        passInput = findViewById(R.id.passInput);
        loginButton = findViewById(R.id.loginButton);
        toSignUp = findViewById(R.id.toSignUp);

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
                // move "request token" "request code" and "get user profile" functionality to this button in that order.
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                // add code here:



                finish();
            } else {
                Toast.makeText(this, "Login Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}