package com.cybexmobile.activity.introduction;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.R;

public class WalletIntroductionActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_introduction);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

}
