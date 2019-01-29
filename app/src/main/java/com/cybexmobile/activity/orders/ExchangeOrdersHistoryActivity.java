package com.cybexmobile.activity.orders;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;

import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.fragment.orders.OrdersHistoryFragment;
import com.cybexmobile.fragment.orders.TradeHistoryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import info.hoang8f.android.segmented.SegmentedGroup;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;

public class ExchangeOrdersHistoryActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.sg_own_order_history)
    SegmentedGroup mSegmentedGroup;

    private TradeHistoryFragment mTradeHistoryFragment;
    private OrdersHistoryFragment mOrdersHistoryFragment;
    private WatchlistData mWatchlistData;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_orders_history);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        mWatchlistData = (WatchlistData) getIntent().getSerializableExtra(INTENT_PARAM_WATCHLIST);
        mSegmentedGroup.setOnCheckedChangeListener(this);
        initFragment(savedInstanceState);
    }

    private void initFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(savedInstanceState != null){
            mTradeHistoryFragment = (TradeHistoryFragment) fragmentManager.getFragment(savedInstanceState, TradeHistoryFragment.class.getSimpleName());
            mOrdersHistoryFragment = (OrdersHistoryFragment) fragmentManager.getFragment(savedInstanceState, OrdersHistoryFragment.class.getSimpleName());
        }
        showFragment(R.id.rb_own_order_history);
    }

    private void showFragment(@IdRes int selectedId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(mTradeHistoryFragment != null && mTradeHistoryFragment.isAdded()){
            transaction.hide(mTradeHistoryFragment);
        }
        if(mOrdersHistoryFragment != null && mOrdersHistoryFragment.isAdded()){
            transaction.hide(mOrdersHistoryFragment);
        }
        switch (selectedId) {
            case R.id.rb_own_order_history:
                if (mOrdersHistoryFragment == null) {
                    mOrdersHistoryFragment = OrdersHistoryFragment.getInstance(mWatchlistData, false);
                }
                if(mOrdersHistoryFragment.isAdded()){
                    transaction.show(mOrdersHistoryFragment);
                } else {
                    transaction.add(R.id.layout_container, mOrdersHistoryFragment, OrdersHistoryFragment.class.getSimpleName());
                }
                break;
            case R.id.rb_own_trade_history:
                if (mTradeHistoryFragment == null) {
                    mTradeHistoryFragment = TradeHistoryFragment.getInstance(mWatchlistData, false);
                }
                if(mTradeHistoryFragment.isAdded()){
                    transaction.show(mTradeHistoryFragment);
                } else {
                    transaction.add(R.id.layout_container, mTradeHistoryFragment, TradeHistoryFragment.class.getSimpleName());
                }
                break;
        }
        transaction.commit();
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(mOrdersHistoryFragment != null && mOrdersHistoryFragment.isAdded()){
            fragmentManager.putFragment(outState, OrdersHistoryFragment.class.getSimpleName(), mOrdersHistoryFragment);
        }
        if(mTradeHistoryFragment != null && mTradeHistoryFragment.isAdded()){
            fragmentManager.putFragment(outState, TradeHistoryFragment.class.getSimpleName(), mTradeHistoryFragment);
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        showFragment(checkedId);
    }
}
