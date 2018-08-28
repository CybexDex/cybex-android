package com.cybex.provider.apollo;

import com.apollographql.apollo.ApolloClient;
import com.cybex.provider.utils.SSLSocketFactoryUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApolloClientApi {

    private static String BASE_URL = "https://gateway.cybex.io/gateway";

    private ApolloClientApi() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                //.cache(new Cache(10*1024*1024))
                .addInterceptor(interceptor)
                .sslSocketFactory(SSLSocketFactoryUtils.createSSLSocketFactory(), SSLSocketFactoryUtils.createTrustAllManager())//信任所有证书
                .hostnameVerifier(new SSLSocketFactoryUtils.TrustAllHostnameVerifier())
                .build();
        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public static ApolloClient getApolloClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .sslSocketFactory(SSLSocketFactoryUtils.createSSLSocketFactory(), SSLSocketFactoryUtils.createTrustAllManager())//信任所有证书
                .hostnameVerifier(new SSLSocketFactoryUtils.TrustAllHostnameVerifier())
                .build();

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public static ApolloClientApi getInstance() {return ApolloClientProvider.factory;}

    private static class ApolloClientProvider {
        private static final ApolloClientApi factory = new ApolloClientApi();
    }
}
