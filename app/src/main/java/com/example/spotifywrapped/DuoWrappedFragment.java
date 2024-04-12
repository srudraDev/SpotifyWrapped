package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DuoWrappedFragment extends Fragment {
    private Button present_button;
    private Button duo_button;
    private Button past_button;
    private Button public_button;
    private Button settings_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.duo_wrapped, container, false);

        settings_button = view.findViewById(R.id.settings_button);

        settings_button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsPage.class);
            startActivity(intent);
        });

        return view;
    }
}