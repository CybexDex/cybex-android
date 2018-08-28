package com.cybexmobile.utils;

import com.cybex.provider.utils.SSLSocketFactoryUtils;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class PicassoUtils {

    public static Picasso getPicassoInstance(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .sslSocketFactory(SSLSocketFactoryUtils.createSSLSocketFactory(), SSLSocketFactoryUtils.createTrustAllManager())//信任所有证书
                .hostnameVerifier(new SSLSocketFactoryUtils.TrustAllHostnameVerifier())
                .build();
        return new Picasso.Builder(context).downloader(new OkHttp3Downloader(okHttpClient)).build();
    }
}
