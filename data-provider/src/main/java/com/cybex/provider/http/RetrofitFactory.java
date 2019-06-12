package com.cybex.provider.http;

import com.cybex.provider.utils.SSLSocketFactoryUtils;
import com.cybex.provider.websocket.WebSocketNodeConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    //cybex正式服务器
    private static final String cybex_base_url = "https://app.cybex.io/";
    //cybex测试服务器
    private static final String cybex_base_url_test = "http://47.100.98.113:3039/";
    //faucet正式服务器
    private static final String faucet_base_url = "https://faucet.cybex.io/";
    //faucet测试服务器
    private static final String faucet_base_url_test = "https://faucet.51nebula.com/";
    //网关测试正式服务器
    private static final String gateway_base_url = "https://gateway-query.cybex.io/";
    //网关测试测试服务器 暂无
    private static final String gateway_base_url_test = "https://gateway-query.cybex.io/";
    //Eto正式服务器
    private static final String eto_base_url = "https://etoapi.cybex.io/api/";
    //Eto测试服务器
    private static final String eto_base_url_test = "https://etoapi.cybex.io/api/";
    //Chat正式服务器
    private static final String chat_base_url = "https://chat.cybex.io/";
    //Chat测式服务器
    private static final String chat_base_url_test = "http://47.91.242.71:9099/";

    private static final String eva_base_url = "https://api.evaluape.io/";

    private static final String cybex_live_base_url = "https://live.cybex.io/";
    private static final String cybex_live_base_url_test = "https://cybtestbrowser.nbltrust.com/";

    private OkHttpClient okHttpClient;

    private CybexHttpApi cybexHttpApi;
    private MainHttpApi mainHttpApi;
    private FaucetHttpApi faucetHttpApi;
    private GatewayHttpApi gatewayHttpApi;
    private EtoHttpApi etoHttpApi;
    private ChatHttpApi chatHttpApi;
    private EvaHttpApi evaHttpApi;
    private CybexLiveHttpApi cybexLiveHttpApi;

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

    public MainHttpApi apiMain(){
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before api");
        }
        if(mainHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? cybex_base_url : cybex_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            mainHttpApi = retrofit.create(MainHttpApi.class);
        }
        return mainHttpApi;
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
                    .baseUrl(isOfficialServer ? WebSocketNodeConfig.getInstance().getEto() != null ? WebSocketNodeConfig.getInstance().getEto() : eto_base_url : eto_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            etoHttpApi = retrofit.create(EtoHttpApi.class);
        }
        return etoHttpApi;
    }

    public ChatHttpApi apiChat() {
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiEto");
        }
        if(chatHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? chat_base_url : chat_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            chatHttpApi = retrofit.create(ChatHttpApi.class);
        }
        return chatHttpApi;
    }

    public EvaHttpApi apiEva() {
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiEto");
        }
        if(evaHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? eva_base_url : eva_base_url)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            evaHttpApi = retrofit.create(EvaHttpApi.class);
        }
        return evaHttpApi;
    }

    public CybexLiveHttpApi apiCybexLive() {
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before apiEto");
        }
        if(cybexLiveHttpApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(isOfficialServer ? cybex_live_base_url : cybex_live_base_url_test)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            cybexLiveHttpApi = retrofit.create(CybexLiveHttpApi.class);
        }
        return cybexLiveHttpApi;
    }

    public void setOfficialServer(boolean officialServer) {
        isOfficialServer = officialServer;
    }
}
