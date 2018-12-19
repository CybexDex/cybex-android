package com.cybexmobile.activity.openorders;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.apihk.LimitOrderWrapper;
import com.cybexmobile.R;
import com.cybexmobile.adapter.OpenOrderRecyclerViewAdapter;
import com.cybexmobile.data.item.OpenOrderItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_USDT;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_USDT;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.graphene.chain.Operations.ID_CANCEL_LMMIT_ORDER_OPERATION;

public class OpenOrdersActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, OpenOrderRecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = "OpenOrdersActivity";
    private static final String TAB_ALL = "All";
    private static final String TAB_BUY = "Buy";
    private static final String TAB_SELL = "Sell";
    private final List<String> mCompareSymbol = Arrays.asList(new String[]{"JADE.USDT", "JADE.ETH", "JADE.BTC", "CYB"});

    private String mUserName;
    private double mTotalOpenOrderRMBPrice;
    private double mSellOpenOrderRMBPrice;
    private double mBuyOpenOrderRMBPrice;

    private SegmentedGroup mSegmentedGroup;
    private TextView mOpenOrderTotalValue, mTvOpenOrderTotalTitle;
    private RecyclerView mRecyclerView;
    private OpenOrderRecyclerViewAdapter mOpenOrderRecycerViewAdapter;
    private Toolbar mToolbar;
    private WebSocketService mWebSocketService;
    private SharedPreferences mSharedPreference;
    private FullAccountObject mFullAccountObject;

    private OpenOrderItem mCurrOpenOrderItem;
    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();
    private List<WatchlistData> mWatchlistDataList = new ArrayList<>();

    private DecimalFormat format = new DecimalFormat("0.0000");

    private String mCurrTab = TAB_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);
        EventBus.getDefault().register(this);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initViews();
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = mSharedPreference.getString(PREF_NAME, null);
        mOpenOrderRecycerViewAdapter = new OpenOrderRecyclerViewAdapter(this, mOpenOrderItems);
        mOpenOrderRecycerViewAdapter.setOnItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mOpenOrderRecycerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        showLoadDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    private void loadLimitOrderData() {
        if(mFullAccountObject == null) {
            return;
        }
        LimitOrderWrapper.getInstance().get_opend_limit_orders(
                mFullAccountObject.account.id.toString(),
                mOpendLimitOrderCallback);
    }

    private void parseOpenOrderItems(List<LimitOrder> limitOrders){
        mOpenOrderItems.clear();
        if(limitOrders == null || limitOrders.size() == 0){
            return;
        }
        for (LimitOrder limitOrder : limitOrders) {
            OpenOrderItem item = new OpenOrderItem();
            item.limitOrder = limitOrder;
            item.baseAsset = mWebSocketService.getAssetObject(limitOrder.key.asset1);
            item.quoteAsset = mWebSocketService.getAssetObject(limitOrder.key.asset2);
            item.isSell = checkIsSell(item.baseAsset.symbol, item.quoteAsset.symbol, mCompareSymbol);
            mOpenOrderItems.add(item);
        }
    }

    private boolean checkIsSell(String baseSymbol, String quoteSymbol, List<String> compareSymbol) {
        boolean isContainBase = compareSymbol.contains(baseSymbol);
        boolean isContainQuote = compareSymbol.contains(quoteSymbol);
        int baseIndex = compareSymbol.indexOf(baseSymbol);
        int quoteIndex = compareSymbol.indexOf(quoteSymbol);
        if (isContainBase) {
            if (!isContainQuote) {
                return false;
            } else {
                if (baseIndex < quoteIndex) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            if (isContainQuote) {
                return true;
            } else {
                return false;
            }
        }
    }

    private MessageCallback<Reply<List<LimitOrder>>> mOpendLimitOrderCallback = new MessageCallback<Reply<List<LimitOrder>>>() {
        @Override
        public void onMessage(Reply<List<LimitOrder>> reply) {
            parseOpenOrderItems(reply.result);
            calculateTotalValue();
            hideLoadDialog();
            mOpenOrderRecycerViewAdapter.getFilter().filter(mCurrTab);
            mOpenOrderRecycerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFailure() {

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            //AccountFragment已经获取了FullAccount数据
            mFullAccountObject = mWebSocketService.getFullAccount(mUserName);
            loadLimitOrderData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void calculateTotalValue() {
        if (mOpenOrderItems.size() == 0) {
            mOpenOrderTotalValue.setText(String.format("≈¥%s", "0.0000"));
            return;
        }
        mWatchlistDataList = mWebSocketService.getAllWatchlistData();
        for (OpenOrderItem item : mOpenOrderItems) {
            calculateTotalRmbPrice(mWatchlistDataList, item);
        }
        displayTotalValue();
    }

    private void calculateTotalRmbPrice(List<WatchlistData> watchlistDataList, OpenOrderItem openOrderItem) {
        /**
         * fix online crash
         * java.lang.NullPointerException: Attempt to read from field
         * 'int com.cybex.provider.graphene.chain.AssetObject.precision' on a null object reference
         */
        if(watchlistDataList == null || watchlistDataList.size() == 0){
            return;
        }
        if(openOrderItem.baseAsset == null || openOrderItem.quoteAsset == null){
            return;
        }
        if (openOrderItem.baseAsset.id.toString().equals(ASSET_ID_CYB)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_CYB);
            openOrderItem.itemRMBPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (openOrderItem.baseAsset.id.toString().equals(ASSET_ID_ETH)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_ETH);
            openOrderItem.itemRMBPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (openOrderItem.baseAsset.id.toString().equals(ASSET_ID_USDT)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_USDT);
            openOrderItem.itemRMBPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (openOrderItem.baseAsset.id.toString().equals(ASSET_ID_BTC)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_BTC);
            openOrderItem.itemRMBPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else {
            for (WatchlistData watchlistData : watchlistDataList) {
                if (watchlistData.getQuoteId().equals(openOrderItem.baseAsset.id.toString())) {
                    openOrderItem.itemRMBPrice = watchlistData.getRmbPrice() * watchlistData.getCurrentPrice();
                    break;
                }
            }
        }
        double openOrderRMBPrice;
        if (openOrderItem.limitOrder.is_sell) {
            openOrderRMBPrice = AssetUtil.multiply(AssetUtil.divide(openOrderItem.limitOrder.min_to_receive, Math.pow(10, openOrderItem.baseAsset.precision)), openOrderItem.itemRMBPrice);
            mSellOpenOrderRMBPrice += openOrderRMBPrice;
        } else {
            openOrderRMBPrice = AssetUtil.multiply(AssetUtil.divide(openOrderItem.limitOrder.amount_to_sell, Math.pow(10, openOrderItem.baseAsset.precision)), openOrderItem.itemRMBPrice);
            mBuyOpenOrderRMBPrice += openOrderRMBPrice;
        }
        mTotalOpenOrderRMBPrice += openOrderRMBPrice;
    }

    private void initViews() {
        mSegmentedGroup = findViewById(R.id.open_orders_segmented_group);
        mRecyclerView = findViewById(R.id.open_orders_recycler_view);
        mOpenOrderTotalValue = findViewById(R.id.open_orders_total_value);
        mTvOpenOrderTotalTitle = findViewById(R.id.open_orders_title);
        mSegmentedGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.open_orders_segment_all:
                mCurrTab = TAB_ALL;
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_total_value);
                break;
            case R.id.open_orders_segment_buy:
                mCurrTab = TAB_BUY;
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_buy_total_value);
                break;
            case R.id.open_orders_segment_sell:
                mCurrTab = TAB_SELL;
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_sell_total_value);
                break;
        }
        mOpenOrderRecycerViewAdapter.getFilter().filter(mCurrTab);
        displayTotalValue();
    }

    @Override
    public void onItemClick(OpenOrderItem itemValue) {
        showLoadDialog();
        mCurrOpenOrderItem = itemValue;
        loadLimitOrderCancelFee(ASSET_ID_CYB);
    }

    public void loadLimitOrderCancelFee(String assetId) {
        mWebSocketService.loadLimitOrderCancelFee(assetId, ID_CANCEL_LMMIT_ORDER_OPERATION,
                BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(ObjectId.create_from_string(""),
                        ObjectId.create_from_string(ASSET_ID_CYB),
                        mCurrOpenOrderItem.baseAsset.id,
                        mCurrOpenOrderItem.quoteAsset.id,
                        0, 0, 0));
    }

    public void displayTotalValue() {
        double total = 0;
        switch (mSegmentedGroup.getCheckedRadioButtonId()) {
            case R.id.open_orders_segment_all:
                total = mTotalOpenOrderRMBPrice;
                break;
            case R.id.open_orders_segment_buy:
                total = mBuyOpenOrderRMBPrice;
                break;
            case R.id.open_orders_segment_sell:
                total = mSellOpenOrderRMBPrice;
                break;
        }
        String cybrmb = format.format(total);
        mOpenOrderTotalValue.setText(String.format("≈¥%s", cybrmb));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderCancel(Event.LimitOrderCancel event) {
        hideLoadDialog();
        if (event.isSuccess()) {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_cancel_order_successfully), R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_cancel_order_failed), R.drawable.ic_error_16px);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadRequiredCancelFee(Event.LoadRequiredCancelFee event) {
        FeeAmountObject feeAmount = event.getFee();
        AccountBalanceObject accountBalance = getBalance(feeAmount.asset_id, mFullAccountObject);
        if (feeAmount.asset_id.equals(ASSET_ID_CYB)) {
            if (accountBalance.balance >= feeAmount.amount) {//cyb足够扣手续费
                limitOrderCancelConfirm(mUserName, feeAmount);
            } else { //cyb不够扣手续费 扣取委单的base或者quote
                if (ASSET_ID_CYB.equals(mCurrOpenOrderItem.baseAsset.id.toString())) {
                    hideLoadDialog();
                    ToastMessage.showNotEnableDepositToastMessage(this,
                            getResources().getString(R.string.text_not_enough),
                            R.drawable.ic_error_16px);
                } else {
                    loadLimitOrderCancelFee(mCurrOpenOrderItem.baseAsset.id.toString());
                }
            }
        } else {
            if (accountBalance.balance > feeAmount.amount) {
                limitOrderCancelConfirm(mUserName, feeAmount);
            } else {
                hideLoadDialog();
                ToastMessage.showNotEnableDepositToastMessage(this,
                        getResources().getString(R.string.text_not_enough),
                        R.drawable.ic_error_16px);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {
        mFullAccountObject = event.getFullAccount();
        mTotalOpenOrderRMBPrice = 0;
        mSellOpenOrderRMBPrice = 0;
        mBuyOpenOrderRMBPrice = 0;
        loadLimitOrderData();
    }



    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private MessageCallback mLimitOrderCancelCallback = new MessageCallback<Reply<String>>() {

        @Override
        public void onMessage(Reply<String> reply) {
            EventBus.getDefault().post(new Event.LimitOrderCancel(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.LimitOrderCancel(false));
        }
    };

    private void limitOrderCancelConfirm(String userName, FeeAmountObject feeAmount){
        hideLoadDialog();
        String priceStr;
        String amountStr;
        String totalStr;
        String feeStr;
        LimitOrder limitOrder = mCurrOpenOrderItem.limitOrder;
        AssetObject baseAsset = mCurrOpenOrderItem.baseAsset;
        AssetObject quoteAsset = mCurrOpenOrderItem.quoteAsset;
        AssetsPair.Config assetPairConfig = AssetPairCache.getInstance().getAssetPairConfig(
                mCurrOpenOrderItem.isSell ? quoteAsset.id.toString() : baseAsset.id.toString(),
                mCurrOpenOrderItem.isSell ? baseAsset.id.toString() : quoteAsset.id.toString());
        if(mCurrOpenOrderItem.isSell){
            double amount = AssetUtil.subtract(AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, baseAsset.precision)),
                    AssetUtil.divide(limitOrder.sold, Math.pow(10, baseAsset.precision)));
            double total = AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, quoteAsset.precision));
            double price = AssetUtil.divide(total, amount);
            priceStr = String.format("%s %s", AssetUtil.formatNumberRounding(price, Integer.parseInt(assetPairConfig.last_price)), AssetUtil.parseSymbol(quoteAsset.symbol));
            amountStr = String.format("%s %s", AssetUtil.formatNumberRounding(amount, Integer.parseInt(assetPairConfig.amount)), AssetUtil.parseSymbol(baseAsset.symbol));
            totalStr = String.format("%s %s", AssetUtil.formatNumberRounding(total, Integer.parseInt(assetPairConfig.last_price)), AssetUtil.parseSymbol(quoteAsset.symbol));
        } else {
            double amount = AssetUtil.subtract(AssetUtil.divide(limitOrder.min_to_receive, Math.pow(10, quoteAsset.precision)),
                    AssetUtil.divide(limitOrder.received, Math.pow(10, quoteAsset.precision)));
            double total = AssetUtil.divide(limitOrder.amount_to_sell, Math.pow(10, baseAsset.precision));
            double price = AssetUtil.divide(total, amount);
            priceStr = String.format("%s %s", AssetUtil.formatNumberRounding(price, Integer.parseInt(assetPairConfig.last_price)), AssetUtil.parseSymbol(baseAsset.symbol));
            amountStr = String.format("%s %s", AssetUtil.formatNumberRounding(amount, Integer.parseInt(assetPairConfig.amount)), AssetUtil.parseSymbol(quoteAsset.symbol));
            totalStr = String.format("%s %s", AssetUtil.formatNumberRounding(total, Integer.parseInt(assetPairConfig.last_price)), AssetUtil.parseSymbol(baseAsset.symbol));
        }
        if(feeAmount.asset_id.equals(ASSET_ID_CYB)){
            AssetObject cybAsset = mWebSocketService.getAssetObject(ASSET_ID_CYB);
            feeStr = String.format("%s %s", AssetUtil.formatNumberRounding(feeAmount.amount/Math.pow(10, cybAsset.precision), cybAsset.precision), AssetUtil.parseSymbol(cybAsset.symbol));
        } else {
            feeStr = String.format("%s %s", AssetUtil.formatNumberRounding(feeAmount.amount / Math.pow(10, baseAsset.precision), baseAsset.precision), AssetUtil.parseSymbol(baseAsset.symbol));
        }
        CybexDialog.showLimitOrderCancelConfirmationDialog(this, !mCurrOpenOrderItem.isSell,
                priceStr, amountStr, totalStr, feeStr,
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        checkIfLocked(userName, feeAmount);
                    }
                });
    }

    private void checkIfLocked(String userName, FeeAmountObject feeAmount) {
        if (!BitsharesWalletWraper.getInstance().is_locked()) {
            toCancelLimitOrder(feeAmount);
            return;
        }
        CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFullAccountObject.account, userName, new UnlockDialog.UnLockDialogClickListener() {
            @Override
            public void onUnLocked(String password) {
                showLoadDialog();
                toCancelLimitOrder(feeAmount);
            }
        });
    }

    private void toCancelLimitOrder(FeeAmountObject feeAmount) {
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    Operations.limit_order_cancel_operation operation = BitsharesWalletWraper.getInstance().
                            getLimitOrderCancelOperation(mFullAccountObject.account.id, ObjectId.create_from_string(feeAmount.asset_id),
                                    mCurrOpenOrderItem.limitOrder.order_id, feeAmount.amount);
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFullAccountObject.account, operation, ID_CANCEL_LMMIT_ORDER_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mLimitOrderCancelCallback);
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

    private AccountBalanceObject getBalance(String assetId, FullAccountObject fullAccount) {
        if (assetId == null || fullAccount == null) {
            return null;
        }
        List<AccountBalanceObject> accountBalances = fullAccount.balances;
        if (accountBalances == null || accountBalances.size() == 0) {
            return null;
        }
        AccountBalanceObject accountBalanceObject = null;
        for (AccountBalanceObject accountBalance : accountBalances) {
            if (accountBalance.asset_type.toString().equals(assetId)) {
                accountBalanceObject = accountBalance;
                break;
            }
        }
        return accountBalanceObject;
    }

}
