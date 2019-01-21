package com.cybexmobile.activity.deploy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybexmobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends BaseActivity implements QRCodeView.Delegate {
    public static int RESULT_CODE = 3;

    private Unbinder mUnbinder;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.scan_view)
    ZXingView mScanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_layout);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mScanView.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScanView.startCamera();
        mScanView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        mScanView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mScanView.onDestroy();
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {

        Intent intent = new Intent();
        intent.putExtra(Constant.SCAN_RESULT, result);
        setResult(RESULT_CODE, intent);
        vibrate();
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null)
            vibrator.vibrate(200);
    }
}
