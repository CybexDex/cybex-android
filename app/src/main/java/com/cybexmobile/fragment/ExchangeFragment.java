package com.cybexmobile.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cybexmobile.R;
import com.cybexmobile.activity.MarketsActivity;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.AssetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.ACTION_SELL;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_FROM;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

public class ExchangeFragment extends BaseFragment {

    private static final String TAG_BUY = "Buy";
    private static final String TAG_SELL = "Sell";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.tl_exchange)
    TabLayout mTlExchange;

    private BuySellFragment mBuyFragment;
    private BuySellFragment mSellFragment;
    private OpenOrdersFragment mOpenOrdersFragment;

    private Unbinder mUnbinder;

    private String mAction;
    private WatchlistData mWatchlistData;

    private WebSocketService mWebSocketService;

    public static ExchangeFragment getInstance(String action, WatchlistData watchlist){
        ExchangeFragment fragment = new ExchangeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_PARAM_ACTION, action);
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlist);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setHasOptionsMenu(true);
        Bundle bundle = getArguments();
        if(bundle != null){
            mAction = bundle.getString(INTENT_PARAM_ACTION, ACTION_BUY);
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
        }
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MarketsActivity.class);
                intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchlistData);
                intent.putExtra(INTENT_PARAM_FROM, ExchangeLimitOrderFragment.class.getSimpleName());
                getContext().startActivity(intent);
            }
        });
        mTlExchange.getTabAt(mAction == null || mAction.equals(ACTION_BUY) ? 0 : 1).select();
        mTlExchange.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitleData();
        initFragment(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMarketIntentToExchange(Event.MarketIntentToExchange event){
        mWatchlistData = event.getWatchlist();
        mAction = event.getAction();
        mTlExchange.getTabAt(mAction == null || mAction.equals(ACTION_BUY) ? 0 : 1).select();
        mBuyFragment.changeWatchlist(event.getWatchlist());
        mSellFragment.changeWatchlist(event.getWatchlist());
        setTitleData();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            //当WatchlistData为空时 默认取行情页第一个tab的第一个交易对
            if(mWatchlistData == null){
                mWebSocketService = binder.getService();
                mWatchlistData = mWebSocketService.getFirstWatchlist();
                if(mBuyFragment != null){
                    mBuyFragment.changeWatchlist(mWatchlistData);
                }
                setTitleData();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void setTitleData(){
        if(mWatchlistData == null){
            return;
        }
        mTvTitle.setText(String.format("%s/%s", AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol()), AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
    }

    private void initFragment(Bundle savedInstanceState){
        FragmentManager childFragmentManager = getChildFragmentManager();
        if(savedInstanceState != null){
            mBuyFragment = (BuySellFragment) childFragmentManager.getFragment(savedInstanceState, BuySellFragment.class.getSimpleName() + TAG_BUY);
            mSellFragment = (BuySellFragment) childFragmentManager.getFragment(savedInstanceState, BuySellFragment.class.getSimpleName() + TAG_SELL);
            mOpenOrdersFragment = (OpenOrdersFragment) childFragmentManager.getFragment(savedInstanceState, OpenOrdersFragment.class.getSimpleName());
        }
        showFragment(mAction != null && mAction.equals(ACTION_SELL) ? 1 : 0);
    }

    private void showFragment(int position){
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        if(mBuyFragment != null && mBuyFragment.isAdded()){
            transaction.hide(mBuyFragment);
        }
        if(mSellFragment != null && mSellFragment.isAdded()){
            transaction.hide(mSellFragment);
        }
        if(mOpenOrdersFragment != null && mOpenOrdersFragment.isAdded()){
            transaction.hide(mOpenOrdersFragment);
        }
        switch (position){
            case 0:
                if(mBuyFragment == null){
                    mBuyFragment = BuySellFragment.getInstance(ACTION_BUY, mWatchlistData);
                }
                if(mBuyFragment.isAdded()){
                    transaction.show(mBuyFragment);
                } else {
                    transaction.add(R.id.layout_container, mBuyFragment, BuySellFragment.class.getSimpleName() + TAG_BUY);
                }
                break;
            case 1:
                if(mSellFragment == null){
                    mSellFragment = BuySellFragment.getInstance(ACTION_SELL, mWatchlistData);
                }
                if(mSellFragment.isAdded()){
                    transaction.show(mSellFragment);
                } else {
                    transaction.add(R.id.layout_container, mSellFragment, BuySellFragment.class.getSimpleName() + TAG_SELL);
                }
                break;
            case 2:
                if(mOpenOrdersFragment == null){
                    mOpenOrdersFragment = OpenOrdersFragment.getInstance();
                }
                if(mOpenOrdersFragment.isAdded()){
                    transaction.show(mOpenOrdersFragment);
                } else {
                    transaction.add(R.id.layout_container, mOpenOrdersFragment, OpenOrdersFragment.class.getSimpleName());
                }
                break;
        }
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fragmentManager = getChildFragmentManager();
        if(mBuyFragment != null && mBuyFragment.isAdded()){
            fragmentManager.putFragment(outState, BuySellFragment.class.getSimpleName() + TAG_BUY, mBuyFragment);
        }
        if(mSellFragment != null && mSellFragment.isAdded()){
            fragmentManager.putFragment(outState, BuySellFragment.class.getSimpleName() + TAG_SELL, mSellFragment);
        }
        if(mOpenOrdersFragment != null && mOpenOrdersFragment.isAdded()){
            fragmentManager.putFragment(outState, OpenOrdersFragment.class.getSimpleName(), mOpenOrdersFragment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_exchange, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_order_history:

                break;
        }
        return true;
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }


}
