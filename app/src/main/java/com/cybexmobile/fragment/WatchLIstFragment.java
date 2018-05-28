package com.cybexmobile.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybexmobile.adapter.WatchListRecyclerViewAdapter;
import com.cybexmobile.fragment.data.WatchListData;
import com.cybexmobile.R;
import com.cybexmobile.market.MarketStat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WatchLIstFragment extends Fragment implements MarketStat.OnMarketStatUpdateListener, MarketStat.getResultListener {

    private static final String TAG = "WatchListFragment";
    private MarketStat marketStat;
    private List<WatchListData> watchListDataList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    private TabLayout mTabLayout;
    private Context mContext;
    private View view;
    private ProgressBar mProgressBar;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private Handler mHandler;

    private String[] mTabs = new String[]{"CYB", "ETH", "BTC", "EOS"};
    private String[] mAssetCode = new String[]{"1.3.0", "1.3.2", "1.3.3", "1.3.4", "1.3.5"};
    private String mTab;
    private WatchListRecyclerViewAdapter mWatchListRecyclerViewAdapter;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WatchLIstFragment() {
        marketStat = MarketStat.getInstance();
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WatchLIstFragment newInstance(int columnCount) {
        WatchLIstFragment fragment = new WatchLIstFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.watchlist_list, container, false);
        mContext = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mTabLayout = view.findViewById(R.id.watch_list_coin_tab);
        addTabsToTabLayout(mTabs);
        setOnClickListenerToTab();
        mProgressBar = (ProgressBar) view.findViewById(R.id.watch_list_progress_bar);
        if (marketStat.getmWatchListDataListHashMap().get("1.3.0").size() == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        watchListDataList.addAll(marketStat.getmWatchListDataListHashMap().get("1.3.0"));
        mWatchListRecyclerViewAdapter = new WatchListRecyclerViewAdapter(watchListDataList, mListener, getContext());
        if (mColumnCount <= 1) {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 1);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, mColumnCount));
        }
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mWatchListRecyclerViewAdapter);
        return view;
    }

    private void addTabsToTabLayout(String[] tabs) {
        for (String tab : tabs) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tab));
        }
    }

    private void setOnClickListenerToTab() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final String baseAsset = getAssetFromTab(tab.getText().toString());
                mTab  = (String) tab.getText();
                if (marketStat.getmCoinListHashMap().get(baseAsset) == null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    marketStat.getCoinPairConfiguration(new MarketStat.callBackListener() {
                        @Override
                        public void continueGetWebSocketConnect() {
                            EventBus.getDefault().unregister(WatchLIstFragment.this);
                            marketStat.startRun(WatchLIstFragment.this, baseAsset, mTab);
                        }
                    }, baseAsset);

                } else {
                    if (!EventBus.getDefault().isRegistered(WatchLIstFragment.this) && marketStat.getmWatchListDataListHashMap().get(baseAsset).size() != 0) {
                        EventBus.getDefault().register(WatchLIstFragment.this);
                    }
                    if (watchListDataList != null) {
                        watchListDataList.clear();
                        watchListDataList.addAll(marketStat.getmWatchListDataListHashMap().get(baseAsset));
                    }
                    mWatchListRecyclerViewAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private String getAssetFromTab(String tabName) {
        String result = "1.3.0";
        switch (tabName) {
            case "CYB":
                result = "1.3.0";
                break;
            case "ETH":
                result = "1.3.2";
                break;
            case "EOS":
                result = "1.3.4";
                break;
            case "BTC":
                result = "1.3.3";
                break;
        }
        return result;
    }

    @Override
    public void getResultListener(HashMap<String, List<WatchListData>> DataList, String baseAsset) {
        if (!EventBus.getDefault().isRegistered(WatchLIstFragment.this) && DataList.get(baseAsset).size() != 0) {
            EventBus.getDefault().register(WatchLIstFragment.this);
        }
        if (watchListDataList != null) {
            watchListDataList.clear();
            watchListDataList.addAll(DataList.get(baseAsset));
        }
        mWatchListRecyclerViewAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        for (int i = 0; i < watchListDataList.size(); i++) {
            if (string.equals(watchListDataList.get(i).getSubscribeId())) {
                updateWatchListData(watchListDataList.get(i), i);
            }
        }
    }

    private void updateWatchListData(final WatchListData watchListData, final int i) {
        final Handler handler = new Handler();
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final WatchListData newWatchListData = marketStat.getWatchLIstData(watchListData.getBaseId(), watchListData.getQuoteId(), watchListData.getSubscribeId(), mTab);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (watchListDataList.size() > i) {
                            mWatchListRecyclerViewAdapter.setItemToPosition(newWatchListData, i);
                        }
                    }
                });
            }
        });
        mThread.start();

    }

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
        void onListFragmentInteraction(WatchListData item, List<WatchListData> dataList, int position);
    }
}
