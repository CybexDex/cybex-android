package com.cybex.eto.activity.record;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.cybex.eto.R;
import com.cybex.eto.adapter.EtoRecordsRecyclerViewAdapter;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.http.entity.EtoRecord;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

    private List<EtoRecord> mEtoRecords;
    private String mUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_records);
        initView();
        setSupportActionBar(mToolbar);
        etoActivityComponent().inject(this);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mEtoRecordsPresenter.attachView(this);
        mRvEtoRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRefreshLayout.autoRefresh();
    }

    private void initView(){
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
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onLoadEtoRecords(int mode, List<EtoRecord> etoRecords) {
        if(mode == LOAD_REFRESH){
            mEtoRecords = etoRecords;
            mRefreshLayout.finishRefresh();
        } else if(mode == LOAD_MORE){
            mEtoRecords.addAll(etoRecords);
            mRefreshLayout.finishLoadMore();
        }
        if(mEtoRecordsAdapter == null){
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

    private void refreshEtoRecords(){
        if(TextUtils.isEmpty(mUserName)){
            mRefreshLayout.finishRefresh();
            return;
        }
        int limit = mEtoRecords == null || mEtoRecords.size() <= LOAD_COUNT ?
                LOAD_COUNT : (mEtoRecords.size() / LOAD_COUNT +
                (mEtoRecords.size() % LOAD_COUNT == 0 ? 0 : 1)) * LOAD_COUNT;
        mEtoRecordsPresenter.loadEtoRecords(LOAD_REFRESH, mUserName, 1, limit);
    }

    private void loadMoreEtoRecords(){
        if(TextUtils.isEmpty(mUserName)){
            mRefreshLayout.finishLoadMore();
            return;
        }
        if(mEtoRecords == null || mEtoRecords.size() == 0 || mEtoRecords.size() % LOAD_COUNT != 0){
            mRefreshLayout.finishLoadMore();
            mRefreshLayout.setNoMoreData(true);
            return;
        }
        int page = mEtoRecords.size() / LOAD_COUNT + 1;
        mEtoRecordsPresenter.loadEtoRecords(LOAD_MORE, mUserName, page, LOAD_COUNT);
    }
}
