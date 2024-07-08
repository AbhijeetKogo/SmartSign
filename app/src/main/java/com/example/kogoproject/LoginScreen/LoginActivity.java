package com.example.kogoproject.LoginScreen;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kogoproject.HomeScreen.HomeScreen;
import com.example.kogoproject.HomeScreen.RequestModel;
import com.example.kogoproject.HomeScreen.ResponseModel;
import com.example.kogoproject.OfflineActivity;
import com.example.kogoproject.R;
import com.example.kogoproject.SharedPrefManager;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class LoginActivity extends AppCompatActivity {

    EditText newUrl;

    Button startOnline;

    Button startoffline;

    TextView exit;

    private String signage_id;
    private int REQ_CODE = 100;

    private static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    private static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String READ_EXTERNAL = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED;
    private static final String READ_IMAGE_STORAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES;
    private static final String READ_VIDEO_STORAGE_PERMISSION = Manifest.permission.READ_MEDIA_VIDEO;


    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            showPermissionDialogUpdated();
        }
        else {
            showPermissionDialog();
        }

        startOnline = findViewById(R.id.startonline);

        newUrl = findViewById(R.id.new_url);

        exit = findViewById(R.id.exit);

        startoffline = findViewById(R.id.startoffline);

        exit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        startOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if WiFi is connected
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isWifiConnected = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

                if (isWifiConnected) {
                    createOnlineFolder();
                    signage_id = newUrl.getText().toString();
                    sharedPrefManager.saveSignage(signage_id);
                    callHomeApi();
//                    newUrl.setText("");
                } else {
                    Log.e("TAG", "onClick: " );
                    Toast.makeText(LoginActivity.this, "WiFi not Connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startoffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOfflineFolder();
                AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(LoginActivity.this);
                myAlertBuilder.setTitle("Check all points before go offline!");
                String message = ":- Folder 'OfflineISmart' should not empty\n" +
                        ":- Video extension should be .mp4 only\n" +
                        ":- Video width and height should be 1080*1920\n" +
                        ":- Video size should be between 60-70 MB\n" +
                        ":- Image extension should be only .jpg or .png\n" +
                        ":- Image width and height should be 1080*1920";
                myAlertBuilder.setMessage(message);
                myAlertBuilder.setPositiveButton("GO OFFLINE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                                + "/smartSignOffline";

                        boolean directoryExists = isDirectoryExists(directoryPath);

                        if (directoryExists) {
                            // Directory exists
                            File[] files = new File(directoryPath).listFiles();

                            List<String> imageExtensions = Arrays.asList("jpg", "png");
                            List<String> videoExtensions = Arrays.asList("mp4");

                            int imageCount = 0;
                            int videoCount = 0;

                            if (files != null) {
                                for (File file : files) {
                                    if (file.isFile()) {
                                        String fileName = file.getName().toLowerCase();
                                        if (imageExtensions.contains(getFileExtension(fileName))) {
                                            imageCount++;
                                        } else if (videoExtensions.contains(getFileExtension(fileName))) {
                                            videoCount++;
                                        }
                                    }
                                }
                            }

//                            if (imageCount > 0) {
//                                // Directory contains images
//                                Log.e("SmartSignOffline", "Directory contains " + imageCount + " image(s)");
//                            } else if (videoCount > 0) {
//                                // Directory contains videos
//                                Log.e("SmartSignOffline", "Directory contains " + videoCount + " video(s)");
//                            } else {
//                                // Directory does not contain any images or videos
//                                Log.e("SmartSignOffline", "Directory does not contain any images or videos");
//                            }

                            if (imageCount > 0 || videoCount > 0) {
                                // Directory is not empty
                                sharedPrefManager.saveUserOffline();
                                Intent intent = new Intent(LoginActivity.this,OfflineActivity.class);
                                startActivity(intent);
                                Log.e("SmartSignOffline", "onClick: Directory is not empty");
                            } else {
                                // Directory is empty
                                Log.e("SmartSignOffline", "onClick: Directory is empty");
                                Toast.makeText(LoginActivity.this,"Smart Offline Folder is Empty",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Directory does not exist
                            Toast.makeText(LoginActivity.this,"Smart Offline Not Exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                myAlertBuilder.setNegativeButton("LET ME CHECK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(LoginActivity.this, "You Clicked No", Toast.LENGTH_SHORT).show();
                    }
                });
                myAlertBuilder.show();
            }
        });

        sharedPrefManager = new SharedPrefManager(getApplicationContext());
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(LoginActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(LoginActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    // Gets the file extension from the file name
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int index = fileName.lastIndexOf(".");
        if (index < 0) {
            return null;
        }

        return fileName.substring(index + 1);
    }

    private void showPermissionDialogUpdated(){
        if (ContextCompat.checkSelfPermission(this,CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,READ_IMAGE_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,READ_VIDEO_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this,"Permission Accepted",Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(this,new String[]{CALL_PHONE,LOCATION_PERMISSION,READ_IMAGE_STORAGE_PERMISSION,READ_VIDEO_STORAGE_PERMISSION},REQ_CODE);
        }
    }


    private void showPermissionDialog(){
        if (ContextCompat.checkSelfPermission(this,CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,READ_EXTERNAL) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL) == PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this,"Permission Accepted",Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(this,new String[]{CALL_PHONE,LOCATION_PERMISSION,READ_EXTERNAL,WRITE_EXTERNAL},REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE) {

            if (grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission Accepted",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this,"Not Granted",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.TIRAMISU){
                showPermissionDialogUpdated();
            }
            else {
                showPermissionDialog();
            }
        }
    }

    private void createOnlineFolder() {
        File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File smartSign = new File(download, "smartSignOnline");

        if (!smartSign.exists()) {
            smartSign.mkdir();
        }
        else {
            Log.e("folder", "Folder Already Exist " );
        }
    }


    private void createOfflineFolder() {
        File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File smartSign = new File(download, "smartSignOffline");

        if (!smartSign.exists()) {
            smartSign.mkdir();
        }
        else {
            Log.e("folder", "Folder Already Exist " );
        }
    }

    private boolean isDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.exists() && directory.isDirectory();
    }


    private void callHomeApi(){
        Log.e("TAG", "callHomeApi: " );
        LoginRequestModel loginRequestModel = new LoginRequestModel(signage_id);

        Call<LoginResponseModel> call = ApiController.getInstance().getApi().getUserData(loginRequestModel);
        call.enqueue(new Callback<LoginResponseModel>() {
            @Override
            public void onResponse(Call<LoginResponseModel> call, Response<LoginResponseModel> response) {
                LoginResponseModel loginResponseModel = response.body();
                assert loginResponseModel != null;
                if (!signage_id.trim().isEmpty()) {
                        sharedPrefManager.saveUser(loginResponseModel);
                        if (sharedPrefManager.isLoggedIn() && sharedPrefManager.getUser().getResultcode() == 200) {
                            Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.putExtra("userId", signage_id);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("LoginActivity", "signage_id: " + signage_id + ", loc_uid: " + sharedPrefManager.getUser().getLoc_uid());
                            Toast.makeText(LoginActivity.this, "Invalid Signage Id", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                    Log.d("LoginActivity", "signage_id: " + signage_id + ", loc_uid: " + sharedPrefManager.getUser().loc_uid);
                    Toast.makeText(LoginActivity.this, "Signage Id is Required", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponseModel> call, Throwable t) {

            }
        });
    }

}