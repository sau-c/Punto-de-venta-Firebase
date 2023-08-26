package com.example.puntodeventa.models;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IGoogleSheets {
    @GET
    Call<String> getAccesorio(@Url String url);

    @POST("exec")
    Call<String> getStringRequestBody(@Body String body);
}

