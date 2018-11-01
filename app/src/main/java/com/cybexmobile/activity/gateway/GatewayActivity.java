package com.cybexmobile.activity.gateway;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.utils.NetworkUtils;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.records.DepositAndWithdrawTotalActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.fragment.DepositItemFragment;
import com.cybexmobile.fragment.WithdrawItemFragment;
import com.cybexmobile.fragment.dummy.DummyContent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import info.hoang8f.android.segmented.SegmentedGroup;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.utils.NetworkUtils.TYPE_NOT_CONNECTED;

public class GatewayActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, DepositItemFragment.OnListFragmentInteractionListener {

    public static String INTENT_ACCOUNT_BALANCE_ITEMS = "intent_account_balance_items";
    public static String INTENT_IS_DEPOSIT = "intent_is_deposit";

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
    @BindView(R.id.gate_way_checkbox)
    CheckBox mCheckBox;
    @BindView(R.id.gate_way_search)
    SearchView mSearchView;

    private String mAccountName;

    private Unbinder mUnbinder;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItemList = new ArrayList<>();
    private WebSocketService mWebSocketService;
    private ScreenSlidePagerAdapter mScreenSlidePagerAdapter;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            loadData(mWebSocketService.getFullAccount(mAccountName));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);
        mUnbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        setSupportActionBar(mToolbar);
        setViews();
        mAccountBalanceObjectItemList = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_ACCOUNT_BALANCE_ITEMS);
        if (mAccountBalanceObjectItemList == null) {
            mAccountBalanceObjectItemList = new ArrayList<>();
            mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
            Intent intent = new Intent(this, WebSocketService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
        mScreenSlidePagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        if (getIntent().getBooleanExtra(INTENT_IS_DEPOSIT, true)) {
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    private void setSearchViewStyle() {
        mSearchView.setIconifiedByDefault(false);
        EditText searchEditText = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ImageView closeButton = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchEditText.setTextColor(getResources().getColor(R.color.font_color_white_dark));
        searchEditText.setTypeface(null, Typeface.BOLD);
        searchEditText.setHintTextColor(getResources().getColor(R.color.primary_color_grey));
        searchEditText.setHint(getResources().getString(R.string.gate_way_search));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setFocusable(false);
                mSearchView.setQuery("", false);
            }
        });
    }

    private void setSearchListener() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                EventBus.getDefault().post(new Event.onSearchBalanceAsset(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                EventBus.getDefault().post(new Event.onSearchBalanceAsset(newText));
                return false;
            }
        });
    }

    @OnCheckedChanged(R.id.gate_way_checkbox)
    public void onCheckBoxCheckedChange(CompoundButton button, boolean checked) {
        EventBus.getDefault().post(new Event.onHideZeroBalanceAssetCheckBox(checked));

    }

    private void setViews() {
        mSegmentedGroup.setOnCheckedChangeListener(this);
        setSearchViewStyle();
        setSearchListener();
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
                case 1:
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
        EventBus.getDefault().unregister(this);
        if (mWebSocketService != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deposit_withdraw_total_records, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_records:
                Intent intent = new Intent(this, DepositAndWithdrawTotalActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
                    fragment = DepositItemFragment.newInstance(mAccountBalanceObjectItemList);
                    break;
                case 1:
                    fragment = WithdrawItemFragment.newInstance(mAccountBalanceObjectItemList);
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

    private void loadData(FullAccountObject fullAccountObject) {
        if (fullAccountObject == null) {
            return;
        }
        if (NetworkUtils.getConnectivityStatus(this) == TYPE_NOT_CONNECTED) {
            return;
        }
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        if (accountBalanceObjects != null && accountBalanceObjects.size() > 0) {
            for (AccountBalanceObject balance : accountBalanceObjects) {
                if (balance.balance == 0) {
                    continue;
                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                mAccountBalanceObjectItemList.add(item);
            }

        }
        mScreenSlidePagerAdapter.notifyDataSetChanged();
    }
}
