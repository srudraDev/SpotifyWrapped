package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView
        .OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    private boolean isLoadingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView
                = findViewById(R.id.bottomNavigationView);
                
        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);
    }
    HomeFragment firstFragment = new HomeFragment();
    WrappedFragment secondFragment = new WrappedFragment();
    PastWrappedFragment thirdFragment = new PastWrappedFragment();
    LLMFragment fourthFragment = new LLMFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (isLoadingData) {
            return false;
        }
        if (item.getItemId() == R.id.home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, firstFragment)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.wrapped) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, secondFragment)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.past) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, thirdFragment)
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.LLM) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, fourthFragment)
                    .commit();
            return true;
        }
        return false;
    }
    public void settings_btn_click(View view) {
        startActivity(new Intent(this, SettingsPage.class));
    }
    public void setLoadingData(boolean loading) {
        isLoadingData = loading;
    }
}