package com.cybexmobile.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cybexmobile.R;
import com.cybexmobile.adapter.WatchlistSelectRecyclerViewAdapter;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

public class WatchlistSelectActivity extends BaseActivity implements WatchlistSelectRecyclerViewAdapter.OnItemClickListener{

    @BindView(R.id.activity_watchlist_select_rv_watchlist)
    RecyclerView mRvWatchlist;
    @BindView(R.id.activity_watchlist_select_rb_cyb)
    RadioButton mRbCyb;
    @BindView(R.id.activity_watchlist_select_rb_eth)
    RadioButton mRbEth;
    @BindView(R.id.activity_watchlist_select_rb_usdt)
    RadioButton mRbUsdt;
    @BindView(R.id.activity_watchlist_select_rb_btc)
    RadioButton mRbBtc;

    private WebSocketService mWebSocketService;
    private WatchlistSelectRecyclerViewAdapter mWatchlistSelectRecyclerViewAdapter;
    private List<WatchlistData> mWatchlists = new ArrayList<>();
    private WatchlistData mCurrWatchlist;

    private String mCurrentBaseAssetId = Constant.ASSET_ID_ETH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist_select);
        EventBus.getDefault().register(this);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = (int)(display.getHeight() * 0.7);
        params.gravity = Gravity.TOP;
        ButterKnife.bind(this);
        mRvWatchlist.setLayoutManager(new LinearLayoutManager(this));
        mRvWatchlist.setItemAnimator(null);
        mWatchlistSelectRecyclerViewAdapter = new WatchlistSelectRecyclerViewAdapter(this, mWatchlists);
        mWatchlistSelectRecyclerViewAdapter.setOnItemClickListener(this);
        mRvWatchlist.setAdapter(mWatchlistSelectRecyclerViewAdapter);
        mCurrWatchlist = (WatchlistData) getIntent().getSerializableExtra(INTENT_PARAM_WATCHLIST);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unbindService(mConnection);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onItemClick(WatchlistData watchlist) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_WATCHLIST, watchlist);
        setResult(1, intent);
        finish();
    }

    @OnCheckedChanged({R.id.activity_watchlist_select_rb_eth, R.id.activity_watchlist_select_rb_cyb,
            R.id.activity_watchlist_select_rb_usdt, R.id.activity_watchlist_select_rb_btc})
    public void onCheckedChanged(CompoundButton button, boolean checked){
        if(!checked || mWebSocketService == null){
            return;
        }
        switch (button.getId()){
            case R.id.activity_watchlist_select_rb_eth:
                mCurrentBaseAssetId = Constant.ASSET_ID_ETH;
                break;
            case R.id.activity_watchlist_select_rb_cyb:
                mCurrentBaseAssetId = Constant.ASSET_ID_CYB;
                break;
            case R.id.activity_watchlist_select_rb_usdt:
                mCurrentBaseAssetId = Constant.ASSET_ID_USDT;
                break;
            case R.id.activity_watchlist_select_rb_btc:
                mCurrentBaseAssetId = Constant.ASSET_ID_BTC;
                break;
        }
        mWebSocketService.loadWatchlistData(mCurrentBaseAssetId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlist(Event.UpdateWatchlist event) {
        WatchlistData data = event.getData();
        int index = mWatchlists.indexOf(data);
        if (index != -1) {
            mWatchlists.set(index, data);
            //交易排序
            Collections.sort(mWatchlists, new Comparator<WatchlistData>() {
                @Override
                public int compare(WatchlistData o1, WatchlistData o2) {
                    return o1.getBaseVol() > o2.getBaseVol() ? -1 : 1;
                }
            });
            mWatchlistSelectRecyclerViewAdapter.notifyItemChanged(index);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlists(Event.UpdateWatchlists event) {
        if(!event.getBaseAssetId().equals(mCurrentBaseAssetId)){
            return;
        }
        mWatchlists.clear();
        mWatchlists.addAll(event.getData());
        //交易排序
        Collections.sort(mWatchlists, new Comparator<WatchlistData>() {
            @Override
            public int compare(WatchlistData o1, WatchlistData o2) {
                return o1.getBaseVol() > o2.getBaseVol() ? -1 : 1;
            }
        });
        mWatchlistSelectRecyclerViewAdapter.notifyDataSetChanged();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mWebSocketService.loadWatchlistData(mCurrentBaseAssetId);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };
}
