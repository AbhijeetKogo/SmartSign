package com.example.kogoproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kogoproject.HomeScreen.HomeScreen;
import com.example.kogoproject.LoginScreen.LoginActivity;

public class BootReceiver extends BroadcastReceiver {

    SharedPrefManager sharedPrefManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            sharedPrefManager = new SharedPrefManager(context.getApplicationContext());

            Intent iNext;
            if (sharedPrefManager.isLoggedIn() && !sharedPrefManager.isLoggedOut()){
                Log.d("MainActivity", "User is logged in and has not logged out explicitly");
                iNext = new Intent(context, HomeScreen.class);
                iNext.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(iNext);
            } else if (sharedPrefManager.isLoggedInOffline() && !sharedPrefManager.isLoggedOutOffline()) {
                iNext = new Intent(context,OfflineActivity.class);
                iNext.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(iNext);
            } else {
                Log.d("MainActivity", "User is not logged in or has logged out explicitly");
                // User is not logged in or has logged out explicitly
                iNext = new Intent(context, LoginActivity.class);
                iNext.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(iNext);
            }
        }
    }
}