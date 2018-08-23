package com.cybexmobile.activity.splash;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.cybexmobile.activity.main.BottomNavigationActivity;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.R;
import com.cybexmobile.service.WebSocketService;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends BaseActivity{

    private static final int REQUEST_CODE_WRITE_SETTING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentService = new Intent(SplashActivity.this, WebSocketService.class);
        startService(intentService);
        setContentView(R.layout.activity_splash);
        /**
         * fix bug
         * Android6.0 注册广播无法动态申请CHANGE_NETWORK_STATE权限，跳转至系统界面手动开启WRITE_SETTINGS权限
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions();
        } else {
            gotoMain();
        }
    }

    private void gotoMain(){
            new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, BottomNavigationActivity.class);
                startActivity(i);
                finish();
            }
        }, 1500);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean canWriteSetting(){
        return Settings.System.canWrite(this);
    }

    private void requestPermissions(){
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(Manifest.permission.CHANGE_NETWORK_STATE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Permission>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Permission permission) {
                        if(permission.granted || canWriteSetting()){
                            gotoMain();
                        } else {
                            Intent intentSetting = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intentSetting.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intentSetting, REQUEST_CODE_WRITE_SETTING);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        finish();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_WRITE_SETTING){
            if(canWriteSetting()){
                gotoMain();
            } else {
              finish();
            }
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

}
