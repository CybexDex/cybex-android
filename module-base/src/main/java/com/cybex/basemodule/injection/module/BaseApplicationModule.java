package com.cybex.basemodule.injection.module;

import android.app.Application;
import android.content.Context;

import com.cybex.basemodule.injection.annotation.ApplicationContext;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseApplicationModule {

    protected final Application mApplication;

    public BaseApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }
}
