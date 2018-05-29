package com.cybexmobile.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;

public class WalletIntroductionActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_introduction);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

}
