package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.market.MarketTicker;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountFragment extends BaseFragment {

    private OnAccountFragmentInteractionListener mListener;
    private RecyclerView mPortfolioRecyclerView;
    private PortfolioRecyclerViewAdapter mPortfolioRecyclerViewAdapter;
    private TextView mLoginTextView, mMembershipTextView, mViewAllTextView, mSayHelloTextView, mTotalAccountTextView, mAccountTotalRmbTextView;
    private WebView mAvatarWebView;
    private ImageView mAvatarImageView, mBalanceInfoImageView;
    private LinearLayout mBeforeLoginLayout, mAfterLoginLayout;
    private RelativeLayout mPortfolioTitleLayout, mOpenOrderLayout, mOpenLockAssetsLayout;
    private SharedPreferences mSharedPreference;
    //所用资产
    private volatile List<AccountBalanceObject> mAccountObjectBalance = new ArrayList<>();
    //所有资产详情
    private volatile List<AssetObject> mAssetObjects = new ArrayList<>();
    //所有委单
    private volatile List<LimitOrderObject> mLimitOrderObjectList = new ArrayList<>();
    private volatile double mTotalCyb;
    private boolean mIsLoginIn;
    private String mName;
    private String mMembershipExpirationDate;

    private double mCybRmbPrice;

    private WebSocketService mWebSocketService;

    private static final int MESSAGE_WHAT_REFRUSH_VIEW = 1;
    private static final int MESSAGE_WHAT_REFRUSH_TOTAL_CYB = 2;

    private boolean mIsViewCreated;

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
        mIsLoginIn = mSharedPreference.getBoolean("isLoggedIn", false);
        mName = mSharedPreference.getString("name", "");
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        initViews(view);
        setClickListener();
        setViews();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewCreated = true;
        if(mIsLoginIn){
            if(mWebSocketService != null){
                loadData(mWebSocketService.getFullAccount(mName));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOut(Event.LoginOut event) {
        mIsLoginIn = false;
        mName = null;
        setViews();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event){
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if(assetRmbPrices == null || assetRmbPrices.size() == 0){
            return;
        }
        for(AssetRmbPrice assetRmbPrice : assetRmbPrices){
            if("CYB".equals(assetRmbPrice.getName())){
                setTotalRmbTextView(mTotalCyb * assetRmbPrice.getValue());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        loadData(event.getData());
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_WHAT_REFRUSH_VIEW:
                    updateMemberShipViewData();
                    mPortfolioRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_WHAT_REFRUSH_TOTAL_CYB:
                    if (mTotalCyb == 0) {
                        mTotalAccountTextView.setText("0.00000≈¥0.00");
                        mAccountTotalRmbTextView.setText("");
                    } else {
                        mTotalAccountTextView.setText(String.format(Locale.US, "%.5f", mTotalCyb));
                        if(mWebSocketService != null){
                            AssetRmbPrice assetRmbPrice = mWebSocketService.getAssetRmbPrice("CYB");
                            mCybRmbPrice = assetRmbPrice == null ? 0 : assetRmbPrice.getValue();
                        }
                        setTotalRmbTextView(mTotalCyb * mCybRmbPrice);
                    }
                    break;
            }
        }
    };

    private WebSocketClient.MessageCallback getObjectCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<AssetObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<AssetObject>> reply) {
            mAssetObjects = reply.result;
            for(AssetObject assetObject : mAssetObjects){
                if (!assetObject.id.toString().equals("1.3.0")) {
                    try {
                        BitsharesWalletWraper.getInstance().get_ticker("1.3.0", assetObject.id.toString(), getTickerCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                } else {
                    for(AccountBalanceObject balanceObject : mAccountObjectBalance){
                        if(balanceObject.asset_type.toString().equals(assetObject.id.toString())){
                            calculateTotalCyb(balanceObject.balance, assetObject.precision, 1);
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure() {

        }
    };

    private WebSocketClient.MessageCallback getTickerCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<MarketTicker>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<MarketTicker> reply) {
            MarketTicker ticker = reply.result;
            for(AssetObject assetObject : mAssetObjects){
                if(!ticker.quote.equals(assetObject.id.toString())){
                    continue;
                }
                for(AccountBalanceObject balanceObject : mAccountObjectBalance){
                    if(balanceObject.asset_type.toString().equals(assetObject.id.toString())){
                        calculateTotalCyb(balanceObject.balance, assetObject.precision, ticker.latest);
                    }
                }
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
            if(mIsViewCreated){
                loadData(mWebSocketService.getFullAccount(mName));
            }
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

    private void initViews(View view) {
        mBeforeLoginLayout = view.findViewById(R.id.account_no_log_in);
        mAfterLoginLayout = view.findViewById(R.id.account_logged_in);
        mLoginTextView = view.findViewById(R.id.account_log_in_text);
        mSayHelloTextView = view.findViewById(R.id.account_say_hello_text_view);
        mMembershipTextView = view.findViewById(R.id.account_membership_text);
        mPortfolioRecyclerView = view.findViewById(R.id.account_my_asset_recycler_view);
        mViewAllTextView = view.findViewById(R.id.account_view_all);
        mAvatarImageView = view.findViewById(R.id.account_avatar);
        mAvatarWebView = view.findViewById(R.id.account_avatar_webview);
        mTotalAccountTextView = view.findViewById(R.id.account_balance);
        mOpenOrderLayout = view.findViewById(R.id.account_open_order_item_background);
        mOpenLockAssetsLayout = view.findViewById(R.id.account_lockup_item_background);
        mPortfolioTitleLayout = view.findViewById(R.id.portfolio_title_layout);
        mBalanceInfoImageView = view.findViewById(R.id.balance_info_question_marker);
        mAccountTotalRmbTextView = view.findViewById(R.id.account_balance_total_rmb);
    }

    private void setViews() {
        if (mIsLoginIn) {
            processLogIn();
        } else {
            mAvatarImageView.setVisibility(View.VISIBLE);
            mAvatarWebView.setVisibility(View.GONE);
            mBeforeLoginLayout.setVisibility(View.VISIBLE);
            mAfterLoginLayout.setVisibility(View.GONE);
        }
    }

    private void processLogIn() {
        mAfterLoginLayout.setVisibility(View.VISIBLE);
        mBeforeLoginLayout.setVisibility(View.GONE);
        mAvatarWebView.setVisibility(View.VISIBLE);
        mAvatarImageView.setVisibility(View.GONE);
        loadWebView(mAvatarWebView, 56);
        initSayHelloViewData();
        initMemberShipViewData();
        initRecyclerViewData();
    }


    private void initMemberShipViewData() {
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.membership_item_background);
        drawable.mutate().setAlpha(50);
        mMembershipTextView.setBackground(drawable);
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

    private void initRecyclerViewData() {
        mPortfolioRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPortfolioTitleLayout.setVisibility(mAccountObjectBalance.size() == 0 ? View.GONE : View.VISIBLE);
        mPortfolioRecyclerViewAdapter = new PortfolioRecyclerViewAdapter(mAccountObjectBalance);
        mPortfolioRecyclerView.setAdapter(mPortfolioRecyclerViewAdapter);
    }

    private void initSayHelloViewData() {
        mSayHelloTextView.setText(mName);
    }

    private void setTotalRmbTextView(double totalRmb){
        mAccountTotalRmbTextView.setText(String.format(Locale.US, "≈¥%.2f", totalRmb));
    }

    private void setClickListener() {
        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        mViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PortfolioActivity.class);
                startActivity(intent);
            }
        });
        mOpenOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OpenOrdersActivity.class);
                startActivity(intent);
            }
        });
        mOpenLockAssetsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LockAssetsActivity.class);
                startActivity(intent);
            }
        });

        mBalanceInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CybexDialog.showBalanceDialog(getActivity());
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAccountFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("LogIn", false)) {
                    mName = data.getStringExtra("name");
                    mIsLoginIn = true;
                    processLogIn();
                    if(mWebSocketService != null){
                        loadData(mWebSocketService.getFullAccount(mName));
                    }
                }
            }
        }
    }

    private void loadData(FullAccountObject fullAccountObject){
        if(fullAccountObject == null){
            return;
        }
        hideLoadDialog();
        mLimitOrderObjectList = fullAccountObject.limit_orders;
        mAccountObjectBalance = fullAccountObject.balances;
        mMembershipExpirationDate  = fullAccountObject.account.membership_expiration_date;
        mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_VIEW);
        if(mLimitOrderObjectList.size() > 0){
            for (LimitOrderObject limitOrderObject : mLimitOrderObjectList) {
                mTotalCyb += limitOrderObject.for_sale / Math.pow(10, 5);
            }
            mHandler.sendEmptyMessage(MESSAGE_WHAT_REFRUSH_TOTAL_CYB);
        }
        if(mAccountObjectBalance.size() > 0){
            List<String> objectIds = new ArrayList<>();
            for (AccountBalanceObject balance : mAccountObjectBalance) {
                objectIds.add(balance.asset_type.toString());
            }
            try {
                BitsharesWalletWraper.getInstance().get_objects(objectIds, getObjectCallback);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
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
}
