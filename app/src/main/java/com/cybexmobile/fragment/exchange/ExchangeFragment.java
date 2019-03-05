package com.cybexmobile.fragment.exchange;

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

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.provider.graphene.rte.RteRequest;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.websocket.rte.RxRteWebSocket;
import com.cybexmobile.R;
import com.cybexmobile.activity.markets.MarketsActivity;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybexmobile.activity.orders.ExchangeOrdersHistoryActivity;
import com.cybexmobile.dialog.WatchlistSelectDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.constant.Constant;
import com.cybexmobile.fragment.orders.OpenOrdersFragment;
import com.cybexmobile.shake.AntiShake;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.BUNDEL_SAVE_SHOW_BUY_SELL_SPINNER_POSITION;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_PRECISION;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_PRECISION_SPINNER_POSITION;
import static com.cybex.provider.graphene.chain.Operations.ID_CREATE_LIMIT_ORDER_OPERATION;
import static com.cybex.basemodule.constant.Constant.ACTION_BUY;
import static com.cybex.basemodule.constant.Constant.ACTION_SELL;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_ACTION;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_CYB_ASSET_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_CYB_FEE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FEE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACTION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FROM;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.REQUEST_CODE_SELECT_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.RESULT_CODE_SELECTED_WATCHLIST;

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
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private RxRteWebSocket mRxRteWebSocket;

    private String mName;
    private boolean mIsLoginIn;

    private final Gson mGson = new Gson();
    private final JsonParser mJsonParser = new JsonParser();
    private RteRequest mRteRequestDepth;
    private RteRequest mRteRequestTicker;

    private int mPrecision = -1;
    private int mPrecisionSpinnerPosition = 0;
    private int mShowBuySellSpinnerPosition = 0;

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
            mPrecision = savedInstanceState.getInt(BUNDLE_SAVE_PRECISION, -1);
            mPrecisionSpinnerPosition = savedInstanceState.getInt(BUNDLE_SAVE_PRECISION_SPINNER_POSITION, 0);
            mShowBuySellSpinnerPosition = savedInstanceState.getInt(BUNDEL_SAVE_SHOW_BUY_SELL_SPINNER_POSITION, 0);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mName = sharedPreferences.getString(PREF_NAME, "");
        mIsLoginIn = sharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        initRTEWebSocket();
    }

    private void initRTEWebSocket() {
        mRxRteWebSocket = new RxRteWebSocket(RxRteWebSocket.RTE_URL);
        mCompositeDisposable.add(mRxRteWebSocket.onOpen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketOpen>() {
                    @Override
                    public void accept(WebSocketOpen webSocketOpen) throws Exception {
                        if(mWatchlistData != null) {
                            mRteRequestDepth = new RteRequest(RteRequest.TYPE_SUBSCRIBE,
                                    "ORDERBOOK." + mWatchlistData.getQuoteSymbol().replace(".", "_") +
                                            mWatchlistData.getBaseSymbol().replace(".", "_") + "." + (mPrecision == -1 ? mWatchlistData.getPricePrecision() : mPrecision) + ".10");
                            sendRteRquest(mRteRequestDepth);
                            mRteRequestTicker = new RteRequest(RteRequest.TYPE_SUBSCRIBE, "TICKER." + mWatchlistData.getQuoteSymbol().replace(".", "_") +
                                    mWatchlistData.getBaseSymbol().replace(".", "_"));
                            sendRteRquest(mRteRequestTicker);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mCompositeDisposable.add(mRxRteWebSocket.onFailure()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketFailure>() {
                    @Override
                    public void accept(WebSocketFailure webSocketFailure) throws Exception {
                        mRxRteWebSocket.reconnect(3, TimeUnit.SECONDS);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mCompositeDisposable.add(mRxRteWebSocket.onSubscribe(RxRteWebSocket.SUBSCRIBE_DEPTH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketMessage>() {
                    @Override
                    public void accept(WebSocketMessage webSocketMessage) throws Exception {
                        JsonElement jsonElement = mJsonParser.parse(webSocketMessage.getText());
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        List<List<String>> sellOrders = mGson.fromJson(jsonObject.get("asks"), new TypeToken<List<List<String>>>(){}.getType());
                        List<List<String>> buyOrders = mGson.fromJson(jsonObject.get("bids"), new TypeToken<List<List<String>>>(){}.getType());
                        String topic = jsonObject.get("topic").getAsString();
                        if (topic.contains(mWatchlistData.getBaseSymbol().replace(".", "_")) && topic.contains(mWatchlistData.getQuoteSymbol().replace(".", "_"))) {
                            if(mBuyFragment != null && mBuyFragment.isResumed()) {
                                mBuyFragment.notifyLimitOrderDataChanged(sellOrders, buyOrders);
                            }
                            if(mSellFragment != null && mSellFragment.isResumed()) {
                                mSellFragment.notifyLimitOrderDataChanged(sellOrders, buyOrders);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mCompositeDisposable.add(mRxRteWebSocket.onSubscribe(RxRteWebSocket.SUBSCRIBE_TICKET)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocketMessage>() {
                    @Override
                    public void accept(WebSocketMessage webSocketMessage) throws Exception {
                        JsonElement jsonElement = mJsonParser.parse(webSocketMessage.getText());
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        double price = jsonObject.get("px").getAsDouble();
                        String topic = jsonObject.get("topic").getAsString();
                        if (topic.contains(mWatchlistData.getBaseSymbol().replace(".", "_")) && topic.contains(mWatchlistData.getQuoteSymbol().replace(".", "_"))) {
                            if(mBuyFragment != null && mBuyFragment.isResumed()) {
                                mBuyFragment.notifyMarketPriceDataChanged(price);
                            }
                            if(mSellFragment != null && mSellFragment.isResumed()) {
                                mSellFragment.notifyMarketPriceDataChanged(price);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
        mRxRteWebSocket.connect();
    }

    /**
     *
     * @param request
     */
    private void sendRteRquest(RteRequest request) {
        mCompositeDisposable.add(mRxRteWebSocket.sendMessage(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
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
            notifyWatchlistDataChange(watchlist);
            setTitleData();
            notifyFullAccountDataChange(mWebSocketService.getFullAccount(mName));
            //切换交易对 重新加载交易手续费
            loadLimitOrderCreateFee(ASSET_ID_CYB);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!mRxRteWebSocket.isConnected()) {
            return;
        }
        if(mOpenOrdersFragment != null) {
            mOpenOrdersFragment.onParentHiddenChanged(hidden);
        }
        if(mRteRequestDepth != null) {
            mRteRequestDepth.setType(hidden ? RteRequest.TYPE_UNSUBSCRIBE : RteRequest.TYPE_SUBSCRIBE);
            sendRteRquest(mRteRequestDepth);
        }
        if(mRteRequestTicker != null) {
            mRteRequestTicker.setType(hidden ? RteRequest.TYPE_UNSUBSCRIBE : RteRequest.TYPE_SUBSCRIBE);
            sendRteRquest(mRteRequestTicker);
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
        notifyWatchlistDataChange(event.getWatchlist());
        setTitleData();
        mAction = event.getAction();
        mTlExchange.getTabAt(mAction == null || mAction.equals(ACTION_BUY) ? 0 : 1).select();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimitOrderClick(Event.LimitOrderClick event){
        if(mBuyFragment != null && mBuyFragment.isVisible()){
            mBuyFragment.changeBuyOrSellPrice(event.getPrice());
        }
        if(mSellFragment != null && mSellFragment.isVisible()){
            mSellFragment.changeBuyOrSellPrice(event.getPrice());
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
        notifyWatchlistDataChange(event.getWatchlist());
        setTitleData();
        loadLimitOrderCreateFee(ASSET_ID_CYB);
    }

    @Override
    public void onClick(View v) {
        if (AntiShake.check(v.getId())) { return; }
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
        if (AntiShake.check(view.getId())) { return; }
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
        if (AntiShake.check(item.getItemId())) { return false; }
        Intent intent = new Intent(getContext(), ExchangeOrdersHistoryActivity.class);
        intent.putExtra(INTENT_PARAM_WATCHLIST, mWatchlistData);
        getContext().startActivity(intent);
        return false;
    }

    public void loadLimitOrderCreateFee(String assetId){
        if(mWatchlistData == null || mWebSocketService == null){
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
                notifyWatchlistDataChange(mWebSocketService.getWatchlist(Constant.ASSET_ID_ETH, Constant.ASSET_ID_CYB));
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
        //取消订阅
        if(mWatchlistData != null && mRxRteWebSocket.isConnected()) {
            if(mRteRequestDepth != null) {
                mRteRequestDepth.setType(RteRequest.TYPE_UNSUBSCRIBE);
                sendRteRquest(mRteRequestDepth);
            }
            if(mRteRequestTicker != null) {
                mRteRequestTicker.setType(RteRequest.TYPE_UNSUBSCRIBE);
                sendRteRquest(mRteRequestTicker);
            }
        }
        mWatchlistData = watchlistData;
        //重新订阅
        if(mRxRteWebSocket.isConnected()) {
            mRteRequestDepth = new RteRequest(RteRequest.TYPE_SUBSCRIBE,
                    "ORDERBOOK." + mWatchlistData.getQuoteSymbol().replace(".", "_") +
                            mWatchlistData.getBaseSymbol().replace(".", "_") + "." + mWatchlistData.getPricePrecision() + ".10");
            sendRteRquest(mRteRequestDepth);
            mRteRequestTicker = new RteRequest(RteRequest.TYPE_SUBSCRIBE, "TICKER." + mWatchlistData.getQuoteSymbol().replace(".", "_") +
                    mWatchlistData.getBaseSymbol().replace(".", "_"));
            sendRteRquest(mRteRequestTicker);
        }
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

    public void reSubscribeOrderBook(int precision, int position) {
        notifyPrecisionChanged(precision, position);
        if(mRteRequestDepth != null) {
            mRteRequestDepth.setType(RteRequest.TYPE_UNSUBSCRIBE);
            sendRteRquest(mRteRequestDepth);
        }
        mRteRequestDepth = new RteRequest(RteRequest.TYPE_SUBSCRIBE,
                "ORDERBOOK." + mWatchlistData.getQuoteSymbol().replace(".", "_") +
                        mWatchlistData.getBaseSymbol().replace(".", "_") + "." + precision + ".10");
        sendRteRquest(mRteRequestDepth);
    }

    private void notifyPrecisionChanged(int precision, int position) {
        if(mBuyFragment != null){
            mBuyFragment.notifyPrecisionChanged(precision, position);
        }
        if(mSellFragment != null){
            mSellFragment.notifyPrecisionChanged(precision, position);
        }
        mPrecision = precision;
        mPrecisionSpinnerPosition = position;
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

    public void notifyShowBuySellChanged(int position) {
        if (mBuyFragment != null) {
            mBuyFragment.notifyShowBuySellChanged(position);
        }
        if (mSellFragment != null) {
            mSellFragment.notifyShowBuySellChanged(position);
        }
        mShowBuySellSpinnerPosition = position;
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
                    mBuyFragment = BuySellFragment.getInstance(ACTION_BUY, mWatchlistData, mFullAccountObject,
                            mIsLoginIn, mName, mCybExchangeFee, mCybAssetObject, mPrecision, mPrecisionSpinnerPosition, mShowBuySellSpinnerPosition);
                }
                if(mBuyFragment.isAdded()){
                    transaction.show(mBuyFragment);
//                    if (mSellFragment != null && mSellFragment.isVisible()) {
//                        mBuyFragment.changeWatchlist(mWatchlistData);
//                    }
                } else {
                    transaction.add(R.id.layout_container, mBuyFragment, BuySellFragment.class.getSimpleName() + TAG_BUY);
                }
                break;
            case 1:
                if(mSellFragment == null){
                    mSellFragment = BuySellFragment.getInstance(ACTION_SELL, mWatchlistData, mFullAccountObject,
                            mIsLoginIn, mName, mCybExchangeFee, mCybAssetObject, mPrecision, mPrecisionSpinnerPosition, mShowBuySellSpinnerPosition);
                }
                if(mSellFragment.isAdded()){
                    transaction.show(mSellFragment);
//                    if (mBuyFragment != null && mBuyFragment.isVisible()) {
//                        mSellFragment.changeWatchlist(mWatchlistData);
//                    }
                } else {
                    transaction.add(R.id.layout_container, mSellFragment, BuySellFragment.class.getSimpleName() + TAG_SELL);
                }
                break;
            case 2:
                if(mOpenOrdersFragment == null){
                    mOpenOrdersFragment = OpenOrdersFragment.getInstance(mWatchlistData, false);
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
        outState.putInt(BUNDLE_SAVE_PRECISION, mPrecision);
        outState.putInt(BUNDLE_SAVE_PRECISION_SPINNER_POSITION, mPrecisionSpinnerPosition);
        outState.putInt(BUNDEL_SAVE_SHOW_BUY_SELL_SPINNER_POSITION, mShowBuySellSpinnerPosition);
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
        mRxRteWebSocket.close(1000, "close");
        mCompositeDisposable.dispose();
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

    public BuySellFragment getBuyFragment() {
        return mBuyFragment;
    }

    public BuySellFragment getSellFragment() {
        return mSellFragment;
    }

    public OpenOrdersFragment getOpenOrdersFragment() {
        return mOpenOrdersFragment;
    }
}
