package com.cybexmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.R;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.market.MarketTrade;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MarketTradeHistoryFragment extends Fragment implements MarketStat.OnMarketStatUpdateListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String SUBSCRIBE_ID = "subscribe-id";
    private static final String BASE_NAME = "base-name";
    private static final String QUOTE_NAME = "quote-name";
    private static final String MARKET_TRADE = "market_trade";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String mSubscribeId, mBaseName, mQuoteName;
    private OnListFragmentInteractionListener mListener;
    private TextView mQuoteTextView, mBaseTextView;
    private List<MarketTrade> mMarketTradeList;
    private RecyclerView mRecyclerView;
    private TradeHistoryRecyclerViewAdapter mTradeHistoryRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MarketTradeHistoryFragment() {

    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MarketTradeHistoryFragment newInstance(int columnCount, List<MarketTrade> marketTradeList, WatchListData watchListData) {
        MarketTradeHistoryFragment fragment = new MarketTradeHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(SUBSCRIBE_ID, watchListData.getSubscribeId());
        args.putString(BASE_NAME, watchListData.getBase());
        args.putString(QUOTE_NAME, watchListData.getQuote());
        args.putParcelableArrayList(MARKET_TRADE, (ArrayList<? extends Parcelable>) marketTradeList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade_history, container, false);

        // Set the adapter
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.trade_history_list);
        mQuoteTextView = view.findViewById(R.id.market_page_trade_history_quote);
        mBaseTextView = view.findViewById(R.id.market_page_trade_history_base);
        mMarketTradeList = getArguments().getParcelableArrayList(MARKET_TRADE);
        mSubscribeId = getArguments().getString(SUBSCRIBE_ID);
        mBaseName = getArguments().getString(BASE_NAME);
        mQuoteName = getArguments().getString(QUOTE_NAME);
        if(mMarketTradeList != null && mMarketTradeList.size() > 0) {
            String displayBaseName = mMarketTradeList.get(0).base;
            String displayQuoteName = mMarketTradeList.get(0).quote;
            mBaseTextView.setText(displayBaseName.contains("JADE") ? displayBaseName.substring(5, displayBaseName.length()) : displayBaseName);
            mQuoteTextView.setText(displayQuoteName.contains("JADE") ? displayQuoteName.substring(5, displayQuoteName.length()) : displayQuoteName);
        }
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mTradeHistoryRecyclerViewAdapter = new TradeHistoryRecyclerViewAdapter(mMarketTradeList, mListener, getContext());
        mRecyclerView.setAdapter(mTradeHistoryRecyclerViewAdapter);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        if(string.equals(mSubscribeId)) {
            MarketStat.getInstance().subscribe(mBaseName, mQuoteName, MarketStat.STAT_MARKET_FILL_ORDER_HISTORY, (long)5, 3600, this);
        }
    }

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {
        mMarketTradeList.clear();
        mMarketTradeList.addAll(stat.marketTradeList);
        mTradeHistoryRecyclerViewAdapter.notifyDataSetChanged();
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
        void onListFragmentInteraction(MarketTrade item);
    }
}
