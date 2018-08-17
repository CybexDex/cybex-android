package com.cybexmobile.activity.address;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddressManagerActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_manager);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick(R.id.address_manager_tv_withdraw_address)
    public void onWithdrawAddressClick(View view) {
        Intent intent = new Intent(this, WithdrawAddressManagerActivity.class);
        startActivity(intent);

    }

    @OnClick(R.id.address_manager_tv_transfer_account)
    public void onTransferAccountClick(View view) {
        Intent intent = new Intent(this, TransferAccountManagerActivity.class);
        startActivity(intent);
    }
}
