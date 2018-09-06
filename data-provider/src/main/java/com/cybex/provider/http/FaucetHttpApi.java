package com.cybex.provider.http;

import com.cybex.provider.http.response.CreateAccountResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FaucetHttpApi {

    @GET("captcha")
    Observable<ResponseBody> getPinCode();

    @POST("register")
    Observable<CreateAccountResponse> register(@Body RequestBody body);

}
