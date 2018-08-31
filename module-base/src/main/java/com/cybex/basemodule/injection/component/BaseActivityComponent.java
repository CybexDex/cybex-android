package com.cybex.basemodule.injection.component;


import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.injection.annotation.PerActivity;
import com.cybex.basemodule.injection.module.BaseActivityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = BaseActivityModule.class)
public interface BaseActivityComponent {
    void inject (BaseActivity baseActivity);
}
