package com.cybexmobile.activity.web;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.cybexmobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WebActivity extends BaseActivity{

    private static final String TAG = WebActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initWebViewSetting();
//        boolean isNight = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode", false);
//        boolean isZh = (Locale.getDefault().getLanguage().equals("zh"));
        String url = getIntent().getStringExtra(Constant.INTENT_PARAM_URL);
        mWebView.loadUrl(url);
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
                if(hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE){
                    Log.v(TAG, view.getUrl() + "  ---> 重定向 --->  " + url);
                }
                //禁止重定向
                return true;
            }
        });
    }

}
