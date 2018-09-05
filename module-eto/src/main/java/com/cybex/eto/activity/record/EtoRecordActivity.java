package com.cybex.eto.activity.record;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybex.eto.R;
import com.cybex.eto.adapter.EtoRecordsRecyclerViewAdapter;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.http.entity.EtoRecord;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class EtoRecordActivity extends EtoBaseActivity implements EtoRecordMvpView{

    private Toolbar mToolbar;
    private RecyclerView mRvEtoRecords;

    @Inject
    EtoRecordPresenter<EtoRecordMvpView> mEtoRecordsPresenter;

    private EtoRecordsRecyclerViewAdapter mEtoRecordsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eto_records);
        initView();
        setSupportActionBar(mToolbar);
        etoActivityComponent().inject(this);
        mEtoRecordsPresenter.attachView(this);
        mRvEtoRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mEtoRecordsAdapter = new EtoRecordsRecyclerViewAdapter(this, new ArrayList<EtoRecord>());
        mRvEtoRecords.setAdapter(mEtoRecordsAdapter);
    }

    private void initView(){
        mToolbar = findViewById(R.id.toolbar);
        mRvEtoRecords = findViewById(R.id.eto_rv_records);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEtoRecordsPresenter.loadEtoRecords("test1", 1, 20);
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
    public void onLoadEtoRecords(List<EtoRecord> etoRecords) {
        mEtoRecordsAdapter.setData(etoRecords);
    }
}
