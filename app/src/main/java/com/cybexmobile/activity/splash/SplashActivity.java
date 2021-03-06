package com.cybexmobile.activity.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.service.WebSocketService;
import com.cybexmobile.R;
import com.cybexmobile.activity.main.BottomNavigationActivity;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FROM_BROWSER;

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
                if (getIntent().getData() != null) {
                    i.putExtra(INTENT_PARAM_FROM_BROWSER, true);
                }
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
