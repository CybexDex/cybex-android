package com.cybex.provider.http;

import com.cybex.provider.http.response.GateWayAssetInRecordsResponse;
import com.cybex.provider.http.response.GateWayRecordsResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GatewayHttpApi {

    @POST("login")
    Observable<ResponseBody> gatewayLogIn(@Body RequestBody body);

    @POST("records")
    Observable<GateWayRecordsResponse> gatewayRecords(@Body RequestBody body);

    @GET("records/{userAccount}")
    Observable<GateWayRecordsResponse> getDepositWithdrawRecords(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("userAccount") String userAccount,
            @Query("size") int size,
            @Query("offset") int offset,
            @Query("asset") String assetName,
            @Query("fundType") String fundType,
            @Query("groupByAsset") boolean isGroupByAsset,
            @Query("groupByFundType") boolean isGroupByFundType);

    @GET("account-assets/{userAccount}")
    Observable<GateWayAssetInRecordsResponse> getDepositWithdrawAsset(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("userAccount") String userAccount,
            @Query("asset") String assetName,
            @Query("fundType") String fundType);
}
