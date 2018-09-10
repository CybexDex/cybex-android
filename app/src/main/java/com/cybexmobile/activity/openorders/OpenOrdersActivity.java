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
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.OpenOrderRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LimitOrderObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.provider.market.OpenOrder;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

import static com.cybex.provider.graphene.chain.Operations.ID_CANCEL_LMMIT_ORDER_OPERATION;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class OpenOrdersActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, OpenOrderRecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = "OpenOrdersActivity";
    private static final String TAB_ALL = "All";
    private static final String TAB_BUY = "Buy";
    private static final String TAB_SELL = "Sell";

    private String mUserName;
    private double mTotalOpenOrderBalance;
    private double mSellOpenOrderBalance;
    private double mBuyOpenOrderBalance;

    private SegmentedGroup mSegmentedGroup;
    private TextView mOpenOrderTotalValue, mTvOpenOrderTotalTitle;
    private RecyclerView mRecyclerView;
    private OpenOrderRecyclerViewAdapter mOpenOrderRecycerViewAdapter;
    private List<String> mCompareSymbol = Arrays.asList(new String[]{"JADE.USDT", "JADE.ETH", "JADE.BTC", "CYB"});
    private Toolbar mToolbar;
    private WebSocketService mWebSocketService;
    private SharedPreferences mSharedPreference;
    private FullAccountObject mFullAccountObject;

    private OpenOrderItem mCurrOpenOrderItem;
    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();

    private DecimalFormat format = new DecimalFormat("0.00");

    private volatile int mGetTickerCount;
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

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            //AccountFragment已经获取了FullAccount数据
            mFullAccountObject = mWebSocketService.getFullAccount(mUserName);
            List<LimitOrderObject> limitOrderObjects = mFullAccountObject == null ? null : mFullAccountObject.limit_orders;
            if (limitOrderObjects == null || limitOrderObjects.size() == 0) {
                hideLoadDialog();
                return;
            }
            for (LimitOrderObject limitOrderObject : limitOrderObjects) {
                OpenOrderItem item = new OpenOrderItem();
                OpenOrder openOrder = new OpenOrder();
                openOrder.setLimitOrder(limitOrderObject);
                String baseId = limitOrderObject.sell_price.base.asset_id.toString();
                String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
                List<AssetObject> assetObjects = mWebSocketService.getAssetObjects(baseId, quoteId);
                if (assetObjects != null && assetObjects.size() == 2) {
                    String baseSymbol = assetObjects.get(0).symbol;
                    String quoteSymbol = assetObjects.get(1).symbol;
                    item.isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                    openOrder.setBaseObject(assetObjects.get(0));
                    openOrder.setQuoteObject(assetObjects.get(1));
                }
                item.openOrder = openOrder;
                mOpenOrderItems.add(item);
            }
            calculateTotalValue();
            hideLoadDialog();
            mOpenOrderRecycerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void calculateTotalValue() {
        if (mOpenOrderItems.size() == 0) {
            mOpenOrderTotalValue.setText(String.format("≈¥%s", "0.00"));
            return;
        }
        mGetTickerCount = 0;
        for (OpenOrderItem item : mOpenOrderItems) {
            if (item.openOrder.getLimitOrder().sell_price.base.asset_id.toString().equals(ASSET_ID_CYB)) {
                mGetTickerCount++;
                if (item.isSell) {
                    mSellOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision);
                    mTotalOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision);
                } else {
                    mBuyOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision);
                    mTotalOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision);
                }
            } else {
                try {
                    BitsharesWalletWraper.getInstance().get_ticker(ASSET_ID_CYB, item.openOrder.getLimitOrder().sell_price.base.asset_id.toString(), onTickerCallback);
                } catch (NetworkStatusException e) {
                    e.printStackTrace();
                }
            }
        }
        displayTotalValue();
    }

    private WebSocketClient.MessageCallback onTickerCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker ticker = reply.result;
            if (ticker == null) {
                return;
            }
            mGetTickerCount ++;
            for (OpenOrderItem item : mOpenOrderItems) {
                if (item.openOrder.getLimitOrder().sell_price.base.asset_id.toString().equals(ticker.quote)) {
                    if (item.isSell) {
                        mTotalOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision) * ticker.latest;
                        mSellOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision) * ticker.latest;
                    } else {
                        mTotalOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision) * ticker.latest;
                        mBuyOpenOrderBalance += item.openOrder.getLimitOrder().for_sale / Math.pow(10, item.openOrder.getBaseObject().precision) * ticker.latest;
                    }
                }
            }
            displayTotalValue();
        }

        @Override
        public void onFailure() {

        }
    };

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

    public void loadLimitOrderCancelFee(String assetId){
        mWebSocketService.loadLimitOrderCancelFee(assetId, ID_CANCEL_LMMIT_ORDER_OPERATION,
                BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(ObjectId.create_from_string(""),
                        ObjectId.create_from_string(ASSET_ID_CYB),
                        mCurrOpenOrderItem.isSell ? mCurrOpenOrderItem.openOrder.getBaseObject().id : mCurrOpenOrderItem.openOrder.getQuoteObject().id,
                        mCurrOpenOrderItem.isSell ? mCurrOpenOrderItem.openOrder.getQuoteObject().id : mCurrOpenOrderItem.openOrder.getBaseObject().id,
                        0, 0, 0));
    }

    public void displayTotalValue() {
        if(mGetTickerCount != mOpenOrderItems.size()){
            return;
        }
        double total = 0;
        switch (mSegmentedGroup.getCheckedRadioButtonId()){
            case R.id.open_orders_segment_all:
                total = mTotalOpenOrderBalance;
                break;
            case R.id.open_orders_segment_buy:
                total = mBuyOpenOrderBalance;
                break;
            case R.id.open_orders_segment_sell:
                total = mSellOpenOrderBalance;
                break;
        }
        double rmbPrice = mWebSocketService.getAssetRmbPrice("CYB") == null ? 0 : mWebSocketService.getAssetRmbPrice("CYB").getValue();
        Log.v(TAG, "total * rmbPrice=" + total * rmbPrice);
        Log.v(TAG, "format.format(total * rmbPrice)" + format.format(total * rmbPrice));
        String cybrmb = format.format(total * rmbPrice);
        mOpenOrderTotalValue.setText(String.format("≈¥%s", cybrmb));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderCancel(Event.LimitOrderCancel event){
        hideLoadDialog();
        if(event.isSuccess()){
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_cancel_order_successfully), R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_cancel_order_failed), R.drawable.ic_error_16px);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadRequiredCancelFee(Event.LoadRequiredCancelFee event){
        FeeAmountObject feeAmount = event.getFee();
        AccountBalanceObject accountBalance = getBalance(feeAmount.asset_id, mFullAccountObject);
        if(feeAmount.asset_id.equals(ASSET_ID_CYB)){
            if(accountBalance.balance >= feeAmount.amount){//cyb足够扣手续费
                limitOrderCancelConfirm(mUserName, feeAmount);
            } else { //cyb不够扣手续费 扣取委单的base或者quote
                if(ASSET_ID_CYB.equals(mCurrOpenOrderItem.openOrder.getBaseObject().id.toString())){
                    hideLoadDialog();
                    ToastMessage.showNotEnableDepositToastMessage(this,
                            getResources().getString(R.string.text_not_enough),
                            R.drawable.ic_error_16px);
                } else {
                    loadLimitOrderCancelFee(mCurrOpenOrderItem.openOrder.getBaseObject().id.toString());
                }
            }
        } else {
            if(accountBalance.balance > feeAmount.amount){
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
    public void onLoadAssets(Event.LoadAssets event) {
        List<AssetObject> assetObjects = event.getData();
        if (assetObjects == null && assetObjects.size() != 2) {
            return;
        }
        for (int i = 0; i < mOpenOrderItems.size(); i++) {
            LimitOrderObject limitOrderObject = mOpenOrderItems.get(i).openOrder.getLimitOrder();
            String baseId = limitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
            if (baseId.equals(assetObjects.get(0).id.toString()) && quoteId.equals(assetObjects.get(1).id.toString())) {
                String baseSymbol = assetObjects.get(0).symbol;
                String quoteSymbol = assetObjects.get(1).symbol;
                mOpenOrderItems.get(i).isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                mOpenOrderItems.get(i).openOrder.setBaseObject(assetObjects.get(0));
                mOpenOrderItems.get(i).openOrder.setQuoteObject(assetObjects.get(1));
                hideLoadDialog();
                mOpenOrderRecycerViewAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {
        mFullAccountObject = event.getFullAccount();
        mOpenOrderItems.clear();
        mTotalOpenOrderBalance = 0;
        mSellOpenOrderBalance = 0;
        mBuyOpenOrderBalance = 0;
        List<LimitOrderObject> limitOrderObjects = mFullAccountObject == null ? null : mFullAccountObject.limit_orders;
        if (limitOrderObjects == null || limitOrderObjects.size() == 0) {
            hideLoadDialog();
            calculateTotalValue();
            mOpenOrderRecycerViewAdapter.getFilter().filter(mCurrTab);
            mOpenOrderRecycerViewAdapter.notifyDataSetChanged();
            return;
        }
        for (LimitOrderObject limitOrderObject : limitOrderObjects) {
            OpenOrderItem item = new OpenOrderItem();
            OpenOrder openOrder = new OpenOrder();
            openOrder.setLimitOrder(limitOrderObject);
            String baseId = limitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
            List<AssetObject> assetObjects = mWebSocketService.getAssetObjects(baseId, quoteId);
            if (assetObjects != null && assetObjects.size() == 2) {
                String baseSymbol = assetObjects.get(0).symbol;
                String quoteSymbol = assetObjects.get(1).symbol;
                item.isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                openOrder.setBaseObject(assetObjects.get(0));
                openOrder.setQuoteObject(assetObjects.get(1));
            }
            item.openOrder = openOrder;
            mOpenOrderItems.add(item);
        }
        calculateTotalValue();
        hideLoadDialog();
        mOpenOrderRecycerViewAdapter.getFilter().filter(mCurrTab);
        mOpenOrderRecycerViewAdapter.notifyDataSetChanged();
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

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private WebSocketClient.MessageCallback mLimitOrderCancelCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<String>>(){

        @Override
        public void onMessage(WebSocketClient.Reply<String> reply) {
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
        LimitOrderObject limitOrderObject = mCurrOpenOrderItem.openOrder.getLimitOrder();
        AssetObject base = mCurrOpenOrderItem.openOrder.getBaseObject();
        AssetObject quote = mCurrOpenOrderItem.openOrder.getQuoteObject();
        /**
         * fix bug:CYM-349
         * 订单部分撮合
         */
        if(mCurrOpenOrderItem.isSell){
            double amount = limitOrderObject.for_sale / Math.pow(10, base.precision);
            double price = (limitOrderObject.sell_price.quote.amount / Math.pow(10, quote.precision)) / (limitOrderObject.sell_price.base.amount / Math.pow(10, base.precision));
            double total = amount * price;
            priceStr = String.format("%s %s", AssetUtil.formatNumberRounding(price, AssetUtil.pricePrecision(price)), AssetUtil.parseSymbol(quote.symbol));
            amountStr = String.format("%s %s", AssetUtil.formatNumberRounding(amount, AssetUtil.amountPrecision(price)), AssetUtil.parseSymbol(base.symbol));
            totalStr = String.format("%s %s", AssetUtil.formatNumberRounding(total, AssetUtil.pricePrecision(price)), AssetUtil.parseSymbol(quote.symbol));
        } else {
            double amount = limitOrderObject.sell_price.quote.amount / Math.pow(10, quote.precision);
            double price = (limitOrderObject.sell_price.base.amount / Math.pow(10, base.precision)) / amount;
            double total = limitOrderObject.for_sale / Math.pow(10, base.precision);
            priceStr = String.format("%s %s", AssetUtil.formatNumberRounding(price, AssetUtil.pricePrecision(price)), AssetUtil.parseSymbol(base.symbol));
            amountStr = String.format("%s %s", AssetUtil.formatNumberRounding(amount, AssetUtil.amountPrecision(price)), AssetUtil.parseSymbol(quote.symbol));
            totalStr = String.format("%s %s", AssetUtil.formatNumberRounding(total, AssetUtil.pricePrecision(price)), AssetUtil.parseSymbol(base.symbol));
        }
        if(feeAmount.asset_id.equals(ASSET_ID_CYB)){
            AssetObject cybAsset = mWebSocketService.getAssetObject(ASSET_ID_CYB);
            feeStr = String.format("%s %s", AssetUtil.formatNumberRounding(feeAmount.amount/Math.pow(10, cybAsset.precision), cybAsset.precision), AssetUtil.parseSymbol(cybAsset.symbol));
        } else {
            /**
             * fix bug:CYM-419
             * 未保留有效精度
             */
            feeStr = String.format("%s %s", AssetUtil.formatNumberRounding(feeAmount.amount / Math.pow(10, base.precision), base.precision), AssetUtil.parseSymbol(base.symbol));
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
        if(!BitsharesWalletWraper.getInstance().is_locked()){
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

    private void toCancelLimitOrder(FeeAmountObject feeAmount){
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    Operations.limit_order_cancel_operation operation = BitsharesWalletWraper.getInstance().
                            getLimitOrderCancelOperation(mFullAccountObject.account.id, ObjectId.create_from_string(feeAmount.asset_id),
                                    mCurrOpenOrderItem.openOrder.getLimitOrder().id, feeAmount.amount);
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

    private AccountBalanceObject getBalance(String assetId, FullAccountObject fullAccount){
        if(assetId == null || fullAccount == null){
            return null;
        }
        List<AccountBalanceObject> accountBalances = fullAccount.balances;
        if(accountBalances == null || accountBalances.size() == 0){
            return null;
        }
        AccountBalanceObject accountBalanceObject = null;
        for(AccountBalanceObject accountBalance : accountBalances){
            if(accountBalance.asset_type.toString().equals(assetId)){
                accountBalanceObject = accountBalance;
                break;
            }
        }
        return accountBalanceObject;
    }

}
