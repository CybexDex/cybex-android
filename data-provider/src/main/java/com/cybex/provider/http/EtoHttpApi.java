package com.cybex.provider.http;

import com.cybex.provider.http.response.EtoProjectResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EtoHttpApi {

    @GET("cybex/projects")
    Observable<List<EtoProjectResponse>> getEtoProjects(@Query("limit") int limit,
                                                        @Query("offset") int offset,
                                                        @Query("type") String type);
}
