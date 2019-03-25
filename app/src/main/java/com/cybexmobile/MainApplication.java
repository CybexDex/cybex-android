package com.cybexmobile;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;

import com.alibaba.android.arouter.launcher.ARouter;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.help.StoreLanguageHelper;
import com.cybex.provider.apollo.ApolloClientApi;
import com.cybex.provider.graphene.chain.FullNodeServerSelect;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.utils.PicassoUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import io.enotes.sdk.core.ENotesSDK;

import static com.cybex.basemodule.constant.Constant.PREF_SERVER;
import static com.cybex.basemodule.constant.Constant.SERVER_OFFICIAL;

public class MainApplication extends Application {

    private static MainApplication mMainApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mMainApplication = this;
        String server = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_SERVER, SERVER_OFFICIAL);
        RetrofitFactory.getInstance().setOfficialServer(server.equals(SERVER_OFFICIAL));
        ApolloClientApi.getInstance().setOfficialServer(server.equals(SERVER_OFFICIAL));
        FullNodeServerSelect.getInstance().setOfficialServer(server.equals(SERVER_OFFICIAL));
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Picasso picasso = PicassoUtils.getPicassoInstance(this);
        Picasso.setSingletonInstance(picasso);
//        if(!LeakCanary.isInAnalyzerProcess(this)){
//            LeakCanary.install(this);
//        }
        ARouter.init(this);
        ENotesSDK.config.debugCard = true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateResources(base));
        MultiDex.install(this);
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

    public static MainApplication getContext() {
        return mMainApplication;
    }
}
