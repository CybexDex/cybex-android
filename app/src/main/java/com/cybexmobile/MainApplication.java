package com.cybexmobile;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybexmobile.utils.PicassoUtils;
import com.squareup.leakcanary.LeakCanary;
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
        Picasso picasso = PicassoUtils.getPicassoInstance(this);
        Picasso.setSingletonInstance(picasso);
        if(!LeakCanary.isInAnalyzerProcess(this)){
            LeakCanary.install(this);
        }
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
        String defaultLanguage = Locale.getDefault().getLanguage();
        /**
         * fix bug
         * 第一次安装跟随系统语言 如果系统语言非中文和英文时默认为英文
         */
        if(language.equals("")){
            if(defaultLanguage.equals("en") || defaultLanguage.equals("zh")){
                StoreLanguageHelper.setLanguageLocal(context, defaultLanguage);
                return context;
            }
            language = "en";
            StoreLanguageHelper.setLanguageLocal(context, language);
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }
}
