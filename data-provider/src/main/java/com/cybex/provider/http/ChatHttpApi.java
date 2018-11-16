package com.cybex.provider.http;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ChatHttpApi {

    @GET("lastestMsgID")
    Observable<Integer> getLastMsgID(@Query("channel") String channel);

}
