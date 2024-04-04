package com.example.spotifywrapped;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class SettingsPage extends AppCompatActivity {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button delete_button;
    private LinearLayout layout;
    private Dialog myDialog;
    private EditText editEmailText, editPasswordText;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        delete_button = findViewById(R.id.delete_btn);

        layout = findViewById(R.id.linear);

        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.edit_account);

        editEmailText = (EditText) myDialog.findViewById(R.id.edit_account_email);
        editPasswordText = (EditText) myDialog.findViewById(R.id.edit_account_password);
    }

    public void delete_account_button(View view) {
        Log.d("MYLOGGG", "continues before auth");
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d("MYLOGGG", "continues before user");
        FirebaseUser currentUser = auth.getCurrentUser();

        Log.d("MYLOGGG", "continues before delete");
        currentUser.delete();

        Log.d("MYLOGGG", "successfully deleted user");
        MainActivity.setAccountDeleted(true);

        Log.d("MYLOGGG", "successfully set deleted true");
        finish();
    }

    public void edit_account_button(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("MYLOG", "Auth has been done");

        FirebaseUser currentUser = auth.getCurrentUser();
        Log.d("MYLOG", "current User has been done");

//        myDialog.setContentView(R.layout.edit_account);
//
//
//        editEmailText = (EditText) myDialog.findViewById(R.id.edit_account_email);
//        editPasswordText = (EditText) myDialog.findViewById(R.id.edit_account_password);


        editEmailText.setText(currentUser.getEmail());
        Log.d("MYLOG", "edit email text set has been done");

        editPasswordText.setText("***");
        Log.d("MYLOG", "edit password test has been done");

        myDialog.show();
    }

    public void return_from_settings(View view) {
        finish();
    }
    public void cancel_edit_account(View view) {
        myDialog.hide();
    }

    //
    public void successful_account_edit(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("MYLOG", "Auth has been done");

        FirebaseUser currentUser = auth.getCurrentUser();

        if (!editEmailText.getText().toString().equals(currentUser.getEmail())) {
            currentUser.updateEmail(editEmailText.getText().toString());
        }
        if (!editPasswordText.getText().toString().equals("***")) {
            currentUser.updatePassword(editPasswordText.getText().toString());
        }
    }
}