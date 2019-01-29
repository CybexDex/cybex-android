package com.cybexmobile.fragment.orders;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.R;
import com.cybexmobile.adapter.TradeHistoryRecyclerViewAdapter;
import com.cybexmobile.graphene.chain.TradeHistory;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_IS_LOAD_ALL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

/**
 * 交易历史（当前用户）
 */
public class TradeHistoryFragment extends BaseFragment implements OnRefreshListener, OnLoadMoreListener {

    private static final int MAX_PAGE_COUNT = 20;

    @BindView(R.id.order_history_rv)
    RecyclerView mRvOrderHistory;
    @BindView(R.id.layout_refresh_exchange_records)
    SmartRefreshLayout mRefreshLayout;

    private TradeHistoryRecyclerViewAdapter mTradeHistoryRecyclerViewAdapter;

    private List<TradeHistoryItem> mOrderHistoryItems = new ArrayList<>();

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;

    private boolean mIsLoginIn;
    private String mName;
    private boolean mIsLoadAll;

    private FullAccountObject mFullAccountObject;
    private WatchlistData mWatchlistData;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private int mCurrPage;

    public static TradeHistoryFragment getInstance(WatchlistData watchlistData, boolean isLoadAll) {
        TradeHistoryFragment fragment = new TradeHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        bundle.putBoolean(INTENT_PARAM_IS_LOAD_ALL, isLoadAll);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mIsLoginIn = sharedPreferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = sharedPreferences.getString(PREF_NAME, null);
        mWatchlistData = (WatchlistData) getArguments().getSerializable(INTENT_PARAM_WATCHLIST);
        mIsLoadAll = getArguments().getBoolean(INTENT_PARAM_IS_LOAD_ALL, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_history, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mRvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvOrderHistory.setItemAnimator(null);
        mRvOrderHistory.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTradeHistoryRecyclerViewAdapter = new TradeHistoryRecyclerViewAdapter(getContext(), mOrderHistoryItems);
        mRvOrderHistory.setAdapter(mTradeHistoryRecyclerViewAdapter);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
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
        getContext().unbindService(mConnection);
        mCompositeDisposable.isDisposed();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        loadExchangeHistory(++mCurrPage, MAX_PAGE_COUNT, false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadExchangeHistory(mCurrPage = 0, MAX_PAGE_COUNT, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        if (mFullAccountObject == null) {
            mFullAccountObject = event.getFullAccount();
            loadExchangeHistory(mCurrPage, MAX_PAGE_COUNT, false);
        }
    }

    private void parseBuyOrSell(TradeHistoryItem item, Map<String, List<AssetsPair>> assetPairs){
        String payAssetId = item.tradeHistory.pays.asset_id;
        String receiveAssetId = item.tradeHistory.receives.asset_id;
        here:
        for(Map.Entry<String, List<AssetsPair>> entry : assetPairs.entrySet()){
            //支付baseAssetId 接收quoteAssetId 为买单
            if(entry.getKey().equals(payAssetId)){
                for(AssetsPair assetsPair : entry.getValue()){
                    if(assetsPair.getQuote().equals(receiveAssetId)){
                        item.isSell = false;
                        item.baseAsset = assetsPair.getBaseAsset();
                        item.quoteAsset = assetsPair.getQuoteAsset();
                        break here;
                    }
                }
            }
            //支付quoteAssetId 接收baseAssetId 为卖单
            if(entry.getKey().equals(receiveAssetId)){
                for(AssetsPair assetsPair : entry.getValue()){
                    if(assetsPair.getQuote().equals(payAssetId)){
                        item.isSell = true;
                        item.baseAsset = assetsPair.getBaseAsset();
                        item.quoteAsset = assetsPair.getQuoteAsset();
                        break here;
                    }
                }
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mFullAccountObject = mWebSocketService.getFullAccount(mName);
            loadExchangeHistory(mCurrPage, MAX_PAGE_COUNT, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void loadExchangeHistory(int page, int limit, boolean isRefresh) {
        if (!mIsLoginIn || mFullAccountObject == null || (!mIsLoadAll && mWatchlistData == null)) {
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
            return;
        }
        if (!isRefresh && (mOrderHistoryItems.size() == 0 || mOrderHistoryItems.size() % MAX_PAGE_COUNT != 0)) {
            mRefreshLayout.finishLoadMore();
            mRefreshLayout.setNoMoreData(true);
        }
        mCompositeDisposable.add(RetrofitFactory.getInstance().apiCybexLive()
                .getExchangeRecords(mFullAccountObject.account.id.toString(),
                        page,
                        limit,
                        mIsLoadAll ? "null" : mWatchlistData.getBaseId(),
                        mIsLoadAll ? "null" : mWatchlistData.getQuoteId(),
                        "null",
                        "null")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountHistoryObjects -> {
                    mRefreshLayout.finishRefresh();
                    mRefreshLayout.finishLoadMore();
                    if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
                        return;
                    }
                    TradeHistoryItem item = null;
                    Gson gson = new Gson();
                    Map<String, List<AssetsPair>> assetPairs = mWebSocketService.getAssetPairHashMap();
                    if (isRefresh) { mOrderHistoryItems.clear(); }
                    for (AccountHistoryObject accountHistoryObject : accountHistoryObjects) {
                        item = new TradeHistoryItem();
                        item.accountHistoryObject = accountHistoryObject;
                        item.tradeHistory = gson.fromJson(accountHistoryObject.op.get(1), TradeHistory.class);
                        item.feeAsset = mWebSocketService.getAssetObject(item.tradeHistory.fee.asset_id);
                        parseBuyOrSell(item, assetPairs);
                        if (item.baseAsset != null && item.quoteAsset != null) {
                            mOrderHistoryItems.add(item);
                        }
                    }
                    mTradeHistoryRecyclerViewAdapter.notifyDataSetChanged();
                }, throwable -> {
                    mRefreshLayout.finishRefresh();
                    mRefreshLayout.finishLoadMore();
                }));
    }

    public class TradeHistoryItem {
        public boolean isSell;
        public AccountHistoryObject accountHistoryObject;
        public TradeHistory tradeHistory;
        public AssetObject baseAsset;
        public AssetObject quoteAsset;
        public AssetObject feeAsset;
    }
}
