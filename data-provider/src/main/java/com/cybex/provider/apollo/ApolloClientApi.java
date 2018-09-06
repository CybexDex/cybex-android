package com.cybex.provider.apollo;

import com.apollographql.apollo.ApolloClient;
import com.cybex.provider.utils.SSLSocketFactoryUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApolloClientApi {

    //apollo正式服务器
    private static String BASE_URL = "https://gateway.cybex.io/gateway";
    //apollo测试服务器
    public static final String BASE_URL_TEST = "https://gatewaytest.cybex.io/gateway/";

    private OkHttpClient okHttpClient;
    private ApolloClient apolloClient;

    //是否是正式服务器环境
    public boolean isOfficialServer = true;

    private ApolloClientApi() {
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

    public ApolloClient client(){
        if(okHttpClient == null){
            throw new RuntimeException("must getInstance before client");
        }
        if(apolloClient == null){
            apolloClient = ApolloClient.builder()
                    .serverUrl(isOfficialServer ? BASE_URL : BASE_URL_TEST)
                    .okHttpClient(okHttpClient)
                    .build();
        }
        return apolloClient;
    }

    public static ApolloClientApi getInstance() {return ApolloClientProvider.factory;}

    private static class ApolloClientProvider {
        private static final ApolloClientApi factory = new ApolloClientApi();
    }

    public void setOfficialServer(boolean officialServer) {
        isOfficialServer = officialServer;
    }
}
