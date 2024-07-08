package com.example.kogoproject.LoginScreen;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {
    private static final String BASE_URL = "https://shouut.com/isp_consumer_api/signage_v1/";
    private static Retrofit retrofit;
    private static ApiController clientObject;

    public ApiController(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ApiController getInstance(){
        if (clientObject==null)
            clientObject = new ApiController();
        return clientObject;

    }

    ApiInterface getApi(){
        return retrofit.create(ApiInterface.class);
    }

}
