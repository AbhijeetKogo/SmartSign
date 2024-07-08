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
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    private Runnable runnable;
    //    ProgressBar progressBar;
    TextView downloadingtextView;
    private final List<String> imageUrlList = new ArrayList<>();
    private final List<String> videoUrlList = new ArrayList<>();

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


        // File does not exist, proceed with the download
        // Create a DownloadManager instance
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        // Create a DownloadManager.Request with the file URL
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mediaUrl));

        // Set the title of the download request
        request.setTitle(getFileNameFromUrl(mediaUrl));

        // Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationUri(Uri.fromFile(file));
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, fileName);

        // Enqueue the download request
        long downloadId = downloadManager.enqueue(request);
        Log.e("DownloadStarted", "DownloadStarted" + downloadId);


        // Increment the total number of downloads
        totalDownloads++;

//        if (totalDownloads == 1) {
//            // Show "Downloading" toast message
//            downloadingToast = Toast.makeText(HomeScreen.this, "Downloading...", Toast.LENGTH_LONG);
//            downloadingToast.show();
//        }


        // Set up a broadcast receiver to listen for download completion
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


    private void deleteAllLocalFiles(List<String> mediaList) {
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                + "/smartSignOnline";

        // Create a File object representing the smartSignOnline directory
        File smartSignOnlineDir = new File(directoryPath);

        // Get all files in the smartSignOnline directory
        File[] files = smartSignOnlineDir.listFiles();

        // Convert mediaList to a Set for efficient lookup
        Set<String> mediaSet = new HashSet<>(mediaList);

        if (files != null) {
            for (File file : files) {
                try {
                    // Check if the file exists and if its name is not in the mediaList
                    if (file.exists() && !mediaSet.contains(file.getName())) {
                        // Delete the file
                        boolean isDeleted = file.delete();
                        if (isDeleted) {
                            Log.i("deleteAllLocalFiles", "File deleted: " + file.getName());
                        } else {
                            Log.e("deleteAllLocalFiles", "Failed to delete file: " + file.getName());
                        }
                    }
                } catch (SecurityException e) {
                    // Handle security exceptions
                    Log.e("deleteAllLocalFiles", "SecurityException: " + e.getMessage());
                } catch (Exception e) {
                    // Handle other exceptions
                    Log.e("deleteAllLocalFiles", "Exception: " + e.getMessage());
                }
            }
        } else {
            Log.i("deleteAllLocalFiles", "No files found in the directory.");
        }
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


//    private void deleteAllLocalFiles() {
//
//        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                + "/smartSignOnline";
//
//        // Create a File object representing the smartSignOnline directory
//        File smartSignOnlineDir = new File(directoryPath);
//
//        // Get all files in the smartSignOnline directory
//        File[] files = smartSignOnlineDir.listFiles();
//
////        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//
//        // Get all files in the Downloads directory
////        File[] files = downloadsDirectory.listFiles();
//
//        if (files != null) {
//            for (File file : files) {
//                // Delete each file
//                boolean isDeleted = file.delete();
//                if (isDeleted) {
//                    Log.i("deleteAllLocalFiles", "File deleted: " + file.getName());
//                } else {
//                    Log.e("deleteAllLocalFiles", "Failed to delete file: " + file.getName());
//                }
//            }
//        } else {
//            Log.i("deleteAllLocalFiles", "No files found in the Downloads directory.");
//        }
////        videoUrlList.clear();
////        imageUrlList.clear();
////        imageView.setVisibility(View.GONE);
////        videoView.setVisibility(View.GONE);
//        fetchDataFromApi();
//    }

    private void getImageData(File imagePath) {
        String base64Image = convertImageToBase64(imagePath);
        if (base64Image != null) {
            Log.e("TAG", "Base64 Image: " + base64Image);
        } else {
            Log.e("TAG", "Failed to convert image to base64");
        }
    }

    private static String convertImageToBase64(File imagePath) {
        try {
            // Read the image file into a Bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Adjust as needed
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), options);

            // Determine the original image format
            Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
            String extension = MimeTypeMap.getFileExtensionFromUrl(imagePath.getAbsolutePath());
            if (extension != null) {
                extension = extension.toLowerCase();
                if (extension.equals("png")) {
                    compressFormat = Bitmap.CompressFormat.PNG;
                }
                // Add more conditions for other image formats if needed
            }

            // Convert the Bitmap to a byte array with the original format
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(compressFormat, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // Encode the byte array to a base64 string
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            return base64Image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
                            imageUrlList.clear();
                            videoUrlList.clear();
//                            deleteAllLocalFiles();
                            deleteAllLocalFilesAndFetch(new Runnable() {
                                @Override
                                public void run() {
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
                                        if (imageName!= null && imageName.equals(imageNameLocal)){
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


    private static void saveScreenshot(Context context, Bitmap screenshot,String imageName) {
        if (screenshot == null) {
            return;
        }

        // Get the directory to save the screenshot
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Ensure that the image name includes a file extension
        if (!imageName.contains(".")) {
            imageName += ".png";  // Default to PNG if no extension is provided
        }

        // Generate a unique filename based on the original image name and a timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String uniqueImageName = "screenshot_" + timestamp + "_" + imageName;

        File screenshotFile = new File(directory, uniqueImageName);

        try {
            // Save the screenshot to the file
            FileOutputStream outputStream = new FileOutputStream(screenshotFile);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Log the file path
            Log.e("TAG", "Saved screenshot to: " + screenshotFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Notify the media scanner to recognize the new file
        MediaScannerConnection.scanFile(context,
                new String[]{screenshotFile.getAbsolutePath()},
                null,
                null);
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
                List<String> imageList = new ArrayList<>();
                List<String> videoList = new ArrayList<>();
                List<String> mediaList = new ArrayList<>();
                for (Offer offer : offerList){
                    if (offer.getOffer_image_path()!=null && !offer.getOffer_image_path().isEmpty()){
                        imageList.add(offer.getOffer_image_path());
                    }
                    if (offer.getOffer_video_path()!=null && !offer.getOffer_video_path().isEmpty()){
                        videoList.add(offer.getOffer_video_path());
                    }
                }
                mediaList.addAll(imageList);
                mediaList.addAll(videoList);

//                deleteAllLocalFiles(mediaList);
                Log.e("imageList", "onResponse: "+imageList );
                Log.e("videoList", "onResponse: "+videoList );
                Log.e("MediaList", "onResponse: "+mediaList );

                int totalDownloads = mediaList.size();
                final int[] downloadsCompleted = {0};

                for (String mediaUrl : mediaList) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        downloadMedia(HomeScreen.this, mediaUrl, new DownloadCallback() {
                            @Override
                            public void onDownloadComplete(String filePath) {
                                // Increment the count of completed downloads
                                downloadsCompleted[0]++;
                                // Check if the downloaded file is an image or video
                                if (filePath.endsWith(".mp4")) {
                                    videoUrlList.add(filePath);
                                } else {
                                    imageUrlList.add(filePath);
                                }
                                if (downloadsCompleted[0] == totalDownloads){
                                    Log.e("imageList", "onDownloadComplete: "+imageUrlList );
                                    Log.e("videoList", "onDownloadComplete: "+videoUrlList );
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!imageUrlList.isEmpty()){
                                                Log.e("startImageLoop", "run: "+imageUrlList );
                                                Log.e("startImageLoop", "run: "+videoUrlList );
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                        progressBar.setVisibility(View.GONE);
                                                        downloadingtextView.setVisibility(View.GONE);
                                                        imageView.setVisibility(View.VISIBLE);
                                                        startImageLoop(imageUrlList);
                                                    }
                                                }, 0);
                                            } else if (!videoUrlList.isEmpty()) {
                                                Log.e("startImageLoop", "run: "+imageUrlList );
                                                Log.e("startImageLoop", "run: "+videoUrlList );
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                        progressBar.setVisibility(View.GONE);
                                                        downloadingtextView.setVisibility(View.GONE);
                                                        videoView.setVisibility(View.VISIBLE);
                                                        startVideoLoop(videoUrlList);
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

    int currentImageIndex = 0 ;
    int currentVideoIndex = 0 ;


    private Runnable imageRunnable;

    private void startImageLoop(List<String> imageList) {
        Log.e("imageList", "startImageLoop: " + imageList);
        currentImageIndex = 0;
        if (imageRunnable != null) {
            handler.removeCallbacks(imageRunnable);
        }
        imageRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e("imageList", "run: imageList1");
                if (currentImageIndex < imageList.size()) {
                    Log.e("imageList", "run: imageList2");
                    String filePath = imageList.get(currentImageIndex);
                    imageView.setVisibility(View.VISIBLE);
                    loadImage(filePath);

                    currentImageIndex++;
                    handler.postDelayed(imageRunnable, 12000); // Change this line
                } else {
                    Log.e("imageList", "run: Call from else block1");
                    if (videoUrlList.isEmpty()) {
                        currentImageIndex = 0; // Reset the image index
                        startImageLoop(imageList); // Start the image loop again
                    } else {
                        Log.e("imageList", "run: Call from else block2");
                        imageView.setVisibility(View.GONE);
                        startVideoLoop(videoUrlList);
                    }
                }
            }
        };
        handler.postDelayed(imageRunnable, 0);
    }

    private void loadImage(String imageUrl) {
        File file = new File(imageUrl);
        imageNameLocal = file.getName();
        Log.e("imageInLoadMethod", "loadImage: " + imageUrl);
        Picasso.get()
                .load(new File(imageUrl))
                .fit()
                .into(imageView);
    }


    private Runnable videoRunnable;

    private void startVideoLoop(List<String> videoList) {
        Log.e("video", "startVideoLoop: "+videoList );
        currentVideoIndex = 0;
        if (videoRunnable != null) {
            handler.removeCallbacks(videoRunnable);
        }
//        Handler handler = new Handler(Looper.getMainLooper());
        videoRunnable = new Runnable() {
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
                            startImageLoop(imageUrlList);
                        } else {
                            handler.postDelayed(videoRunnable, 0);
                        }
                    }
                });

                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e("video", "Error occurred while playing video: " + videoUrl + ", Error code: " + what + ", Extra code: " + extra);
//                        Toast.makeText(HomeScreen.this, "Can't play this video", Toast.LENGTH_SHORT).show();
                        return true; // Returning true indicates we handled the error
                    }
                });
            }
        };
        handler.postDelayed(videoRunnable, 0);
    }

    private void loadVideo(String videoUrl) {
        File file = new File(videoUrl);
        videoNameLocal = file.getName();
        Log.e("image", "loadVideo: "+videoUrl );
        Uri uri = Uri.parse(videoUrl);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }

