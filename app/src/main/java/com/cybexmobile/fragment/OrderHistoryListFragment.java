package com.cybexmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.R;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybexmobile.market.OrderBook;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OrderHistoryListFragment extends Fragment implements MarketStat.OnMarketStatUpdateListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ORDER_BOOK = "order-book";
    private static final String SUBSCRIBE_ID = "subscribe-id";
    private static final String BASE_NAME = "base-name";
    private static final String QUOTE_NAME = "quote-name";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private String mSubscribeId, mBaseName, mQuoteName;
    private OrderHistoryItemRecyclerViewAdapter mOrderHistoryItemRecycerViewAdapter;
    private OrderBook mOrderBook;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrderHistoryListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OrderHistoryListFragment newInstance(int columnCount, OrderBook orderBook, WatchListData watchListData) {
        OrderHistoryListFragment fragment = new OrderHistoryListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putSerializable(ORDER_BOOK, orderBook);
        args.putString(SUBSCRIBE_ID, watchListData.getSubscribeId());
        args.putString(BASE_NAME, watchListData.getBase());
        args.putString(QUOTE_NAME, watchListData.getQuote());
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
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);
        Context context = view.getContext();
        mOrderBook = (OrderBook) getArguments().getSerializable(ORDER_BOOK);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        mSubscribeId = getArguments().getString(SUBSCRIBE_ID);
        mBaseName = getArguments().getString(BASE_NAME);
        mQuoteName = getArguments().getString(QUOTE_NAME);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mOrderHistoryItemRecycerViewAdapter = new OrderHistoryItemRecyclerViewAdapter(mQuoteName, mOrderBook, mListener, getContext());
        recyclerView.setAdapter(mOrderHistoryItemRecycerViewAdapter);
        return view;
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        if(string.equals(mSubscribeId)) {
            MarketStat.getInstance().subscribe(mBaseName, mQuoteName, MarketStat.STAT_MARKET_ORDER_BOOK, (long)5, 3600, this);
        }
    }

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {
        mOrderHistoryItemRecycerViewAdapter.setmValues(stat.orderBook);
        mOrderHistoryItemRecycerViewAdapter.notifyDataSetChanged();
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
