package com.cybexmobile.injection.component;

import com.cybex.basemodule.injection.annotation.PerActivity;
import com.cybex.basemodule.injection.component.BaseActivityComponent;
import com.cybexmobile.activity.gateway.records.DepositAndWithdrawTotalActivity;
import com.cybexmobile.injection.module.AppActivityModule;

import dagger.Component;

@PerActivity
@Component(dependencies = BaseActivityComponent.class, modules = AppActivityModule.class)
public interface AppActivityComponent {
    void inject(DepositAndWithdrawTotalActivity depositAndWithdrawTotalActivity);
}