//
//    private void startVideoLoop(List<String> videoList) {
//        Log.e("video", "startVideoLoop: " + videoList);
//        currentVideoIndex = 0;
//        if (videoRunnable != null) {
//            handler.removeCallbacks(videoRunnable);
//        }
//
//        videoRunnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.e("video", "run: 1");
//                if (videoList.isEmpty()) {
//                    Log.e("video", "No videos to play.");
//                    return;
//                }
//
//                String videoUrl = videoList.get(currentVideoIndex);
//                videoView.setVisibility(View.VISIBLE);
//                videoView.setZOrderOnTop(true);
//                loadVideo(videoUrl);
//
//                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        Log.e("video", "run: 2");
//                        currentVideoIndex++;
//
//                        if (currentVideoIndex >= videoList.size()) {
//                            currentVideoIndex = 0;
//                            // Video loop is completed, start image loop
//                            videoView.setVisibility(View.GONE);
//                            imageView.setVisibility(View.VISIBLE);
//                            startImageLoop(imageUrlList);
//                        } else {
//                            handler.postDelayed(videoRunnable, 0);
//                        }
//                    }
//                });
//
//                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                    @Override
//                    public boolean onError(MediaPlayer mp, int what, int extra) {
//                        Log.e("video", "Error occurred while playing video: " + videoUrl + ", Error code: " + what + ", Extra code: " + extra);
//                        Toast.makeText(HomeScreen.this, "Can't play this video", Toast.LENGTH_SHORT).show();
//                        return true; // Returning true indicates we handled the error
//                    }
//                });
//            }
//        };
//        handler.postDelayed(videoRunnable, 0);
//    }
//
//    private void loadVideo(String videoUrl) {
//        File file = new File(videoUrl);
//        videoNameLocal = file.getName();
//        Log.e("image", "loadVideo: " + videoUrl);
//        Uri uri = Uri.fromFile(file); // Use Uri.fromFile for local files
//        videoView.setVideoURI(uri);
//        videoView.requestFocus();
//        videoView.start();
//    }


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