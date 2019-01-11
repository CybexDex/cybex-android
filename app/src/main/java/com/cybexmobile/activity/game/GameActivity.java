package com.cybexmobile.activity.game;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybexmobile.R;
import com.cybexmobile.activity.web.AndroidtoJs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_URL;
import static com.cybex.basemodule.constant.Constant.PREF_HISTORY_URL;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class GameActivity extends BaseActivity {
    private static final String TAG = GameActivity.class.getSimpleName();
    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private AndroidtoJs mAndroidtoJs;
    private String url;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
            if (fullAccountObject != null) {
                mAndroidtoJs.setAccountObject(fullAccountObject);
                mWebView.addJavascriptInterface(mAndroidtoJs, "Potral");
                mWebView.loadUrl(url);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private String mName;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_layout);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        EventBus.getDefault().register(this);
        mName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        url = getIntent().getStringExtra(INTENT_PARAM_URL);
        clearWebViewCache();
        mAndroidtoJs = new AndroidtoJs(this);
        initWebViewSetting();
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void clearWebViewCache() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String historyUrl = preferences.getString(PREF_HISTORY_URL, null);
        //访问url地址不一致 清除缓存
        if (!TextUtils.isEmpty(historyUrl) && !historyUrl.equals(url)) {
            mWebView.clearCache(true);
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        }
        //记录url地址
        if (TextUtils.isEmpty(historyUrl) || !historyUrl.equals(url)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_HISTORY_URL, url);
            editor.apply();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameDeposit(Event.onGameDeposit gameDeposit) {
       mWebView.loadUrl("javascript:collectCallback('" + gameDeposit.getStatus() +"')");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameUnlocked (Event.onGameUnlocked gameUnlocked) {
        mWebView.loadUrl("javascript:loginCallback('" + gameUnlocked.getResult() +"')");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
        mUnbinder.unbind();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void initWebViewSetting() {
        WebSettings webSettings = mWebView.getSettings();
        //支持javascript交互
        webSettings.setJavaScriptEnabled(true);
        //将图片调整到适合webview的大小
        webSettings.setUseWideViewPort(true);
        //缩放至屏幕的大小
        webSettings.setLoadWithOverviewMode(true);
        //支持缩放
        webSettings.setSupportZoom(true);
        //设置内置的缩放控件
        webSettings.setBuiltInZoomControls(true);
        //隐藏原生的缩放控件
        webSettings.setDisplayZoomControls(false);

        webSettings.setDomStorageEnabled(true);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(mProgressBar == null){
                    return;
                }
                if(newProgress == 100){
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebView.HitTestResult hitTestResult = view.getHitTestResult();
                if (hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
                    Log.v(TAG, view.getUrl() + "  ---> 重定向 --->  " + url);
                }
                //禁止重定向
                return false;
            }
        });
    }
}
