package com.example.spotifywrapped;

import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditAccountInformation extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_account_page);

    }

    public void change_email_button(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.edit_account_layout, null);

        TextView popupTextView = findViewById(R.id.popup_textView);
        popupTextView.setText("Your old email is: " + user.getEmail() + "\nWhat would you like to change it to?");



        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER,0, 0);



        //FirebaseAuth auth = FirebaseAuth.getInstance();
        //FirebaseUser currentUser = auth.getCurrentUser();

        //currentUser.updateEmail("gavgustin3@gmail.com");

    }

    public void change_passsword_button(View view) {

        //FirebaseAuth auth = FirebaseAuth.getInstance();
        //FirebaseUser currentUser = auth.getCurrentUser();
        //currentUser.updatePassword("hello123");
    }
}
