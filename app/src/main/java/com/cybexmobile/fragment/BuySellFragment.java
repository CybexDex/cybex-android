package com.cybexmobile.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.BuySellOrderRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.Price;
import com.cybexmobile.market.Order;
import com.cybexmobile.utils.AssetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

public class BuySellFragment extends BaseFragment {

    @BindView(R.id.buysell_btn_buy_sell)
    Button mBtnBuySell;
    @BindView(R.id.buysell_et_asset_price)
    EditText mEtAssetPrice;
    @BindView(R.id.buysell_et_asset_amount)
    EditText mEtAssetAmount;
    @BindView(R.id.buysell_tv_add)
    TextView mTvAssetAdd;
    @BindView(R.id.buysell_tv_sub)
    TextView mTvAssetSub;
    @BindView(R.id.butsell_tv_asset_rmb_price)
    TextView mTvAssetRmbPrice;
    @BindView(R.id.buysell_tv_asset_symbol)
    TextView mTvAssetSymbol;
    @BindView(R.id.buysell_tv_not_enough)
    TextView mTvNotEnough;
    @BindView(R.id.buysell_tv_percentage_25)
    TextView mTvPercentage25;
    @BindView(R.id.buysell_tv_percentage_50)
    TextView mTvPercentage50;
    @BindView(R.id.buysell_tv_percentage_75)
    TextView mTvPercentage75;
    @BindView(R.id.buysell_tv_percentage_100)
    TextView mTvPercentage100;
    @BindView(R.id.buysell_tv_asset_available)
    TextView mTvAssetAvailable;
    @BindView(R.id.buysell_tv_exchange_free)
    TextView mTvExchangeFree;
    @BindView(R.id.buysell_tv_asset_total)
    TextView mTvAssetTotal;

    private MarketTradeHistoryFragment mMarketTradeHistoryFragment;
    private ExchangeLimitOrderFragment mExchangeLimitOrderFragment;

    private String mCurrentAction = ACTION_BUY;

    private Unbinder mUnbinder;

    private WatchlistData mWatchlistData;

    public static BuySellFragment getInstance(String action, WatchlistData watchlistData){
        BuySellFragment fragment = new BuySellFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_PARAM_ACTION, action);
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if(bundle != null){
            mWatchlistData = (WatchlistData)bundle.getSerializable(INTENT_PARAM_WATCHLIST);
            mCurrentAction = bundle.getString(INTENT_PARAM_ACTION, ACTION_BUY);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buysell, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment(savedInstanceState);
        initOrResetViewData();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager fragmentManager = getChildFragmentManager();
        if(mMarketTradeHistoryFragment != null && mMarketTradeHistoryFragment.isAdded()){
            fragmentManager.putFragment(outState, MarketTradeHistoryFragment.class.getSimpleName(), mMarketTradeHistoryFragment);
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @OnClick({R.id.buysell_tv_add, R.id.buysell_tv_sub})
    public void onAssetPriceClick(View view){

    }

    @OnClick({R.id.buysell_tv_percentage_25, R.id.buysell_tv_percentage_50, R.id.buysell_tv_percentage_75, R.id.buysell_tv_percentage_100})
    public void onAssetAmountClick(View view){

    }

    private void initFragment(Bundle savedInstanceState){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(savedInstanceState != null){
            mMarketTradeHistoryFragment = (MarketTradeHistoryFragment) fragmentManager.getFragment(savedInstanceState, MarketTradeHistoryFragment.class.getSimpleName());
            mExchangeLimitOrderFragment = (ExchangeLimitOrderFragment) fragmentManager.getFragment(savedInstanceState, ExchangeLimitOrderFragment.class.getSimpleName());
        }
        if(mMarketTradeHistoryFragment == null){
            mMarketTradeHistoryFragment = MarketTradeHistoryFragment.newInstance(mWatchlistData);
        }
        if(mExchangeLimitOrderFragment == null){
            mExchangeLimitOrderFragment = ExchangeLimitOrderFragment.getInstance(mWatchlistData);
        }
        if(mExchangeLimitOrderFragment.isAdded()){
            transaction.show(mExchangeLimitOrderFragment);
        }else{
            transaction.add(R.id.layout_limit_order_container, mExchangeLimitOrderFragment, ExchangeLimitOrderFragment.class.getSimpleName());
        }
        if(mMarketTradeHistoryFragment.isAdded()){
            transaction.show(mMarketTradeHistoryFragment);
        }else{
            transaction.add(R.id.layout_trade_history_container, mMarketTradeHistoryFragment, MarketTradeHistoryFragment.class.getSimpleName());
        }
        transaction.commit();
    }

    private void initOrResetViewData(){
        if(mWatchlistData == null){
            return;
        }
        String baseSymbol = AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol());
        String quoteSymbol = AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol());
        mTvAssetSymbol.setText(quoteSymbol);
        initOrResetButtonData();
    }

    private void initOrResetButtonData(){
        mBtnBuySell.setText(String.format("%s %s", getResources().getString(mCurrentAction.equals(ACTION_BUY) ?
                R.string.text_buy : R.string.text_sell), mWatchlistData == null ? "" : AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())));
        mBtnBuySell.setBackgroundResource(mCurrentAction.equals(ACTION_BUY) ?
                R.drawable.bg_btn_green_gradient_enabled : R.drawable.bg_btn_red_gradient_enabled);
    }

    /**
     * 交易对改变
     * @param watchlist
     */
    public void changeWatchlist(WatchlistData watchlist){
        if(watchlist == null){
            return;
        }
        this.mWatchlistData = watchlist;
        if(mMarketTradeHistoryFragment != null){
            mMarketTradeHistoryFragment.changeWatchlist(mWatchlistData);
        }
        if(mExchangeLimitOrderFragment != null){
            mExchangeLimitOrderFragment.changeWatchlist(mWatchlistData);
        }
        initOrResetViewData();
    }
}
