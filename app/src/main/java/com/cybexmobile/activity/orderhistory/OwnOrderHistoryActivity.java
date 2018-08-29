package com.cybexmobile.activity.orderhistory;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.R;
import com.cybexmobile.adapter.OwnOrderHistoryRecyclerViewAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybexmobile.faucet.AssetsPair;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybexmobile.graphene.chain.OrderHistory;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybexmobile.service.WebSocketService;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

public class OwnOrderHistoryActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.order_history_rv)
    RecyclerView mRvOrderHistory;

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;

    private OwnOrderHistoryRecyclerViewAdapter mOwnOrderHistoryRecyclerViewAdapter;

    private List<OrderHistoryItem> mOrderHistoryItems = new ArrayList<>();

    private boolean mIsLoginIn;
    private String mName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_order_history);
        EventBus.getDefault().register(this);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsLoginIn = sharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = sharedPreferences.getString(PREF_NAME, null);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        mRvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        mRvOrderHistory.setItemAnimator(null);
        mRvOrderHistory.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mOwnOrderHistoryRecyclerViewAdapter = new OwnOrderHistoryRecyclerViewAdapter(this, mOrderHistoryItems);
        mRvOrderHistory.setAdapter(mOwnOrderHistoryRecyclerViewAdapter);
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
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        EventBus.getDefault().unregister(this);
        unbindService(mConnection);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
       FullAccountObject fullAccountObject = event.getFullAccount();
       mWebSocketService.loadAccountHistory(fullAccountObject.account.id, 100);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountHistory(Event.LoadAccountHistory event){
        List<AccountHistoryObject> accountHistoryObjects = event.getAccountHistoryObjects();
        if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
            return;
        }
        OrderHistoryItem item = null;
        Iterator<AccountHistoryObject> it = accountHistoryObjects.iterator();
        //过滤非交易记录 op4为交易记录
        Gson gson = new Gson();
        Map<String, List<AssetsPair>> assetPairs = mWebSocketService.getAssetPairHashMap();
        while (it.hasNext()){
            AccountHistoryObject accountHistoryObject = it.next();
            if(accountHistoryObject.op.get(0).getAsInt() == 4){
                item = new OrderHistoryItem();
                item.accountHistoryObject = accountHistoryObject;
                item.orderHistory = gson.fromJson(accountHistoryObject.op.get(1), OrderHistory.class);
                /**
                 * fix bug:CYM-443
                 * 交易历史时间字段值不停在闪动
                 */
                /**
                 * fix bug:CYM-447
                 * 丢失成交历史记录
                 */
                if(hasExist(item.accountHistoryObject.id, mOrderHistoryItems)){
                    item = null;
                    it.remove();
                    continue;
                }
                parseBuyOrSell(item, assetPairs);
                //加载区块信息
                item.callId = BitsharesWalletWraper.getInstance().get_call_id().getAndIncrement();
                mWebSocketService.loadBlock(item.callId, item.accountHistoryObject.block_num);
                mOrderHistoryItems.add(item);
            } else {
                it.remove();
            }
        }
        mOwnOrderHistoryRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadBlock(Event.LoadBlock event){
        if(mOrderHistoryItems == null || mOrderHistoryItems.size() == 0){
            return;
        }
        event.getCallId();
        for(int i = 0; i < mOrderHistoryItems.size(); i++){
            if(mOrderHistoryItems.get(i).callId == event.getCallId()){
                mOrderHistoryItems.get(i).block = event.getBlockHeader();
                mOwnOrderHistoryRecyclerViewAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private boolean hasExist(String id, List<OrderHistoryItem> orderHistories){
        for(OrderHistoryItem item : orderHistories){
            if(item.accountHistoryObject.id.equals(id)){
                return true;
            }
        }
        return false;
    }

    private void parseBuyOrSell(OrderHistoryItem item, Map<String, List<AssetsPair>> assetPairs){
        String payAssetId = item.orderHistory.pays.asset_id;
        String receiveAssetId = item.orderHistory.receives.asset_id;
        here:
        for(Map.Entry<String, List<AssetsPair>> entry : assetPairs.entrySet()){
            //支付baseAssetId 接收quoteAssetId 为买单
            if(entry.getKey().equals(payAssetId)){
                for(AssetsPair assetsPair : entry.getValue()){
                    if(assetsPair.getQuote().equals(receiveAssetId)){
                        item.isSell = false;
                        item.baseAsset = assetsPair.getBaseAsset();
                        item.quoteAsset = assetsPair.getQuoteAsset();
                        break here;
                    }
                }
            }
            //支付quoteAssetId 接收baseAssetId 为卖单
            if(entry.getKey().equals(receiveAssetId)){
                for(AssetsPair assetsPair : entry.getValue()){
                    if(assetsPair.getQuote().equals(payAssetId)){
                        item.isSell = true;
                        item.baseAsset = assetsPair.getBaseAsset();
                        item.quoteAsset = assetsPair.getQuoteAsset();
                        break here;
                    }
                }
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mIsLoginIn){
                FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
                if(fullAccountObject != null){
                    mWebSocketService.loadAccountHistory(fullAccountObject.account.id, 100);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

   public class OrderHistoryItem {
        public int callId;//通过callid对应请求结果
        public boolean isSell;
        public AccountHistoryObject accountHistoryObject;
        public OrderHistory orderHistory;
        public AssetObject baseAsset;
        public AssetObject quoteAsset;
        public BlockHeader block;
   }
}
