package com.example.kogoproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.kogoproject.HomeScreen.HomeScreen;
import com.example.kogoproject.LoginScreen.LoginActivity;

public class MainActivity extends AppCompatActivity {

    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefManager = new SharedPrefManager(getApplicationContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent iNext;
                if (sharedPrefManager.isLoggedIn() && !sharedPrefManager.isLoggedOut()){
                    Log.d("MainActivity", "User is logged in and has not logged out explicitly");
                    iNext = new Intent(MainActivity.this, HomeScreen.class);
                    startActivity(iNext);
                    finish();
                } else if (sharedPrefManager.isLoggedInOffline() && !sharedPrefManager.isLoggedOutOffline()) {
                    iNext = new Intent(MainActivity.this,OfflineActivity.class);
                    startActivity(iNext);
                    finish();
                } else {
                    Log.d("MainActivity", "User is not logged in or has logged out explicitly");
                    // User is not logged in or has logged out explicitly
                    iNext = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(iNext);
                    finish();
                }
            }
        },0);


    }




}

