package com.cybex.eto.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.eto.R;

import java.util.Locale;

public class TermsAndConditionsActivity extends BaseActivity {

    Toolbar mToolbar;
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        mToolbar = findViewById(R.id.toolbar);
        mWebView = findViewById(R.id.terms_and_condition_wv);
        setSupportActionBar(mToolbar);
        initWebViewSetting();
        boolean isNight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode", false);
        boolean isZh = (Locale.getDefault().getLanguage().equals("zh"));
        if (isNight) {
            if (isZh) {
                mWebView.loadUrl("file:///android_asset/cn_light.html");
            } else {
                mWebView.loadUrl("file:///android_asset/en_light.html");
            }
        } else {
            if (isZh) {
                mWebView.loadUrl("file:///android_asset/cn_dark.html");
            } else {
                mWebView.loadUrl("file:///android_asset/en_dark.html");
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
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
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoadDialog();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoadDialog();
            }
        });
    }
}
