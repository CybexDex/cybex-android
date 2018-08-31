package com.cybex.basemodule.injection.component;


import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.injection.annotation.PerActivity;
import com.cybex.basemodule.injection.module.ActivityModule;

import dagger.Component;

@PerActivity
@Component(modules = ActivityModule.class)
public interface ActivityComponent {
    void inject (BaseActivity baseActivity);
}
