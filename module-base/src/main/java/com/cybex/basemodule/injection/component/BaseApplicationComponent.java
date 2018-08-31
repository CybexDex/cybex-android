package com.cybex.basemodule.injection.component;

import android.app.Application;
import android.content.Context;

import com.cybex.basemodule.injection.annotation.ApplicationContext;
import com.cybex.basemodule.injection.module.BaseApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = BaseApplicationModule.class)
public interface BaseApplicationComponent {

    @ApplicationContext
    Context context();

    Application application();

}
