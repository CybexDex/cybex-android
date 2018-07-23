package com.cybexmobile.activity.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.transfer.TransferRecordsActivity;
import com.cybexmobile.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TransferActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_et_account_name)
    EditText mEtAccountName;//账户名
    @BindView(R.id.transfer_tv_crypto)
    TextView mTvCrypto;//币种
    @BindView(R.id.transfer_et_quantity)
    EditText mEtQuantity;//金额
    @BindView(R.id.transfer_et_remark)
    EditText mTvRemark;//备注
    @BindView(R.id.transfer_tv_fee)
    TextView mTvFee;//手续费
    @BindView(R.id.transfer_btn_transfer)
    Button mBtnTransfer;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick(R.id.transfer_tv_crypto)
    public void onCryptoClick(View view){

    }

    @OnClick(R.id.transfer_tv_transfer_records)
    public void onTransferRecord(View view){
        Intent intent = new Intent(this, TransferRecordsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.transfer_btn_transfer)
    public void onTransferClick(View view){

    }

}
