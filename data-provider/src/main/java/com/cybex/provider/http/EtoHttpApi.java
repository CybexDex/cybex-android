package com.cybex.provider.http;

import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.response.EtoBaseResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EtoHttpApi {

    @GET("cybex/projects")
    Observable<EtoBaseResponse<List<EtoProject>>> getEtoProjects(@Query("limit") int limit,
                                                     @Query("offset") int offset,
                                                     @Query("type") String type);

    @GET("cybex/projects/banner")
    Observable<EtoBaseResponse<List<EtoBanner>>> getEtoBanner();
}
