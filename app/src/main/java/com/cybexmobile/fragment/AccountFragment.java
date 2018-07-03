package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cybexmobile.activity.BottomNavigationActivity;
import com.cybexmobile.activity.LockAssetsActivity;
import com.cybexmobile.activity.SettingActivity;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.activity.LoginActivity;
import com.cybexmobile.activity.OpenOrdersActivity;
import com.cybexmobile.activity.PortfolioActivity;
import com.cybexmobile.adapter.PortfolioRecyclerViewAdapter;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.crypto.Sha256Object;
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.market.OpenOrder;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_LOGIN_IN;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_NAME;
import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

/**
 * 帐户界面
 * 数据流程:FullAccountObject -> AssetObject ->MarketTicker
 */
public class AccountFragment extends BaseFragment {

    private static final String TAG = "AccountFragment";

    private static final int REQUEST_CODE_LOGIN = 1;

    private OnAccountFragmentInteractionListener mListener;
    private PortfolioRecyclerViewAdapter mPortfolioRecyclerViewAdapter;

    @BindView(R.id.account_my_asset_recycler_view)
    RecyclerView mPortfolioRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.account_membership_text)
    TextView mMembershipTextView;
    @BindView(R.id.account_say_hello_text_view)
    TextView mSayHelloTextView;
    @BindView(R.id.account_balance)
    TextView mTvTotalCybAmount;
    @BindView(R.id.account_balance_total_rmb)
    TextView mTvTotalRmbAmount;
    @BindView(R.id.account_avatar_webview)
    WebView mAvatarWebView;
    @BindView(R.id.account_avatar)
    ImageView mAvatarImageView;
    @BindView(R.id.account_no_log_in)
    LinearLayout mBeforeLoginLayout;
    @BindView(R.id.account_logged_in)
    LinearLayout mAfterLoginLayout;
    @BindView(R.id.portfolio_title_layout)
    RelativeLayout mPortfolioTitleLayout;

    private Unbinder mUnbinder;

    private SharedPreferences mSharedPreference;

    //Recyclerview item
    private volatile List<AccountBalanceObjectItem> mAccountBalanceObjectItems = new ArrayList<>();
    //所有委单
    private volatile List<LimitOrderObject> mLimitOrderObjectList = new ArrayList<>();
    private volatile double mTotalCyb;
    private volatile double mLimitOrderTotalValue;
    private volatile double mLimitOrderBuyTotalValue;
    private volatile double mLimitOrderSellTotalValue;
    private boolean mIsLoginIn;
    private String mName;
    private String mMembershipExpirationDate;
    private List<String> mCompareSymbol = Arrays.asList(new String[]{"1.3.27", "1.3.2", "1.3.3", "1.3.0"});
    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();

    private double mCybRmbPrice;

    private WebSocketService mWebSocketService;

    private static final int MESSAGE_WHAT_REFRUSH_MEMBERSHIP_EXPIRATION = 1;
    private static final int MESSAGE_WHAT_REFRUSH_TOTAL_CYB = 2;
    private static final int MESSAGE_WHAT_REFRESH_PORTFOLIO = 3;

    private static final String PARAM_TOTAL_CYB_VALUE = "total_cyb_value";
    private static final String PARAM_TOTAL_RMB_VALUE = "total_rmb_value";
    private static final String PARAM_MEMBERSHIP_DATE = "membership_date";
    private static final String PARAM_ACCOUNT_BALANCE_OBJECT_ITEMS = "account_balance_object_items";

    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLoginIn = mSharedPreference.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = mSharedPreference.getString(PREF_NAME, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setViews();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPortfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPortfolioRecyclerViewAdapter = new PortfolioRecyclerViewAdapter(R.layout.item_portfolio_horizontal, mAccountBalanceObjectItems);
        mPortfolioRecyclerView.setAdapter(mPortfolioRecyclerViewAdapter);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if(mIsLoginIn){
            if (!mIsNetWorkAvailable) {
                mTotalCyb = savedInstanceState.getDouble(PARAM_TOTAL_CYB_VALUE);
                mCybRmbPrice = savedInstanceState.getDouble(PARAM_TOTAL_RMB_VALUE);
                mMembershipExpirationDate = savedInstanceState.getString(PARAM_MEMBERSHIP_DATE);
                mAccountBalanceObjectItems.addAll((List<AccountBalanceObjectItem>) savedInstanceState.getSerializable(PARAM_ACCOUNT_BALANCE_OBJECT_ITEMS));
                mPortfolioTitleLayout.setVisibility(mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0 ? View.GONE : View.VISIBLE);
                mPortfolioRecyclerViewAdapter.notifyDataSetChanged();
                mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
                mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_MEMBERSHIP_EXPIRATION);
                setTotalCybAndRmbTextView(mTotalCyb, mTotalCyb * mCybRmbPrice);
                return;
            }
            if(mWebSocketService != null){
                loadData(mWebSocketService.getFullAccount(mName));
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mIsNetWorkAvailable) {
            outState.putDouble(PARAM_TOTAL_CYB_VALUE, mTotalCyb);
            outState.putDouble(PARAM_TOTAL_RMB_VALUE, mCybRmbPrice);
            outState.putString(PARAM_MEMBERSHIP_DATE, mMembershipExpirationDate);
            outState.putSerializable(PARAM_ACCOUNT_BALANCE_OBJECT_ITEMS, (Serializable) mAccountBalanceObjectItems);
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.account_log_in_text)
    public void onLoginClick(View view){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @OnClick(R.id.account_view_all)
    public void onAllPortfolioClick(View view){
        Intent intent = new Intent(getActivity(), PortfolioActivity.class);
        intent.putExtra(PortfolioActivity.INTENT_ACCOUNT_BALANCE_ITEMS, (Serializable) mAccountBalanceObjectItems);
        startActivity(intent);
    }

    @OnClick(R.id.account_open_order_item_background)
    public void onOpenOrderClick(View view){
        Intent intent = new Intent(getActivity(), OpenOrdersActivity.class);
        intent.putExtra("TotalValue", mLimitOrderTotalValue);
        intent.putExtra("TotalSellValue", mLimitOrderSellTotalValue);
        intent.putExtra("TotalBuyValue", mLimitOrderBuyTotalValue);
        startActivity(intent);
    }

    @OnClick(R.id.account_lockup_item_background)
    public void onLockAssetsClick(View view){
        Intent intent = new Intent(getContext(), LockAssetsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.balance_info_question_marker)
    public void onBalanceInfoClick(View view){
        CybexDialog.showBalanceDialog(getActivity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event) {
        resetAccountDate();
        setViews();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginIn(Event.LoginIn event){
        mName = event.getName();
        mIsLoginIn = true;
        setViews();
        loadData(mWebSocketService.getFullAccount(mName));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event){
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if(assetRmbPrices == null || assetRmbPrices.size() == 0){
            return;
        }
        for(AssetRmbPrice assetRmbPrice : assetRmbPrices){
            if("CYB".equals(assetRmbPrice.getName())){
                mCybRmbPrice = assetRmbPrice.getValue();
                setTotalCybAndRmbTextView(mTotalCyb, mTotalCyb * mCybRmbPrice);
                break;
            }
        }
        if(mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0){
            return;
        }
        if(mCybRmbPrice != mAccountBalanceObjectItems.get(0).cybPrice){
            for(AccountBalanceObjectItem item : mAccountBalanceObjectItems){
                item.cybPrice = mCybRmbPrice;
            }
            mPortfolioRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAsset(Event.LoadAsset event){
        AssetObject assetObject = event.getData();
        if(assetObject == null){
            return;
        }
        for(int i=0; i<mAccountBalanceObjectItems.size(); i++){
            AccountBalanceObjectItem item = mAccountBalanceObjectItems.get(i);
            if(assetObject.id.toString().equals(item.accountBalanceObject.asset_type.toString())){
                item.assetObject = assetObject;
                if(item.marketTicker != null || item.assetObject.id.toString().equals("1.3.0")){
                    calculateTotalCyb(item.accountBalanceObject.balance, item.assetObject.precision,
                            item.accountBalanceObject.asset_type.toString().equals("1.3.0") ? 1 : item.marketTicker.latest);
                }
                mPortfolioRecyclerViewAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        loadData(event.getFullAccount());
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_WHAT_REFRUSH_MEMBERSHIP_EXPIRATION:
                    updateMemberShipViewData();
                    break;
                case MESSAGE_WHAT_REFRUSH_TOTAL_CYB:
                    AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                    mCybRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
                    Log.v(TAG, "mTotalCyb=" + mTotalCyb + "  mTotalCyb * mCybRmbPrice=" + mTotalCyb * mCybRmbPrice);
                    setTotalCybAndRmbTextView(mTotalCyb, mTotalCyb * mCybRmbPrice);
                    break;
                case MESSAGE_WHAT_REFRESH_PORTFOLIO:
                    mPortfolioTitleLayout.setVisibility(View.VISIBLE);
                    Object obj = msg.obj;
                    if(obj != null){
                        mPortfolioRecyclerViewAdapter.notifyItemChanged((Integer) obj);
                    }else{
                        mPortfolioRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    private WebSocketClient.MessageCallback onTickerCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker ticker = reply.result;
            if(ticker == null){
                return;
            }
            for(int i=0; i<mAccountBalanceObjectItems.size(); i++){
                AccountBalanceObjectItem item = mAccountBalanceObjectItems.get(i);
                if(ticker.quote.toString().equals(item.accountBalanceObject.asset_type.toString())){
                    item.marketTicker = ticker;
                    if(item.assetObject != null){
                        calculateTotalCyb(item.accountBalanceObject.balance, item.assetObject.precision,
                                item.accountBalanceObject.asset_type.toString().equals("1.3.0") ? 1 : item.marketTicker.latest);
                    }
                    for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                        if (limitOrderObject.sell_price.base.asset_id.toString().equals(item.accountBalanceObject.asset_type.toString())) {
                            mTotalCyb += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.marketTicker.latest;
                            mLimitOrderTotalValue += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.marketTicker.latest;
                            if (checkIsSell(limitOrderObject.sell_price.base.asset_id.toString(), limitOrderObject.sell_price.quote.asset_id.toString(),mCompareSymbol)) {
                                mLimitOrderSellTotalValue += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.marketTicker.latest;
                            } else {
                                mLimitOrderBuyTotalValue += (limitOrderObject.for_sale / Math.pow(10, item.assetObject.precision)) * item.marketTicker.latest;
                            }
                            break;
                        }
                    }
                    Message message = Message.obtain();
                    message.what = MESSAGE_WHAT_REFRESH_PORTFOLIO;
                    message.obj = i;
                    mHandler.sendMessage(message);
                    break;
                }
            }

        }

        @Override
        public void onFailure() {

        }
    };

    private WebSocketClient.MessageCallback mCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker ticker = reply.result;
            if (ticker == null) {
                return;
            }
            for (OpenOrderItem openOrderItem : mOpenOrderItems) {
                if (openOrderItem.isSell) {
                    double price = (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10, openOrderItem.openOrder.getQuoteObject().precision)) / (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount / Math.pow(10, openOrderItem.openOrder.getBaseObject().precision));

                    if (ticker.quote.equals(openOrderItem.openOrder.getQuoteObject().id.toString())) {
                        mTotalCyb += price * (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount /Math.pow(10, openOrderItem.openOrder.getBaseObject().precision)) * ticker.latest;
                    }
                } else {
                    double price = (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount / Math.pow(10, openOrderItem.openOrder.getBaseObject().precision)) / (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10, openOrderItem.openOrder.getQuoteObject().precision));

                    if (ticker.quote.equals(openOrderItem.openOrder.getQuoteObject().id.toString())) {
                        mTotalCyb += price * (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10,openOrderItem.openOrder.getQuoteObject().precision)) * ticker.latest;
                    }
                }
                break;
            }

        }

        @Override
        public void onFailure() {

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            Log.v(TAG, "service connected " );
            loadData(mWebSocketService.getFullAccount(mName));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void calculateTotalCyb(long balance, int precision, double priceCyb){
        double price = balance / Math.pow(10, precision);
        mTotalCyb += (price * priceCyb);
        mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_TOTAL_CYB);
    }

    private void setViews() {
        mAvatarImageView.setVisibility(mIsLoginIn ? View.GONE : View.VISIBLE);
        mAvatarWebView.setVisibility(mIsLoginIn ? View.VISIBLE : View.GONE);
        mBeforeLoginLayout.setVisibility(mIsLoginIn ? View.GONE : View.VISIBLE);
        mAfterLoginLayout.setVisibility(mIsLoginIn ? View.VISIBLE : View.GONE);
        mPortfolioTitleLayout.setVisibility(mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0 ? View.GONE : View.VISIBLE);
        mSayHelloTextView.setText(mName == null ? "" : mName);
        mMembershipTextView.setText("");
        mTvTotalCybAmount.setText("--");
        mTvTotalRmbAmount.setText("≈ ¥--");
        if (mIsLoginIn) {
            loadWebView(mAvatarWebView, 56);
        }
    }

    private void updateMemberShipViewData(){
        try {
            String registerYear = mMembershipExpirationDate.substring(0, 4);
            if (Integer.parseInt(registerYear) < 1970) {
                mMembershipTextView.setText(getActivity().getResources().getString(R.string.account_membership_lifetime));
            } else {
                mMembershipTextView.setText(getActivity().getResources().getString(R.string.account_membership_basic));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setTotalCybAndRmbTextView(double totalCyb, double totalRmb){
        mTvTotalCybAmount.setText(totalCyb == 0 ? "--" : String.format(Locale.US, "%.5f", mTotalCyb));
        if (totalCyb == 0) {
            mTvTotalRmbAmount.setText("≈¥--");
        } else {
            mTvTotalRmbAmount.setText(totalRmb == 0 ? "≈¥--" : String.format(Locale.US, "≈¥%.2f", totalRmb));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(INTENT_PARAM_LOGIN_IN, false)) {
                mName = data.getStringExtra(INTENT_PARAM_NAME);
                mIsLoginIn = true;
                setViews();
                loadData(mWebSocketService.getFullAccount(mName));
            }
        }
    }

    private void loadData(FullAccountObject fullAccountObject){
        if(fullAccountObject == null){
            return;
        }
        if (!mIsNetWorkAvailable) {
            return;
        }
        mTotalCyb = 0;
        mLimitOrderTotalValue = 0;
        mLimitOrderBuyTotalValue = 0;
        mLimitOrderSellTotalValue = 0;
        mLimitOrderObjectList.clear();
        mAccountBalanceObjectItems.clear();

        hideLoadDialog();
        mLimitOrderObjectList.addAll(fullAccountObject.limit_orders);
        mMembershipExpirationDate  = fullAccountObject.account.membership_expiration_date;
        mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_MEMBERSHIP_EXPIRATION);
//        if(mLimitOrderObjectList != null && mLimitOrderObjectList.size() > 0){
//            for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
//
////                OpenOrderItem item = new OpenOrderItem();
////                OpenOrder openOrder = new OpenOrder();
////                openOrder.setLimitOrder(limitOrderObject);
////                String baseId = limitOrderObject.sell_price.base.asset_id.toString();
////                String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
////                List<AssetObject> assetObjects = mWebSocketService.getAssetObjects(baseId, quoteId);
////                if (assetObjects != null && assetObjects.size() == 2) {
////                    String baseSymbol = assetObjects.get(0).symbol;
////                    String quoteSymbol = assetObjects.get(1).symbol;
////                    item.isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
////                    openOrder.setBaseObject(assetObjects.get(0));
////                    openOrder.setQuoteObject(assetObjects.get(1));
////                }
////                item.openOrder = openOrder;
////                calculateLimitOrderTotalValue(item);
////                mOpenOrderItems.add(item);
//
////                mTotalCyb += limitOrderObject.for_sale / Math.pow(10, 5);
//            }
//            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_TOTAL_CYB);
//        }
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        if(accountBalanceObjects != null && accountBalanceObjects.size() > 0){
            for (AccountBalanceObject balance : accountBalanceObjects) {
                /**
                 * fix bug
                 * CYM-241
                 * 过滤为0的资产
                 */
                if(balance.balance == 0){
                    continue;
                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                item.cybPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
                if(item.assetObject != null && balance.asset_type.toString().equals("1.3.0")){
                    calculateTotalCyb(balance.balance, item.assetObject.precision, 1);
                }
                if (!balance.asset_type.toString().equals("1.3.0")) {
                    try {
                        BitsharesWalletWraper.getInstance().get_ticker("1.3.0", balance.asset_type.toString(), onTickerCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }
                for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                    if (limitOrderObject.sell_price.base.asset_id.toString().equals("1.3.0") && limitOrderObject.sell_price.base.asset_id.toString().equals(balance.asset_type.toString())) {
                        mTotalCyb += limitOrderObject.for_sale / Math.pow(10, 5);
                        mLimitOrderTotalValue += limitOrderObject.for_sale / Math.pow(10, 5);
                        if (checkIsSell(limitOrderObject.sell_price.base.asset_id.toString(), limitOrderObject.sell_price.quote.asset_id.toString(), mCompareSymbol)) {
                            mLimitOrderSellTotalValue +=limitOrderObject.for_sale / Math.pow(10, 5);
                        } else {
                            mLimitOrderBuyTotalValue += limitOrderObject.for_sale / Math.pow(10, 5);
                        }
                    }
                }
                mAccountBalanceObjectItems.add(item);
            }
            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRESH_PORTFOLIO);
        }

    }

    private void loadWebView(WebView webView, int size) {
        Sha256Object.encoder encoder = new Sha256Object.encoder();
        encoder.write(mName.getBytes());
        String htmlShareAccountName = "<html><head><style>body,html {margin:0; padding:0; text-align:center;}</style><meta name=viewport content=width=" + size + ",user-scalable=no/></head><body><canvas width=" + size + " height=" + size + " data-jdenticon-hash=" + encoder.result().toString() + "></canvas><script src=https://cdn.jsdelivr.net/jdenticon/1.3.2/jdenticon.min.js async></script></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadData(htmlShareAccountName, "text/html", "UTF-8");
    }

    private void resetAccountDate(){
        mIsLoginIn = false;
        mName = null;
        mTotalCyb = 0;
        mLimitOrderTotalValue = 0;
        mAccountBalanceObjectItems.clear();
        mLimitOrderObjectList.clear();
        mPortfolioRecyclerViewAdapter.notifyDataSetChanged();
        if(mWebSocketService != null){
            mWebSocketService.clearAccountCache();
        }
    }

    private boolean checkIsSell(String baseSymbol, String quoteSymbol, List<String> compareSymbol) {
        boolean isContainBase = compareSymbol.contains(baseSymbol);
        boolean isContainQuote = compareSymbol.contains(quoteSymbol);
        int baseIndex = compareSymbol.indexOf(baseSymbol);
        int quoteIndex = compareSymbol.indexOf(quoteSymbol);
        if (isContainBase) {
            if (!isContainQuote) {
                return false;
            } else {
                if (baseIndex < quoteIndex) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            if (isContainQuote) {
                return true;
            } else {
                return false;
            }
        }
    }

//    private void calculateLimitOrderTotalValue(OpenOrderItem openOrderItem) {
//        AssetObject base = openOrderItem.openOrder.getBaseObject();
//        AssetObject quote = openOrderItem.openOrder.getQuoteObject();
//        double price;
//        if (base != null && quote != null) {
//            if (openOrderItem.isSell) {
//                price = (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10, quote.precision)) / (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount / Math.pow(10, base.precision));
//                if (openOrderItem.openOrder.getQuoteObject().symbol.equals("CYB")) {
//                    mTotalCyb += price * (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount / Math.pow(10, openOrderItem.openOrder.getBaseObject().precision));
//                } else {
//                    try {
//                        BitsharesWalletWraper.getInstance().get_ticker("1.3.0", openOrderItem.openOrder.getQuoteObject().id.toString(), mCallback);
//                    } catch (NetworkStatusException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            } else {
//                price = (openOrderItem.openOrder.getLimitOrder().sell_price.base.amount / Math.pow(10, base.precision)) / (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10, quote.precision));
//                if (openOrderItem.openOrder.getBaseObject().symbol.equals("CYB")) {
//                    mTotalCyb += price * (openOrderItem.openOrder.getLimitOrder().sell_price.quote.amount / Math.pow(10, openOrderItem.openOrder.getQuoteObject().precision));
//                } else {
//                    try {
//                        BitsharesWalletWraper.getInstance().get_ticker("1.3.0", openOrderItem.openOrder.getBaseObject().id.toString(), mCallback);
//                    } catch (NetworkStatusException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountFragmentInteractionListener) {
            mListener = (OnAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
        getContext().unbindService(mConnection);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setting, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(getContext(), SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAccountFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAccountFragmentInteraction(Uri uri);
    }

    public void isNetWorkAvailable(boolean isNetWorkAvailable) {
        loadData(mWebSocketService.getFullAccount(mName));
    }


}
