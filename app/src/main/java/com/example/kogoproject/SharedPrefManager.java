package com.example.kogoproject;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.kogoproject.HomeScreen.Offer;
import com.example.kogoproject.HomeScreen.ResponseModel;
import com.example.kogoproject.HomeScreen.SyncResponseModel;
import com.example.kogoproject.LoginScreen.LoginResponseModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "SMARTSIGN";
    private SharedPreferences sharedPreferences;
    Context context;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        this.context = context;
    }

    public void saveUser(LoginResponseModel loginResponseModel){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("resultcode",loginResponseModel.getResultcode());
        editor.putString("resultmsg",loginResponseModel.getResultmsg());
        editor.putString("locid",loginResponseModel.getLocid());
        editor.putString("loc_uid",loginResponseModel.getLoc_uid());
        editor.putInt("sync_timing",loginResponseModel.getSync_timing());
        editor.putString("location_logo",loginResponseModel.getLocation_logo());
        editor.putBoolean("logged_in",true);
        editor.putBoolean("logged_out",false);
        editor.apply();
    }

    public void saveSignage(String signage){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("signageId",signage);
        editor.apply();
    }

    public String getSignage(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString("signageId", null);
    }

    public LoginResponseModel getUser(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return new LoginResponseModel(sharedPreferences.getInt("resultcode",-1),
                sharedPreferences.getString("resultmsg",null),
                sharedPreferences.getString("locid",null),
                sharedPreferences.getString("loc_uid",null),
                sharedPreferences.getInt("sync_timing",-1),
                sharedPreferences.getString("location_logo",null));
    }

    public boolean isLoggedIn(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("logged_in",false);
    }

    public boolean isLoggedOut(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("logged_out",false);
    }

    public void saveUserOffline(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("logged_in_offline",true);
        editor.putBoolean("logged_out_offline",false);
        editor.apply();
    }

    public boolean isLoggedInOffline(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("logged_in_offline",false);
    }

    public boolean isLoggedOutOffline(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("logged_out_offline",false);
    }

    public void saveResponseModelInSharedPreference(ResponseModel responseModel) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(responseModel);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("resultcode", responseModel.getResultcode()); // Save result code
        editor.putString("resultmsg", responseModel.getResultmsg()); // Save result message
        editor.putString("offerData", json);
        editor.apply();
    }

    public ResponseModel getFieldsFromSharedPreference() {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        int resultcode = sharedPreferences.getInt("resultcode", -1); // Retrieve result code
        String resultmsg = sharedPreferences.getString("resultmsg", null); // Retrieve result message
        String json = sharedPreferences.getString("offerData", null);

        if (json != null) {
            Gson gson = new Gson();
            ResponseModel responseModel = gson.fromJson(json, ResponseModel.class);
            responseModel.setResultcode(resultcode); // Set result code
            responseModel.setResultmsg(resultmsg); // Set result message
            return responseModel;
        } else {
            return null;  // Handle the case when the data is not available in SharedPreferences
        }
    }


    public void syncData(SyncResponseModel syncResponseModel){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("resultcode",syncResponseModel.getResultcode());
        editor.putInt("sync_available",syncResponseModel.getSync_available());
        editor.putInt("take_shot",syncResponseModel.getTake_shot());
        editor.putInt("islogout",syncResponseModel.getIslogout());
        editor.putString("sync_available_cond",syncResponseModel.getSync_available_cond());
        editor.putBoolean("logged_in",true);
        editor.putBoolean("logged_out",false);
        editor.apply();
    }

    public SyncResponseModel getSyncData(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return new SyncResponseModel(sharedPreferences.getInt("resultcode",-1),
                sharedPreferences.getInt("sync_available",-1),
                sharedPreferences.getInt("take_shot",-1),
                sharedPreferences.getInt("islogout",-1),
                sharedPreferences.getString("sync_available_cond",null));
    }

    public void logoutOffline(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean("logged_in_offline",false);
        editor.putBoolean("logged_out_offline",true);
        editor.apply();
    }

    public void logout(){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean("logged_in",false);
        editor.putBoolean("logged_out",true);
        editor.apply();
    }
}
