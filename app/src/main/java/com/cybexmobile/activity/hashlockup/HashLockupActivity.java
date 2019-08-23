package com.cybexmobile.activity.hashlockup;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.HtlcAdapterItemObject;
import com.cybex.provider.graphene.chain.HtlcObject;
import com.cybexmobile.R;
import com.cybexmobile.injection.base.AppBaseActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class HashLockupActivity extends BaseActivity implements HashLockUpView, OnRefreshListener {

    @Inject
    HashLockUpPresenter<HashLockUpView> mHashLockUpPresenter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private List<HtlcAdapterItemObject> mHtlcList = new ArrayList<>();
    private HashLockUpAdapter mHashLockUpAdapter;
    private FullAccountObject mFullAccountobject;

    private String mAccountName;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mFullAccountobject = mWebSocketService.getFullAccount(mAccountName);
            mRefreshLayout.autoRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.hash_lockup_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.hash_lockup_refresh_layout)
    SmartRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_lockup);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRefreshLayout.setOnRefreshListener(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unbindService(mConnection);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mHtlcList.clear();
        setItemList(mFullAccountobject);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        AccountObject accountObject = event.getAccountObject();
        if(accountObject == null){
            return;
        }
        for (HtlcAdapterItemObject htlcAdapterItemObject : mHtlcList) {
            if (htlcAdapterItemObject.getHtlcObject().transfer.from.toString().equals(accountObject.id.toString())) {
                if (htlcAdapterItemObject.getFrom() != null) {
                    continue;
                }
                htlcAdapterItemObject.setFrom(accountObject.name);
                mHashLockUpAdapter.notifyDataSetChanged();
                break;
            }
            if (htlcAdapterItemObject.getHtlcObject().transfer.to.toString().equals(accountObject.id.toString())) {
                if (htlcAdapterItemObject.getTo() != null) {
                    continue;
                }
                htlcAdapterItemObject.setTo(accountObject.name);
                mHashLockUpAdapter.notifyDataSetChanged();
                break;
            }

        }
    }

    private void setItemList(FullAccountObject fullAccountObject) {
        if (fullAccountObject == null) {
            return;
        }
        List<HtlcObject> htlcObjectList = fullAccountObject.htlcs;
        if (htlcObjectList != null && htlcObjectList.size() != 0) {
            for (HtlcObject htlcObject : htlcObjectList) {
                HtlcAdapterItemObject htlcAdapterItemObject = new HtlcAdapterItemObject();
                htlcAdapterItemObject.setAssetObject(mWebSocketService.getAssetObject(htlcObject.transfer.asset_id.toString()));
                htlcAdapterItemObject.setHtlcObject(htlcObject);
                mHtlcList.add(htlcAdapterItemObject);
                mWebSocketService.loadAccountObject(htlcObject.transfer.from.toString());
                mWebSocketService.loadAccountObject(htlcObject.transfer.to.toString());
            }
            if (mHashLockUpAdapter == null) {
                mHashLockUpAdapter = new HashLockUpAdapter(mHtlcList, getContext());
                mRecyclerView.setAdapter(mHashLockUpAdapter);
            } else {
                mHashLockUpAdapter.notifyDataSetChanged();
            }
        }
        mRefreshLayout.finishRefresh();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }
}
