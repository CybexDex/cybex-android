package com.cybex.provider.http;

import com.cybex.provider.graphene.eva.EvaProject;
import com.cybex.provider.http.entity.Announce;
import com.cybex.provider.http.response.CybexBaseResponse;
import com.squareup.okhttp.RequestBody;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HEAD;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface EvaHttpApi {

    @FormUrlEncoded
    @POST("project/show")
    Observable<CybexBaseResponse<EvaProject>> postEvaProjectInfo(@FieldMap Map<String, String> data);

}
