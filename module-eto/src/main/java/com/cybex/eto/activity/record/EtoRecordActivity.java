package com.cybex.eto.activity.record;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.eto.R;
import com.cybex.eto.adapter.EtoRecordsRecyclerViewAdapter;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.http.entity.EtoRecord;
import com.cybex.provider.http.entity.NewEtoRecord;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.eto.activity.record.EtoRecordPresenter.LOAD_MORE;
import static com.cybex.eto.activity.record.EtoRecordPresenter.LOAD_REFRESH;

public class EtoRecordActivity extends EtoBaseActivity implements EtoRecordMvpView,
        OnRefreshListener, OnLoadMoreListener {

    private static final int LOAD_COUNT = 20;

    private Toolbar mToolbar;
    private RecyclerView mRvEtoRecords;

    @Inject
    EtoRecordPresenter<EtoRecordMvpView> mEtoRecordsPresenter;

    private EtoRecordsRecyclerViewAdapter mEtoRecordsAdapter;
    private SmartRefreshLayout mRefreshLayout;
    private WebSocketService mWebSocketService;

    private List<NewEtoRecord> mEtoRecords;
    private String mUserName;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();

            mRefreshLayout.autoRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_records);
        initView();
        setSupportActionBar(mToolbar);
        etoActivityComponent().inject(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mEtoRecordsPresenter.attachView(this);
        mRvEtoRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar);
        mRvEtoRecords = findViewById(R.id.eto_rv_records);
        mRefreshLayout = findViewById(R.id.eto_records_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onNoUserError(String message) {
        ToastMessage.showNotEnableDepositToastMessage(this, message, R.drawable.ic_error_16px);
        mRefreshLayout.finishRefresh();
    }

    @Override
    public void onLoadEtoRecords(int mode, List<NewEtoRecord> etoRecords) {
        if (mode == LOAD_REFRESH) {
            mEtoRecords = etoRecords;
            mRefreshLayout.finishRefresh();
        } else if (mode == LOAD_MORE) {
            mEtoRecords.addAll(etoRecords);
            mRefreshLayout.finishLoadMore();
        }

        for (NewEtoRecord newEtoRecord : mEtoRecords) {
            newEtoRecord.setPayAssetObject(mWebSocketService.getAssetObject(newEtoRecord.getPayAssetID()));
            newEtoRecord.setReceiveAssetObject(mWebSocketService.getAssetObject(newEtoRecord.getReceiveAssetID()));
        }
        if (mEtoRecordsAdapter == null) {
            mEtoRecordsAdapter = new EtoRecordsRecyclerViewAdapter(this, mEtoRecords);
            mRvEtoRecords.setAdapter(mEtoRecordsAdapter);
        } else {
            mEtoRecordsAdapter.setData(mEtoRecords);
        }

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        loadMoreEtoRecords();
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshEtoRecords();
    }

    private void refreshEtoRecords() {
        if (TextUtils.isEmpty(mUserName)) {
            mRefreshLayout.finishRefresh();
            mEtoRecordsAdapter = new EtoRecordsRecyclerViewAdapter(this, mEtoRecords);
            mRvEtoRecords.setAdapter(mEtoRecordsAdapter);
            return;
        }
        int limit = mEtoRecords == null || mEtoRecords.size() <= LOAD_COUNT ?
                LOAD_COUNT : (mEtoRecords.size() / LOAD_COUNT +
                (mEtoRecords.size() % LOAD_COUNT == 0 ? 0 : 1)) * LOAD_COUNT;
        mEtoRecordsPresenter.loadEtoRecords(LOAD_REFRESH, mUserName, 1, limit);
    }

    private void loadMoreEtoRecords() {
        if (TextUtils.isEmpty(mUserName)) {
            mRefreshLayout.finishLoadMore();
            return;
        }
        if (mEtoRecords == null || mEtoRecords.size() == 0 || mEtoRecords.size() % LOAD_COUNT != 0) {
            mRefreshLayout.finishLoadMore();
            mRefreshLayout.setNoMoreData(true);
            return;
        }
        int page = mEtoRecords.size() / LOAD_COUNT + 1;
        mEtoRecordsPresenter.loadEtoRecords(LOAD_MORE, mUserName, page, LOAD_COUNT);
    }
}
