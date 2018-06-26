package com.cybexmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.adapter.TradeHistoryRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.R;
import com.cybexmobile.market.MarketTrade;
import com.cybexmobile.utils.AssetUtil;
import com.google.gson.internal.LinkedTreeMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class MarketTradeHistoryFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_WATCHLIST = "watchlist";
    private TextView mQuoteTextView, mBaseTextView, mTvBasePrice;
    private List<MarketTrade> mMarketTradeList = new ArrayList<>();
    private WatchlistData mWatchlistData;
    private RecyclerView mRecyclerView;
    private TradeHistoryRecyclerViewAdapter mTradeHistoryRecyclerViewAdapter;

    public static MarketTradeHistoryFragment newInstance(WatchlistData watchListData) {
        MarketTradeHistoryFragment fragment = new MarketTradeHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_trade_history, container, false);
        mRecyclerView = view.findViewById(R.id.trade_history_list);
        mTvBasePrice = view.findViewById(R.id.market_page_base_asset_price);
        mQuoteTextView = view.findViewById(R.id.market_page_trade_history_quote);
        mBaseTextView = view.findViewById(R.id.market_page_trade_history_base);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewData();
    }

    private void initViewData(){
        if(mWatchlistData == null){
            return;
        }
        String trimmedBase = AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol());
        String trimmedQuote = AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol());
        mTvBasePrice.setText(getResources().getString(R.string.market_page_trade_history_price).replace("--", trimmedBase));
        mBaseTextView.setText(getResources().getString(R.string.market_page_trade_history_base).replace("--", trimmedBase));
        mQuoteTextView.setText(getResources().getString(R.string.market_page_trade_history_quote).replace("--", trimmedQuote));
        mTradeHistoryRecyclerViewAdapter = new TradeHistoryRecyclerViewAdapter(mMarketTradeList, mWatchlistData.getBasePrecision(), mWatchlistData.getQuotePrecision(), getContext());
        mRecyclerView.setAdapter(mTradeHistoryRecyclerViewAdapter);
        loadMarketTradHistory();
    }

    private void loadMarketTradHistory(){
        if(mWatchlistData != null){
            try {
                BitsharesWalletWraper.getInstance().get_fill_order_history(mWatchlistData.getBaseAsset().id, mWatchlistData.getQuoteAsset().id, 40, mMarketTradeHistoryCallback);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
    }

    private WebSocketClient.MessageCallback<WebSocketClient.Reply<List<HashMap<String, Object>>>> mMarketTradeHistoryCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<HashMap<String, Object>>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<HashMap<String, Object>>> reply) {
            List<HashMap<String, Object>> hashMaplist = reply.result;
            if(hashMaplist == null || hashMaplist.size() == 0){
                return;
            }
            List<MarketTrade> marketTrades = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.US);
            MarketTrade marketTrade = null;
            for (int i = 0; i < hashMaplist.size(); i += 2) {
                marketTrade = new MarketTrade();
                LinkedTreeMap op = (LinkedTreeMap) hashMaplist.get(i).get("op");
                LinkedTreeMap pays = (LinkedTreeMap) op.get("pays");
                LinkedTreeMap receives = (LinkedTreeMap) op.get("receives");
                String date = (String) hashMaplist.get(i).get("time");
                try {
                    Date converted = simpleDateFormat.parse(date);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(converted);
                    cal.add(Calendar.HOUR_OF_DAY, 8);
                    marketTrade.date = simpleDateFormat1.format(cal.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                marketTrade.base = mWatchlistData.getBaseSymbol();
                marketTrade.quote = mWatchlistData.getQuoteSymbol();
                String paysAmount = String.format("%s", pays.get("amount"));
                String receiveAmount = String.format("%s", receives.get("amount"));
                if (pays.get("asset_id").equals(mWatchlistData.getBaseId())) {
                    marketTrade.baseAmount = Double.parseDouble(paysAmount) / Math.pow(10, mWatchlistData.getBasePrecision());
                    marketTrade.quoteAmount = Double.parseDouble(receiveAmount) / Math.pow(10, mWatchlistData.getQuotePrecision());
                    marketTrade.price = marketTrade.baseAmount / marketTrade.quoteAmount;
                    marketTrade.showRed = "showRed";
                } else {
                    marketTrade.quoteAmount = Double.parseDouble(paysAmount) / Math.pow(10, mWatchlistData.getQuotePrecision());
                    marketTrade.baseAmount = Double.parseDouble(receiveAmount) / Math.pow(10, mWatchlistData.getBasePrecision());

                    marketTrade.price = marketTrade.baseAmount / marketTrade.quoteAmount;
                    marketTrade.showRed = "showGreen";
                }
                marketTrades.add(marketTrade);
            }
            EventBus.getDefault().post(new Event.UpdateMarketTrade(marketTrades));
        }

        @Override
        public void onFailure() {

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribeMarket(Event.SubscribeMarket event) {
        if(mWatchlistData.getSubscribeId() == event.getCallId()) {
            loadMarketTradHistory();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateMarketTrade(Event.UpdateMarketTrade event) {
        mMarketTradeList.clear();
        mMarketTradeList.addAll(event.getData());
        mTradeHistoryRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    public void changeWatchlist(WatchlistData watchlist){
        this.mWatchlistData = watchlist;
        initViewData();
    }

}
