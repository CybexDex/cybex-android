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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.provider.market.WatchlistData;
import com.cybexmobile.adapter.WatchListRecyclerViewAdapter;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybex.basemodule.event.Event;
import com.cybexmobile.R;

import com.cybex.basemodule.service.WebSocketService;
import com.cybexmobile.adapter.decoration.VisibleDividerItemDecoration;
import com.cybexmobile.utils.WatchlistComparator;
import com.cybexmobile.widget.MultiStateRadioButton;
import com.cybexmobile.widget.MultiStateRadioGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_USDT;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_BTC;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_ETH;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_USDT;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WatchlistFragment extends BaseFragment implements MultiStateRadioGroup.OnCheckedChangeListener {

    private static final String TAG = "WatchListFragment";
    private List<WatchlistData> mWatchlistData = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    private TabLayout mTabLayout;
    private View view;
    private ProgressBar mProgressBar;
    private MultiStateRadioGroup mMultiStateRadioGroup;
    private MultiStateRadioButton mMultiStateRadioButton_name;
    private MultiStateRadioButton mMultiStateRadioButton_vol;
    private MultiStateRadioButton mMultiStateRadioButton_price;
    private MultiStateRadioButton mMultiStateRadioButton_change;

    private String[] mTabs = new String[]{"ETH", "CYB", "USDT", "BTC"};
    private String mCurrentTab;
    private String mCurrentBaseAssetId;
    private WatchListRecyclerViewAdapter mWatchListRecyclerViewAdapter;
    private OnListFragmentInteractionListener mListener;

    private WebSocketService mWebSocketService;
    private boolean mIsViewCreated;
    private WatchlistComparator mWatchlistComparator;
    private int mSort;

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
        mMultiStateRadioButton_name = view.findViewById(R.id.watchlist_msrb_name);
        mMultiStateRadioButton_vol = view.findViewById(R.id.watchlist_msrb_vol);
        mMultiStateRadioButton_price = view.findViewById(R.id.watchlist_msrb_price);
        mMultiStateRadioButton_change = view.findViewById(R.id.watchlist_msrb_change);
        if(mMultiStateRadioButton_change.isChecked()) {
            mSort = mMultiStateRadioButton_change.getState() == MultiStateRadioButton.STATE_UP ?
                    WatchlistComparator.CHANGE_UP : WatchlistComparator.CHANGE_DOWN;
        }
        if(mMultiStateRadioButton_price.isChecked()) {
            mSort = mMultiStateRadioButton_price.getState() == MultiStateRadioButton.STATE_UP ?
                    WatchlistComparator.PRICE_UP : WatchlistComparator.PRICE_DOWN;
        }
        if(mMultiStateRadioButton_name.isChecked()) {
            mSort = mMultiStateRadioButton_name.getState() == MultiStateRadioButton.STATE_UP ?
                    WatchlistComparator.NAME_UP : WatchlistComparator.NAME_DOWN;
        }
        if(mMultiStateRadioButton_vol.isChecked()) {
            mSort = mMultiStateRadioButton_vol.getState() == MultiStateRadioButton.STATE_UP ?
                    WatchlistComparator.VOL_UP : WatchlistComparator.VOL_DOWN;
        }
        mMultiStateRadioGroup = view.findViewById(R.id.watchlist_msrg);
        mMultiStateRadioGroup.setOnCheckedChangeListener(this);
        mWatchListRecyclerViewAdapter = new WatchListRecyclerViewAdapter(mWatchlistData, mListener, getContext());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new VisibleDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
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
    public void onUpdateWatchlist(Event.UpdateWatchlist event) {
        WatchlistData data = event.getData();
        int index = mWatchlistData.indexOf(data);
        if (index != -1) {
            mWatchlistData.set(index, data);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlists(Event.UpdateWatchlists event) {
        /**
         * fix bug
         * 解决切换tab数据错误
         */
        if(!event.getBaseAssetId().equals(mCurrentBaseAssetId)){
            return;
        }
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mWatchlistData.clear();
        mWatchlistData.addAll(event.getData());
        if(mWatchlistComparator == null){
            mWatchlistComparator = new WatchlistComparator(mSort);
        }
        //交易排序
        Collections.sort(mWatchlistData, mWatchlistComparator);
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

        for (WatchlistData watchlistData : mWatchlistData) {
            mWebSocketService.setRMBPriceForQuoteAssetAsBaseInOtherTab(watchlistData);
        }
        if (assetRmbPrice.getValue() != mWatchlistData.get(0).getRmbPrice()) {
            for (WatchlistData watchlistData : mWatchlistData) {
                watchlistData.setRmbPrice(assetRmbPrice.getValue());
            }
        }
        if(mWatchlistComparator == null){
            mWatchlistComparator = new WatchlistComparator(mSort);
        }
        //交易排序
        Collections.sort(mWatchlistData, mWatchlistComparator);
        mWatchListRecyclerViewAdapter.notifyDataSetChanged();
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
                mRecyclerView.setVisibility(View.GONE);
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
        String result = ASSET_ID_ETH;
        switch (assetName) {
            case ASSET_SYMBOL_CYB:
                result = ASSET_ID_CYB;
                break;
            case ASSET_SYMBOL_ETH:
                result = ASSET_ID_ETH;
                break;
            case ASSET_SYMBOL_USDT:
                result = ASSET_ID_USDT;
                break;
            case ASSET_SYMBOL_BTC:
                result = ASSET_ID_BTC;
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

    @Override
    public void onCheckedChanged(MultiStateRadioGroup group, int checkedId, int state) {
        switch (checkedId) {
            case R.id.watchlist_msrb_name:
                if(state == MultiStateRadioButton.STATE_UP){
                    mSort = WatchlistComparator.NAME_UP;
                } else {
                    mSort = WatchlistComparator.NAME_DOWN;
                }
                break;
            case R.id.watchlist_msrb_vol:
                if(state == MultiStateRadioButton.STATE_UP){
                    mSort = WatchlistComparator.VOL_UP;
                } else {
                    mSort = WatchlistComparator.VOL_DOWN;
                }
                break;
            case R.id.watchlist_msrb_price:
                if(state == MultiStateRadioButton.STATE_UP){
                    mSort = WatchlistComparator.PRICE_UP;
                } else {
                    mSort = WatchlistComparator.PRICE_DOWN;
                }
                break;
            case R.id.watchlist_msrb_change:
                if(state == MultiStateRadioButton.STATE_UP){
                    mSort = WatchlistComparator.CHANGE_UP;
                } else {
                    mSort = WatchlistComparator.CHANGE_DOWN;
                }
                break;
        }
        if(mWatchlistComparator == null){
            mWatchlistComparator = new WatchlistComparator(mSort);
        } else {
            mWatchlistComparator.setSort(mSort);
        }
        //交易排序
        Collections.sort(mWatchlistData, mWatchlistComparator);
        mWatchListRecyclerViewAdapter.notifyDataSetChanged();
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
        void onListFragmentInteraction(WatchlistData item);
    }


    @Override
    public void onNetWorkStateChanged(boolean isAvailable){

    }

}
