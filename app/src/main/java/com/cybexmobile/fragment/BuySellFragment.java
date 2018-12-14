package com.cybexmobile.fragment;

import android.app.Dialog;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.cybexmobile.activity.login.LoginActivity;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import butterknife.Unbinder;

import static com.cybex.provider.graphene.chain.Operations.ID_CREATE_LIMIT_ORDER_OPERATION;
import static com.cybex.basemodule.constant.Constant.ACTION_BUY;
import static com.cybex.basemodule.constant.Constant.ACTION_SELL;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_ACTION;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_CYB_ASSET_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_CYB_FEE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FEE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_NAME;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACTION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CYB_ASSET_OBJECT;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FEE;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;

public class BuySellFragment extends BaseFragment implements SoftKeyBoardListener.OnSoftKeyBoardChangeListener{

    private static final String TAG = BuySellFragment.class.getSimpleName();

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
    NestedScrollView mScrollView;
    @BindView(R.id.buysell_checkbox_market_trades)
    CheckBox mCbMarketTrades;
    @BindView(R.id.layout_trade_history)
    FrameLayout mLayoutTradeHistory;

    private MarketTradeHistoryFragment mMarketTradeHistoryFragment;
    private ExchangeLimitOrderFragment mExchangeLimitOrderFragment;

    private String mCurrentAction = ACTION_BUY;
    private WatchlistData mWatchlistData;
    private FullAccountObject mFullAccountObject;
    private boolean mIsLoginIn;
    private String mName;
    //交易对的手续费 买单为base 卖单为quote
    private FeeAmountObject mBaseOrQuoteExchangeFee;
    private FeeAmountObject mCybExchangeFee;
    private AssetObject mCybAssetObject;

    private Unbinder mUnbinder;

    private double mAssetRmbPrice;
    //成交额
    private double mAssetTotal;
    //已经精确的余额
    private double mBalanceAvailable;
    //cyb资产是否足够扣手续费
    private boolean mIsCybBalanceEnough;
    //交易资产是否足够
    private boolean mIsExchangeBalanceEnough;

    private boolean mIsViewCreated;

