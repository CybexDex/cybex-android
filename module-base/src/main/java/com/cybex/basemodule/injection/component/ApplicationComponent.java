package com.cybex.basemodule.injection.component;

import android.app.Application;
import android.content.Context;

import com.cybex.basemodule.injection.annotation.ApplicationContext;
import com.cybex.basemodule.injection.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    @ApplicationContext
    Context context();

    Application application();

}
