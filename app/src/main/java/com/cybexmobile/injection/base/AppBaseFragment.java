package com.cybexmobile.injection.base;

import com.cybex.basemodule.base.BaseFragment;
import com.cybexmobile.injection.component.AppActivityComponent;
import com.cybexmobile.injection.component.DaggerAppActivityComponent;

public abstract class AppBaseFragment extends BaseFragment{

    private AppActivityComponent mAppActivityComponent;

    public AppActivityComponent appActivityComponent() {
        if (mAppActivityComponent == null) {
            mAppActivityComponent = DaggerAppActivityComponent.builder()
                    .baseActivityComponent(baseActivityComponent())
                    .build();
        }
        return mAppActivityComponent;
    }
}
