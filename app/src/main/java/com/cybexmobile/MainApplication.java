package com.cybexmobile;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.cybexmobile.helper.StoreLanguageHelper;
import com.cybexmobile.utils.SSLSocketFactoryUtils;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Picasso picasso = SSLSocketFactoryUtils.getPicassoInstance(this);
        Picasso.setSingletonInstance(picasso);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateResources(base));
    }

    private Context updateResources(Context context) {
        String language = StoreLanguageHelper.getLanguageLocal(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }
}
