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
import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AssetsPair;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.apihk.LimitOrderWrapper;
import com.cybexmobile.R;
import com.cybexmobile.adapter.OrdersHistoryRecyclerViewAdapter;
import com.cybexmobile.data.item.OpenOrderItem;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_IS_LOAD_ALL;
import static com.cybex.basemodule.constant.Constant.BUNDLE_SAVE_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_IS_LOAD_ALL;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

/**
 * 历史委托（当前用户）
 */
public class OrdersHistoryFragment extends BaseFragment implements OnRefreshListener, OnLoadMoreListener {

    private static final int MAX_PAGE_COUNT = 20;

    @BindView(R.id.order_history_rv)
    RecyclerView mRvOrderHistory;
    @BindView(R.id.layout_refresh_orders_history)
    SmartRefreshLayout mRefreshLayout;

    private WatchlistData mWatchlistData;
    private FullAccountObject mFullAccount;
    private WebSocketService mWebSocketService;

    private OrdersHistoryRecyclerViewAdapter mOrdersHistoryRecyclerViewAdapter;

    private List<OpenOrderItem> mOrderHistoryItems = new ArrayList<>();

    private Unbinder mUnbinder;

    private boolean mIsLoginIn;
    private String mName;
    private boolean mIsLoadAll;

    private String mLastOrderId;

    public static OrdersHistoryFragment getInstance(WatchlistData watchlistData, boolean isLoadAll){
        OrdersHistoryFragment fragment = new OrdersHistoryFragment();
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
        Bundle bundle = getArguments();
        if(bundle != null){
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
            mIsLoadAll = bundle.getBoolean(INTENT_PARAM_IS_LOAD_ALL, false);
        }
        if(savedInstanceState != null){
            mWatchlistData = (WatchlistData) savedInstanceState.getSerializable(BUNDLE_SAVE_WATCHLIST);
            mFullAccount = (FullAccountObject) savedInstanceState.getSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT);
            mIsLoadAll = savedInstanceState.getBoolean(BUNDLE_SAVE_IS_LOAD_ALL, false);
        }
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_history, container, false);
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
        mOrdersHistoryRecyclerViewAdapter = new OrdersHistoryRecyclerViewAdapter(getContext(), mOrderHistoryItems);
        mRvOrderHistory.setAdapter(mOrdersHistoryRecyclerViewAdapter);
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_SAVE_WATCHLIST, mWatchlistData);
        outState.putSerializable(BUNDLE_SAVE_FULL_ACCOUNT_OBJECT, mFullAccount);
        outState.putBoolean(BUNDLE_SAVE_IS_LOAD_ALL, mIsLoadAll);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        nextPageOrderId();
        loadOrdersHistory(false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadLastOrderId();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mFullAccount = mWebSocketService.getFullAccount(mName);
            loadLastOrderId();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void parseOpenOrderItems(List<LimitOrder> limitOrders, boolean isRefresh){
        if (isRefresh) {
            mOrderHistoryItems.clear();
        }
        if(limitOrders == null || limitOrders.size() == 0){
            return;
        }
        for (LimitOrder limitOrder : limitOrders) {
            OpenOrderItem item = new OpenOrderItem();
            item.limitOrder = limitOrder;
            AssetsPair assetsPair = AssetPairCache.getInstance().getAssetPair(limitOrder.key.asset1, limitOrder.key.asset2);
            item.isSell = limitOrder.is_sell ? limitOrder.key.asset2.equals(assetsPair.getBase()) : limitOrder.key.asset1.equals(assetsPair.getBase());
            item.baseAsset = assetsPair.getBaseAsset();
            item.quoteAsset = assetsPair.getQuoteAsset();
            mOrderHistoryItems.add(item);
        }
        mLastOrderId = mOrderHistoryItems.get(mOrderHistoryItems.size() - 1).limitOrder.order_id.toString();
    }

    private void loadLastOrderId() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        LimitOrderWrapper.getInstance().get_limit_order_id_by_time(format.format(new Date()), new MessageCallback<Reply<String>>() {
            @Override
            public void onMessage(Reply<String> reply) {
                mLastOrderId = reply.result;
                loadOrdersHistory(true);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * 下一页OrderId
     *
     */
    private void nextPageOrderId() {
        String[] idSplits = mLastOrderId.split("\\.");
        int idNext = Integer.parseInt(idSplits[2]) - 1;
        mLastOrderId =  mLastOrderId.replace(idSplits[2], String.valueOf(idNext > 0 ? idNext : 0));
    }

    private void loadOrdersHistory(boolean isRefresh) {
        if(!mIsLoginIn || mFullAccount == null || (!mIsLoadAll && mWatchlistData == null)) {
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
            return;
        }
        if (!isRefresh && (mOrderHistoryItems.size() == 0 || mOrderHistoryItems.size() % MAX_PAGE_COUNT != 0)) {
            mRefreshLayout.finishLoadMore();
            mRefreshLayout.setNoMoreData(true);
        }
        if (mIsLoadAll) {
            LimitOrderWrapper.getInstance().get_limit_order_status(
                    mFullAccount.account.id.toString(),
                    mLastOrderId,
                    MAX_PAGE_COUNT,
                    new MessageCallback<Reply<List<LimitOrder>>>() {
                        @Override
                        public void onMessage(Reply<List<LimitOrder>> reply) {
                            parseOpenOrderItems(reply.result, isRefresh);
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadMore();
                            mOrdersHistoryRecyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
        } else {
            LimitOrderWrapper.getInstance().get_market_limit_order_status(
                    mFullAccount.account.id.toString(),
                    mLastOrderId,
                    mWatchlistData.getBaseId(),
                    mWatchlistData.getQuoteId(),
                    MAX_PAGE_COUNT,
                    new MessageCallback<Reply<List<LimitOrder>>>() {
                        @Override
                        public void onMessage(Reply<List<LimitOrder>> reply) {
                            parseOpenOrderItems(reply.result, isRefresh);
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadMore();
                            mOrdersHistoryRecyclerViewAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
        }
    }

}
