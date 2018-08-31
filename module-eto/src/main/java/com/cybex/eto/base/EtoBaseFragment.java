package com.cybex.eto.base;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.eto.injection.component.DaggerEtoActivityComponent;
import com.cybex.eto.injection.component.EtoActivityComponent;

public abstract class EtoBaseFragment extends BaseFragment{

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
