package com.cybex.eto.injection.component;

import com.cybex.basemodule.injection.annotation.PerActivity;
import com.cybex.basemodule.injection.component.BaseActivityComponent;
import com.cybex.basemodule.injection.module.BaseActivityModule;
import com.cybex.eto.activity.details.EtoDetailsActivity;
import com.cybex.eto.fragment.EtoFragment;
import com.cybex.eto.injection.module.EtoActivityModule;

import dagger.Component;

@PerActivity
@Component(dependencies = BaseActivityComponent.class, modules = EtoActivityModule.class)
public interface EtoActivityComponent {

    void inject(EtoFragment etoFragment);
    void inject(EtoDetailsActivity etoDetailsActivity);
}
