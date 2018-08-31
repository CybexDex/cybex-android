package com.cybex.provider.http;

import com.cybex.provider.utils.SSLSocketFactoryUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    public static final String baseUrl = "https://app.cybex.io/";
    public static final String url_pin_code = "https://faucet.cybex.io/captcha";
    public static final String url_register = "https://faucet.cybex.io/register";
    public static final String url_deposit_withdraw_log_in = "https://gateway-query.cybex.io/login";
    public static final String url_deposit_withdraw_records = "https://gateway-query.cybex.io/records";

    //Eto测试服务器
    public static final String baseUrlEto_test = "https://ieo-apitest.cybex.io/api/";
    //Eto正式服务器
    public static final String baseUrlEto = "https://eto.cybex.io/api/";

    private OkHttpClient okHttpClient;

    private CybexHttpApi cybexHttpApi;
    private EtoHttpApi etoHttpApi;

    private RetrofitFactory(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                //.cache(new Cache(10*1024*1024))
                .addInterceptor(interceptor)
                .sslSocketFactory(SSLSocketFactoryUtils.createSSLSocketFactory(), SSLSocketFactoryUtils.createTrustAllManager())//信任所有证书
                .hostnameVerifier(new SSLSocketFactoryUtils.TrustAllHostnameVerifier())
                .build();
    }
    
    public static RetrofitFactory getInstance() {
        return RetrofitFactoryProvider.factory;
    }

    private static class RetrofitFactoryProvider{
        private static final RetrofitFactory factory = new RetrofitFactory();
    }

    public CybexHttpApi api(){
        if(cybexHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            cybexHttpApi = retrofit.create(CybexHttpApi.class);
        }
        return cybexHttpApi;
    }

    public EtoHttpApi apiEto() {
        if(etoHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrlEto_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            etoHttpApi = retrofit.create(EtoHttpApi.class);
        }
        return etoHttpApi;
    }


}
