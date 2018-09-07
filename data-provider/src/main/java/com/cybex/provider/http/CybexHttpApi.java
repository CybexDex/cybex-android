package com.cybex.provider.http;

import com.cybex.provider.http.entity.AppVersion;
import com.cybex.provider.http.response.AppConfigResponse;
import com.cybex.provider.http.response.AssetsPairResponse;
import com.cybex.provider.http.response.AssetsPairToppingResponse;
import com.cybex.provider.http.response.GateWayRecordsResponse;
import com.cybex.provider.http.response.CnyResponse;
import com.cybex.provider.http.response.CreateAccountResponse;

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

    @GET("market_list")
    Observable<AssetsPairResponse> getAssetsPair(@Query("base") String base);

    @GET("json/marketlists.json")
    Observable<List<AssetsPairToppingResponse>> getAssetsPairTopping();

    @GET("json/settings.json")
    Observable<AppConfigResponse> getSettingConfig();

}
