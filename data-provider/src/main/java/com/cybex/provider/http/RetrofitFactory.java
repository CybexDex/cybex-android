package com.cybex.provider.http;

import com.cybex.provider.utils.SSLSocketFactoryUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    //cybex正式服务器
    public static final String cybex_base_url = "https://app.cybex.io/";
    //cybex测试服务器
    public static final String cybex_base_url_test = "http://47.91.242.71:3039/";
    //faucet正式服务器
    public static final String faucet_base_url = "https://faucet.cybex.io/";
    //faucet测试服务器
    public static final String faucet_base_url_test = "https://faucet.51nebula.com/";
    //网关测试正式服务器
    public static final String gateway_base_url = "https://gateway-query.cybex.io/";
    //网关测试测试服务器 暂无
    public static final String gateway_base_url_test = "https://gateway-query.cybex.io/";
    //Eto正式服务器
    public static final String eto_base_url = "https://eto.cybex.io/api/";
    //Eto测试服务器
    public static final String eto_base_url_test = "https://ieo-apitest.cybex.io/api/";

    private OkHttpClient okHttpClient;

    private CybexHttpApi cybexHttpApi;
    private FaucetHttpApi faucetHttpApi;
    private GatewayHttpApi gatewayHttpApi;
    private EtoHttpApi etoHttpApi;

    //是否是正式服务器环境
    public boolean isOfficialServer = true;

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
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before api");
        }
        if(cybexHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? cybex_base_url : cybex_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            cybexHttpApi = retrofit.create(CybexHttpApi.class);
        }
        return cybexHttpApi;
    }

    public GatewayHttpApi apiGateway(){
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiGateway");
        }
        if(gatewayHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? gateway_base_url : gateway_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            gatewayHttpApi = retrofit.create(GatewayHttpApi.class);
        }
        return gatewayHttpApi;
    }

    public FaucetHttpApi apiFaucet(){
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiFaucet");
        }
        if(faucetHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? faucet_base_url : faucet_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            faucetHttpApi = retrofit.create(FaucetHttpApi.class);
        }
        return faucetHttpApi;
    }

    public EtoHttpApi apiEto() {
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiEto");
        }
        if(etoHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? eto_base_url : eto_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            etoHttpApi = retrofit.create(EtoHttpApi.class);
        }
        return etoHttpApi;
    }

    public void setOfficialServer(boolean officialServer) {
        isOfficialServer = officialServer;
    }
}
