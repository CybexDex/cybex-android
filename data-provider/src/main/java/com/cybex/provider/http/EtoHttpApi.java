package com.cybex.provider.http;

import com.cybex.provider.http.entity.EtoBanner;
import com.cybex.provider.http.entity.EtoErrorMsgResponse;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoUserCurrentStatus;
import com.cybex.provider.http.entity.EtoProjectStatus;
import com.cybex.provider.http.entity.EtoUserStatus;
import com.cybex.provider.http.entity.EtoRecordPage;
import com.cybex.provider.http.response.EtoBaseResponse;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EtoHttpApi {

    @GET("cybex/projects")
    Observable<EtoBaseResponse<List<EtoProject>>> getEtoProjects(@Query("limit") int limit,
                                                     @Query("offset") int offset,
                                                     @Query("type") String type);

    @GET("cybex/projects/banner")
    Observable<EtoBaseResponse<List<EtoBanner>>> getEtoBanner(@Query("client") String client);

    @GET("cybex/trade/list")
    Observable<EtoBaseResponse<EtoRecordPage>> getEtoRecords(@Query("cybex_name") String account,
                                                             @Query("page") int page,
                                                             @Query("limit") int limit);

    @GET("cybex/project/detail")
    Observable<EtoBaseResponse<EtoProject>> getEtoProjectDetails(@Query("project") String id);

    @GET("cybex/user/check_status")
    Flowable<EtoBaseResponse<EtoUserStatus>> getEtoUserStatus(@Query("cybex_name") String name,
                                                                @Query("project") String id);

    @GET("cybex/user/current")
    Flowable<EtoBaseResponse<EtoUserCurrentStatus>> getUserCurrent(@Query("cybex_name") String name,
                                                                   @Query("project") String id);
    @POST("cybex/user/create")
    Observable<EtoBaseResponse<EtoErrorMsgResponse>> createETO(@Body RequestBody body);

    @GET("cybex/project/current")
    Flowable<EtoBaseResponse<EtoProjectStatus>> refreshProjectStatus(@Query("project") String id);
}
