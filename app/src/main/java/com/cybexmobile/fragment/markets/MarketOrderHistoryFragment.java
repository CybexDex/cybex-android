package com.cybexmobile.fragment.markets;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.rte.RteRequest;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
import com.cybex.provider.market.WatchlistData;
import com.cybex.provider.websocket.rte.RxRteWebSocket;
import com.cybexmobile.R;
import com.cybexmobile.adapter.MarketOrderHistoryRecyclerViewAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class MarketOrderHistoryFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_WATCHLIST = "watchlist";

    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.order_history_tv_buy_price)
    TextView mTvBuyPrice;
    @BindView(R.id.order_history_tv_buy_amount)
    TextView mTvBuyAmount;
    @BindView(R.id.order_history_tv_sell_price)
    TextView mTvSellPrice;
    @BindView(R.id.order_history_tv_sell_amount)
    TextView mTvSellAmount;

    private WatchlistData mWatchlistData;
    private MarketOrderHistoryRecyclerViewAdapter mOrderHistoryItemRecycerViewAdapter;
    private final Gson mGson = new Gson();
    private final JsonParser mJsonParser = new JsonParser();
    private RteRequest mRteRequestDepth;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private RxRteWebSocket mRxRteWebSocket;

    private Unbinder mUnbunder;

    public static MarketOrderHistoryFragment newInstance(WatchlistData watchListData) {
        MarketOrderHistoryFragment fragment = new MarketOrderHistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WATCHLIST, watchListData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWatchlistData = (WatchlistData) getArguments().getSerializable(ARG_WATCHLIST);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market_order_history, container, false);
        mUnbunder = ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mWatchlistData != null){
            mTvBuyPrice.setText(getResources().getString(R.string.market_page_buy_price).replace("--", AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
            mTvBuyAmount.setText(getResources().getString(R.string.market_page_trade_history_quote).replace("--", AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())));
            mTvSellPrice.setText(getResources().getString(R.string.market_page_sell_price).replace("--", AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol())));
            mTvSellAmount.setText(getResources().getString(R.string.market_page_trade_history_quote).replace("--", AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol())));
            mOrderHistoryItemRecycerViewAdapter = new MarketOrderHistoryRecyclerViewAdapter(mWatchlistData, getContext());
            recyclerView.setAdapter(mOrderHistoryItemRecycerViewAdapter);
            initRTEWebSocket();
        }
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
                                            mWatchlistData.getBaseSymbol().replace(".", "_") + "." + mWatchlistData.getPricePrecision() + ".20");
                            sendRteRequest(mRteRequestDepth);
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
                        Log.d("dzm", webSocketMessage.getText());
                        JsonElement jsonElement = mJsonParser.parse(webSocketMessage.getText());
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        List<List<String>> sellOrders = mGson.fromJson(jsonObject.get("asks"), new TypeToken<List<List<String>>>(){}.getType());
                        List<List<String>> buyOrders = mGson.fromJson(jsonObject.get("bids"), new TypeToken<List<List<String>>>(){}.getType());
                        mOrderHistoryItemRecycerViewAdapter.setValues(sellOrders, buyOrders);
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
    private void sendRteRequest(RteRequest request) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbunder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRxRteWebSocket.close(1000, "close");
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

}
