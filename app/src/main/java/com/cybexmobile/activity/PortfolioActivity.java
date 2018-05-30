package com.cybexmobile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.adapter.PortfolioListRecyclerViewAdapter;
import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.graphene.chain.AccountBalanceObject;

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
        mPortfolioRecyclerView = findViewById(R.id.portfolio_page_recycler_view);
        mAccountBalanceObjectList = BitsharesWalletWraper.getInstance().getMyFullAccountInstance().get(0).balances;
        mPortfolioListAdapter = new PortfolioListRecyclerViewAdapter(mAccountBalanceObjectList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mPortfolioRecyclerView.setLayoutManager(layoutManager);
        mPortfolioRecyclerView.setAdapter(mPortfolioListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
