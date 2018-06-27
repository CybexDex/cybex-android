package com.cybexmobile.fragment;

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

import com.cybexmobile.R;
import com.cybexmobile.adapter.ExchangeOpenOrderRecyclerViewAdapter;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.market.OpenOrder;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

/**
 * 交易界面当前用户当前交易对委单
 */
public class OpenOrdersFragment extends BaseFragment {

    @BindView(R.id.open_orders_recycler_view)
    RecyclerView mRvOpenOrders;

    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();
    private WatchlistData mWatchlistData;

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;
    private ExchangeOpenOrderRecyclerViewAdapter mOpenOrderRecyclerViewAdapter;

    private List<String> mCompareSymbol = Arrays.asList(new String[]{"JADE.USDT", "JADE.ETH", "JADE.BTC", "CYB"});

    public static OpenOrdersFragment getInstance(WatchlistData watchlistData){
        OpenOrdersFragment fragment = new OpenOrdersFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
        Bundle bundle = getArguments();
        if(bundle != null){
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_orders, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mRvOpenOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvOpenOrders.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOpenOrderRecyclerViewAdapter = new ExchangeOpenOrderRecyclerViewAdapter(getContext(), mOpenOrderItems);
        mRvOpenOrders.setAdapter(mOpenOrderRecyclerViewAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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

    /**
     * 交易对改变
     * @param watchlist
     */
    public void changeWatchlist(WatchlistData watchlist){
        if(watchlist == null){
            return;
        }
        this.mWatchlistData = watchlist;
        notifyRecyclerView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        List<LimitOrderObject> limitOrderObjects = event.getData() == null ? null : event.getData().limit_orders;
        parseOpenOrderItems(limitOrderObjects);
        notifyRecyclerView();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isLoginIn = sharedPreferences.getBoolean("isLoggedIn", false);
            if(isLoginIn){
                FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(sharedPreferences.getString("name", null));
                List<LimitOrderObject> limitOrderObjects = fullAccountObject == null ? null : fullAccountObject.limit_orders;
                parseOpenOrderItems(limitOrderObjects);
                notifyRecyclerView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

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

    private void notifyRecyclerView(){
        if(mOpenOrderItems == null || mOpenOrderItems.size() == 0){
            return;
        }
        if(mWatchlistData == null){
            return;
        }
        List<OpenOrderItem> openOrderItems = new ArrayList<>();
        //过滤非当前交易对委单
        for(OpenOrderItem item : mOpenOrderItems){
            String baseId = item.openOrder.getLimitOrder().sell_price.base.asset_id.toString();
            String quoteId = item.openOrder.getLimitOrder().sell_price.quote.asset_id.toString();
            if(!(mWatchlistData.getBaseId().equals(baseId) || mWatchlistData.getBaseId().equals(quoteId)) ||
                    !(mWatchlistData.getQuoteId().equals(baseId) || mWatchlistData.getQuoteId().equals(quoteId))){
                continue;
            }
            openOrderItems.add(item);
        }
        mOpenOrderRecyclerViewAdapter.setOpenOrderItems(openOrderItems);
    }

    private void parseOpenOrderItems(List<LimitOrderObject> limitOrderObjects){
        if(limitOrderObjects == null || limitOrderObjects.size() == 0){
            return;
        }
        mOpenOrderItems.clear();
        for (LimitOrderObject limitOrderObject : limitOrderObjects) {
            String baseId = limitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
            OpenOrderItem item = new OpenOrderItem();
            OpenOrder openOrder = new OpenOrder();
            openOrder.setLimitOrder(limitOrderObject);
            List<AssetObject> assetObjects = mWebSocketService.getAssetObjects(baseId, quoteId);
            if (assetObjects != null && assetObjects.size() == 2) {
                String baseSymbol = assetObjects.get(0).symbol;
                String quoteSymbol = assetObjects.get(1).symbol;
                item.isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                openOrder.setBaseObject(assetObjects.get(0));
                openOrder.setQuoteObject(assetObjects.get(1));
            }
            item.openOrder = openOrder;
            mOpenOrderItems.add(item);
        }
    }
}
