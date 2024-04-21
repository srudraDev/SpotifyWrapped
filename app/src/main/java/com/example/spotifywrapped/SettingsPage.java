package com.example.spotifywrapped;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SettingsPage extends AppCompatActivity {
    private Dialog myDialog;
    private EditText confirmedEmail;
    private EditText confirmedPassword;
    private DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (getResources().getDisplayMetrics().heightPixels * .5);
        myDialog = new Dialog(this);
        Objects.requireNonNull(myDialog.getWindow()).setLayout(width, height);
        myDialog.setContentView(R.layout.edit_account);
    }


    public void delete_account_button(View view) {
        Log.d("DELETE", "ATTEMPTING TO DELETE USER");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.delete();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Log.d("DELETE", "USER COULD NOT BE DELETED");
        }
        finish();
    }

    public void edit_account_button(View view) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (getResources().getDisplayMetrics().heightPixels * .5);
        Objects.requireNonNull(myDialog.getWindow()).setLayout(width, height);
        myDialog.setContentView(R.layout.resign_in_page);
        confirmedEmail = myDialog.findViewById(R.id.confirm_email);
        confirmedPassword = myDialog.findViewById(R.id.confirm_password);
        myDialog.show();
    }

    public void return_from_settings(View view) {
        finish();
    }
    public void myDialog_hide(View view) {
        myDialog.hide();
    }

    public void successful_account_edit(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("EDIT", "AUTH SUCCESSFUL");
        FirebaseUser currentUser = auth.getCurrentUser();

        assert currentUser != null;
        reference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        EditText editEmailText = myDialog.findViewById(R.id.edit_account_email);
        EditText editPasswordText = myDialog.findViewById(R.id.edit_account_password);

        String newEmailText = editEmailText.getText().toString().trim();
        Log.d("EDIT", ("EMAIL: " + newEmailText));

        String newPasswordText = editPasswordText.getText().toString().trim();

        currentUser.verifyBeforeUpdateEmail(newEmailText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("EDIT CONFIRM", "User email address updated.");

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("email", newEmailText);

                        reference.updateChildren(updates);

                        Log.d("CURRENT EMAIL: ", Objects.requireNonNull(currentUser.getEmail()));
                        myDialog.hide();
                        myDialog.setContentView(R.layout.confirm_email_message);
                        myDialog.show();

                    } else {
                        Toast.makeText(this, "Couldn't update email", Toast.LENGTH_SHORT).show();

                        Log.d("EMAIL DENIED", "User email address not updated.");
                    }
                });

        currentUser.updatePassword(newPasswordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("PASSWORD", "PASSWORD UPDATED");
                    } else {
                        Log.d("PASSWORD FAIL", "PASSWORD NOT UPDATED");
                        Toast.makeText(this, "Couldn't update password", Toast.LENGTH_SHORT).show();
                    }
                });

        myDialog.hide();
    }

    public void confirm_information_press(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        String oldEmail = confirmedEmail.getText().toString();
        String oldPassword = confirmedPassword.getText().toString();

        AuthCredential credential = EmailAuthProvider.getCredential(oldEmail, oldPassword);
        assert currentUser != null;
        currentUser.reauthenticate(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d("RE AUTH", "REAUTHENTICATION SUCCESSFUL");
                myDialog.setContentView(R.layout.edit_account);
            } else {
                Toast.makeText(this, "Incorrect User Information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void before_confirm_delete(View view) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (getResources().getDisplayMetrics().heightPixels * .4);
        Objects.requireNonNull(myDialog.getWindow()).setLayout(width, height);
        myDialog.setContentView(R.layout.confirm_delete_account);
        myDialog.show();
    }
}