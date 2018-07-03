package com.cybexmobile.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.cybexmobile.R;
import com.cybexmobile.adapter.CommonRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LockUpAssetObject;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

import static com.cybexmobile.utils.Constant.PREF_NAME;
import static com.cybexmobile.utils.Constant.PREF_PASSWORD;

public class LockAssetsActivity extends BaseActivity {

    private static final String TAG = "LockAssetsActivity";

    //无数据
    private static final int MESSAGE_WHAT_NO_DATA = 1;
    //刷新整个列表
    private static final int MESSAGE_WHAT_NOTIFY_DATA = 2;

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    CommonRecyclerViewAdapter mAdapter;
    private List<String> mAddresses = new ArrayList<>();
    private List<LockUpAssetItem> mLockUpAssetItems = new ArrayList<>();

    private WebSocketService mWebSocketService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_assets);
        EventBus.getDefault().register(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        initViews();
        loadData();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mLockUpAssetItems != null && mLockUpAssetItems.size() > 0){
                for (LockUpAssetItem item : mLockUpAssetItems) {
                    item.assetObject = mWebSocketService == null ? null : mWebSocketService.getAssetObject(item.lockUpAssetobject.balance.asset_id.toString());
                    item.cybRmbPrice = mWebSocketService == null ? 0 : mWebSocketService.getAssetRmbPrice("CYB").getValue();
                }
                mHandler.sendEmptyMessage(MESSAGE_WHAT_NOTIFY_DATA);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void loadData(){
        showLoadDialog(true);
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreference.getString(PREF_NAME, "");
        String password = sharedPreference.getString(PREF_PASSWORD,"");
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

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommonRecyclerViewAdapter(mLockUpAssetItems);
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

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_WHAT_NOTIFY_DATA:
                    mAdapter.notifyDataSetChanged();
                case MESSAGE_WHAT_NO_DATA:
                    hideLoadDialog();
                    break;
            }
        }
    };

    private WebSocketClient.MessageCallback mLockupAssetCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LockUpAssetObject>>>(){

        @Override
        public void onMessage(WebSocketClient.Reply<List<LockUpAssetObject>> reply) {
            List<LockUpAssetObject> lockUpAssetObjects = reply.result;
            if(lockUpAssetObjects == null || lockUpAssetObjects.size() == 0){
                mHandler.sendEmptyMessage(MESSAGE_WHAT_NO_DATA);
                return;
            }
            EventBus.getDefault().post(new Event.ThreadScheduler<>(lockUpAssetObjects));
        }

        @Override
        public void onFailure() {

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThreadScheduler(Event.ThreadScheduler<List<LockUpAssetObject>> event){
        List<LockUpAssetObject> lockUpAssetObjects = event.getData();
        for (LockUpAssetObject lockUpAssetObject : lockUpAssetObjects) {
            long timeStamp = getTimeStamp(lockUpAssetObject.vesting_policy.begin_timestamp);
            long currentTimeStamp = System.currentTimeMillis();
            long duration = lockUpAssetObject.vesting_policy.vesting_duration_seconds;
            if (timeStamp + duration * 1000 > currentTimeStamp) {
                LockUpAssetItem item = new LockUpAssetItem();
                item.lockUpAssetobject = lockUpAssetObject;
                if(mWebSocketService != null){
                    item.assetObject = mWebSocketService.getAssetObject(lockUpAssetObject.balance.asset_id.toString());
                    AssetRmbPrice rmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                    item.cybRmbPrice = rmbPrice == null ? 0 : rmbPrice.getValue();
                }
                mLockUpAssetItems.add(item);
            }
        }
        if(mWebSocketService != null){
            mHandler.sendEmptyMessage(MESSAGE_WHAT_NOTIFY_DATA);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAsset(Event.LoadAsset event){
        AssetObject assetObject = event.getData();
        if(assetObject == null){
            return;
        }
        for(int i=0; i < mLockUpAssetItems.size(); i++){
            LockUpAssetItem item = mLockUpAssetItems.get(i);
            if(item.lockUpAssetobject.balance.asset_id.toString().equals(assetObject.id.toString())){
                item.assetObject = assetObject;
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event){
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if(assetRmbPrices == null || assetRmbPrices.size() == 0){
            return;
        }
        for(AssetRmbPrice assetRmbPrice : assetRmbPrices){
            if(assetRmbPrice.getName().equals("CYB")){
                for(LockUpAssetItem item : mLockUpAssetItems){
                    item.cybRmbPrice = assetRmbPrice.getValue();
                }
             break;
            }
        }
        mAdapter.notifyDataSetChanged();
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
    }

    public class LockUpAssetItem {
        public LockUpAssetObject lockUpAssetobject;
        public AssetObject assetObject;
        public double cybRmbPrice;
    }
}
