package com.example.kogoproject.HomeScreen;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SyncApiInterface {
    @Headers({
            "Content-Type: application/json",
            "Authorization: bcaf75d0a47bad12faa1272e3d2e68a5=="
    })

    @POST("signage_sync")
    Call<SyncResponseModel> getUpdatedData(
            @Body RequestModel requestModel
    );
}
