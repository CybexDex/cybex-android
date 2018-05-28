package com.cybexmobile.api;

import retrofit2.Retrofit;

public class RetrofitApi {
    private static Retrofit retrofit = null;
    public static GetCnyInterface getCnyInterface() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://app.cybex.io/")
                    .build();
        }
        return retrofit.create(GetCnyInterface.class);
    }
}
