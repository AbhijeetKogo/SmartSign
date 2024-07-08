package com.example.kogoproject.LoginScreen;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers({
            "Content-Type: application/json",
            "Authorization: bcaf75d0a47bad12faa1272e3d2e68a5=="
    })

    @POST("signage_login")
    Call<LoginResponseModel> getUserData(
            @Body LoginRequestModel loginRequestModel
    );
}
