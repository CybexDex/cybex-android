package com.cybexmobile.activity.balance;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.transfer.TransferActivity;
import com.cybexmobile.adapter.PortfolioRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybexmobile.cache.BalanceCache;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;

import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;
import static com.cybexmobile.utils.NetworkUtils.TYPE_NOT_CONNECTED;

public class AccountBalanceActivity extends BaseActivity {
    private static final String TAG = AccountBalanceActivity.class.getName();

    private static final int MESSAGE_WHAT_REFRUSH_TOTAL_CYB = 1;
    private static final int MESSAGE_WHAT_REFRESH_PORTFOLIO = 2;


    private String mAccountName;
    private double mTotalBalanceCyb;
    private double mCybRmbPrice;
    private int mRefreshCount;
    private int mNetworkState;
    private boolean mIsLoginIn;
    private volatile int mTickerCount;


    private Unbinder mUnbinder;
    private PortfolioRecyclerViewAdapter mBalanceRecyclerAdapter;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems = new ArrayList<>();
    private List<LimitOrderObject> mLimitOrderObjectList = new ArrayList<>();
    private WebSocketService mWebSocketService;
    private SharedPreferences mSharedPreferences;
    private FullAccountObject mFullAccountObject;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.account_balance_total_balance)
    TextView mTotalBalanceTv;
    @BindView(R.id.account_balance_info_question_marker)
    ImageView mQuestionMarkerIv;
    @BindView(R.id.account_balance_total_rmb)
    TextView mTotalRmbTv;
    @BindView(R.id.account_balance_deposit_layout)
    LinearLayout mDepositButtonLayout;
    @BindView(R.id.account_balance_withdraw_layout)
    LinearLayout mWithdrawButtonLayout;
    @BindView(R.id.account_balance_transfer_layout)
    LinearLayout mTransferButtonLayout;
    @BindView(R.id.account_balance_recycler_view)
    RecyclerView mAccountBalanceRecyclerView;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if (!(mNetworkState == TYPE_NOT_CONNECTED)) {
                loadData(mWebSocketService.getFullAccount(mAccountName));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_balance);
        mUnbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        setSupportActionBar(mToolbar);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAccountName = mSharedPreferences.getString(PREF_NAME, "");
        mIsLoginIn = mSharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mNetworkState = NetworkUtils.getConnectivityStatus(this);
        mAccountBalanceRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAccountBalanceRecyclerView.getItemAnimator().setChangeDuration(0);
        addCustomDivider();
        mBalanceRecyclerAdapter = new PortfolioRecyclerViewAdapter(R.layout.item_portfolio_vertical, mAccountBalanceObjectItems, this);
        mAccountBalanceRecyclerView.setAdapter(mBalanceRecyclerAdapter);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (mIsLoginIn) {
            if (mNetworkState == TYPE_NOT_CONNECTED) {
                mTotalBalanceCyb =  BalanceCache.getInstance().getmTotalCybBalance();
                mCybRmbPrice =  BalanceCache.getInstance().getmTotalRmbBalance();
                mAccountBalanceObjectItems.addAll(BalanceCache.getInstance().getmAccountBalanceObjectItemList());
                mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
                setTotalCybAndRmbTextView(mTotalBalanceCyb, mTotalBalanceCyb * mCybRmbPrice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BalanceCache.getInstance().setmTotalCybBalance(mTotalBalanceCyb);
        BalanceCache.getInstance().setmTotalRmbBalance(mCybRmbPrice);
        BalanceCache.getInstance().setmAccountBalanceObjectItemList(mAccountBalanceObjectItems);
        mUnbinder.unbind();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick(R.id.account_balance_info_question_marker)
    public void onBalanceInfoClick(View view) {
        CybexDialog.showBalanceDialog(this);
    }

    @OnClick(R.id.account_balance_deposit_layout)
    public void onDepositButtonClicked(View view) {
        Intent intent = new Intent(this, GatewayActivity.class);
        intent.putExtra(GatewayActivity.INTENT_ACCOUNT_BALANCE_ITEMS, (Serializable) mAccountBalanceObjectItems);
        intent.putExtra(GatewayActivity.INTENT_IS_DEPOSIT, true);
        startActivity(intent);
    }

    @OnClick(R.id.account_balance_withdraw_layout)
    public void onWithdrawButtonClicked(View view) {
        Intent intent = new Intent(this, GatewayActivity.class);
        intent.putExtra(GatewayActivity.INTENT_ACCOUNT_BALANCE_ITEMS, (Serializable) mAccountBalanceObjectItems);
        intent.putExtra(GatewayActivity.INTENT_IS_DEPOSIT, false);
        startActivity(intent);
    }

    @OnClick(R.id.account_balance_transfer_layout)
    public void onTransferButtonClicked(View view) {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.putExtra(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS, (Serializable) mAccountBalanceObjectItems);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event) {
        resetAccountDate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        for (AssetRmbPrice assetRmbPrice : assetRmbPrices) {
            if ("CYB".equals(assetRmbPrice.getName())) {
                mCybRmbPrice = assetRmbPrice.getValue();
                if (mTickerCount == mFullAccountObject.balances.size() + mFullAccountObject.limit_orders.size()) {
                    setTotalCybAndRmbTextView(mTotalBalanceCyb, mTotalBalanceCyb * mCybRmbPrice);
                }
                break;
            }
        }
        if (mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0) {
            return;
        }
        if (mCybRmbPrice != mAccountBalanceObjectItems.get(0).cybPrice) {
            for (AccountBalanceObjectItem item : mAccountBalanceObjectItems) {
                item.cybPrice = mCybRmbPrice;
            }
            mBalanceRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAsset(Event.LoadAsset event) {
        AssetObject assetObject = event.getData();
        if (assetObject == null) {
            return;
        }
        for (int i = 0; i < mAccountBalanceObjectItems.size(); i++) {
            AccountBalanceObjectItem item = mAccountBalanceObjectItems.get(i);
            if (assetObject.id.toString().equals(item.accountBalanceObject.asset_type.toString())) {
                item.assetObject = assetObject;
                if (item.marketTicker != null || item.assetObject.id.toString().equals("1.3.0")) {
                    calculateTotalCyb(item.accountBalanceObject.balance, item.assetObject.precision,
                            item.accountBalanceObject.asset_type.toString().equals("1.3.0") ? 1 : item.marketTicker.latest);
                }
                mBalanceRecyclerAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {
        mRefreshCount++;
        if (mRefreshCount == 1 || mRefreshCount % 10 == 0) {
            loadData(event.getFullAccount());
        }
    }


    private void addCustomDivider() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mAccountBalanceRecyclerView.addItemDecoration(itemDecoration);
    }

    private void loadData(FullAccountObject fullAccountObject) {
        if (fullAccountObject == null) {
            return;
        }
        if (NetworkUtils.getConnectivityStatus(this) == TYPE_NOT_CONNECTED) {
            return;
        }
        mFullAccountObject = fullAccountObject;
        mTickerCount = 0;
        mTotalBalanceCyb = 0;
        mLimitOrderObjectList.clear();
        mAccountBalanceObjectItems.clear();
        mLimitOrderObjectList.addAll(fullAccountObject.limit_orders);
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        if (accountBalanceObjects != null && accountBalanceObjects.size() > 0) {
            for (AccountBalanceObject balance : accountBalanceObjects) {
                /**
                 * fix bug
                 * CYM-241
                 * 过滤为0的资产
                 */
                if (balance.balance == 0) {
                    boolean shouldHide = true;
                    for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                        if (limitOrderObject.sell_price.base.asset_id.equals(balance.asset_type)) {
                            shouldHide = false;
                            break;
                        }
                    }
                    if (shouldHide) {
                        mTickerCount ++;
                        continue;
                    }

                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                item.cybPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
                if (item.assetObject != null && balance.asset_type.toString().equals("1.3.0")) {
                    mTickerCount ++;
                    calculateTotalCyb(balance.balance, item.assetObject.precision, 1);
                }
                mAccountBalanceObjectItems.add(item);
                if (!balance.asset_type.toString().equals("1.3.0")) {
                    try {
                        BitsharesWalletWraper.getInstance().get_ticker("1.3.0", balance.asset_type.toString(), onTickerCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                } else {
                    for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                        if (limitOrderObject.sell_price.base.asset_id.toString().equals("1.3.0")) {
                            mTickerCount ++;
                            mTotalBalanceCyb += limitOrderObject.for_sale / Math.pow(10, 5);
                            item.frozenAmount += limitOrderObject.for_sale / Math.pow(10, 5);

                        }
                    }
                }
            }
            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_WHAT_REFRUSH_TOTAL_CYB:
                    AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                    mCybRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
                    Log.v(TAG, "mTotalCyb=" + mTotalBalanceCyb + "  mTotalCyb * mCybRmbPrice=" + mTotalBalanceCyb * mCybRmbPrice);
                    setTotalCybAndRmbTextView(mTotalBalanceCyb, mTotalBalanceCyb * mCybRmbPrice);
                    break;
                case MESSAGE_WHAT_REFRESH_PORTFOLIO:
                    if (mAccountBalanceRecyclerView.getVisibility() != View.VISIBLE) {
                        mAccountBalanceRecyclerView.setVisibility(View.VISIBLE);
                    }
                    if (mTickerCount == mFullAccountObject.balances.size() + mFullAccountObject.limit_orders.size()) {
                        Log.e("adapterChange", "adapterChange");
                        mBalanceRecyclerAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    private WebSocketClient.MessageCallback onTickerCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker ticker = reply.result;
            if (ticker == null) {
                return;
            }
            for (int i = 0; i < mAccountBalanceObjectItems.size(); i++) {
                AccountBalanceObjectItem item = mAccountBalanceObjectItems.get(i);
                if (ticker.quote.equals(item.accountBalanceObject.asset_type.toString())) {
                    item.marketTicker = ticker;
                    if (item.assetObject != null) {
                        mTickerCount ++;
                        calculateTotalCyb(item.accountBalanceObject.balance, item.assetObject.precision,
                                item.accountBalanceObject.asset_type.toString().equals("1.3.0") ? 1 : item.marketTicker.latest);
                    }
                    for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                        if (limitOrderObject.sell_price.base.asset_id.toString().equals(item.accountBalanceObject.asset_type.toString()) && !limitOrderObject.sell_price.base.asset_id.toString().equals("1.3.0")) {
                            mTickerCount ++;
                            mTotalBalanceCyb += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.marketTicker.latest;
                            item.frozenAmount += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision));
                        }
                    }
                    Message message = Message.obtain();
                    message.what = MESSAGE_WHAT_REFRESH_PORTFOLIO;
                    message.obj = i;
                    mHandler.sendMessage(message);
                    break;
                }
            }

        }

        @Override
        public void onFailure() {

        }
    };

    private void calculateTotalCyb(long balance, int precision, double priceCyb) {
        double price = balance / Math.pow(10, precision);
        mTotalBalanceCyb += (price * priceCyb);
        if (mTickerCount == mFullAccountObject.balances.size() + mFullAccountObject.limit_orders.size()) {
            Log.e("Ticker", "total_cyb_get");
            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_TOTAL_CYB);
        }
    }

    private void setTotalCybAndRmbTextView(double totalCyb, double totalRmb) {
        mTotalBalanceTv.setText(totalCyb == 0 ? "0.00000" : String.format(Locale.US, "%.5f", mTotalBalanceCyb));
        if (totalCyb == 0) {
            mTotalRmbTv.setText("≈¥0.00");
        } else {
            mTotalRmbTv.setText(totalRmb == 0 ? "≈¥0.00" : String.format(Locale.US, "≈¥%.2f", totalRmb));
        }
    }

    private void resetAccountDate() {
        mAccountName = null;
        mTotalBalanceCyb = 0;
        mAccountBalanceObjectItems.clear();
        mLimitOrderObjectList.clear();
        mBalanceRecyclerAdapter.notifyDataSetChanged();
    }
}
