package com.cybex.provider.http;

import com.cybex.provider.http.entity.Announce;
import com.cybex.provider.http.entity.AppVersion;
import com.cybex.provider.http.entity.CybexBanner;
import com.cybex.provider.http.entity.HotAssetPair;
import com.cybex.provider.http.entity.SubLink;
import com.cybex.provider.http.response.AppConfigResponse;
import com.cybex.provider.http.response.AssetsPairResponse;
import com.cybex.provider.http.response.AssetsPairToppingResponse;
import com.cybex.provider.http.response.CnyResponse;
import com.cybex.provider.http.response.CybexBaseResponse;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MainHttpApi {

    @GET("v1/api/announce")
    Observable<CybexBaseResponse<List<Announce>>> getAnnounces(@Query("lang") String lang);

    @GET("v1/api/hotpair")
    Observable<CybexBaseResponse<List<HotAssetPair>>> getHotAssetPairs();

    @GET("v1/api/app_sublinks")
    Observable<CybexBaseResponse<List<SubLink>>> getSubLinks(@Query("lang") String lang, @Query("env") String env);

    @GET("v1/api/banners")
    Observable<CybexBaseResponse<List<CybexBanner>>> getBanners(@Query("lang") String lang);

}
