package com.cybexmobile.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.R;

import com.cybexmobile.service.WebSocketService;
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
public class WatchLIstFragment extends Fragment {

    private static final String TAG = "WatchListFragment";
    private List<WatchlistData> mWatchlistData = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    private TabLayout mTabLayout;
    private View view;
    private ProgressBar mProgressBar;

    private String[] mTabs = new String[]{"ETH", "BTC", "USDT", "CYB"};
    private String mCurrentTab;
    private String mCurrentBaseAssetId;
    private WatchListRecyclerViewAdapter mWatchListRecyclerViewAdapter;
    private OnListFragmentInteractionListener mListener;

    private WebSocketService mWebSocketService;
    private boolean mIsViewCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_watchlist, container, false);
        mRecyclerView = view.findViewById(R.id.list);
        mTabLayout = view.findViewById(R.id.watch_list_coin_tab);
        mProgressBar = view.findViewById(R.id.watch_list_progress_bar);
        mWatchListRecyclerViewAdapter = new WatchListRecyclerViewAdapter(mWatchlistData, mListener, getContext());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setAdapter(mWatchListRecyclerViewAdapter);
        initTabs(mTabs);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIsViewCreated = true;
        loadWatchlistData();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getContext().unbindService(mConnection);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        if (mWebSocketService != null) {
            mWebSocketService.loadWatchlistData(mCurrentBaseAssetId);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlist(Event.UpdateWatchlist event) {
        WatchlistData data = event.getData();
        int index = mWatchlistData.indexOf(data);
        if (index != -1) {
            mWatchlistData.set(index, data);
            mWatchListRecyclerViewAdapter.notifyItemChanged(index);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlists(Event.UpdateWatchlists event) {
        mProgressBar.setVisibility(View.GONE);
        mWatchlistData.clear();
        mWatchlistData.addAll(event.getData());
        mWatchListRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        AssetRmbPrice assetRmbPrice = null;
        for (AssetRmbPrice rmbPrice : assetRmbPrices) {
            if (rmbPrice.getName().equals(mCurrentTab)) {
                assetRmbPrice = rmbPrice;
                break;
            }
        }
        if (assetRmbPrice == null || mWatchlistData == null || mWatchlistData.size() == 0) {
            return;
        }
        if(assetRmbPrice.getValue() != mWatchlistData.get(0).getRmbPrice()){
            for (WatchlistData watchlistData : mWatchlistData) {
                watchlistData.setRmbPrice(assetRmbPrice.getValue());
            }
            mWatchListRecyclerViewAdapter.notifyDataSetChanged();
        }

    }


    private void loadWatchlistData() {
        if (mWebSocketService != null && mIsViewCreated) {
            mWebSocketService.loadWatchlistData(mCurrentBaseAssetId);
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            loadWatchlistData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void initTabs(String[] tabs) {
        setTabListener();
        for (String tab : tabs) {
            mTabLayout.addTab(mTabLayout.newTab().setText(tab));
        }

    }

    private void setTabListener() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTab = tab.getText().toString();
                mCurrentBaseAssetId = getAssetId(mCurrentTab);
                mProgressBar.setVisibility(View.VISIBLE);
                loadWatchlistData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private String getAssetId(String assetName) {
        String result = "1.3.2";
        switch (assetName) {
            case "CYB":
                result = "1.3.0";
                break;
            case "ETH":
                result = "1.3.2";
                break;
            case "USDT":
                result = "1.3.27";
                break;
            case "BTC":
                result = "1.3.3";
                break;
        }
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
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
        void onListFragmentInteraction(WatchlistData item, List<WatchlistData> dataList, int position);
    }


}
