package com.cybexmobile.activity.orders;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;

import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.R;
import com.cybexmobile.fragment.orders.OpenOrdersFragment;
import com.cybexmobile.fragment.orders.OrdersHistoryFragment;
import com.cybexmobile.fragment.orders.TradeHistoryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import info.hoang8f.android.segmented.SegmentedGroup;
import io.enotes.sdk.repository.db.entity.Card;

public class OrdersHistoryActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.sg_orders_history)
    SegmentedGroup mSegmentedGroup;

    private TradeHistoryFragment mTradeHistoryFragment;
    private OrdersHistoryFragment mOrdersHistoryFragment;
    private OpenOrdersFragment mOpenOrdersFragment;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_history);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        mSegmentedGroup.setOnCheckedChangeListener(this);
        initFragment(savedInstanceState);
    }

    private void initFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(savedInstanceState != null){
            mOpenOrdersFragment = (OpenOrdersFragment) fragmentManager.getFragment(savedInstanceState, OpenOrdersFragment.class.getSimpleName());
            mTradeHistoryFragment = (TradeHistoryFragment) fragmentManager.getFragment(savedInstanceState, TradeHistoryFragment.class.getSimpleName());
            mOrdersHistoryFragment = (OrdersHistoryFragment) fragmentManager.getFragment(savedInstanceState, OrdersHistoryFragment.class.getSimpleName());
        }
        showFragment(R.id.rb_open_orders);
    }

    private void showFragment(@IdRes int selectedId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mOpenOrdersFragment != null && mOpenOrdersFragment.isAdded()) {
            transaction.hide(mOpenOrdersFragment);
        }
        if(mTradeHistoryFragment != null && mTradeHistoryFragment.isAdded()){
            transaction.hide(mTradeHistoryFragment);
        }
        if(mOrdersHistoryFragment != null && mOrdersHistoryFragment.isAdded()){
            transaction.hide(mOrdersHistoryFragment);
        }
        switch (selectedId) {
            case R.id.rb_open_orders:
                if (mOpenOrdersFragment == null) {
                    mOpenOrdersFragment = OpenOrdersFragment.getInstance(null, true);
                }
                if(mOpenOrdersFragment.isAdded()){
                    transaction.show(mOpenOrdersFragment);
                } else {
                    transaction.add(R.id.layout_container, mOpenOrdersFragment, OpenOrdersFragment.class.getSimpleName());
                }
                break;
            case R.id.rb_orders_history:
                if (mOrdersHistoryFragment == null) {
                    mOrdersHistoryFragment = OrdersHistoryFragment.getInstance(null, true);
                }
                if(mOrdersHistoryFragment.isAdded()){
                    transaction.show(mOrdersHistoryFragment);
                } else {
                    transaction.add(R.id.layout_container, mOrdersHistoryFragment, OrdersHistoryFragment.class.getSimpleName());
                }
                break;
            case R.id.rb_trade_history:
                if (mTradeHistoryFragment == null) {
                    mTradeHistoryFragment = TradeHistoryFragment.getInstance(null, true);
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
    protected void nfcStartReadCard() {
        if (mOpenOrdersFragment != null && mSegmentedGroup.getCheckedRadioButtonId() == R.id.rb_open_orders) {
            if (mOpenOrdersFragment.getUnlockDialog() != null && mOpenOrdersFragment.getUnlockDialog().isVisible()) {
                mOpenOrdersFragment.getUnlockDialog().showProgress();
            } else {
                super.nfcStartReadCard();
            }
        } else {
            super.nfcStartReadCard();
        }

    }

    @Override
    protected void readCardOnSuccess(Card card) {
        if (mOpenOrdersFragment != null && mSegmentedGroup.getCheckedRadioButtonId() == R.id.rb_open_orders) {
            currentCard = card;
            cardApp = card;
            if (isLoginFromENotes()) {
                if (mOpenOrdersFragment.getUnlockDialog() != null && mOpenOrdersFragment.getUnlockDialog().isVisible()) {
                    mOpenOrdersFragment.hideEnotesDialog();
                    mOpenOrdersFragment.toCancelLimitOrder();
                } else {
                    super.nfcStartReadCard();
                }
            }
        } else {
            super.readCardOnSuccess(card);
        }
    }

    @Override
    protected void readCardError(int code, String message) {
        super.readCardError(code, message);
        if (mOpenOrdersFragment != null && mSegmentedGroup.getCheckedRadioButtonId() == R.id.rb_open_orders) {
            if (mOpenOrdersFragment.getUnlockDialog() != null && mOpenOrdersFragment.getUnlockDialog().isVisible()) {
                mOpenOrdersFragment.hideProgress();
            }
        }
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
        if (mOpenOrdersFragment != null && mOpenOrdersFragment.isAdded()) {
            fragmentManager.putFragment(outState, OpenOrdersFragment.class.getSimpleName(), mOpenOrdersFragment);
        }
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
