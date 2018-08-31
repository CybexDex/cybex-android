package com.cybex.eto.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.eto.R;
import com.cybex.eto.fragment.EtoFragment;

public class LauncherActivity extends BaseActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, new EtoFragment());
        transaction.commit();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
