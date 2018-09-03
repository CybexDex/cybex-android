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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.cybex.basemodule.R2;
import com.cybex.basemodule.base.BaseFragment;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.cybexmobile.activity.markets.MarketsActivity;
import com.cybexmobile.activity.orderhistory.OwnOrderHistoryActivity;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybexmobile.dialog.WatchlistSelectDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.toast.message.ToastMessage;
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

import static com.cybex.provider.graphene.chain.Operations.ID_CREATE_LIMIT_ORDER_OPERATION;
import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.ACTION_SELL;
import static com.cybexmobile.utils.Constant.ASSET_ID_CYB;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_ACTION;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_CYB_ASSET_OBJECT;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_CYB_FEE;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_FEE;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_FROM;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;
import static com.cybexmobile.utils.Constant.REQUEST_CODE_SELECT_WATCHLIST;
import static com.cybexmobile.utils.Constant.RESULT_CODE_SELECTED_WATCHLIST;

public class ExchangeFragment extends BaseFragment implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener, TabLayout.OnTabSelectedListener, WatchlistSelectDialog.OnWatchlistSelectedListener{

    private static final String TAG_BUY = "Buy";
    private static final String TAG_SELL = "Sell";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.cb_title)
    CheckBox mCbTitle;
    @BindView(R.id.tl_exchange)
    TabLayout mTlExchange;

    private BuySellFragment mBuyFragment;
    private BuySellFragment mSellFragment;
    private OpenOrdersFragment mOpenOrdersFragment;

    private Unbinder mUnbinder;

    private String mAction;
    private WatchlistData mWatchlistData;
    private FullAccountObject mFullAccountObject;
    private FeeAmountObject mExchangeFee;
    private FeeAmountObject mCybExchangeFee;
    private AssetObject mCybAssetObject;

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
            mFullAccountObject = (FullAccountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT);
            mExchangeFee = (FeeAmountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FEE);
            mCybExchangeFee = (FeeAmountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_CYB_FEE);
            mCybAssetObject = (AssetObject) savedInstanceState.getSerializable(BUNDLE_SAVE_CYB_ASSET_OBJECT);
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
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.inflateMenu(R.menu.menu_exchange);
        mTlExchange.getTabAt(mAction == null || mAction.equals(ACTION_BUY) ? 0 : 1).select();
        mTlExchange.addOnTabSelectedListener(this);
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
        mCbTitle.setChecked(false);
        if(requestCode == REQUEST_CODE_SELECT_WATCHLIST && resultCode == RESULT_CODE_SELECTED_WATCHLIST){
            WatchlistData watchlist = (WatchlistData) data.getSerializableExtra(INTENT_PARAM_WATCHLIST);
            //change watchlistdata 同一个交易对数据不刷新
            if(mWatchlistData != null && watchlist.getBaseId().equals(mWatchlistData.getBaseId()) && watchlist.getQuoteId().equals(mWatchlistData.getQuoteId())){
                return;
            }
            mWatchlistData = watchlist;
            setTitleData();
            notifyWatchlistDataChange(mWatchlistData);
            notifyFullAccountDataChange(mWebSocketService.getFullAccount(mName));
            //切换交易对 重新加载交易手续费
            loadLimitOrderCreateFee(ASSET_ID_CYB);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadLimitOrderCreateFee(Event.LoadRequiredFee event){
        mExchangeFee = event.getFee();
        if(mExchangeFee.asset_id.equals(ASSET_ID_CYB) && mCybExchangeFee == null){
            mCybExchangeFee = mExchangeFee;
        }
        if(mCybAssetObject == null){
            mCybAssetObject = mWebSocketService.getAssetObject(ASSET_ID_CYB);
        }
        notifyExchangeFee(mExchangeFee, mCybAssetObject);
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
        if(mWatchlistData == null){
            return;
        }
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
        notifyFullAccountDataChange(event.getFullAccount());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mIsLoginIn = true;
        mName = event.getName();
        notifyLoginStateDataChange(true, mName);
        notifyFullAccountDataChange(mWebSocketService.getFullAccount(mName));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event){
        mName = null;
        mIsLoginIn = false;
        notifyLoginStateDataChange(false, null);
        notifyFullAccountDataChange(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderCreate(Event.LimitOrderCreate event){
        if(event.isSuccess()){
            /**
             * fix bug:CYM-381
             * 挂单成功清空输入框数据
             */
            if(mAction.equals(ACTION_BUY)){
                mBuyFragment.clearEditTextData();
            } else {
              mSellFragment.clearEditTextData();
            }
            ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                    R.string.toast_message_place_order_successfully), R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(getActivity(), getResources().getString(
                    R.string.toast_message_place_order_failed), R.drawable.ic_error_16px);
        }
    }

    /**
     * fix bug:CYM-348
     * 行情界面数据没加载出来进入交易界面，交易界面数据为空
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitExchangeWatchlist(Event.InitExchangeWatchlist event){
        if(mWatchlistData != null){
            return;
        }
        mWatchlistData = event.getWatchlist();
        notifyWatchlistDataChange(mWatchlistData);
        loadLimitOrderCreateFee(ASSET_ID_CYB);
        setTitleData();
    }

    @Override
    public void onClick(View v) {
        /**
         * fix bug:CYM-454
         * WatchlistData为空不能跳转，防止MarketsActivity crash
         */
        if(mWatchlistData == null){
            return;
        }
        Intent intent = new Intent(getContext(), MarketsActivity.class);
        intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchlistData);
        intent.putExtra(INTENT_PARAM_FROM, ExchangeLimitOrderFragment.class.getSimpleName());
        getContext().startActivity(intent);
    }

    @OnClick(R.id.cb_title)
    public void onTitleClick(View view){
        WatchlistSelectDialog fragment = new WatchlistSelectDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, mWatchlistData);
        fragment.setArguments(bundle);
        fragment.setTargetFragment(this, REQUEST_CODE_SELECT_WATCHLIST);
        fragment.show(getFragmentManager(), WatchlistSelectDialog.class.getSimpleName());
        fragment.setOnWatchlistSelectListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = new Intent(getContext(), OwnOrderHistoryActivity.class);
        getContext().startActivity(intent);
        return false;
    }

    public void loadLimitOrderCreateFee(String assetId){
        if(mWatchlistData == null){
            return;
        }
        mWebSocketService.loadLimitOrderCreateFee(assetId, ID_CREATE_LIMIT_ORDER_OPERATION,
                BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(ObjectId.create_from_string(""),
                        ObjectId.create_from_string(ASSET_ID_CYB),
                        mWatchlistData.getBaseAsset().id,
                        mWatchlistData.getQuoteAsset().id,  0, 0, 0));
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
                setTitleData();
            }
            if(mIsLoginIn){
                notifyFullAccountDataChange(mWebSocketService.getFullAccount(mName));
            }
            loadLimitOrderCreateFee(ASSET_ID_CYB);
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

    private void notifyExchangeFee(FeeAmountObject fee, AssetObject cybAsset){
        if(mBuyFragment != null){
            mBuyFragment.changeFee(fee, cybAsset);
        }
        if(mSellFragment != null){
            mSellFragment.changeFee(fee, cybAsset);
        }
    }

    private void notifyFullAccountDataChange(FullAccountObject fullAccount){
        mFullAccountObject = fullAccount;
        if(mBuyFragment != null){
            mBuyFragment.changeFullAccount(fullAccount);
        }
        if(mSellFragment != null){
            mSellFragment.changeFullAccount(fullAccount);
        }
    }

    private void notifyLoginStateDataChange(boolean loginState, String name){
        if(mBuyFragment != null){
            mBuyFragment.changeLoginState(loginState, name);
        }
        if(mSellFragment != null){
            mSellFragment.changeLoginState(loginState, name);
        }
    }

    private void setTitleData(){
        if(mWatchlistData == null){
            return;
        }
        mCbTitle.setText(String.format("%s/%s", AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol()), AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
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
                    mBuyFragment = BuySellFragment.getInstance(ACTION_BUY, mWatchlistData, mFullAccountObject, mIsLoginIn, mName, mCybExchangeFee, mCybAssetObject);
                }
                if(mBuyFragment.isAdded()){
                    transaction.show(mBuyFragment);
                } else {
                    transaction.add(R.id.layout_container, mBuyFragment, BuySellFragment.class.getSimpleName() + TAG_BUY);
                }
                break;
            case 1:
                if(mSellFragment == null){
                    mSellFragment = BuySellFragment.getInstance(ACTION_SELL, mWatchlistData, mFullAccountObject, mIsLoginIn, mName, mCybExchangeFee, mCybAssetObject);
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
        outState.putSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT, mFullAccountObject);
        outState.putSerializable(BUNDLE_SAVE_FEE, mExchangeFee);
        outState.putSerializable(BUNDLE_SAVE_CYB_FEE, mCybExchangeFee);
        outState.putSerializable(BUNDLE_SAVE_CYB_ASSET_OBJECT, mCybAssetObject);
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
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

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


    @Override
    public void onWatchlistSelectDismiss() {
        mCbTitle.setChecked(false);
    }
}
