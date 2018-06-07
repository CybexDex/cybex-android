package com.cybexmobile.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    public static final String baseUrl = "https://app.cybex.io/";
    public static final String url_pin_code = "https://faucet.cybex.io/";
    public static final String url_register = "https://faucet.cybex.io/register";

    private CybexHttpApi cybexHttpApi;

    private RetrofitFactory(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                //.cache(new Cache(10*1024*1024))
                .addInterceptor(interceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        cybexHttpApi = retrofit.create(CybexHttpApi.class);
    }
    
    public static RetrofitFactory getInstance() {
        return RetrofitFactoryProvider.factory;
    }

    private static class RetrofitFactoryProvider{
        private static final RetrofitFactory factory = new RetrofitFactory();
    }

    public CybexHttpApi api(){
        return cybexHttpApi;
    }

}
