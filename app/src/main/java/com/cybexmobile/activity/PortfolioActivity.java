package com.cybexmobile.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybexmobile.adapter.PortfolioRecyclerViewAdapter;
import com.cybexmobile.R;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;

import java.util.ArrayList;
import java.util.List;

public class PortfolioActivity extends BaseActivity {

    public static String INTENT_ACCOUNT_BALANCE_ITEMS = "intent_account_balance_items";

    private RecyclerView mPortfolioRecyclerView;
    private Toolbar mToolbar;
    private PortfolioRecyclerViewAdapter mPortfolioListAdapter;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        mAccountBalanceObjectItems.addAll((List<AccountBalanceObjectItem>)getIntent().getSerializableExtra(INTENT_ACCOUNT_BALANCE_ITEMS));
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mPortfolioRecyclerView = findViewById(R.id.portfolio_page_recycler_view);
        mPortfolioListAdapter = new PortfolioRecyclerViewAdapter(R.layout.item_portfolio_vertical, mAccountBalanceObjectItems);
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

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

}
