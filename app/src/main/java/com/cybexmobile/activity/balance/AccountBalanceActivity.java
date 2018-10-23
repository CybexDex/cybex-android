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

import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.utils.NetworkUtils;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.transfer.TransferActivity;
import com.cybexmobile.adapter.PortfolioRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybexmobile.cache.BalanceCache;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.basemodule.service.WebSocketService;

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

import static com.cybex.basemodule.constant.Constant.ASSET_ID_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_USDT;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_USDT;
import static com.cybex.provider.utils.NetworkUtils.TYPE_NOT_CONNECTED;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;

import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class AccountBalanceActivity extends BaseActivity {
    private static final String TAG = AccountBalanceActivity.class.getName();

    private static final int MESSAGE_WHAT_REFRESH_PORTFOLIO = 1;


    private String mAccountName;
    private double mTotalRmbPrice;
    private double mTotalCybPrice;
    private int mRefreshCount;
    private int mNetworkState;
    private boolean mIsLoginIn;
    private List<String> mAssetWhiteList;
    private List<WatchlistData> mWatchlistDataList;


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
            mAssetWhiteList = mWebSocketService.getAssetWhiteList();
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
                mTotalCybPrice = BalanceCache.getInstance().getmTotalCybBalance();
                mTotalRmbPrice = BalanceCache.getInstance().getmTotalRmbBalance();
                mAccountBalanceObjectItems.addAll(BalanceCache.getInstance().getmAccountBalanceObjectItemList());
                mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
                setTotalCybAndRmbTextView(mTotalCybPrice, mTotalRmbPrice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BalanceCache.getInstance().setmTotalCybBalance(mTotalCybPrice);
        BalanceCache.getInstance().setmTotalRmbBalance(mTotalRmbPrice);
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
            if (ASSET_SYMBOL_CYB.equals(assetRmbPrice.getName())) {
                double cybRmbPrice = assetRmbPrice.getValue();
                setTotalCybAndRmbTextView(mTotalRmbPrice / cybRmbPrice,  mTotalRmbPrice);
                break;
            }
        }
        if (mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0) {
            return;
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
                calculateTotalRmbPrice(item, mWatchlistDataList);
                setTotalCybAndRmbTextView(mTotalCybPrice, mTotalRmbPrice);
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
        mTotalRmbPrice = 0;
        mLimitOrderObjectList.clear();
        mAccountBalanceObjectItems.clear();
        mLimitOrderObjectList.addAll(fullAccountObject.limit_orders);
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        mWatchlistDataList = mWebSocketService.getAllWatchlistData();
        if (accountBalanceObjects != null && accountBalanceObjects.size() > 0) {
            for (AccountBalanceObject balance : accountBalanceObjects) {
                if (!mAssetWhiteList.contains(balance.asset_type.toString())) {
                    continue;
                }
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
                        continue;
                    }

                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                calculateTotalRmbPrice(item, mWatchlistDataList);
                mAccountBalanceObjectItems.add(item);
            }
            mTotalCybPrice = (mTotalRmbPrice / (mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_CYB).getValue()));
            setTotalCybAndRmbTextView(mTotalCybPrice, mTotalRmbPrice);
            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
        }
    }

    private void calculateTotalRmbPrice(AccountBalanceObjectItem item, List<WatchlistData> watchlistDataList) {
        if(watchlistDataList == null || watchlistDataList.size() == 0){
            return;
        }
        if (item.assetObject != null) {
            if (item.accountBalanceObject.asset_type.toString().equals(ASSET_ID_CYB)) {
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_CYB);
                item.balanceItemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
            } else if (item.accountBalanceObject.asset_type.toString().equals(ASSET_ID_ETH)) {
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_ETH);
                item.balanceItemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
            } else if (item.accountBalanceObject.asset_type.toString().equals(ASSET_ID_USDT)) {
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_USDT);
                item.balanceItemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
            } else if (item.accountBalanceObject.asset_type.toString().equals(ASSET_ID_BTC)) {
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_BTC);
                item.balanceItemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
            } else {
                for (WatchlistData watchlistData : watchlistDataList) {
                    if (watchlistData.getQuoteId().equals(item.accountBalanceObject.asset_type.toString())) {
                        item.balanceItemRmbPrice = watchlistData.getRmbPrice() * watchlistData.getCurrentPrice();
                        break;
                    }
                }
            }
            mTotalRmbPrice += (item.accountBalanceObject.balance / Math.pow(10, item.assetObject.precision)) * item.balanceItemRmbPrice;
        }

        for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
            if (limitOrderObject.sell_price.base.asset_id.toString().equals(item.accountBalanceObject.asset_type.toString())) {
                item.frozenAmount += limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision);
                mTotalRmbPrice += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.balanceItemRmbPrice;
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_WHAT_REFRESH_PORTFOLIO:
                    if (mAccountBalanceRecyclerView.getVisibility() != View.VISIBLE) {
                        mAccountBalanceRecyclerView.setVisibility(View.VISIBLE);
                    }
                    Log.e("adapterChange", "adapterChange");
                    mBalanceRecyclerAdapter.notifyDataSetChanged();

                    break;
            }
        }
    };

    private void setTotalCybAndRmbTextView(double totalCyb, double totalRmb) {
        mTotalBalanceTv.setText(totalCyb == 0 ? "0.00000" : String.format(Locale.US, "%.5f", totalCyb));
        if (totalCyb == 0) {
            mTotalRmbTv.setText("≈¥0.00");
        } else {
            mTotalRmbTv.setText(totalRmb == 0 ? "≈¥0.00" : String.format(Locale.US, "≈¥%.2f", totalRmb));
        }
    }

    private void resetAccountDate() {
        mAccountName = null;
        mTotalRmbPrice = 0;
        mAccountBalanceObjectItems.clear();
        mLimitOrderObjectList.clear();
        mBalanceRecyclerAdapter.notifyDataSetChanged();
    }
}
