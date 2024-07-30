package com.example.kogoproject.HomeScreen;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.kogoproject.LoginScreen.LoginActivity;
import com.example.kogoproject.R;
import com.example.kogoproject.SharedPrefManager;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends AppCompatActivity {

    ImageView logoutButton;

    String UserId;

    private int logoutButtonClickCount = 0;
    private static final int LOGOUT_CLICK_THRESHOLD = 10;

    private int totalDownloads = 0;
    private int completedDownloads = 0;

    private List<Offer> offerList;
    private List<Offer> saveOfferList;
    private Handler handler;

    Handler mediaHandler = new Handler();

    private Runnable runnable;
    //    ProgressBar progressBar;
    TextView downloadingtextView;
    private final List<String> imageUrlList = new ArrayList<>();
    private final List<String> videoUrlList = new ArrayList<>();
//    private final List<String> mediaUrlList = new ArrayList<>();
//    private final List<String> sortedFilePath = new ArrayList<>();
    private final List<String> OfferPath = new ArrayList<>();

    private String imageNameLocal = "";
    private String videoNameLocal = "";

    ImageView imageView;

    VideoView videoView;
    String downloadingMessage;


    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_screen);
        logoutButton = findViewById(R.id.logoutbutton);

//        UserId = getIntent().getStringExtra("userId");

        imageView = findViewById(R.id.imageView);

        videoView = findViewById(R.id.videoView);


//        progressBar = findViewById(R.id.progressBar);

        downloadingtextView = findViewById(R.id.downloadTextView);

//        progressBar.setVisibility(View.GONE);
        downloadingtextView.setVisibility(View.GONE);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Increment the logout button click count
                logoutButtonClickCount++;

                // Check if the count has reached the threshold
                if (logoutButtonClickCount >= LOGOUT_CLICK_THRESHOLD) {
                    // Perform logout action
                    logOutUser();
                } else {
                    // Inform the user about the remaining logout button clicks required
                    int remainingClicks = LOGOUT_CLICK_THRESHOLD - logoutButtonClickCount;
                }
            }
        });
        sharedPrefManager = new SharedPrefManager(getApplicationContext());
        fetchDataFromApi();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("Sync Api", "run: ");
                syncApi();
                Log.e("syncApiCall", "run: " );
                // Schedule the next sync API call after 3 minutes
//                handler.postDelayed(this, 3*60*1000);
//                handler.postDelayed(this, 3000);
                handler.postDelayed(this, 1*60*1000);
            }
        };

        // Start the first sync API call
        handler.post(runnable);
    }

    private String getFileNameFromUrl(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        } else {
            // Use a default filename if extraction fails
            return "file_" + System.currentTimeMillis();
        }
    }

    //    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public interface DownloadCallback {
        void onDownloadComplete(String filePath);

    }

    //    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void downloadMedia(Context context, String mediaUrl, DownloadCallback callback) {

        // Extract the filename from the URL
        String fileName = getFileNameFromUrl(mediaUrl);

        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/smartSignOnline";
        File file = new File(directoryPath,fileName);
//        File file = new File(smartSignDir, fileName);

        if (file.exists()) {
            // File already exists, invoke the callback with the local file path
            Log.i("offer", "File already exists: " + fileName);
            if (callback != null) {
                callback.onDownloadComplete(file.getAbsolutePath());
            }
            return;
        }

//        if (!file.exists()){
        downloadingMessage = "Please Wait,New Content is Downloading \n Don't turn off the Standee";
        downloadingtextView.setText(downloadingMessage);
//        progressBar.setVisibility(View.VISIBLE);
        downloadingtextView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
