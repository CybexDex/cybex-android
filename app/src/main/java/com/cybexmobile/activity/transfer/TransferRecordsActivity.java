package com.cybexmobile.activity.transfer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.TransferRecordsRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.faucet.AssetsPair;
import com.cybexmobile.graphene.chain.AccountHistoryObject;
import com.cybexmobile.graphene.chain.BlockHeader;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.GlobalConfigObject;
import com.cybexmobile.graphene.chain.Operations;
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
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

public class TransferRecordsActivity extends BaseActivity implements TransferRecordsRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_records_rv_transfer_records)
    RecyclerView mRvTransferRecords;

    private TransferRecordsRecyclerViewAdapter mTransferRecordsAdapter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;

    private boolean mIsLoginIn;
    private String mName;
    private List<TransferHistoryItem> mTransferHistoryItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_transfer_records);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsLoginIn = preferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = preferences.getString(PREF_NAME, "");
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mRvTransferRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mTransferRecordsAdapter = new TransferRecordsRecyclerViewAdapter(this, mTransferHistoryItems);
        mTransferRecordsAdapter.setOnItemClickListener(this);
        mRvTransferRecords.setAdapter(mTransferRecordsAdapter);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onItemClick() {
        Intent intent = new Intent(this, TransferDetailsActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountHistory(Event.LoadAccountHistory event){
        List<AccountHistoryObject> accountHistoryObjects = event.getAccountHistoryObjects();
        if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
            return;
        }
        TransferHistoryItem item = null;
        Iterator<AccountHistoryObject> it = accountHistoryObjects.iterator();
        //过滤非交易记录 op4为交易记录
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        while (it.hasNext()){
            AccountHistoryObject accountHistoryObject = it.next();
            if(accountHistoryObject.op.get(0).getAsInt() == 0){
                item = new TransferHistoryItem();
                item.accountHistoryObject = accountHistoryObject;
                item.transferOperation = gson.fromJson(accountHistoryObject.op.get(1), Operations.transfer_operation.class);
                //加载区块信息
                item.callId = BitsharesWalletWraper.getInstance().get_call_id().getAndIncrement();
                //mWebSocketService.loadBlock(item.callId, item.accountHistoryObject.block_num);
                mTransferHistoryItems.add(item);
            } else {
                it.remove();
            }
        }
        mTransferRecordsAdapter.notifyDataSetChanged();
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

    public class TransferHistoryItem {
        public int callId;//通过callid对应请求结果
        public AccountHistoryObject accountHistoryObject;
        public Operations.transfer_operation transferOperation;
        public BlockHeader block;
    }
}
