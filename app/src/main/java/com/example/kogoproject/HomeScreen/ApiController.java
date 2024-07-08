package com.example.kogoproject.HomeScreen;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {
    private static final String BASE_URL = "https://shouut.com/isp_consumer_api/signage_v1/";
    private static Retrofit retrofit;
    private static ApiController apiController;

    public ApiController(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ApiController getInstance(){
        if (apiController==null){
            apiController = new ApiController();
        }
        return apiController;
    }

    ApiInterface getApi(){ return retrofit.create(ApiInterface.class);}

    SyncApiInterface getSyncApiData(){
        return retrofit.create(SyncApiInterface.class);
    }

    ScreenShotApiInterface getScreenshotData(){
        return retrofit.create(ScreenShotApiInterface.class);
    }

}
