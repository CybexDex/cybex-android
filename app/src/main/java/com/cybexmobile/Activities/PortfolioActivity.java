package com.cybexmobile.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cybexmobile.API.BitsharesWalletWraper;
import com.cybexmobile.Adapter.PortfolioListRecyclerViewAdapter;
import com.cybexmobile.Adapter.PortfolioRecyclerViewAdapter;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.account_balance_object;

import java.util.List;

public class PortfolioActivity extends AppCompatActivity {
    RecyclerView mPortfolioRecyclerView;
    PortfolioListRecyclerViewAdapter mPortfolioListAdapter;
    List<account_balance_object> mAccountBalanceObjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        mPortfolioRecyclerView = findViewById(R.id.portfolio_page_recycler_view);
//        mAccountBalanceObjectList = (List<account_balance_object>) getIntent().getSerializableExtra("BalanceList");
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
