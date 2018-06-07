package com.cybexmobile.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.adapter.PortfolioListRecyclerViewAdapter;
import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.service.WebSocketService;

import java.util.ArrayList;
import java.util.List;

public class PortfolioActivity extends BaseActivity {
    RecyclerView mPortfolioRecyclerView;
    PortfolioListRecyclerViewAdapter mPortfolioListAdapter;
    List<AccountBalanceObject> mAccountBalanceObjectList;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        mPortfolioRecyclerView = findViewById(R.id.portfolio_page_recycler_view);
        mPortfolioListAdapter = new PortfolioListRecyclerViewAdapter(mAccountBalanceObjectList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPortfolioRecyclerView.setLayoutManager(layoutManager);
        mPortfolioRecyclerView.setAdapter(mPortfolioListAdapter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            WebSocketService webSocketService = binder.getService();
            mAccountBalanceObjectList = webSocketService.getFullAccount(true).balances;
            if(mPortfolioListAdapter != null){
                mPortfolioListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
