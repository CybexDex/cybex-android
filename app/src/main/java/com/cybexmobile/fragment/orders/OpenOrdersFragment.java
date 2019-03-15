package com.cybexmobile.fragment.orders;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.dialog.UnlockDialogWithEnotes;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.apihk.LimitOrderWrapper;
import com.cybexmobile.R;
import com.cybexmobile.adapter.OpenOrderRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.enotes.sdk.core.CardManager;
import io.enotes.sdk.utils.ReaderUtils;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_IS_LOAD_ALL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_IS_LOAD_ALL;
import static com.cybex.provider.graphene.chain.Operations.ID_CANCEL_ALL_OPERATION;
import static com.cybex.provider.graphene.chain.Operations.ID_CANCEL_LMMIT_ORDER_OPERATION;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

/**
 * 委托（当前交易对，当前用户）
 *
 */
public class OpenOrdersFragment extends BaseFragment implements OpenOrderRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.open_orders_title_rl)
    RelativeLayout mOpenOrderTitleLayout;
    @BindView(R.id.open_orders_cancel_all)
    TextView mOpenOrdersCancelAll;
    @BindView(R.id.open_orders_recycler_view)
    RecyclerView mRvOpenOrders;

    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();
    private WatchlistData mWatchlistData;
    private FullAccountObject mFullAccount;
    private OpenOrderItem mCurrOpenOrderItem;

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;
    private OpenOrderRecyclerViewAdapter mOpenOrderRecyclerViewAdapter;

    private boolean mIsLoginIn;
    private String mName;
    private boolean mIsLoadAll;
    private int mCancelOperationId;
    private UnlockDialogWithEnotes mUnlockDialogWithEnotes;
    private FeeAmountObject mCancelLimitOrderFeeObject;
    private FeeAmountObject mCancelAllOrderFeeObject;
    private boolean mIsUsedCloudPassword = false;

    public static OpenOrdersFragment getInstance(WatchlistData watchlistData, boolean isLoadAll){
        OpenOrdersFragment fragment = new OpenOrdersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        bundle.putBoolean(INTENT_PARAM_IS_LOAD_ALL, isLoadAll);
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
            mIsLoadAll = bundle.getBoolean(INTENT_PARAM_IS_LOAD_ALL, false);
        }
        if(savedInstanceState != null){
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mFullAccount = (FullAccountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT);
            mIsLoadAll = savedInstanceState.getBoolean(BUNDLE_SAVE_IS_LOAD_ALL, false);
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
        mOpenOrderRecyclerViewAdapter = new OpenOrderRecyclerViewAdapter(getContext(), mOpenOrderItems);
        mOpenOrderRecyclerViewAdapter.setOnItemClickListener(this);
        mRvOpenOrders.setAdapter(mOpenOrderRecyclerViewAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
        outState.putSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT, mFullAccount);
        outState.putBoolean(BUNDLE_SAVE_IS_LOAD_ALL, mIsLoadAll);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        } else {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    /**
     * 流量优化
     * 界面不显示取消网络请求
     * @param hidden
     */
    public void onParentHiddenChanged(boolean hidden) {
        if(hidden) {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            return;
        }
        if(!this.isHidden()) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        mUnlockDialogWithEnotes = null;
        getContext().unbindService(mConnection);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick(R.id.open_orders_cancel_all)
    public void onCancelAllClicked(View view) {
        showLoadDialog();
        mCancelOperationId = ID_CANCEL_ALL_OPERATION;
        loadLimitOrderCancelFee(ASSET_ID_CYB, mCancelOperationId);
    }

    @Override
    public void onItemClick(OpenOrderItem itemValue) {
        showLoadDialog();
        mCancelOperationId = ID_CANCEL_LMMIT_ORDER_OPERATION;
        mCurrOpenOrderItem = itemValue;
        loadLimitOrderCancelFee(ASSET_ID_CYB, mCancelOperationId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadRequiredCancelFee(Event.LoadRequiredCancelFee event){
        mCancelLimitOrderFeeObject = event.getFee();
        AccountBalanceObject accountBalance = getBalance(mCancelLimitOrderFeeObject.asset_id, mFullAccount);
        if (mCancelLimitOrderFeeObject.asset_id.equals(ASSET_ID_CYB)) {
            if (accountBalance.balance >= mCancelLimitOrderFeeObject.amount) {//cyb足够扣手续费
                limitOrderCancelConfirm(mName, mCancelLimitOrderFeeObject);
            } else { //cyb不够扣手续费 扣取委单的base或者quote
                if (ASSET_ID_CYB.equals(mCurrOpenOrderItem.isSell ? mCurrOpenOrderItem.quoteAsset.id.toString() : mCurrOpenOrderItem.baseAsset.id.toString())) {
                    hideLoadDialog();
                    ToastMessage.showNotEnableDepositToastMessage(getActivity(),
                            getContext().getResources().getString(R.string.text_not_enough),
                            R.drawable.ic_error_16px);
                } else {
                    loadLimitOrderCancelFee(mCurrOpenOrderItem.isSell ? mCurrOpenOrderItem.quoteAsset.id.toString() : mCurrOpenOrderItem.baseAsset.id.toString(), mCancelOperationId);
                }
            }
        } else {
            if (accountBalance.balance > mCancelLimitOrderFeeObject.amount) {
                limitOrderCancelConfirm(mName, mCancelLimitOrderFeeObject);
            } else {
                hideLoadDialog();
                ToastMessage.showNotEnableDepositToastMessage(getActivity(),
                        getContext().getResources().getString(R.string.text_not_enough),
                        R.drawable.ic_error_16px);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadRequiredCancelAllFee(Event.LoadRequiredCancelAllFee event) {
        mCancelAllOrderFeeObject = event.getFee();
        AccountBalanceObject accountBalanceObject = getBalance(mCancelAllOrderFeeObject.asset_id, mFullAccount);
        if (mCancelAllOrderFeeObject.asset_id.equals(ASSET_ID_CYB)) {
            if (accountBalanceObject.balance >= mCancelAllOrderFeeObject.amount) {
                limitOrderCancelConfirm(mName, mCancelAllOrderFeeObject);
            } else {
                hideLoadDialog();
                ToastMessage.showNotEnableDepositToastMessage(getActivity(),
                        getContext().getResources().getString(R.string.text_not_enough),
                        R.drawable.ic_error_16px);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        mFullAccount = event.getFullAccount();
        loadLimitOrderData();
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
            mFullAccount = mWebSocketService.getFullAccount(mName);
            loadLimitOrderData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private MessageCallback<Reply<List<LimitOrder>>> mOpendLimitOrderCallback = new MessageCallback<Reply<List<LimitOrder>>>() {
        @Override
        public void onMessage(Reply<List<LimitOrder>> reply) {
            parseOpenOrderItems(reply.result);
            if (mOpenOrderItems != null && mOpenOrderItems.size() > 0) {
                mOpenOrderTitleLayout.setVisibility(View.VISIBLE);
            } else {
                mOpenOrderTitleLayout.setVisibility(View.GONE);
            }
            mOpenOrderRecyclerViewAdapter.setOpenOrderItems(mOpenOrderItems);
        }

        @Override
        public void onFailure() {

        }
    };

    /**
     * 交易对改变
     * @param watchlist
     */
    public void changeWatchlist(WatchlistData watchlist){
        if(watchlist == null){
            return;
        }
        this.mWatchlistData = watchlist;
        loadLimitOrderData();
    }

    public void loadLimitOrderCancelFee(String assetId, int operationId) {
        if (operationId == ID_CANCEL_ALL_OPERATION) {
            mWebSocketService.loadLimitOrderCancelAllFee(assetId, operationId,
            BitsharesWalletWraper.getInstance().getLimitOrderCancelAllOperation(ObjectId.create_from_string(ASSET_ID_CYB),
                    0, ObjectId.create_from_string(""), mIsLoadAll ? ObjectId.create_from_string(ASSET_ID_CYB) : ObjectId.create_from_string(mWatchlistData.getBaseId()),
                    mIsLoadAll ? ObjectId.create_from_string(ASSET_ID_CYB) : ObjectId.create_from_string(mWatchlistData.getQuoteId())));
        } else {
            mWebSocketService.loadLimitOrderCancelFee(assetId, operationId,
                    BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(ObjectId.create_from_string(""),
                            ObjectId.create_from_string(ASSET_ID_CYB),
                            mCurrOpenOrderItem.baseAsset.id,
                            mCurrOpenOrderItem.quoteAsset.id, 0, 0, 0));
        }
    }

    private void limitOrderCancelConfirm(String userName, FeeAmountObject feeAmount){
        hideLoadDialog();
        CybexDialog.showLimitOrderCancelConfirmationDialog(getContext(),
                mCancelOperationId == ID_CANCEL_LMMIT_ORDER_OPERATION ? getResources().getString(R.string.dialog_text_confirm_order_cancellation) : getResources().getString(R.string.dialog_text_confirm_all_orders_cancellation),
                null,
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        mIsUsedCloudPassword = false;
                        checkIfLocked(userName, feeAmount);
                    }
                });
    }

    private void checkIfLocked(String userName, FeeAmountObject feeAmount) {
        if (isLoginFromENotes()) {
            mUnlockDialogWithEnotes = CybexDialog.showUnlockWithEnotesWalletDialog(getFragmentManager(), mFullAccount
                            .account, userName,
                    new UnlockDialogWithEnotes.UnLockDialogClickListener() {
                        @Override
                        public void onUnLocked(String password) {
                            if (password != null) {
                                mIsUsedCloudPassword = true;
                                toCancelLimitOrder();
                            }
                        }
                    }, new UnlockDialogWithEnotes.OnDismissListener() {
                        @Override
                        public void onDismiss(int result) {

                        }
                    });
        } else {
            if (!BitsharesWalletWraper.getInstance().is_locked()) {
                showLoadDialog();
                toCancelLimitOrder();
                return;
            }
            CybexDialog.showUnlockWalletDialog(getFragmentManager(), mFullAccount.account, userName, new UnlockDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    showLoadDialog();
                    toCancelLimitOrder();
                }
            });
        }
    }

    public void toCancelLimitOrder() {
        BaseActivity activity = (BaseActivity) getActivity();
        try {
            if (isLoginFromENotes() && !mIsUsedCloudPassword) {

                CardManager cardManager = activity.cardManager;
                if (!cardManager.isConnected()) {
                    if (ReaderUtils.supportNfc(activity))
                        showToast(activity, getString(R.string.error_connect_card));
                    else
                        showToast(activity, getString(R.string.error_connect_card_ble));
                    return;
                }
                if (!cardManager.isPresent()) {
                    if (ReaderUtils.supportNfc(activity))
                        showToast(activity, getString(R.string.error_connect_card));
                    else
                        showToast(activity, getString(R.string.error_connect_card_ble));
                    return;
                }
                if (activity.currentCard != null && !activity.currentCard.getCurrencyPubKey().equals(getLoginPublicKey())) {
                    showToast(activity, getString(R.string.please_right_card));
                    return;
                }
                if (BaseActivity.cardApp != null) {
                    activity.currentCard = BaseActivity.cardApp;
                }
            }
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = new SignedTransaction();
                    if (mCancelOperationId == ID_CANCEL_LMMIT_ORDER_OPERATION) {
                        Operations.limit_order_cancel_operation operation = BitsharesWalletWraper.getInstance().
                                getLimitOrderCancelOperation(mFullAccount.account.id, ObjectId.create_from_string(mCancelLimitOrderFeeObject.asset_id),
                                        mCurrOpenOrderItem.limitOrder.order_id, mCancelLimitOrderFeeObject.amount);
                        if (activity.currentCard == null || mIsUsedCloudPassword) {
                            signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                                    mFullAccount.account, operation, ID_CANCEL_LMMIT_ORDER_OPERATION, reply.result);
                        } else {
                            signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransactionByENotes(
                                    activity.cardManager,
                                    activity.currentCard,
                                    mFullAccount.account,
                                    operation,
                                    ID_CANCEL_LMMIT_ORDER_OPERATION,
                                    reply.result);
                        }
                    } else {
                        Operations.cancel_all_operation operation = BitsharesWalletWraper.getInstance().
                                getLimitOrderCancelAllOperation(ObjectId.create_from_string(mCancelAllOrderFeeObject.asset_id), mCancelAllOrderFeeObject.amount, mFullAccount.account.id,
                                        mIsLoadAll ? ObjectId.create_from_string(ASSET_ID_CYB) : ObjectId.create_from_string(mWatchlistData.getQuoteId()),
                                        mIsLoadAll ? ObjectId.create_from_string(ASSET_ID_CYB) : ObjectId.create_from_string(mWatchlistData.getBaseId()));
                        if (activity.currentCard == null || mIsUsedCloudPassword) {
                            signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                                    mFullAccount.account, operation, ID_CANCEL_ALL_OPERATION, reply.result);
                        } else {
                            signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransactionByENotes(
                                    activity.cardManager, activity.currentCard, mFullAccount.account, operation,
                                    ID_CANCEL_ALL_OPERATION, reply.result);
                        }

                    }
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, new MessageCallback<Reply<String>>() {
                            @Override
                            public void onMessage(Reply<String> reply) {
                                hideLoadDialog();
                                if (reply.result == null && reply.error == null) {
                                    ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                                            R.string.toast_message_cancel_order_successfully), R.drawable.ic_check_circle_green);
                                } else {
                                    ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                                            R.string.toast_message_cancel_order_failed), R.drawable.ic_error_16px);
                                }
                            }

                            @Override
                            public void onFailure() {
                                hideLoadDialog();
                                ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                                        R.string.toast_message_cancel_order_failed), R.drawable.ic_error_16px);
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

    private void loadLimitOrderData(){
        if(!mIsLoginIn || mFullAccount == null || (!mIsLoadAll && mWatchlistData == null)) {
            return;
        }
        if (mIsLoadAll) {
            LimitOrderWrapper.getInstance().get_opend_limit_orders(
                    mFullAccount.account.id.toString(),
                    mOpendLimitOrderCallback);
        } else {
            LimitOrderWrapper.getInstance().get_opend_market_limit_orders(
                    mFullAccount.account.id.toString(),
                    mWatchlistData.getBaseId(),
                    mWatchlistData.getQuoteId(),
                    mOpendLimitOrderCallback);
        }
    }

    private void clearLimitOrderData(){
        mOpenOrderItems.clear();
        mOpenOrderRecyclerViewAdapter.setOpenOrderItems(mOpenOrderItems);
    }

    private void parseOpenOrderItems(List<LimitOrder> limitOrders){
        mOpenOrderItems.clear();
        if(limitOrders == null || limitOrders.size() == 0){
            return;
        }
        for (LimitOrder limitOrder : limitOrders) {
            OpenOrderItem item = new OpenOrderItem();
            item.limitOrder = limitOrder;
            AssetsPair assetsPair = AssetPairCache.getInstance().getAssetPair(limitOrder.key.asset1, limitOrder.key.asset2);
            if (assetsPair == null) {
                continue;
            }
            item.isSell = limitOrder.is_sell ? limitOrder.key.asset2.equals(assetsPair.getBase()) : limitOrder.key.asset1.equals(assetsPair.getBase());
            item.baseAsset = assetsPair.getBaseAsset();
            item.quoteAsset = assetsPair.getQuoteAsset();
            if (mIsLoadAll) {
                if ((item.baseAsset.symbol.startsWith("CYB") || item.baseAsset.symbol.startsWith("JADE"))
                        && (item.quoteAsset.symbol.startsWith("CYB") || item.quoteAsset.symbol.startsWith("JADE"))) {
                    mOpenOrderItems.add(item);
                }
            } else {
                if ((item.baseAsset.symbol.startsWith("CYB") || item.baseAsset.symbol.startsWith("JADE") || item.baseAsset.symbol.startsWith("ARENA"))
                        && (item.quoteAsset.symbol.startsWith("CYB") || item.quoteAsset.symbol.startsWith("JADE") || item.quoteAsset.symbol.startsWith("ARENA"))) {
                    mOpenOrderItems.add(item);
                }
            }
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

    public void showProgress(){
        mUnlockDialogWithEnotes.showProgress();
    }

    public void hideProgress(){
        mUnlockDialogWithEnotes.hideProgress();
    }

    public void hideEnotesDialog(){
        mUnlockDialogWithEnotes.dismiss();
    }

    public UnlockDialogWithEnotes getUnlockDialog() {
        return mUnlockDialogWithEnotes;
    }
}
