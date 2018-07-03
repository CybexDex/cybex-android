package com.cybexmobile.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.MarketsActivity;
import com.cybexmobile.activity.OwnOrderHistoryActivity;
import com.cybexmobile.activity.WatchlistSelectActivity;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.ACTION_SELL;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_ACCOUNT_BALANCE;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_ACTION;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_FROM;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;
import static com.cybexmobile.utils.Constant.REQUEST_CODE_SELECT_WATCHLIST;
import static com.cybexmobile.utils.Constant.RESULT_CODE_SELECTED_WATCHLIST;

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
    private AccountBalanceObject mAccountBalance;

    private WebSocketService mWebSocketService;

    private String mName;
    private boolean mIsLoginIn;

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
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if(bundle != null){
            mAction = bundle.getString(INTENT_PARAM_ACTION, ACTION_BUY);
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
        }
        if (savedInstanceState != null) {
            mAction = savedInstanceState.getString(BUNDLE_SAVE_ACTION);
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mAccountBalance = (AccountBalanceObject) savedInstanceState.getSerializable(BUNDLE_SAVE_ACCOUNT_BALANCE);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mName = sharedPreferences.getString(PREF_NAME, "");
        mIsLoginIn = sharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
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
                if(tab.getPosition() == 0){
                    mAction = ACTION_BUY;
                } else if(tab.getPosition() == 1){
                    mAction = ACTION_SELL;
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_WATCHLIST && resultCode == RESULT_CODE_SELECTED_WATCHLIST){
            //change watchlistdata
            mWatchlistData = (WatchlistData) data.getSerializableExtra(INTENT_PARAM_WATCHLIST);
            setTitleData();
            notifyWatchlistDataChange(mWatchlistData);
            notifyAccountBalanceDataChange(mWebSocketService.getFullAccount(mName));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMarketIntentToExchange(Event.MarketIntentToExchange event){
        mWatchlistData = event.getWatchlist();
        mAction = event.getAction();
        mTlExchange.getTabAt(mAction == null || mAction.equals(ACTION_BUY) ? 0 : 1).select();
        notifyWatchlistDataChange(event.getWatchlist());
        setTitleData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderClick(Event.LimitOrderClick event){
        if(mBuyFragment != null && mBuyFragment.isVisible()){
            mBuyFragment.changeBuyOrSellPrice(event.getPrice(), event.getQuoteAmount());
        }
        if(mSellFragment != null && mSellFragment.isVisible()){
            mSellFragment.changeBuyOrSellPrice(event.getPrice(), event.getQuoteAmount());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        for (AssetRmbPrice rmbPrice : assetRmbPrices) {
            if (mWatchlistData.getBaseSymbol().contains(rmbPrice.getName())) {
                notifyRmbPriceDataChange(rmbPrice.getValue());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        notifyAccountBalanceDataChange(event.getFullAccount());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mIsLoginIn = true;
        mName = event.getName();
        notifyLoginStateDataChange(true);
        notifyAccountBalanceDataChange(mWebSocketService.getFullAccount(mName));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event){
        mName = null;
        mIsLoginIn = false;
        notifyLoginStateDataChange(false);
        notifyAccountBalanceDataChange(null);
    }

    @OnClick(R.id.tv_title)
    public void onTitleClick(View view){
        Intent intent = new Intent(getContext(), WatchlistSelectActivity.class);
        intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchlistData);
        startActivityForResult(intent, REQUEST_CODE_SELECT_WATCHLIST);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            //当WatchlistData为空时，默认取CYB/ETH交易对数据
            if(mWatchlistData == null){
                mWatchlistData = mWebSocketService.getWatchlist(Constant.ASSET_ID_ETH, Constant.ASSET_ID_CYB);
                notifyWatchlistDataChange(mWatchlistData);
                if(mIsLoginIn){
                    notifyAccountBalanceDataChange(mWebSocketService.getFullAccount(mName));
                }
                setTitleData();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void notifyRmbPriceDataChange(double rmbPrice){
        if(mBuyFragment != null){
            mBuyFragment.changeRmbPrice(rmbPrice);
        }
        if(mSellFragment != null){
            mSellFragment.changeRmbPrice(rmbPrice);
        }
    }

    private void notifyWatchlistDataChange(WatchlistData watchlistData){
        if(mBuyFragment != null){
            mBuyFragment.changeWatchlist(watchlistData);
        }
        if(mSellFragment != null){
            mSellFragment.changeWatchlist(watchlistData);
        }
        if(mOpenOrdersFragment != null){
            mOpenOrdersFragment.changeWatchlist(watchlistData);
        }
    }

    private void notifyAccountBalanceDataChange(FullAccountObject fullAccount){
        AccountBalanceObject accountBalanceObject = null;
        if(mWatchlistData != null && fullAccount != null){
            List<AccountBalanceObject> accountBalances = fullAccount.balances;
            if(accountBalances == null || accountBalances.size() == 0){
                return;
            }
            for(AccountBalanceObject accountBalance : accountBalances){
                if(accountBalance.asset_type.toString().equals(mWatchlistData.getBaseId())){
                    accountBalanceObject = accountBalance;
                    break;
                }
            }
        }
        if(mAccountBalance == accountBalanceObject){
            return;
        }
        mAccountBalance = accountBalanceObject;
        if(mBuyFragment != null){
            mBuyFragment.changeAccountBalance(mAccountBalance);
        }
        if(mSellFragment != null){
            mSellFragment.changeAccountBalance(mAccountBalance);
        }
    }

    private void notifyLoginStateDataChange(boolean loginState){
        if(mBuyFragment != null){
            mBuyFragment.changeLoginState(loginState);
        }
        if(mSellFragment != null){
            mSellFragment.changeLoginState(loginState);
        }
    }

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
                    mBuyFragment = BuySellFragment.getInstance(ACTION_BUY, mWatchlistData, mAccountBalance, mIsLoginIn);
                }
                if(mBuyFragment.isAdded()){
                    transaction.show(mBuyFragment);
                } else {
                    transaction.add(R.id.layout_container, mBuyFragment, BuySellFragment.class.getSimpleName() + TAG_BUY);
                }
                break;
            case 1:
                if(mSellFragment == null){
                    mSellFragment = BuySellFragment.getInstance(ACTION_SELL, mWatchlistData, mAccountBalance, mIsLoginIn);
                }
                if(mSellFragment.isAdded()){
                    transaction.show(mSellFragment);
                } else {
                    transaction.add(R.id.layout_container, mSellFragment, BuySellFragment.class.getSimpleName() + TAG_SELL);
                }
                break;
            case 2:
                if(mOpenOrdersFragment == null){
                    mOpenOrdersFragment = OpenOrdersFragment.getInstance(mWatchlistData);
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
        outState.putString(BUNDLE_SAVE_ACTION, mAction);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
        outState.putSerializable(BUNDLE_SAVE_ACCOUNT_BALANCE, mAccountBalance);
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
                Intent intent = new Intent(getContext(), OwnOrderHistoryActivity.class);
                getContext().startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }


}
