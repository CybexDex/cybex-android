package com.cybexmobile.activity.game;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.web.AndroidtoJs;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_URL;
import static com.cybex.basemodule.constant.Constant.PREF_HISTORY_URL;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;

public class GameActivity extends BaseActivity {
    private static final String TAG = GameActivity.class.getSimpleName();
    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private AndroidtoJs mAndroidtoJs;
    private String url;
    private FullAccountObject mFullAccountObject;

    private AccountObject mFromAccountObject;
    private AccountObject mToAccountObject;
    private AssetObject mTransferAssetObject;
    private double mTransferAssetAmount;
    private FeeAmountObject mTransferFee;

    private CallBackFunction mTransferCallBackFunction;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mFullAccountObject = mWebSocketService.getFullAccount(mName);
            if (mFullAccountObject != null) {
                mAndroidtoJs.setAccountObject(mFullAccountObject);
                mWebView.loadUrl(url);
                registerHandler();
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
    BridgeWebView mWebView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_layout);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
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

    private void registerHandler() {
        //getUserName
        mWebView.registerHandler("getUserName", (data, function) -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GameActivity.this);
            String username = sharedPreferences.getString(PREF_NAME, "");
            function.onCallBack(username);
        });
        //jsOpenUrl
        mWebView.registerHandler("jsOpenUrl", (data, function) -> {
            Intent intent = new Intent(GameActivity.this, GatewayActivity.class);
            intent.putExtra(GatewayActivity.INTENT_IS_DEPOSIT, true);
            startActivity(intent);
        });
        //transfer
        mWebView.registerHandler("transfer", (data, function) -> {
            if (data == null || mFullAccountObject == null) return;
            JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
            String accountId = jsonObject.get("accountId").getAsString();
            String assetId = jsonObject.get("assetId").getAsString();
            mTransferAssetAmount = jsonObject.get("assetAmount").getAsDouble();
            mTransferAssetObject = mWebSocketService.getAssetObject(assetId);
            mTransferCallBackFunction = function;
            try {
                List<String> accountIds = new ArrayList<>();
                accountIds.add(accountId);
                BitsharesWalletWraper.getInstance().get_accounts(accountIds, new MessageCallback<Reply<List<AccountObject>>>() {
                    @Override
                    public void onMessage(Reply<List<AccountObject>> reply) {
                        mFromAccountObject = mFullAccountObject.account;
                        mToAccountObject = reply.result.get(0);
                        if (mFromAccountObject == null || mToAccountObject == null) return;
                        checkIsLockAndLoadTransferFee(ASSET_ID_CYB);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        });
        //searchBalanceWithAsset
        mWebView.registerHandler("searchBalanceWithAsset", (data, function) -> {
            if (data == null || mFullAccountObject == null) return;
            JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
            String assetSymbol = jsonObject.get("asset").getAsString();
            AssetObject assetObject = mWebSocketService.getAssetObjectBySymbol(assetSymbol);
            List<AccountBalanceObject> balances = mFullAccountObject.balances;
            for (AccountBalanceObject balance : balances) {
                if (balance.asset_type.toString().equals(assetObject.id.toString())) {
                    function.onCallBack(String.valueOf(balance.balance / Math.pow(10, assetObject.precision)));
                    break;
                }
            }
        });
    }

    private void checkIsLockAndLoadTransferFee(String feeAssetId){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject,
                    mFromAccountObject.name, password -> loadTransferFee(feeAssetId), result -> {
                        //操作取消
                        if (result == -1) {
                            mTransferCallBackFunction.onCallBack("3");
                        }
                    });
        } else {
            loadTransferFee(feeAssetId);
        }
    }

    private void loadTransferFee(String feeAssetId){
        Operations.transfer_operation transfer_operation =  BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mFromAccountObject.id,
                ObjectId.create_from_string(ASSET_ID_CYB),
                0,
                ObjectId.create_from_string(feeAssetId),
                0,
                null,
                mFromAccountObject.options.memo_key,
                mFromAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(feeAssetId, ID_TRANSER_OPERATION, transfer_operation, new MessageCallback<Reply<List<FeeAmountObject>>>() {

                @Override
                public void onMessage(Reply<List<FeeAmountObject>> reply) {
                    FeeAmountObject fee = reply.result.get(0);
                    if(fee.asset_id.equals(ASSET_ID_CYB)){
                        if (fee.asset_id.equals(mTransferAssetObject.id.toString())){
                            mTransferFee = fee;
                            toTransfer();
                        } else {
                            checkIsLockAndLoadTransferFee(mTransferAssetObject.id.toString());
                        }
                    } else {
                        mTransferFee = fee;
                        toTransfer();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void toTransfer(){
        showLoadDialog();
        Operations.base_operation transferOperation =  BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mToAccountObject.id,
                mTransferAssetObject.id,
                mTransferFee.amount,
                ObjectId.create_from_string(mTransferFee.asset_id),
                (long) (mTransferAssetAmount * Math.pow(10, mTransferAssetObject.precision)),
                null,
                mFromAccountObject.options.memo_key,
                mToAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFromAccountObject, transferOperation, ID_TRANSER_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, new MessageCallback<Reply<String>>() {
                            @Override
                            public void onMessage(Reply<String> reply) {
                                hideLoadDialog();
                                mTransferCallBackFunction.onCallBack(reply.result == null && reply.error == null ? "0" : "2");
                            }

                            @Override
                            public void onFailure() {

                            }
                        });
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
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
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void initWebViewSetting() {
        WebSettings webSettings = mWebView.getSettings();
        //支持javascript交互
        //webSettings.setJavaScriptEnabled(true);
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
    }
}
