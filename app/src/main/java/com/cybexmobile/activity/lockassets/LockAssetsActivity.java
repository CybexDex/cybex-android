package com.cybexmobile.activity.lockassets;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.activity.transfer.TransferActivity;
import com.cybexmobile.adapter.CommonRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LockAssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;
import com.cybex.basemodule.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_USDT;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_USDT;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.provider.graphene.chain.Operations.ID_BALANCE_CLAIM_OPERATION;

public class LockAssetsActivity extends BaseActivity implements CommonRecyclerViewAdapter.OnClickLockAssetItemListener {

    private static final String TAG = "LockAssetsActivity";

    //无数据
    private static final int MESSAGE_WHAT_NO_DATA = 1;
    //刷新整个列表
    private static final int MESSAGE_WHAT_NOTIFY_DATA = 2;

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    CommonRecyclerViewAdapter mAdapter;
    private List<String> mAddresses = new ArrayList<>();
    private List<LockAssetItem> mLockAssetItems = new ArrayList<>();
    private List<WatchlistData> mWatchlistDataList = new ArrayList<>();

    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;
    private String mName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_assets);
        EventBus.getDefault().register(this);
        mName = getIntent().getStringExtra(INTENT_PARAM_NAME);
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        initViews();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadFullAccount(Event.UpdateFullAccount event) {
        if (mAccountObject == null) {
            mAccountObject = event.getFullAccount().account;
            checkIfLocked(mName);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
            mWatchlistDataList = mWebSocketService.getAllWatchlistData();
            if (fullAccountObject != null) {
                mAccountObject = fullAccountObject.account;
                checkIfLocked(mName);
            }
            if (mLockAssetItems != null && mLockAssetItems.size() > 0) {
                for (LockAssetItem item : mLockAssetItems) {
                    item.assetObject = mWebSocketService == null ? null : mWebSocketService.getAssetObject(item.lockAssetobject.balance.asset_id.toString());
                    calculateItemRmbPrice(item, item.lockAssetobject, mWatchlistDataList);
                }
                mHandler.sendEmptyMessage(MESSAGE_WHAT_NOTIFY_DATA);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void checkIfLocked(String userName) {
        if (mAccountObject == null) {
            return;
        }
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject,
                    userName, new UnlockDialog.UnLockDialogClickListener() {
                        @Override
                        public void onUnLocked(String password) {
                            showLoadDialog(true);
                            loadData(userName, password);
                        }
                    });
        } else {
            showLoadDialog(true);
            loadData(userName, BitsharesWalletWraper.getInstance().getPassword());
        }
    }

    private void loadData(String name, String password) {
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> e) {
                e.isDisposed();
                e.onNext(BitsharesWalletWraper.getInstance().getAddressList(name, password));
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v(TAG, "onSubscribe");
                    }

                    @Override
                    public void onNext(List<String> strings) {
                        Log.v(TAG, "onNext");

                        mAddresses.clear();
                        mAddresses.addAll(strings);

                        try {
                            BitsharesWalletWraper.getInstance().get_balance_objects(mAddresses, mLockupAssetCallback);
                        } catch (NetworkStatusException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(TAG, "onError");
                        hideLoadDialog();
                    }

                    @Override
                    public void onComplete() {
                        Log.v(TAG, "onComplete");

                    }
                });

    }

    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommonRecyclerViewAdapter(mLockAssetItems, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private long getTimeStamp(String strTimeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        TimeZone mTimeZone = calendar.getTimeZone();
        int mOffset = mTimeZone.getRawOffset();
        try {
            Date parsedDate = dateFormat.parse(strTimeStamp);
            return parsedDate.getTime() + mOffset;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_WHAT_NOTIFY_DATA:
                    Object obj = msg.obj;
                    if (obj != null) {
                        mAdapter.notifyItemChanged((Integer) obj);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                case MESSAGE_WHAT_NO_DATA:
                    hideLoadDialog();
                    break;
            }
        }
    };

    private MessageCallback mLockupAssetCallback = new MessageCallback<Reply<List<LockAssetObject>>>() {

        @Override
        public void onMessage(Reply<List<LockAssetObject>> reply) {
            List<LockAssetObject> lockAssetObjects = reply.result;
            if (lockAssetObjects == null || lockAssetObjects.size() == 0) {
                mHandler.sendEmptyMessage(MESSAGE_WHAT_NO_DATA);
                return;
            }
            EventBus.getDefault().post(new Event.ThreadScheduler<>(lockAssetObjects));
        }

        @Override
        public void onFailure() {

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThreadScheduler(Event.ThreadScheduler<List<LockAssetObject>> event) {
        List<LockAssetObject> lockAssetObjects = event.getData();
        for (LockAssetObject lockAssetObject : lockAssetObjects) {
            long timeStamp = getTimeStamp(lockAssetObject.vesting_policy.begin_timestamp);
            long currentTimeStamp = System.currentTimeMillis();
            long duration = lockAssetObject.vesting_policy.vesting_duration_seconds;
                LockAssetItem item = new LockAssetItem();
                item.lockAssetobject = lockAssetObject;
                if (mWebSocketService != null) {
                    item.assetObject = mWebSocketService.getAssetObject(lockAssetObject.balance.asset_id.toString());
                    calculateItemRmbPrice(item, item.lockAssetobject, mWatchlistDataList);
                    mLockAssetItems.add(item);
                }
        }
        if (mWebSocketService != null) {
            mHandler.sendEmptyMessage(MESSAGE_WHAT_NOTIFY_DATA);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAsset(Event.LoadAsset event) {
        AssetObject assetObject = event.getData();
        if (assetObject == null) {
            return;
        }
        for (int i = 0; i < mLockAssetItems.size(); i++) {
            LockAssetItem item = mLockAssetItems.get(i);
            if (item.lockAssetobject.balance.asset_id.toString().equals(assetObject.id.toString()) && item.assetObject == null) {
                item.assetObject = assetObject;
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        if (mLockAssetItems != null && mLockAssetItems.size() > 0) {
            for (LockAssetItem item : mLockAssetItems) {
                calculateItemRmbPrice(item, item.lockAssetobject, mWatchlistDataList);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void calculateItemRmbPrice(LockAssetItem lockAssetItem, LockAssetObject lockAssetObject, List<WatchlistData> watchlistDataList) {
        if (watchlistDataList == null || watchlistDataList.size() == 0) {
            return;
        }
        if (lockAssetObject.balance.asset_id.toString().equals(ASSET_ID_CYB)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_CYB);
            lockAssetItem.itemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (lockAssetObject.balance.asset_id.toString().equals(ASSET_ID_ETH)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_ETH);
            lockAssetItem.itemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (lockAssetObject.balance.asset_id.toString().equals(ASSET_ID_USDT)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_USDT);
            lockAssetItem.itemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else if (lockAssetObject.balance.asset_id.toString().equals(ASSET_ID_BTC)) {
            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice(ASSET_SYMBOL_BTC);
            lockAssetItem.itemRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
        } else {
            for (WatchlistData watchlistData : watchlistDataList) {
                if (watchlistData.getQuoteId().equals(lockAssetObject.balance.asset_id.toString())) {
                    lockAssetItem.itemRmbPrice = watchlistData.getRmbPrice() * watchlistData.getCurrentPrice();
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(LockAssetItem lockAssetItem) {
        CybexDialog.showBalanceClaimDialog(this, String.format("%s %s", AssetUtil.formatNumberRounding(lockAssetItem.lockAssetobject.balance.amount / Math.pow(10, lockAssetItem.assetObject.precision), lockAssetItem.assetObject.precision),
                AssetUtil.parseSymbol(lockAssetItem.assetObject.symbol)), mAccountObject.name, new CybexDialog.ConfirmationDialogClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                if (mAccountObject == null) {
                    return;
                }
                if (BitsharesWalletWraper.getInstance().is_locked()) {
                    CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mName
                            , new UnlockDialog.UnLockDialogClickListener() {
                                @Override
                                public void onUnLocked(String password) {
                                    broadcastOperation(lockAssetItem);
                                }
                            });
                } else {
                    broadcastOperation(lockAssetItem);
                }
            }
        });

    }

    private void broadcastOperation(LockAssetItem lockAssetItem) {
        Types.public_key_type public_key_type = BitsharesWalletWraper.getInstance().getPublicKeyFromAddress(lockAssetItem.lockAssetobject.owner);
        try {
            Operations.balance_claim_operation operation = BitsharesWalletWraper.getInstance().getBalanceClaimOperation(
                    0,
                    ObjectId.create_from_string("1.3.0"),
                    mAccountObject.id,
                    lockAssetItem.lockAssetobject.id,
                    public_key_type,
                    (long)lockAssetItem.lockAssetobject.balance.amount,
                    lockAssetItem.lockAssetobject.balance.asset_id
            );
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mAccountObject,
                            operation,
                            ID_BALANCE_CLAIM_OPERATION,
                            reply.result
                    );
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mBalanceClaimCallBack);
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

    MessageCallback<Reply<String>> mBalanceClaimCallBack = new MessageCallback<Reply<String>>() {
        @Override
        public void onMessage(Reply<String> reply) {
            EventBus.getDefault().post(new Event.BalanceClaim(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.BalanceClaim(false));

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBalanceClaim(Event.BalanceClaim balanceClaim) {
        if (balanceClaim.isSuccess()) {
            ToastMessage.showNotEnableDepositToastMessage(
                    LockAssetsActivity.this,
                    getResources().getString(R.string.toast_message_balance_claim_succeeded),
                    R.drawable.ic_check_circle_green);
            if (mLockAssetItems != null && mLockAssetItems.size() > 0) {
                mLockAssetItems.clear();
                loadData(mName, BitsharesWalletWraper.getInstance().getPassword());
            }
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_balance_claim_failed), R.drawable.ic_error_16px);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
    }

    public class LockAssetItem {
        public LockAssetObject lockAssetobject;
        public AssetObject assetObject;
        public double itemRmbPrice;
    }
}
