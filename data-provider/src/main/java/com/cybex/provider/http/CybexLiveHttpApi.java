package com.cybex.provider.http;

import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.squareup.okhttp.ResponseBody;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CybexLiveHttpApi {

    @GET("get_ops_fill_pair")
    Observable<List<AccountHistoryObject>> getExchangeRecords(@Query("account") String account,
                                                                                               @Query("page") int page,
                                                                                               @Query("limit") int limit,
                                                                                               @Query("base") String base,
                                                                                               @Query("quote") String quote,
                                                                                               @Query("start") String start,
                                                                                               @Query("end") String end);

    @GET("get_ops_by_transfer_accountspair_mongo")
    Observable<List<AccountHistoryObject>> getTransferRecords(@Query("asset") String asset,
                                                                               @Query("page") int page,
                                                                               @Query("limit") int limit,
                                                                               @Query("acct_from") String acct_from,
                                                                               @Query("acct_to") String acct_to);
    @GET("get_fill_bypair")
    Observable<List<Object>> getTransactionRecords(@Query("account") String account,
                                                         @Query("page") int page,
                                                         @Query("limit") int limit,
                                                         @Query("start") String start,
                                                         @Query("end") String end,
                                                         @Query(value = "filter_in", encoded = true) String filerIn,
                                                         @Query(value = "filter_out", encoded = true) String filterOut);

}
