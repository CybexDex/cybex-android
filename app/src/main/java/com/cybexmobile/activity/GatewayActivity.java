package com.cybexmobile.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cybexmobile.R;
import com.cybexmobile.adapter.DepositAndWithdrawAdapter;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.fragment.DepositItemFragment;
import com.cybexmobile.fragment.WithdrawItemFragment;
import com.cybexmobile.fragment.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import info.hoang8f.android.segmented.SegmentedGroup;

public class GatewayActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, DepositItemFragment.OnListFragmentInteractionListener {

    public static String INTENT_ACCOUNT_BALANCE_ITEMS = "intent_account_balance_items";
    public static String INTENT_IS_DEPOSIT = "intent_is_deposit";
    public static String INTENT_IS_WITHDRAW = "intent_is_withdraw";

    @BindView(R.id.gate_way_segmented_group)
    SegmentedGroup mSegmentedGroup;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gate_way_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.gate_way_segment_deposit)
    RadioButton mDepositButton;
    @BindView(R.id.gate_way_segment_withdraw)
    RadioButton mWithdrawButton;

    private Unbinder mUnbinder;
    private List<AccountBalanceObjectItem> mAccountBalanceObjcetItemList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setViews();
        mAccountBalanceObjcetItemList = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_ACCOUNT_BALANCE_ITEMS);
        mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        if (getIntent().getBooleanExtra(INTENT_IS_DEPOSIT, false)) {
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    private void setViews() {
        mSegmentedGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.gate_way_segment_deposit:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.gate_way_segment_withdraw:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                default:
                    mDepositButton.setChecked(true);
                    break;
                case  1:
                    mWithdrawButton.setChecked(true);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                default:
                    fragment = DepositItemFragment.newInstance(mAccountBalanceObjcetItemList);
                    break;
                case 1:
                    fragment = WithdrawItemFragment.newInstance(mAccountBalanceObjcetItemList);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
