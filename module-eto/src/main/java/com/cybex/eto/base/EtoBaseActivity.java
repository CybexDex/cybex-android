package com.cybex.eto.base;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.eto.injection.component.DaggerEtoActivityComponent;
import com.cybex.eto.injection.component.EtoActivityComponent;

public abstract class EtoBaseActivity extends BaseActivity {

    private EtoActivityComponent mEtoActivityComponent;

    public EtoActivityComponent etoActivityComponent() {
        if (mEtoActivityComponent == null) {
            mEtoActivityComponent = DaggerEtoActivityComponent.builder()
                    .baseActivityComponent(baseActivityComponent())
                    .build();
        }
        return mEtoActivityComponent;
    }

}
