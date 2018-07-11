package com.cybexmobile.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cybexmobile.R;
import com.cybexmobile.adapter.ExchangeOpenOrderRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.DynamicGlobalPropertyObject;
import com.cybexmobile.graphene.chain.FeeAmountObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.graphene.chain.SignedTransaction;
import com.cybexmobile.market.OpenOrder;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.toast.message.ToastMessage;
import com.cybexmobile.utils.AssetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybexmobile.graphene.chain.Operations.ID_CANCEL_LMMIT_ORDER_OPERATION;
import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.ASSET_ID_CYB;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_FULL_ACCOUNT_OBJECT;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

/**
 * 交易界面当前用户当前交易对委单
 */
public class OpenOrdersFragment extends BaseFragment implements ExchangeOpenOrderRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.open_orders_recycler_view)
    RecyclerView mRvOpenOrders;

    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();
    private WatchlistData mWatchlistData;
    private FullAccountObject mFullAccount;
    private OpenOrderItem mCurrOpenOrderItem;

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;
    private ExchangeOpenOrderRecyclerViewAdapter mOpenOrderRecyclerViewAdapter;

    private boolean mIsLoginIn;
    private String mName;

    private List<String> mCompareSymbol = Arrays.asList(new String[]{"JADE.USDT", "JADE.ETH", "JADE.BTC", "CYB"});

    public static OpenOrdersFragment getInstance(WatchlistData watchlistData){
        OpenOrdersFragment fragment = new OpenOrdersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mIsLoginIn = sharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = sharedPreferences.getString(PREF_NAME, null);
        Bundle bundle = getArguments();
        if(bundle != null){
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
            mFullAccount = (FullAccountObject) bundle.getSerializable(INTENT_PARAM_FULL_ACCOUNT_OBJECT);
        }
        if(savedInstanceState != null){
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mFullAccount = (FullAccountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT);
        }
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_orders, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mRvOpenOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvOpenOrders.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOpenOrderRecyclerViewAdapter = new ExchangeOpenOrderRecyclerViewAdapter(getContext(), mOpenOrderItems);
        mOpenOrderRecyclerViewAdapter.setOnItemClickListener(this);
        mRvOpenOrders.setAdapter(mOpenOrderRecyclerViewAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getContext().unbindService(mConnection);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onItemClick(OpenOrderItem itemValue) {
        mCurrOpenOrderItem = itemValue;
        loadLimitOrderCancelFee(ASSET_ID_CYB);
    }

    /**
     * 交易对改变
     * @param watchlist
     */
    public void changeWatchlist(WatchlistData watchlist){
        if(watchlist == null){
            return;
        }
        this.mWatchlistData = watchlist;
        notifyRecyclerView();
    }

    public void loadLimitOrderCancelFee(String assetId){
        if(mWatchlistData == null){
            return;
        }
        mWebSocketService.loadLimitOrderCancelFee(assetId, ID_CANCEL_LMMIT_ORDER_OPERATION,
                BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(ObjectId.create_from_string(""),
                        ObjectId.create_from_string(ASSET_ID_CYB),
                        mWatchlistData.getBaseAsset().id,
                        mWatchlistData.getQuoteAsset().id,  0, 0, 0));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadRequiredCancelFee(Event.LoadRequiredCancelFee event){
        FeeAmountObject feeAmount = event.getFee();
        AccountBalanceObject accountBalance = getBalance(feeAmount.asset_id, mFullAccount);
        if(feeAmount.asset_id.equals(ASSET_ID_CYB)){
            if(accountBalance.balance >= feeAmount.amount){//cyb足够扣手续费
                limitOrderCancelConfirm(mName, feeAmount);
            } else { //cyb不够扣手续费 扣取委单的base或者quote
                if(ASSET_ID_CYB.equals(mCurrOpenOrderItem.openOrder.getBaseObject().id.toString())){
                    ToastMessage.showNotEnableDepositToastMessage(getActivity(),
                            getContext().getResources().getString(R.string.text_not_enough),
                            R.drawable.ic_error_16px);
                } else {
                    loadLimitOrderCancelFee(mCurrOpenOrderItem.openOrder.getBaseObject().id.toString());
                }
            }
        } else {
            if(accountBalance.balance > feeAmount.amount){
                limitOrderCancelConfirm(mName, feeAmount);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(getActivity(),
                        getContext().getResources().getString(R.string.text_not_enough),
                        R.drawable.ic_error_16px);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderCancel(Event.LimitOrderCancel event){
        if(event.isSuccess()){
            ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                    R.string.toast_message_cancel_order_successfully), R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                    R.string.toast_message_cancel_order_failed), R.drawable.ic_error_16px);
        }
    }

    private void limitOrderCancelConfirm(String userName, FeeAmountObject feeAmount){
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
            priceStr = String.format(AssetUtil.formatPrice(price) + " %s", price, AssetUtil.parseSymbol(quote.symbol));
            amountStr = String.format(AssetUtil.formatAmount(price) + " %s", amount, AssetUtil.parseSymbol(base.symbol));
            totalStr = String.format(AssetUtil.formatPrice(price) + " %s", total, AssetUtil.parseSymbol(quote.symbol));
        } else {
            double amount = limitOrderObject.sell_price.quote.amount / Math.pow(10, quote.precision);
            double price = (limitOrderObject.sell_price.base.amount / Math.pow(10, base.precision)) / amount;
            double total = limitOrderObject.for_sale / Math.pow(10, base.precision);
            priceStr = String.format(AssetUtil.formatPrice(price) + " %s", price, AssetUtil.parseSymbol(base.symbol));
            amountStr = String.format(AssetUtil.formatAmount(price) + " %s", amount, AssetUtil.parseSymbol(quote.symbol));
            totalStr = String.format(AssetUtil.formatPrice(price) + " %s", total, AssetUtil.parseSymbol(base.symbol));
        }
        if(feeAmount.asset_id.equals(ASSET_ID_CYB)){
            AssetObject cybAsset = mWebSocketService.getAssetObject(ASSET_ID_CYB);
            feeStr = String.format(Locale.US, String.format(Locale.US, "%%.%df %%s", cybAsset.precision),
                    feeAmount.amount/Math.pow(10, cybAsset.precision), AssetUtil.parseSymbol(cybAsset.symbol));
        } else {
            /**
             * fix bug:CYM-419
             * 未保留有效精度
             */
            feeStr = String.format(Locale.US, String.format(Locale.US, "%%.%df %%s", base.precision),
                    feeAmount.amount / Math.pow(10, base.precision), AssetUtil.parseSymbol(base.symbol));
        }
        CybexDialog.showLimitOrderCancelConfirmationDialog(getContext(), !mCurrOpenOrderItem.isSell,
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
        CybexDialog.showUnlockWalletDialog(getContext(), new CybexDialog.UnLockDialogClickListener() {
            @Override
            public void onClick(String password, Dialog dialog) {
                int result = BitsharesWalletWraper.getInstance().import_account_password(mFullAccount.account, userName, password);
                if (result == 0) {
                    dialog.dismiss();
                    toCancelLimitOrder(feeAmount);
                } else {
                    LinearLayout errorLayout = dialog.findViewById(R.id.unlock_wallet_dialog_error_layout);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void toCancelLimitOrder(FeeAmountObject feeAmount){
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    Operations.limit_order_cancel_operation operation = BitsharesWalletWraper.getInstance().
                            getLimitOrderCancelOperation(mFullAccount.account.id, ObjectId.create_from_string(feeAmount.asset_id),
                                    mCurrOpenOrderItem.openOrder.getLimitOrder().id, feeAmount.amount);
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFullAccount.account, operation, ID_CANCEL_LMMIT_ORDER_OPERATION, reply.result);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        mFullAccount = event.getFullAccount();
        List<LimitOrderObject> limitOrderObjects = mFullAccount == null ? null : mFullAccount.limit_orders;
        parseOpenOrderItems(limitOrderObjects);
        notifyRecyclerView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mName = event.getName();
        mIsLoginIn = true;
        loadLimitOrderData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event){
        mName = null;
        mIsLoginIn = false;
        clearLimitOrderData();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mIsLoginIn){
                loadLimitOrderData();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

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

    private void loadLimitOrderData(){
        mFullAccount = mWebSocketService.getFullAccount(mName);
        List<LimitOrderObject> limitOrderObjects = mFullAccount == null ? null : mFullAccount.limit_orders;
        parseOpenOrderItems(limitOrderObjects);
        notifyRecyclerView();
    }

    private void clearLimitOrderData(){
        mOpenOrderItems.clear();
        notifyRecyclerView();
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

    private void notifyRecyclerView(){
        if(mOpenOrderItems == null || mOpenOrderItems.size() == 0){
            return;
        }
        if(mWatchlistData == null){
            return;
        }
        List<OpenOrderItem> openOrderItems = new ArrayList<>();
        //过滤非当前交易对委单
        for(OpenOrderItem item : mOpenOrderItems){
            String baseId = item.openOrder.getLimitOrder().sell_price.base.asset_id.toString();
            String quoteId = item.openOrder.getLimitOrder().sell_price.quote.asset_id.toString();
            if(!(mWatchlistData.getBaseId().equals(baseId) || mWatchlistData.getBaseId().equals(quoteId)) ||
                    !(mWatchlistData.getQuoteId().equals(baseId) || mWatchlistData.getQuoteId().equals(quoteId))){
                continue;
            }
            openOrderItems.add(item);
        }
        mOpenOrderRecyclerViewAdapter.setOpenOrderItems(openOrderItems);
    }

    private void parseOpenOrderItems(List<LimitOrderObject> limitOrderObjects){
        if(limitOrderObjects == null || limitOrderObjects.size() == 0){
            return;
        }
        mOpenOrderItems.clear();
        for (LimitOrderObject limitOrderObject : limitOrderObjects) {
            String baseId = limitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
            OpenOrderItem item = new OpenOrderItem();
            OpenOrder openOrder = new OpenOrder();
            openOrder.setLimitOrder(limitOrderObject);
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
