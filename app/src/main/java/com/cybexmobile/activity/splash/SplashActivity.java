package com.cybexmobile.activity.splash;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.service.WebSocketService;
import com.cybexmobile.R;
import com.cybexmobile.activity.main.BottomNavigationActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentService = new Intent(SplashActivity.this, WebSocketService.class);
        startService(intentService);
        setContentView(R.layout.activity_splash);
    }

    private void gotoMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, BottomNavigationActivity.class);
                startActivity(i);
                finish();
            }
        }, 1500);
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
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLazyLoad() {
        super.onLazyLoad();
        gotoMain();
    }
}
