package com.cybexmobile.injection.base;

import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.injection.component.AppActivityComponent;
import com.cybexmobile.injection.component.DaggerAppActivityComponent;

public abstract class AppBaseActivity extends BaseActivity {
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
