package com.cybex.provider.http;

import com.cybex.provider.http.response.GateWayRecordsResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GatewayHttpApi {

    @POST("login")
    Observable<ResponseBody> gatewayLogIn(@Body RequestBody body);

    @POST("records")
    Observable<GateWayRecordsResponse> gatewayRecords(@Body RequestBody body);
}