//        }

        Log.d("MediaUrl", "downloadMedia: "+mediaUrl);


        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mediaUrl));


        request.setTitle(getFileNameFromUrl(mediaUrl));
        request.setDestinationUri(Uri.fromFile(file));

        Log.d("MediaUrl", "downloadMedia: "+Uri.fromFile(file));

        long downloadId = downloadManager.enqueue(request);
        Log.e("DownloadStarted", "DownloadStarted" + downloadId);


        totalDownloads++;

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Check if the broadcast message is for our enqueued download
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {

                    // Increment the count of completed downloads
                    completedDownloads++;

                    // Check if all downloads are complete
                    if (completedDownloads == totalDownloads) {
                        // Cancel the "Downloading..." toast
//                        downloadingToast.cancel();
                        downloadingtextView.setVisibility(View.GONE);
//                        progressBar.setVisibility(View.GONE);
                        // All downloads are complete, show the toast
                        Toast.makeText(HomeScreen.this, "Download Completed", Toast.LENGTH_SHORT).show();
                    }

                    // Optionally, you can get the file path and handle it as needed
                    String filePath = getDownloadedFilePath(context, downloadId);
                    Log.e("pathAfterFileDownloaded", "onReceive: " + filePath);

                    // Use the callback to pass the file path
                    if (callback != null) {
                        callback.onDownloadComplete(filePath);
                        Log.e("filePath", "onReceive: " + filePath);
                    }
                    // Unregister the broadcast receiver
                    context.unregisterReceiver(this);
                }
            }
        };

        // Register the broadcast receiver to listen for download completion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
        }
    }



    private String getDownloadedFilePath(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = downloadManager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

                if (uriIndex != -1) {
                    String uriString = cursor.getString(uriIndex);

                    // Check if uriString is not null and not empty
                    if (uriString != null && !uriString.isEmpty()) {
                        // Convert content URI to absolute file path
                        String absolutePath = Uri.parse(uriString).getPath();
                        cursor.close();
                        Log.e("absolutePath", "getDownloadedFilePath: " + absolutePath);
                        return absolutePath;
                    } else {
                        Log.e("getDownLoadedFilePath", "UriString is null or empty.");
                    }
                } else {
                    Log.e("getDownLoadedFilePath", "UriIndex not found in cursor.");
                }
            }
            cursor.close();
        }

        return null;
    }





    private void deleteAllLocalFilesAndFetch(final Runnable fetchCallback) {
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/smartSignOnline";

        // Create a File object representing the smartSignOnline directory
        File smartSignOnlineDir = new File(directoryPath);

        // Get all files in the smartSignOnline directory
        File[] files = smartSignOnlineDir.listFiles();

        if (files != null && files.length > 0) {
            // Counter to keep track of deleted files
            final int[] deletedCount = {0};
            final int totalFiles = files.length;

            for (final File file : files) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Check if the file exists before attempting to delete it
                            if (file.exists()) {
                                // Delete the file
                                boolean isDeleted = file.delete();
                                if (isDeleted) {
                                    Log.i("deleteAllLocalFiles", "File deleted: " + file.getName());
                                } else {
                                    Log.e("deleteAllLocalFiles", "Failed to delete file: " + file.getName());
                                }
                            } else {
                                Log.e("deleteAllLocalFiles", "File does not exist: " + file.getName());
                            }
                        } catch (SecurityException e) {
                            // Handle security exceptions
                            Log.e("deleteAllLocalFiles", "SecurityException: " + e.getMessage());
                        } catch (Exception e) {
                            // Handle other exceptions
                            Log.e("deleteAllLocalFiles", "Exception: " + e.getMessage());
                        } finally {
                            // Increment the deleted count
                            synchronized (deletedCount) {
                                deletedCount[0]++;
                                // Check if all files have been processed
                                if (deletedCount[0] == totalFiles) {
                                    // All files have been processed, call the fetch method
                                    if (fetchCallback != null) {
                                        fetchCallback.run();
                                    }
                                }
                            }
                        }
                    }
                }).start();
            }
        } else {
            Log.i("deleteAllLocalFiles", "No files found in the directory.");
            // No files to delete, directly call the fetch method
            if (fetchCallback != null) {
                fetchCallback.run();
            }
        }
    }




    public static List<String> sortFilePaths(List<String> mediaList, List<String> filePathList) {
        // Create a map to link media URLs to file paths
        Map<String, String> mediaToFilePathMap = new HashMap<>();
        for (String filePath : filePathList) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            mediaToFilePathMap.put(fileName, filePath);
        }

        // Create a sorted list of file paths based on the media list order
        List<String> sortedFilePathList = new ArrayList<>();
        for (String mediaUrl : mediaList) {
            String mediaFileName = mediaUrl.substring(mediaUrl.lastIndexOf('/') + 1);
            String filePath = mediaToFilePathMap.get(mediaFileName);
            sortedFilePathList.add(filePath);
        }

        return sortedFilePathList;
    }




    private void syncApi(){
//        isRunning = true;
        Log.e("TAG", "syncApi: " );
//        Toast.makeText(HomeScreen.this, "Sync Call", Toast.LENGTH_SHORT).show();

        RequestModel requestModel = new RequestModel(UserId);
        Call<SyncResponseModel> call = ApiController.getInstance().getSyncApiData().getUpdatedData(requestModel);
        call.enqueue(new Callback<SyncResponseModel>() {
            @Override
            public void onResponse(Call<SyncResponseModel> call, Response<SyncResponseModel> response) {
                if (response.isSuccessful() && response.body()!=null){
                    SyncResponseModel syncResponseModel = response.body();
//                    Toast.makeText(HomeScreen.this, "Sync Call", Toast.LENGTH_SHORT).show();
                    if (syncResponseModel.getResultcode() == 200){
                        sharedPrefManager.syncData(syncResponseModel);
                        Log.e("syncData", "onResponse: "+sharedPrefManager.getSyncData().getSync_available());
                        if (sharedPrefManager.getSyncData().getSync_available()!=0){
                            Toast.makeText(HomeScreen.this, "New Content Downloading...", Toast.LENGTH_SHORT).show();
//                            imageUrlList.clear();
//                            videoUrlList.clear();
//                            deleteAllLocalFiles();
                            deleteAllLocalFilesAndFetch(new Runnable() {
                                @Override
                                public void run() {
//                                    mediaUrlList.clear();
//                                    sortedFilePath.clear();
                                    fetchDataFromApi();
                                }
                            });
//                            fetchDataFromApi();
                            Log.e("savedModelForSyncApi", "onResponse: "+sharedPrefManager.getSyncData().getSync_available());
                        }
                        if (sharedPrefManager.getSyncData().getTake_shot()!=0){
                            Toast.makeText(HomeScreen.this,"Screenshot Sent Successfully",Toast.LENGTH_LONG).show();
                            Log.e("Get Screenshot", "onResponse: "+sharedPrefManager.getSyncData().getTake_shot() );
                            if (sharedPrefManager.getFieldsFromSharedPreference()!=null){
                                saveOfferList = sharedPrefManager.getFieldsFromSharedPreference().getOffer();
                                for (Offer screenShot : saveOfferList){
                                    if (screenShot.getOffer_image_path() != null && !screenShot.getOffer_image_path().isEmpty() || screenShot.getOffer_video_path()!=null && !screenShot.getOffer_video_path().isEmpty()) {
                                        Uri imageUri = Uri.parse(screenShot.getOffer_image_path());
                                        Uri videoUri = Uri.parse(screenShot.getOffer_video_path());
                                        String imageName = imageUri.getLastPathSegment();
                                        String videoName = videoUri.getLastPathSegment();
                                        String offerId = screenShot.getOffer_id();
                                        Log.e("Get Screenshot", "onResponse: "+imageUri);
                                        Log.e("Get Screenshot", "onResponse: "+videoUri );
                                        Log.e("Get Screenshot", "onResponse: "+imageName);
                                        Log.e("Get Screenshot", "onResponse: "+videoName);
                                        Log.e("Get Screenshot", "onResponse: "+offerId);
                                        if (imageName!= null && imageName.equals(imageNameLocal)){
                                            Log.e("ImageNameLocal", "onResponse: "+imageNameLocal);
                                            // Retrieve the Android ID
                                            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                                            takeScreenShot(HomeScreen.this,imageName,offerId,UserId,androidId);
                                        }
                                        if (videoName!= null && videoName.equals(videoNameLocal)){
                                            // Retrieve the Android ID
                                            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                                            takeScreenShot(HomeScreen.this,videoName,offerId,UserId,androidId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SyncResponseModel> call, Throwable t) {
                Log.e("error", "onFailure: "+t.getMessage());
            }
        });
    }

    public static void takeScreenShot(Activity activity, String imageName, String offerId, String userId, String androidId){
        View rootView = activity.getWindow().getDecorView().getRootView();

        int desiredWidth = 1920; // Set your desired width in pixels
        int desiredHeight = 1080; // Set your desired height in pixels
        Bitmap screenshot = captureScreen(rootView);
//        Log.e("TAG", "takeScreenShot: "+screenshot);

        // Convert the Bitmap to Base64 along with the offer ID
        ScreenshotResult screenshotResult = convertBitmapToBase64(screenshot, offerId,userId,androidId);

//        convertBitmapToBase64(screenshot);
//        Log.e("TAG", "takeScreenShot: "+screenshot );
        assert screenshotResult != null;
        Log.e("TAG", "takeScreenShot: "+screenshotResult.getAndroidids() );
        Log.e("TAG", "takeScreenShot: "+screenshotResult.getSignage_id() );
        Log.e("TAG", "takeScreenShot: "+screenshotResult.getOffer_id() );
        Log.e("TAG", "takeScreenShot: "+imageName);
        Log.e("TAG", "takeScreenShot: "+screenshotResult.getScreenshot_image());

        Call<ScreenShotResponseModel> call = ApiController.getInstance().getScreenshotData().getScreenShotData(screenshotResult);

        call.enqueue(new Callback<ScreenShotResponseModel>() {
            @Override
            public void onResponse(Call<ScreenShotResponseModel> call, Response<ScreenShotResponseModel> response) {
                ScreenShotResponseModel screenShotResponseModel = response.body();
                if (response.isSuccessful() && response.body()!=null){
                    assert screenShotResponseModel != null;
                    screenShotResponseModel.setResultCode("200");
                    screenShotResponseModel.setResultMessafe("ScreenShot Send Successfully");
                    Log.e("ScreenshotResponse", "onResponse: "+screenShotResponseModel.getResultCode());
                    Log.e("ScreenshotResponse", "onResponse: "+screenShotResponseModel.getResultMessafe());
                }
            }

            @Override
            public void onFailure(Call<ScreenShotResponseModel> call, Throwable t) {
                Log.e("error", "onFailure: "+t.getMessage());
            }
        });

        // Now you can use the 'base64Screenshot' as needed, for example, send it to a server.
        Log.e("TAG", "Base64Screenshot: " + screenshotResult.getOffer_id());
        Log.e("TAG", "Base64Screenshot: " + screenshotResult.getSignage_id());
        Log.e("TAG", "Base64Screenshot: " + screenshotResult.getAndroidids());
        Log.e("TAG", "Base64Screenshot: " + imageName);
//        Log.e("TAG", "Base64Screenshot: " + screenshotResult.getScreenshot_image());

//        saveScreenshot(activity,screenshot,imageName);
    }

    private static Bitmap deepCopyBitmap(Bitmap originalBitmap) {
        if (originalBitmap == null) {
            return null;
        }

        // Create a copy of the original bitmap
        return originalBitmap.copy(originalBitmap.getConfig(), true);
    }

    private static ScreenshotResult convertBitmapToBase64(Bitmap bitmap, String offerId, String userId, String androidId) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Create a deep copy of the original bitmap
        Bitmap copiedBitmap = deepCopyBitmap(bitmap);

        if (copiedBitmap != null) {
            try {
                // Compress the copied bitmap to JPEG format with variable quality
                copiedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                copiedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                // Convert the compressed bitmap to a byte array
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                // Encode the byte array to a base64 string
                String prefix = "data:image/png;base64,";
                String base64Screenshot = prefix + Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Append a timestamp to introduce variability
                String timestamp = String.valueOf(System.currentTimeMillis());
                base64Screenshot += timestamp;

                Log.e("TAG", "convertBitmapToBase64: " + base64Screenshot);

                // Return a new ScreenshotResult instance
                return new ScreenshotResult(base64Screenshot, offerId, userId, androidId);
            } catch (Exception e) {
                // Log any exceptions during the conversion process
                e.printStackTrace();
            } finally {
                try {
                    // Close the ByteArrayOutputStream
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e("TAG", "Copied Bitmap is null");
        }

        // Return null if conversion fails
        return null;
    }


    private static Bitmap captureScreen(View view) {
        // Create a Bitmap with the same dimensions as the view
        Bitmap screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a Canvas using the Bitmap with a slight random offset
        Canvas canvas = new Canvas(screenshot);
        canvas.translate((float) (Math.random() * 10 - 5), (float) (Math.random() * 10 - 5));

        // Draw the view onto the Canvas
        view.draw(canvas);

        return screenshot;
    }





    private void fetchDataFromApi(){
//        Toast.makeText(HomeScreen.this, "Login Successfull", Toast.LENGTH_SHORT).show();
        Log.e("fetchApiCall", "fetchApiCall: 1" );

//        UserId = sharedPrefManager.getUser().getLoc_uid();
        UserId = sharedPrefManager.getSignage();
//        UserId = getIntent().getStringExtra("userId");

        Log.e("userId", "fetchDataFromApi: "+UserId );


        RequestModel requestModel = new RequestModel(UserId);
        Log.e("fetchApiCall", "fetchDataFromApi: "+UserId);

        Call<ResponseModel> call = ApiController.getInstance().getApi().getVideoData(requestModel);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful() && response.body()!=null){
                    Log.e("fetchApiCall", "fetchApiCall: 2" );
                    ResponseModel responseModel = response.body();
                    Log.e("fetchApiCall", "fetchApiCall: 2 --> "+responseModel.getResultcode() );
                    Log.e("fetchApiCall", "fetchApiCall: 2 --> "+responseModel.getResultmsg() );
                    if (responseModel.getResultcode() == 200){
                        Log.e("fetchApiCall", "fetchApiCall: 3" );
                        sharedPrefManager.saveResponseModelInSharedPreference(responseModel);
                        getDataFromSharedPreference();
                    }else if (responseModel.getResultcode() == 400){
                        imageView.setVisibility(View.GONE);
                        videoView.setVisibility(View.GONE);
//                        progressBar.setVisibility(View.VISIBLE);
                        downloadingtextView.setText("Content is not running, Please wait or restart standee after 2 minutes.");
                        downloadingtextView.setVisibility(View.VISIBLE);
//                        logOutUser();
//                        Intent intent = new Intent(HomeScreen.this,LoginActivity.class);
//                        startActivity(intent);
//                        Toast.makeText(HomeScreen.this,responseModel.getResultmsg(),Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(HomeScreen.this,responseModel.getResultmsg(),Toast.LENGTH_LONG).show();
//                        progressBar.setVisibility(View.VISIBLE);
                        downloadingtextView.setText("Waiting for Content Uploading...");
                        downloadingtextView.setVisibility(View.VISIBLE);
                        Log.e("TAG", "onResponse: ");
                    }
                }else {
                    Log.e("error", "onResponse: ");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e("fetchApiCall", "fetchApiCall: fail"+t.getMessage() );
                Log.e("error", "onFailure: " + t.getMessage());
                getDataFromSharedPreference();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataFromSharedPreference();
    }

    public void getDataFromSharedPreference(){
        if (sharedPrefManager.getFieldsFromSharedPreference()!=null){
            Log.e("fetchApiCall", "fetchApiCall: 4" );
            offerList = sharedPrefManager.getFieldsFromSharedPreference().getOffer();
            if (offerList != null){
                Log.e("fetchApiCall", "fetchApiCall: 5" );
                List<String> mediaList = new ArrayList<>();
                for (Offer offer : offerList){
                    if (offer.getOffer_image_path() != null && !offer.getOffer_image_path().isEmpty()) {
                        mediaList.add(offer.getOffer_image_path());
                    }

                    if (offer.getOffer_video_path() != null && !offer.getOffer_video_path().isEmpty()) {
                        mediaList.add(offer.getOffer_video_path());
                    }
                }



                Log.e("startImageLoop", "Media List before Download: "+mediaList );

                int totalDownloads = mediaList.size();
                final int[] downloadsCompleted = {0};


                List<String> mediaUrlList = new ArrayList<>();



                for (String media : mediaList) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        downloadMedia(HomeScreen.this, media, new DownloadCallback() {
                            @Override
                            public void onDownloadComplete(String filePath) {


                                // Increment the count of completed downloads
                                downloadsCompleted[0]++;



                                mediaUrlList.add(filePath);

                                List<String> sortedFilePath = new ArrayList<>();

                                if (downloadsCompleted[0] == totalDownloads){

                                    List<String> sortedFilePathList = sortFilePaths(mediaList, mediaUrlList);

                                    sortedFilePath.addAll(sortedFilePathList);

//                                    Log.e("startImageLoop", "Media List after Download: "+sortedFilePath );
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            Log.e("startImageLoop", "run: "+mediaUrlList );
                                            if (!sortedFilePath.isEmpty()){
//                                                Log.e("startImageLoop", "run: "+mediaUrlList );
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                        progressBar.setVisibility(View.GONE);
                                                        downloadingtextView.setVisibility(View.GONE);
                                                        imageView.setVisibility(View.VISIBLE);
                                                        startLoop(sortedFilePath,offerList);
                                                    }
                                                }, 0);
                                            } else {
//                                                progressBar.setVisibility(View.VISIBLE);
                                                downloadingtextView.setVisibility(View.VISIBLE);
                                                Log.e("startImageLoop", "ImageList and VideoList is empty");
                                            }
                                        }
                                    });
                                }
                                else {
//                                    progressBar.setVisibility(View.VISIBLE);
                                    downloadingtextView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            }else {
                logOutUser();
                Intent intent = new Intent(HomeScreen.this,LoginActivity.class);
                startActivity(intent);
                Toast.makeText(HomeScreen.this,"No Offer Available",Toast.LENGTH_LONG).show();
            }
        }else {
        }
    }


    private Map<String, Integer> buildMediaScreenTimeMap(List<Offer> offers) {
        Map<String, Integer> mediaScreenTimeMap = new HashMap<>();
        for (Offer offer : offers) {
            if (offer.getOffer_image_path() != null && !offer.getOffer_image_path().isEmpty()) {
                mediaScreenTimeMap.put(offer.getOffer_image_path(), Integer.parseInt(offer.getScreen_time()));
            }
            if (offer.getOffer_video_path() != null && !offer.getOffer_video_path().isEmpty()) {
                mediaScreenTimeMap.put(offer.getOffer_video_path(), Integer.parseInt(offer.getScreen_time()));
            }
        }
        return mediaScreenTimeMap;
    }

    private void startLoop(List<String> mediaList, List<Offer> offers) {
        if (mediaList == null || mediaList.isEmpty()) {
            return;
        }

        Log.e("startImageLoop", "Media List in Start Loop: "+mediaList );

        // Build the media screen time map
        Map<String, Integer> mediaScreenTimeMap = buildMediaScreenTimeMap(offers);


        // Start the media loop
        loopMedia(mediaList, mediaScreenTimeMap);
    }


//    private Handler handler1 = new Handler();
    private Runnable mediaRunnable;
    private int currentMediaIndex = 0;

    private void loopMedia(List<String> mediaList, Map<String, Integer> mediaScreenTimeMap) {
        Log.e("startImageLoop", "Media List in Loop Media: " + mediaList);

        currentMediaIndex = 0;
        if (mediaRunnable != null) {
            handler.removeCallbacks(mediaRunnable);
        }

        mediaRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentMediaIndex < mediaList.size()) {
                    String media = mediaList.get(currentMediaIndex);

                    // Display image or play video based on the file extension
                    if (media.endsWith(".jpeg") || media.endsWith(".jpg") || media.endsWith(".png")) {
                        displayImage(media);
                    } else if (media.endsWith(".mp4")) {
                        playVideo(media);
                    }

                    // Find the corresponding screen time in mediaScreenTimeMap
                    String matchedKey = findMatchingUrl(media, mediaScreenTimeMap);
                    Integer delay = mediaScreenTimeMap.get(matchedKey);
                    if (delay == null) {
                        delay = 12; // Default to 12 seconds if screen time is missing
                    }

                    Log.d("MediaLooper", "Displaying media: " + media + " for " + delay + " seconds");

                    currentMediaIndex++;
                    handler.postDelayed(mediaRunnable, delay * 1000); // Convert delay to milliseconds
                } else {
                    // Reset index and start the loop again
                    currentMediaIndex = 0;
                    handler.postDelayed(mediaRunnable, 0);
                }
            }
        };
        handler.postDelayed(mediaRunnable, 0);
    }



    private String findMatchingUrl(String mediaPath, Map<String, Integer> mediaScreenTimeMap) {
        // Assuming the file name is unique, extract it from the local path and match it with the URL
        String fileName = mediaPath.substring(mediaPath.lastIndexOf('/') + 1);

        for (String key : mediaScreenTimeMap.keySet()) {
            if (key.contains(fileName)) {
                return key;
            }
        }

        return null;
    }

    private String extractImageName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        int lastIndex = filePath.lastIndexOf('/');
        if (lastIndex == -1) {
            return filePath; // If there is no '/', the entire filePath is the file name
        }
        return filePath.substring(lastIndex + 1);
    }


    private void displayImage(String imagePath) {

        Log.e("ImagePath", "Image Path: " + imagePath);

        imageNameLocal = extractImageName(imagePath);

//        Log.e("ImagePath", "Image Name: " + imageName);

        videoView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        // Load the image from the file path using Picasso
        Picasso.get()
                .load(new File(imagePath))
                .fit()
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        ViewGroup.LayoutParams params = imageView.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MediaLooper", "Failed to load image: " + imagePath, e);
                    }
                });
    }



    private void playVideo(String videoPath) {
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);


        // Set the video URI
        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);

        // Set media controller for video controls
//        MediaController mediaController = new MediaController(this);
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);

        // Set Z order on top if needed
        videoView.setZOrderOnTop(true);

        // Start the video
        videoView.requestFocus();
        videoView.start();

        // Set completion listener to handle video end
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Handle what to do when video completes
                Log.d("MediaLooper", "Video completed: " + videoPath);
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                // Optionally, you can start the next media here
            }
        });

        // Set error listener to handle video errors
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("MediaLooper", "Error occurred while playing video: " + videoPath + ", Error code: " + what + ", Extra code: " + extra);
                return true; // Returning true indicates we handled the error
            }
        });
    }


    private void stopLoop(){
        // Remove the syncApiRunnable callbacks when stopping the loop
        if (handler != null || runnable != null) {
            assert handler != null;
            handler.removeCallbacks(runnable);
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void logOutUser(){
        stopLoop();
//        isRunning = false;
        sharedPrefManager.logout();
        Intent intent = new Intent(HomeScreen.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}