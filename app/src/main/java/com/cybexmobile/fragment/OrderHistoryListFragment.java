package com.cybexmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.adapter.OrderHistoryRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.Price;
import com.cybexmobile.R;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.market.Order;
import com.cybexmobile.market.OrderBook;
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
import butterknife.Unbinder;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OrderHistoryListFragment extends BaseFragment {

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
    private OnListFragmentInteractionListener mListener;
    private OrderHistoryRecyclerViewAdapter mOrderHistoryItemRecycerViewAdapter;
    private OrderBook mOrderBook;

    private Unbinder mUnbunder;

    public static OrderHistoryListFragment newInstance(WatchlistData watchListData) {
        OrderHistoryListFragment fragment = new OrderHistoryListFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);
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
            mOrderHistoryItemRecycerViewAdapter = new OrderHistoryRecyclerViewAdapter(mWatchlistData.getQuoteSymbol(), mOrderBook, mListener, getContext());
            recyclerView.setAdapter(mOrderHistoryItemRecycerViewAdapter);
        }
        loadOrderBook();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbunder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateOrderBook(Event.UpdateOrderBook event){
        mOrderBook = event.getData();
        mOrderHistoryItemRecycerViewAdapter.setValues(mOrderBook);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubcribeMarket(Event.UpdateRmbPrice event) {
        /**
         * rmb价格刷新 重新加载数据
         */
        loadOrderBook();
    }

    private void loadOrderBook(){
        if(mWatchlistData != null){
            try {
                BitsharesWalletWraper.getInstance().get_limit_orders(mWatchlistData.getBaseAsset().id, mWatchlistData.getQuoteAsset().id, 200, mLimitOrderCallback);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
    }

    private WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LimitOrderObject>>> mLimitOrderCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LimitOrderObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<LimitOrderObject>> reply) {
            List<LimitOrderObject> limitOrders = reply.result;
            if(limitOrders == null || limitOrders.size() == 0){
                return;
            }
            OrderBook orderBook = new OrderBook();
            orderBook.base = mWatchlistData.getBaseSymbol();
            orderBook.quote = mWatchlistData.getQuoteSymbol();
            orderBook.buyOrders = new ArrayList<>();
            orderBook.sellOrders = new ArrayList<>();
            Order order = null;
            for(LimitOrderObject limitOrder : limitOrders){
                if (limitOrder.sell_price.base.asset_id.equals(mWatchlistData.getBaseAsset().id)) {
                    if(orderBook.buyOrders.size() == 20){
                        continue;
                    }
                    order = new Order();
                    order.price = priceToReal(limitOrder.sell_price);
                    order.quoteAmount = ((double) limitOrder.for_sale * (double) limitOrder.sell_price.quote.amount)
                            / (double) limitOrder.sell_price.base.amount
                            / Math.pow(10, mWatchlistData.getQuotePrecision());
                    order.baseAmount = limitOrder.for_sale / Math.pow(10, mWatchlistData.getBasePrecision());
                    orderBook.buyOrders.add(order);
                } else {
                    if(orderBook.sellOrders.size() == 20){
                        continue;
                    }
                    order = new Order();
                    order.price = priceToReal(limitOrder.sell_price);
                    order.quoteAmount = limitOrder.for_sale / Math.pow(10, mWatchlistData.getQuotePrecision());
                    order.baseAmount = (double) limitOrder.for_sale * (double) limitOrder.sell_price.quote.amount
                            / limitOrder.sell_price.base.amount
                            / Math.pow(10, mWatchlistData.getBasePrecision());
                    orderBook.sellOrders.add(order);
                }
            }
            Collections.sort(orderBook.buyOrders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return (o1.price - o2.price) < 0 ? 1 : -1;
                }
            });
            Collections.sort(orderBook.sellOrders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return (o1.price - o2.price) < 0 ? -1 : 1;
                }
            });
            EventBus.getDefault().post(new Event.UpdateOrderBook(orderBook));
        }

        @Override
        public void onFailure() {

        }
    };

    private double assetToReal(Asset a, long p) {
        return (double) a.amount / Math.pow(10, p);
    }

    private double priceToReal(Price p) {
        if (p.base.asset_id.equals(mWatchlistData.getBaseAsset().id)) {
            return assetToReal(p.base, mWatchlistData.getBasePrecision())
                    / assetToReal(p.quote, mWatchlistData.getQuotePrecision());
        } else {
            return assetToReal(p.quote, mWatchlistData.getBasePrecision())
                    / assetToReal(p.base, mWatchlistData.getQuotePrecision());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
