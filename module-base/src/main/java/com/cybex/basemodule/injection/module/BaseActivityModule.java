package com.cybex.basemodule.injection.module;

import android.app.Activity;
import android.content.Context;

import com.cybex.basemodule.injection.annotation.ActivityContext;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseActivityModule {

    private Activity mActivity;

    public BaseActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @ActivityContext
    Context providesContext() {
        return mActivity;
    }
}
