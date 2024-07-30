package com.example.kogoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.kogoproject.HomeScreen.Offer;
import com.example.kogoproject.LoginScreen.LoginActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OfflineActivity extends AppCompatActivity {

    ImageView logoutButton;

    private Runnable runnable;

    private int currentImageIndex = 0;
    private int currentVideoIndex = 0;

    ImageView imageView;

    Handler handler;

    public Boolean isRunning = true;

    VideoView videoView;

    SharedPrefManager sharedPrefManager;

    ArrayList<String> imageList = new ArrayList<>();
    ArrayList<String> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_offline);

        logoutButton = findViewById(R.id.logoutbutton);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                isRunning = false;
            }
        });

        sharedPrefManager = new SharedPrefManager(getApplicationContext());

        fetchAllMedia();

    }

    private void fetchAllMedia() {
        isRunning = true;
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/smartSignOffline";
        File[] files = new File(directoryPath).listFiles();


        List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png");
        List<String> videoExtensions = Arrays.asList("mp4");

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName().toLowerCase();
                    if (imageExtensions.contains(getFileExtension(fileName))) {
                        imageList.add(file.getAbsolutePath());
                    } else if (videoExtensions.contains(getFileExtension(fileName))) {
                        videoList.add(file.getAbsolutePath());
                    }
                }
            }
        }

        if (!videoList.isEmpty() || !imageList.isEmpty()){
            startImageLoop(imageList);
//            startVideoLoop(videoList);
        }
        Log.e("OfflineActivity", "fetchAllMedia: Image List = " + imageList);
        Log.e("OfflineActivity", "fetchAllMedia: Video List = " + videoList);
    }

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


    private void startImageLoop(List<String> imageList) {
        isRunning = true;
        Log.e("imageList", "startImageLoop: "+imageList );
        Handler handler = new Handler(Looper.getMainLooper());
        currentImageIndex = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("imageList", "run: imageList1" );
                if (currentImageIndex < imageList.size()) {
                    Log.e("imageList", "run: imageList2" );
                    String filePath = imageList.get(currentImageIndex);
                    imageView.setVisibility(View.VISIBLE);
                    loadImage(filePath);

                    currentImageIndex++;
                    handler.postDelayed(this, 8000); // Change this line
                } else {
                    Log.e("imageList", "run: Call from else block1");
                    if (videoList.isEmpty()) {
                        currentImageIndex = 0; // Reset the image index
                        startImageLoop(imageList); // Start the image loop again
                    } else {
                        Log.e("imageList", "run: Call from else block2");
                        imageView.setVisibility(View.GONE);
                        startVideoLoop(videoList);
                    }
                }
            }
        },0);
    }

    private void loadImage(String imageUrl) {
        isRunning = true;
        Log.e("image", "loadImage: " + imageUrl);
        Picasso.get()
                .load(new File(imageUrl))
                .fit()
                .into(imageView);
    }


    private void startVideoLoop(List<String> videoList) {
        isRunning = true;
        Log.e("video", "startVideoLoop: "+videoList );
        currentVideoIndex = 0;
        Handler handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("video", "run: 1" );
                String videoUrl = videoList.get(currentVideoIndex);
                videoView.setVisibility(View.VISIBLE);
                videoView.setZOrderOnTop(true);
                loadVideo(videoUrl);

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.e("video", "run: 2" );
                        if (currentVideoIndex >= videoList.size()) {
                            currentVideoIndex = 0;
                        }
                        currentVideoIndex++;

                        if (currentVideoIndex >= videoList.size()) {
                            // Video loop is completed, start image loop
                            videoView.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
                            startImageLoop(imageList);
                        } else {
                            handler.postDelayed(runnable, 0);
                        }
                    }
                });
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void loadVideo(String videoUrl) {
        isRunning = true;
        Log.e("image", "loadVideo: "+videoUrl );
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }


    private void logout() {
        try {
            Log.e("LoggedOut", "logout: 1");
            sharedPrefManager.logoutOffline();
            Log.e("LoggedOut", "logout: 2");
            Intent intent = new Intent(OfflineActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("loggesOut", "logout: " );
            e.printStackTrace();
            // Handle the exception (e.g., show a toast message, log the error, etc.)
        }
    }



}
