package com.cybexmobile.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.activity.LoginActivity;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.widget.OverScrollView;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.ACTION_BUY;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_ACCOUNT_BALANCE;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_ACTION;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACTION;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_LOGIN_IN;
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
    @BindView(R.id.buysell_scroll_view)
    OverScrollView mScrollView;

    private MarketTradeHistoryFragment mMarketTradeHistoryFragment;
    private ExchangeLimitOrderFragment mExchangeLimitOrderFragment;

    private String mCurrentAction = ACTION_BUY;
    private WatchlistData mWatchlistData;
    private AccountBalanceObject mAccountBalance;
    private boolean mIsLoginIn;

    private Unbinder mUnbinder;

    private int mPricePrecision;//价格精度
    private int mAmountPrecision;//数量精度
    private double mAssetRmbPrice;

    public static BuySellFragment getInstance(String action, WatchlistData watchlistData,
                                              AccountBalanceObject accountBalance, boolean isLoginIn){
        BuySellFragment fragment = new BuySellFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_PARAM_ACTION, action);
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        bundle.putSerializable(INTENT_PARAM_ACCOUNT_BALANCE, accountBalance);
        bundle.putBoolean(INTENT_PARAM_LOGIN_IN, isLoginIn);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if(bundle != null){
            mCurrentAction = bundle.getString(INTENT_PARAM_ACTION, ACTION_BUY);
            mIsLoginIn = bundle.getBoolean(INTENT_PARAM_LOGIN_IN);
            mWatchlistData = (WatchlistData)bundle.getSerializable(INTENT_PARAM_WATCHLIST);
            mAccountBalance = (AccountBalanceObject) bundle.getSerializable(INTENT_PARAM_ACCOUNT_BALANCE);

        }
        if(savedInstanceState != null){
            mCurrentAction = savedInstanceState.getString(BUNDLE_SAVE_ACTION);
            mIsLoginIn = savedInstanceState.getBoolean(BUNDLE_SAVE_IS_LOGIN_IN);
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mAccountBalance = (AccountBalanceObject) savedInstanceState.getSerializable(BUNDLE_SAVE_ACCOUNT_BALANCE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buysell, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mEtAssetPrice.setFilters(new InputFilter[]{mPriceFilter});
        mEtAssetAmount.setFilters(new InputFilter[]{mAmountFilter});
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
        outState.putString(BUNDLE_SAVE_ACTION, mCurrentAction);
        outState.putBoolean(BUNDLE_SAVE_IS_LOGIN_IN, mIsLoginIn);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
        outState.putSerializable(BUNDLE_SAVE_ACCOUNT_BALANCE, mAccountBalance);
        FragmentManager fragmentManager = getChildFragmentManager();
        if(mMarketTradeHistoryFragment != null && mMarketTradeHistoryFragment.isAdded()){
            fragmentManager.putFragment(outState, MarketTradeHistoryFragment.class.getSimpleName(), mMarketTradeHistoryFragment);
        }
        if(mExchangeLimitOrderFragment != null && mExchangeLimitOrderFragment.isAdded()){
            fragmentManager.putFragment(outState, ExchangeLimitOrderFragment.class.getSimpleName(), mExchangeLimitOrderFragment);
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
        String assetPriceStr = mEtAssetPrice.getText().toString();
        if(TextUtils.isEmpty(assetPriceStr)){
            return;
        }
        double assetPrice = Double.parseDouble(assetPriceStr);
        switch (view.getId()){
            case R.id.buysell_tv_add:
                assetPrice += (1/Math.pow(10, mPricePrecision));
                break;
            case R.id.buysell_tv_sub:
                assetPrice -= (1/Math.pow(10, mPricePrecision));
                break;
        }
        mEtAssetPrice.setText(String.format(String.format("%%.%sf", mPricePrecision), assetPrice));
    }

    @OnClick({R.id.buysell_tv_percentage_25, R.id.buysell_tv_percentage_50, R.id.buysell_tv_percentage_75, R.id.buysell_tv_percentage_100})
    public void onAssetAmountClick(View view){
        //判断TextView的值 防止余额小于有效精度
        String assetAvailableStr = mTvAssetAvailable.getText().toString();
        switch (view.getId()){
            case R.id.buysell_tv_percentage_25:
                break;
            case R.id.buysell_tv_percentage_50:
                break;
            case R.id.buysell_tv_percentage_75:
                break;
            case R.id.buysell_tv_percentage_100:
                break;
        }
    }

    @OnClick(R.id.buysell_btn_buy_sell)
    public void onBtnBuySellClick(View view){
        if(!mIsLoginIn){
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        } else {

        }
    }

    @OnTextChanged(value = R.id.buysell_et_asset_price, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPriceTextChanged(Editable editable){
        initOrResetRmbTextData();
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
        mEtAssetPrice.setText("");
        mEtAssetAmount.setText("");
        initOrResetButtonData();
        initOrResetAvailableData();
    }

    private void initOrResetAvailableData(){
        mTvAssetAvailable.setText(mAccountBalance == null ? getResources().getString(R.string.text_empty) :
                String.format("%s%s", mAccountBalance.balance/Math.pow(10, mWatchlistData.getBasePrecision()), AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
    }

    private void initOrResetButtonData(){
        if(mIsLoginIn){
            mBtnBuySell.setText(String.format("%s %s", getResources().getString(mCurrentAction.equals(ACTION_BUY) ?
                    R.string.text_buy : R.string.text_sell), mWatchlistData == null ? "" : AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())));
        } else {
            mBtnBuySell.setText(getResources().getString(R.string.text_login_in_to_exchange));
        }
        mBtnBuySell.setBackgroundResource(mCurrentAction.equals(ACTION_BUY) ?
                R.drawable.bg_btn_green_gradient_enabled : R.drawable.bg_btn_red_gradient_enabled);
    }

    private void initOrResetRmbTextData(){
        String assetPrice = mEtAssetPrice.getText().toString();
        mTvAssetRmbPrice.setText(TextUtils.isEmpty(assetPrice) ? "≈--" :
                String.format(Locale.US, "≈%.2f", Double.parseDouble(assetPrice) * mAssetRmbPrice));
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
        mPricePrecision = AssetUtil.pricePrecision(mWatchlistData.getCurrentPrice());
        mAmountPrecision = AssetUtil.amountPrecision(mWatchlistData.getCurrentPrice());
        initOrResetViewData();
    }

    public void changeLoginState(boolean loginState){
        mIsLoginIn = loginState;
        initOrResetButtonData();
    }

    public void changeAccountBalance(AccountBalanceObject accountBalance){
        mAccountBalance = accountBalance;
        initOrResetAvailableData();
    }

    /**
     * 改变买入卖出价
     * @param basePrice
     */
    public void changeBuyOrSellPrice(double basePrice, double quoteAmount) {
        mEtAssetPrice.setText(String.format(AssetUtil.formatPrice(basePrice), basePrice));
        if(quoteAmount != 0){
            mEtAssetAmount.setText(String.format(AssetUtil.formatAmount(basePrice), quoteAmount));
        }
    }

    public void changeRmbPrice(double rmbPrice){
        mAssetRmbPrice = rmbPrice;
        initOrResetRmbTextData();
    }

    private InputFilter mPriceFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(dest.length() == 0 && source.equals(".")){
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == mPricePrecision) {
                    return "";
                }
            }
            return null;
        }
    };

    private InputFilter mAmountFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(dest.length() == 0 && source.equals(".")){
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == mAmountPrecision) {
                    return "";
                }
            }
            return null;
        }
    };
}
