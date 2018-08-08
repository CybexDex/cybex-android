package com.cybexmobile.api;

import com.cybexmobile.data.AppVersion;
import com.cybexmobile.data.AssetsPairResponse;
import com.cybexmobile.data.AssetsPairToppingResponse;
import com.cybexmobile.data.GateWayRecordsResponse;
import com.cybexmobile.faucet.CnyResponse;
import com.cybexmobile.faucet.CreateAccountResponse;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    @GET
    Observable<ResponseBody> getPinCode(@Url String url);

    @POST
    Observable<CreateAccountResponse> register(@Url String url, @Body RequestBody body);

    @GET("market_list")
    Observable<AssetsPairResponse> getAssetsPair(@Query("base") String base);

    @GET("json/marketlists.json")
    Observable<List<AssetsPairToppingResponse>> getAssetsPairTopping();

    @POST
    Observable<ResponseBody> gatewayLogIn(@Url String url, @Body RequestBody body);

    @POST
    Observable<GateWayRecordsResponse> gatewayRecords(@Url String url, @Body RequestBody body);
}
