package com.cybex.provider.http;

import com.cybex.provider.http.gateway.entity.GatewayNewAssetListResponse;
import com.cybex.provider.http.gateway.entity.GatewayNewAssetsInfoResponse;
import com.cybex.provider.http.gateway.entity.GatewayNewRecordsResponse;
import com.cybex.provider.http.response.GateWayAssetInRecordsResponse;
import com.cybex.provider.http.response.GateWayRecordsResponse;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
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

    @GET("v1/assets")
    Observable<GatewayNewAssetListResponse> getAssetList(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization
    );

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

    @GET("v1/users/{user}/records")
    Observable<GatewayNewRecordsResponse> getDepositWithdrawRecordNewGateway(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("user") String user,
            @Query("size") int size,
            @Query("offset") int offset,
            @Query("asset") String assetName,
            @Query("fundType") String fundType);

    @GET("account-assets/{userAccount}")
    Observable<GateWayAssetInRecordsResponse> getDepositWithdrawAsset(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("userAccount") String userAccount,
            @Query("asset") String assetName,
            @Query("fundType") String fundType);

    @GET("v1/users/{user}/assets")
    Observable<GatewayNewAssetsInfoResponse> getAssetInfo(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("user") String user);

    @GET("v1/users/{user}/assets/{asset}/address")
    Observable<JsonObject> getDepositAddress(
            @Header("Content-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("user") String user,
            @Path("asset") String asset);

    @GET("v1/assets/{asset}/address/{address}/verify")
    Observable<JsonObject> verifyAddress(
            @Header("Contetn-Type") String contentType,
            @Header("Authorization") String authorization,
            @Path("asset") String asset,
            @Path("address") String address);
}