    public static BuySellFragment getInstance(String action, WatchlistData watchlistData,
                                              FullAccountObject fullAccountObject, boolean isLoginIn, String name,
                                              FeeAmountObject fee, AssetObject cybAssetObject){
        BuySellFragment fragment = new BuySellFragment();
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_PARAM_ACTION, action);
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        bundle.putSerializable(INTENT_PARAM_FULL_ACCOUNT_OBJECT, fullAccountObject);
        bundle.putBoolean(INTENT_PARAM_LOGIN_IN, isLoginIn);
        bundle.putString(INTENT_PARAM_NAME, name);
        bundle.putSerializable(INTENT_PARAM_FEE, fee);
        bundle.putSerializable(INTENT_PARAM_CYB_ASSET_OBJECT, cybAssetObject);
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
            mName = bundle.getString(INTENT_PARAM_NAME);
            mWatchlistData = (WatchlistData)bundle.getSerializable(INTENT_PARAM_WATCHLIST);
            mFullAccountObject = (FullAccountObject) bundle.getSerializable(INTENT_PARAM_FULL_ACCOUNT_OBJECT);
            mBaseOrQuoteExchangeFee = (FeeAmountObject) bundle.getSerializable(INTENT_PARAM_FEE);
            mCybAssetObject = (AssetObject) bundle.getSerializable(INTENT_PARAM_CYB_ASSET_OBJECT);
        }
        if(savedInstanceState != null){
            mCurrentAction = savedInstanceState.getString(BUNDLE_SAVE_ACTION);
            mIsLoginIn = savedInstanceState.getBoolean(BUNDLE_SAVE_IS_LOGIN_IN);
            mName = savedInstanceState.getString(BUNDLE_SAVE_NAME);
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mFullAccountObject = (FullAccountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT);
            mBaseOrQuoteExchangeFee = (FeeAmountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FEE);
            mCybExchangeFee = (FeeAmountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_CYB_FEE);
            mCybAssetObject = (AssetObject) savedInstanceState.getSerializable(BUNDLE_SAVE_CYB_ASSET_OBJECT);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buysell, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mEtAssetPrice.setFilters(new InputFilter[]{mPriceFilter});
        mEtAssetAmount.setFilters(new InputFilter[]{mAmountFilter});
        SoftKeyBoardListener.setListener(getActivity(), this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewCreated = true;
        initFragment(savedInstanceState);
        initOrResetViewData();
        initOrResetButtonData();
        initOrResetFeeData();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_SAVE_ACTION, mCurrentAction);
        outState.putBoolean(BUNDLE_SAVE_IS_LOGIN_IN, mIsLoginIn);
        outState.putString(BUNDLE_SAVE_NAME, mName);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
        outState.putSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT, mFullAccountObject);
        outState.putSerializable(BUNDLE_SAVE_CYB_ASSET_OBJECT, mCybAssetObject);
        outState.putSerializable(BUNDLE_SAVE_FEE, mBaseOrQuoteExchangeFee);
        outState.putSerializable(BUNDLE_SAVE_CYB_FEE, mCybExchangeFee);
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

    @Override
    public void keyBoardShow(int height) {
        Log.v(TAG, String.format("%s: %s", "keyBoardShow", height));
    }

    @Override
    public void keyBoardHide(int height) {
        Log.v(TAG, String.format("%s: %s", "keyBoardHide", height));
        /**
         * fix online crash
         * java.lang.NullPointerException: Attempt to invoke virtual method
         * 'boolean android.view.View.isFocused()' on a null object reference
         */
        if(mEtAssetPrice != null && mEtAssetPrice.isFocused()){
            mEtAssetPrice.clearFocus();
        }
        if(mEtAssetAmount != null && mEtAssetAmount.isFocused()){
            mEtAssetAmount.clearFocus();
        }
    }

    @OnFocusChange({R.id.buysell_et_asset_price, R.id.buysell_et_asset_amount})
    public void onFocusChanged(View view, boolean isFocused){
        /**
         * fix bug:CYM-400
         * 软盘消失 输入框失去焦点 自动补全精度
         */
        if(!isFocused){
            if(view.getId() == R.id.buysell_et_asset_price){
                String priceStr = mEtAssetPrice.getText().toString();
                if(!TextUtils.isEmpty(priceStr)){
                    if(priceStr.equals(".")){
                        mEtAssetPrice.setText("");
                    } else {
                        mEtAssetPrice.setText(String.format(String.format(Locale.US, "%%.%df",
                                AssetUtil.pricePrecision(mWatchlistData.getCurrentPrice())), Double.parseDouble(priceStr)));
                    }
                }
            } else if (view.getId() == R.id.buysell_et_asset_amount){
                String amountStr = mEtAssetAmount.getText().toString();
                if(!TextUtils.isEmpty(amountStr)){
                    if(amountStr.equals(".")){
                        mEtAssetAmount.setText("");
                    } else {
                        mEtAssetAmount.setText(String.format(String.format(Locale.US, "%%.%df",
                                AssetUtil.amountPrecision(mWatchlistData.getCurrentPrice())), Double.parseDouble(amountStr)));
                    }

                }
            }
        }
    }

    @OnTouch({R.id.buysell_et_asset_amount, R.id.buysell_et_asset_price})
    public boolean onTouchEditText(View view, MotionEvent motionEvent) {
        //未登录时 获取焦点自动跳转到登录界面
        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
            if (!mIsLoginIn) {
                toLogin();
            }
        }
        return false;
    }

    @OnCheckedChanged(R.id.buysell_checkbox_market_trades)
    public void onMarketTradeCheckChanged(CompoundButton button, boolean isChecked){
        mScrollView.scrollTo(0, isChecked ? mLayoutTradeHistory.getTop() : 0);
    }

    @OnTouch(R.id.buysell_scroll_view)
    public boolean onTouchEvent(View view, MotionEvent event){
        /**
         * fix bug:点击空白处隐藏软键盘
         */
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mEtAssetPrice.isFocused()){
                manager.hideSoftInputFromWindow(mEtAssetPrice.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtAssetPrice.clearFocus();
            }
            if(mEtAssetAmount.isFocused()){
                manager.hideSoftInputFromWindow(mEtAssetAmount.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtAssetPrice.clearFocus();
            }
        }
        return false;
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
                assetPrice += (1/Math.pow(10, AssetUtil.pricePrecision(mWatchlistData.getCurrentPrice())));
                break;
            case R.id.buysell_tv_sub:
                if(assetPrice > 0){
                    assetPrice -= (1/Math.pow(10, AssetUtil.pricePrecision(mWatchlistData.getCurrentPrice())));
                }
                break;
        }
        mEtAssetPrice.setText(String.format(String.format("%%.%sf", AssetUtil.pricePrecision(mWatchlistData.getCurrentPrice())), assetPrice));
    }

    @OnClick({R.id.buysell_tv_percentage_25, R.id.buysell_tv_percentage_50, R.id.buysell_tv_percentage_75, R.id.buysell_tv_percentage_100})
    public void onAssetAmountClick(View view){
        String assetPrice = mEtAssetPrice.getText().toString();
        double price = TextUtils.isEmpty(assetPrice) ? 0 : Double.parseDouble(assetPrice);
        if(price == 0){
            return;
        }
        double amount = 0;
        double fee = mBaseOrQuoteExchangeFee.amount/Math.pow(10, mCurrentAction.equals(ACTION_BUY) ?
                mWatchlistData.getBasePrecision() : mWatchlistData.getQuotePrecision());
        /**
         * fix bug:CYM-365
         * 当cyb余额足够扣手续费，交易为花费cyb时 点击100%仓位 计算数量没有减去手续费
         */
        double balanceAvailable;
        if((mCurrentAction.equals(ACTION_SELL) && mWatchlistData.getQuoteId().equals(ASSET_ID_CYB)) ||
                (mCurrentAction.equals(ACTION_BUY) && mWatchlistData.getBaseId().equals(ASSET_ID_CYB))){
            balanceAvailable = mBalanceAvailable - fee;
        } else {
            balanceAvailable = mIsCybBalanceEnough ? mBalanceAvailable : mBalanceAvailable - fee;
        }
        /**
         * fix bug:CYM-397
         * 余额不足以扣手续费时 点击仓位数量输入框设置为0
         */
        if(balanceAvailable <= 0){
            mEtAssetAmount.setText("");
            return;
        }
        switch (view.getId()){
            case R.id.buysell_tv_percentage_25:
                amount = mCurrentAction.equals(ACTION_BUY) ? balanceAvailable * 0.25 / price : balanceAvailable * 0.25;
                break;
            case R.id.buysell_tv_percentage_50:
                amount = mCurrentAction.equals(ACTION_BUY) ? balanceAvailable * 0.50 / price : balanceAvailable * 0.50;
                break;
            case R.id.buysell_tv_percentage_75:
                amount = mCurrentAction.equals(ACTION_BUY) ? balanceAvailable * 0.75 / price : balanceAvailable * 0.75;
                break;
            case R.id.buysell_tv_percentage_100:
                amount = mCurrentAction.equals(ACTION_BUY) ? balanceAvailable * 1 / price : balanceAvailable * 1;
                break;
        }
        //amount 不四舍五入
        mEtAssetAmount.setText(AssetUtil.formatNumberRounding(amount, AssetUtil.amountPrecision(mWatchlistData.getCurrentPrice()), RoundingMode.DOWN));
    }

    @OnClick(R.id.buysell_btn_buy_sell)
    public void onBtnBuySellClick(View view){
        if(!mIsLoginIn){
            toLogin();
            return;
        }
        /**
         * fix bug:CYM-401
         * 价格和数量输入框为0或空 不交易
         */
        String price = mEtAssetPrice.getText().toString();
        String amount = mEtAssetAmount.getText().toString();
        if(TextUtils.isEmpty(price) || Double.parseDouble(price) == 0 ||
                TextUtils.isEmpty(amount) || Double.parseDouble(amount) == 0){
            return;
        }
        if(mIsExchangeBalanceEnough){
            CybexDialog.showLimitOrderCreateConfirmationDialog(getContext(), mCurrentAction.equals(ACTION_BUY),
                    String.format("%s %s", price, AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())),
                    String.format("%s %s", amount, AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())),
                    mTvAssetTotal.getText().toString(),
                    new CybexDialog.ConfirmationDialogClickListener() {
                @Override
                public void onClick(Dialog dialog) {
                    checkIfLocked(mName);
                }
            });
        }
    }

    @OnTextChanged(value = R.id.buysell_et_asset_amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAmountTextChanged(Editable editable){
        calculateTotal();
    }

    @OnTextChanged(value = R.id.buysell_et_asset_price, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPriceTextChanged(Editable editable){
        calculateTotal();
        initOrResetRmbTextData();
    }

    private void calculateTotal(){
        String assetPrice = mEtAssetPrice.getText().toString();
        String assetAmount = mEtAssetAmount.getText().toString();
        /**
         * fix bug
         * parseDouble错误
         */
        double price;
        double amount;
        try{
            price = TextUtils.isEmpty(assetPrice) ? 0 : Double.parseDouble(assetPrice);
            amount = TextUtils.isEmpty(assetAmount) ? 0 : Double.parseDouble(assetAmount);
        } catch (Exception e){
            e.printStackTrace();
            price = 0;
            amount = 0;
        }
        if(price == 0 || amount == 0){
            mTvAssetTotal.setText("--");
            mTvNotEnough.setVisibility(View.INVISIBLE);
            return;
        }
        String assetTotal = AssetUtil.formatNumberRounding(
                AssetUtil.multiply(price, amount), mWatchlistData.getTotalPrecision(),
                mCurrentAction.equals(ACTION_BUY) ? RoundingMode.UP : RoundingMode.DOWN);
        mTvAssetTotal.setText(String.format("%s %s", assetTotal, AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
        mAssetTotal = Double.parseDouble(assetTotal);
        //cyb余额不足扣手续费时 需要扣除手续费
        if(mCurrentAction.equals(ACTION_BUY)){
            if(mIsCybBalanceEnough){
                mIsExchangeBalanceEnough = mBalanceAvailable >= mAssetTotal;
            } else {
                mIsExchangeBalanceEnough = mBalanceAvailable - AssetUtil.divide(mBaseOrQuoteExchangeFee.amount, Math.pow(10, mWatchlistData.getBasePrecision())) >= mAssetTotal;
            }
        } else {
            /**
             * fix bug:CYM-367
             * 点击100%仓位 显示余额不足
             */
            if(mIsCybBalanceEnough){
                mIsExchangeBalanceEnough = mBalanceAvailable >= amount;
            } else {
                mIsExchangeBalanceEnough = mBalanceAvailable - AssetUtil.divide(mBaseOrQuoteExchangeFee.amount, Math.pow(10, mWatchlistData.getQuotePrecision())) >= amount;
            }
        }
        mTvNotEnough.setVisibility(mIsExchangeBalanceEnough ? View.INVISIBLE : View.VISIBLE);
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
        if(mWatchlistData == null || !mIsViewCreated){
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
        if(mFullAccountObject == null || mWatchlistData == null){
            mTvAssetAvailable.setText(getResources().getString(R.string.text_empty));
            return;
        }
        AccountBalanceObject accountBalanceObject = getBalance(mCurrentAction.equals(ACTION_BUY) ?
                mWatchlistData.getBaseId() : mWatchlistData.getQuoteId(), mFullAccountObject);
        if(accountBalanceObject == null || accountBalanceObject.balance == 0){
            mBalanceAvailable = 0;
            mTvAssetAvailable.setText(getResources().getString(R.string.text_empty));
            return;
        }
        mBalanceAvailable  = AssetUtil.divide(accountBalanceObject.balance, Math.pow(10, mCurrentAction.equals(ACTION_BUY) ?
                mWatchlistData.getBasePrecision() : mWatchlistData.getQuotePrecision()));
        mTvAssetAvailable.setText(String.format("%s %s",
                AssetUtil.formatNumberRounding(mBalanceAvailable, mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBasePrecision() : mWatchlistData.getQuotePrecision()),
                mCurrentAction.equals(ACTION_BUY) ? AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol()) : AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())));
    }

    /**
     * 先判断是否登录 再判断cyb手续费是否足够 再判断base或者quote手续费是否足够
     */
    private void initOrResetFeeData(){
        if(mBaseOrQuoteExchangeFee == null){
            mTvExchangeFree.setText(getResources().getString(R.string.text_empty));
            return;
        }
        if(!mIsLoginIn){
            mTvExchangeFree.setText(mCybAssetObject == null ? getResources().getString(R.string.text_empty) : String.format("%s %s",
                    AssetUtil.formatNumberRounding(AssetUtil.divide(mBaseOrQuoteExchangeFee.amount,
                    Math.pow(10, mCybAssetObject.precision)), mCybAssetObject.precision), AssetUtil.parseSymbol(mCybAssetObject.symbol)));
            return;
        }
        AccountBalanceObject accountBalanceObject = getBalance(mBaseOrQuoteExchangeFee.asset_id, mFullAccountObject);
        //先判断cyb是否足够
        if(mBaseOrQuoteExchangeFee.asset_id.equals(ASSET_ID_CYB)){
            //记录cyb手续费
            mCybExchangeFee = mBaseOrQuoteExchangeFee;
            /**
             * fix bug:CYM-380
             * 手续费显示错乱
             */
            if(accountBalanceObject != null && accountBalanceObject.balance >= mBaseOrQuoteExchangeFee.amount){//cyb足够
                mIsCybBalanceEnough = true;
                mTvExchangeFree.setText(mCybAssetObject == null ? getResources().getString(R.string.text_empty) : String.format("%s %s",
                        AssetUtil.formatNumberRounding(AssetUtil.divide(mBaseOrQuoteExchangeFee.amount,
                        Math.pow(10, mCybAssetObject.precision)), mCybAssetObject.precision), AssetUtil.parseSymbol(mCybAssetObject.symbol)));
            } else {//cyb不足
                mIsCybBalanceEnough = false;
                if((mCurrentAction.equals(ACTION_BUY) && mWatchlistData.getBaseId().equals(ASSET_ID_CYB)) ||
                        (mCurrentAction.equals(ACTION_SELL) && mWatchlistData.getQuoteId().equals(ASSET_ID_CYB))){
                    mTvExchangeFree.setText(mCybAssetObject == null ? getResources().getString(R.string.text_empty) : String.format("%s %s",
                                    AssetUtil.formatNumberRounding(AssetUtil.divide(mBaseOrQuoteExchangeFee.amount, Math.pow(10, mCybAssetObject.precision)), mCybAssetObject.precision),
                                    AssetUtil.parseSymbol(mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBaseSymbol() : mWatchlistData.getQuoteSymbol())));
                } else {
                    ((ExchangeFragment)getParentFragment()).loadLimitOrderCreateFee(mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBaseId() : mWatchlistData.getQuoteId());
                }
            }
        } else {
            if((mCurrentAction.equals(ACTION_BUY) && mWatchlistData.getBaseId().equals(mBaseOrQuoteExchangeFee.asset_id)) ||
                    (mCurrentAction.equals(ACTION_SELL) && mWatchlistData.getQuoteId().equals(mBaseOrQuoteExchangeFee.asset_id))){
                if(accountBalanceObject != null && accountBalanceObject.balance >= mBaseOrQuoteExchangeFee.amount){//交易对余额足够
                    mTvExchangeFree.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(AssetUtil.divide(mBaseOrQuoteExchangeFee.amount, Math.pow(10, mCurrentAction.equals(ACTION_BUY) ?
                                    mWatchlistData.getBasePrecision() : mWatchlistData.getQuotePrecision())),
                                    mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBasePrecision() : mWatchlistData.getQuotePrecision()),
                            AssetUtil.parseSymbol(mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBaseSymbol() : mWatchlistData.getQuoteSymbol())));
                } else {//交易对余额不足 显示cyb手续费
                    mTvExchangeFree.setText(mCybAssetObject == null ? getResources().getString(R.string.text_empty) : String.format("%s %s",
                                    AssetUtil.formatNumberRounding(AssetUtil.divide(mCybExchangeFee.amount, Math.pow(10, mCybAssetObject.precision)), mCybAssetObject.precision),
                                    AssetUtil.parseSymbol(mCybAssetObject.symbol)));
                }
            }
        }

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
        try {
            mTvAssetRmbPrice.setText(TextUtils.isEmpty(assetPrice) ? "≈¥ 0.0000" :
                    AssetUtil.formatNumberRounding(Double.parseDouble(assetPrice) * mAssetRmbPrice, mWatchlistData.getRmbPrecision()));
        } catch (Exception e){
            e.printStackTrace();
            mTvAssetRmbPrice.setText("≈¥ 0.0000");
        }

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

    public void changeFee(FeeAmountObject fee, AssetObject cybAsset){
        if(mCybAssetObject == null){
            mCybAssetObject = cybAsset;
        }
        /**
         * fix bug:CYM-413
         * 手续费赋值错误
         */
        if(fee.asset_id.equals(ASSET_ID_CYB) ||
                (mCurrentAction.equals(ACTION_BUY) && mWatchlistData.getBaseId().equals(fee.asset_id)) ||
                (mCurrentAction.equals(ACTION_SELL) && mWatchlistData.getQuoteId().equals(fee.asset_id))){
            mBaseOrQuoteExchangeFee = fee;
            initOrResetFeeData();
        }
    }

    public void changeLoginState(boolean loginState, String name){
        mIsLoginIn = loginState;
        mName = name;
        initOrResetButtonData();
    }

    public void changeFullAccount(FullAccountObject fullAccountObject){
        mFullAccountObject = fullAccountObject;
        initOrResetAvailableData();
        /**
         * fix bug:CYM-422
         * 每次刷新FullAccount数据时重新计算手续费，防止费用减少到手续费临界值时手续费未刷新
         */
        ((ExchangeFragment)getParentFragment()).loadLimitOrderCreateFee(ASSET_ID_CYB);
    }

    /**
     * 改变买入卖出价
     * @param basePrice
     */
    public void changeBuyOrSellPrice(String basePrice) {
        mEtAssetPrice.setText(basePrice);
    }

    public void changeRmbPrice(double rmbPrice){
        mAssetRmbPrice = rmbPrice;
        initOrResetRmbTextData();
    }

    private AccountBalanceObject getBalance(String assetId, FullAccountObject fullAccount){
        if(assetId == null || fullAccount == null){
            return null;
        }
        List<AccountBalanceObject> accountBalances = fullAccount.balances;
        if(accountBalances == null || accountBalances.size() == 0){
            return null;
        }
        AccountBalanceObject accountBalanceObject = null;
        for(AccountBalanceObject accountBalance : accountBalances){
            if(accountBalance.asset_type.toString().equals(assetId)){
                accountBalanceObject = accountBalance;
                break;
            }
        }
        return accountBalanceObject;
    }

    /**
     * 登录
     */
    private void toLogin(){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    /**
     * 检查用户钱包状态
     * @param userName
     */
    private void checkIfLocked(String userName) {
        if(!BitsharesWalletWraper.getInstance().is_locked()){
            toExchange();
            return;
        }
        CybexDialog.showUnlockWalletDialog(getFragmentManager(), mFullAccountObject.account, userName, new UnlockDialog.UnLockDialogClickListener() {
            @Override
            public void onUnLocked(String password) {
                toExchange();
            }
        });
    }

    /**
     * 挂单
     */
    private void toExchange(){
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    long amountSell;
                    long amountReceive;
                    if(mCurrentAction.equals(ACTION_BUY)){
                        amountSell = (long) AssetUtil.multiply(mAssetTotal, Math.pow(10, mWatchlistData.getBasePrecision()));
                        amountReceive = (long) AssetUtil.multiply(Double.parseDouble(mEtAssetAmount.getText().toString()), Math.pow(10, mWatchlistData.getQuotePrecision()));
                    } else {
                        amountSell = (long) AssetUtil.multiply(Double.parseDouble(mEtAssetAmount.getText().toString()), Math.pow(10, mWatchlistData.getQuotePrecision()));
                        amountReceive = (long) AssetUtil.multiply(mAssetTotal, Math.pow(10, mWatchlistData.getBasePrecision()));
                    }
                    Operations.limit_order_create_operation operation = BitsharesWalletWraper.getInstance().getLimitOrderCreateOperation(mFullAccountObject.account.id,
                            mIsCybBalanceEnough ? mCybAssetObject.id : mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBaseAsset().id : mWatchlistData.getQuoteAsset().id,
                            mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getBaseAsset().id : mWatchlistData.getQuoteAsset().id,
                            mCurrentAction.equals(ACTION_BUY) ? mWatchlistData.getQuoteAsset().id : mWatchlistData.getBaseAsset().id,
                            mIsCybBalanceEnough ? mCybExchangeFee.amount : mBaseOrQuoteExchangeFee.amount,
                            amountSell, amountReceive);
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFullAccountObject.account, operation, ID_CREATE_LIMIT_ORDER_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mLimitOrderCreateCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    public void clearEditTextData(){
        mEtAssetAmount.setText("");
        mEtAssetPrice.setText("");
    }

    private WebSocketClient.MessageCallback mLimitOrderCreateCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<String>>(){

        @Override
        public void onMessage(WebSocketClient.Reply<String> reply) {
            EventBus.getDefault().post(new Event.LimitOrderCreate(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.LimitOrderCreate(false));
        }
    };

    private InputFilter mPriceFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(mWatchlistData.getPricePrecision() == 0 && source.equals(".")){
                return "";
            }
            if(dest.length() == 0 && source.equals(".")){
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == mWatchlistData.getPricePrecision()) {
                    return "";
                }
            }
            return null;
        }
    };

    private InputFilter mAmountFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(mWatchlistData.getAmountPrecision() == 0 && source.equals(".")){
                return "";
            }
            if(dest.length() == 0 && source.equals(".")){
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == mWatchlistData.getAmountPrecision()) {
                    return "";
                }
            }
            return null;
        }
    };

}
