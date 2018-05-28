package com.cybexmobile.api;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GetCnyInterface {
    @GET("price")
    Call<ResponseBody> getCny();
}
