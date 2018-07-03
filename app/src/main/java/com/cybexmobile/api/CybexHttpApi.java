package com.cybexmobile.api;


import com.cybexmobile.data.AppVersion;
import com.cybexmobile.data.AssetsPairResponse;
import com.cybexmobile.faucet.CnyResponse;
import com.cybexmobile.faucet.CreateAccountRequest;
import com.cybexmobile.faucet.CreateAccountResponse;
import com.cybexmobile.faucet.DepositAndWithdrawObject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface CybexHttpApi {

    @GET("price")
    Flowable<CnyResponse> getCny();

    @GET("Android_update.json")
    Observable<AppVersion> checkAppUpdate();

    @GET("/json/withdraw.json")
    Observable<ResponseBody> getWithdrawList();

    @GET("/json/deposit.json")
    Observable<ResponseBody> getDepositList();

    @GET("/json/deposit_msg.json")
    Observable<ResponseBody> getDepositMsg();

    @GET("/json/withdraw_msg.json")
    Observable<ResponseBody> getWithdrawMsg();

    @GET
    Observable<ResponseBody> getPinCode(@Url String url);

    @POST
    Observable<CreateAccountResponse> register(@Url String url, @Body RequestBody body);

    @GET("market_list")
    Observable<AssetsPairResponse> getAssetsPair(@Query("base") String base);
}
