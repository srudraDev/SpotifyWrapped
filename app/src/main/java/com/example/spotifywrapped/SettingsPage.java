package com.example.spotifywrapped;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SettingsPage extends AppCompatActivity {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button delete_button;
    private LinearLayout layout;
    private Dialog myDialog;
    private EditText editEmailText, editPasswordText, confirmedEmail, confirmedPassword;
    private String oldEmail, oldPassword;
    private DatabaseReference reference;
    private AuthCredential credential;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        delete_button = findViewById(R.id.delete_btn);

        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.edit_account);

//        int width = (int) (getResources().getDisplayMetrics().widthPixels);
//        int height = (int) (getResources().getDisplayMetrics().heightPixels * .5);
//        myDialog.getWindow().setLayout(width, height);

    }


    public void delete_account_button(View view) {
        Log.d("MYLOG", "continues before auth");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d("MYLOG", "continues before user");
        FirebaseUser currentUser = auth.getCurrentUser();

        Log.d("MYLOG", "continues before delete");
        currentUser.delete();

        Log.d("MYLOG", "successfully deleted user");
        MainActivity.setAccountDeleted(true);

        Log.d("MYLOG", "successfully set deleted true");
        finish();
    }

    public void edit_account_button(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("MYLOG", "Auth has been done");

        FirebaseUser currentUser = auth.getCurrentUser();
        Log.d("MYLOG", "current User has been done");
        myDialog.setContentView(R.layout.resign_in_page);

        confirmedEmail = (EditText) myDialog.findViewById(R.id.confirm_email);
        confirmedPassword = (EditText) myDialog.findViewById(R.id.confirm_password);

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
        Log.d("MYLOG", "Auth has been done!");
        FirebaseUser currentUser = auth.getCurrentUser();

//        reference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        editEmailText = (EditText) myDialog.findViewById(R.id.edit_account_email);
        editPasswordText = (EditText) myDialog.findViewById(R.id.edit_account_password);

        String newEmailText = editEmailText.getText().toString().trim();
        Log.d("MYLOG", ("email text is : " + newEmailText));

        String newPasswordText = editPasswordText.getText().toString().trim();

        //currentUser.sendEmailVerification();
        currentUser.verifyBeforeUpdateEmail(newEmailText)
//        currentUser.updateEmail(newEmailText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("MYLOG", "User email address updated.");

//                        Map<String, Object> updates = new HashMap<>();
//                        updates.put("email", newEmailText);
//
//                        reference.updateChildren(updates);

                        Log.d("MYLOG", currentUser.getEmail());
                        myDialog.hide();
                        myDialog.setContentView(R.layout.confirm_email_message);
                        myDialog.show();

                    } else {
                        Toast.makeText(this, "Couldn't update email", Toast.LENGTH_SHORT);

                        Log.d("MYLOG", "User email address not updated.");
                        return;
                    }
                });

        currentUser.updatePassword(newPasswordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("MYLOG", "User password updated.");
                    } else {
                        Log.d("MYLOG", "User password not updated");
                        Toast.makeText(this, "Couldn't update password", Toast.LENGTH_SHORT);
                    }
                });

        myDialog.hide();
    }

    public void confirm_information_press(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        oldEmail = confirmedEmail.getText().toString();
        oldPassword = confirmedPassword.getText().toString();

        credential = EmailAuthProvider.getCredential(oldEmail, oldPassword);
        currentUser.reauthenticate(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d("MYLOG", "REAUTHENTICATE WAS SUCCESSFUL");
                myDialog.setContentView(R.layout.edit_account);
            } else {
                Toast.makeText(this, "Incorrect User Information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void before_confirm_delete(View view) {
        myDialog.setContentView(R.layout.confirm_delete_account);
        myDialog.show();
    }
}