package com.cybexmobile.dialog;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.cybexmobile.R;
import com.cybexmobile.adapter.WatchlistSelectRecyclerViewAdapter;
import com.cybex.provider.http.entity.AssetRmbPrice;
import com.cybexmobile.event.Event;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;
import static com.cybexmobile.utils.Constant.RESULT_CODE_SELECTED_WATCHLIST;

public class WatchlistSelectDialog extends DialogFragment implements WatchlistSelectRecyclerViewAdapter.OnItemClickListener{

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

    private Unbinder mUnbinder;

    private WebSocketService mWebSocketService;
    private WatchlistSelectRecyclerViewAdapter mWatchlistSelectRecyclerViewAdapter;
    private List<WatchlistData> mWatchlists = new ArrayList<>();
    private WatchlistData mCurrWatchlist;

    private String mCurrentBaseAssetId = Constant.ASSET_ID_ETH;

    private OnWatchlistSelectedListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_Dialog);
        EventBus.getDefault().register(this);
        mCurrWatchlist = (WatchlistData) getArguments().getSerializable(INTENT_PARAM_WATCHLIST);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(R.layout.activity_watchlist_select, window.findViewById(android.R.id.content), false);
        WindowManager manager = window.getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = (int)(display.getHeight() * 0.7);
        params.gravity = Gravity.TOP;
        window.setAttributes(params);
        mUnbinder = ButterKnife.bind(this, view);
        mRvWatchlist.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvWatchlist.setItemAnimator(null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWatchlistSelectRecyclerViewAdapter = new WatchlistSelectRecyclerViewAdapter(getContext(), mCurrWatchlist, mWatchlists);
        mWatchlistSelectRecyclerViewAdapter.setOnItemClickListener(this);
        mRvWatchlist.setAdapter(mWatchlistSelectRecyclerViewAdapter);
        initRadioButton();
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, BIND_AUTO_CREATE);
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
        getContext().unbindService(mConnection);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mListener != null){
            mListener.onWatchlistSelectDismiss();
        }
    }

    @Override
    public void onItemClick(WatchlistData watchlist) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_WATCHLIST, watchlist);
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CODE_SELECTED_WATCHLIST, intent);
        this.dismiss();
    }

    @OnClick(R.id.toolbar)
    public void onToolBarClick(View view){
        this.dismiss();
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
            Collections.sort(mWatchlists);
            /**
             * fix bug:CYM-444
             * 交易对排序后不刷新单条Item，防止数据错乱
             */
            //mWatchlistSelectRecyclerViewAdapter.notifyItemChanged(index);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlists(Event.UpdateWatchlists event) {
        if(!event.getBaseAssetId().equals(mCurrentBaseAssetId)){
            return;
        }
        mWatchlists.clear();
        mWatchlists.addAll(event.getData());
        Collections.sort(mWatchlists);
        //交易排序
        mWatchlistSelectRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
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

    private void initRadioButton(){
        if(mCurrWatchlist == null){
            return;
        }
        AssetObject baseAsset = mCurrWatchlist.getBaseAsset();
        if(baseAsset == null){
            return;
        }
        switch (baseAsset.id.toString()){
            case Constant.ASSET_ID_ETH:
                mRbEth.setChecked(true);
                mCurrentBaseAssetId = Constant.ASSET_ID_ETH;
                break;
            case Constant.ASSET_ID_CYB:
                mRbCyb.setChecked(true);
                mCurrentBaseAssetId = Constant.ASSET_ID_CYB;
                break;
            case Constant.ASSET_ID_USDT:
                mRbUsdt.setChecked(true);
                mCurrentBaseAssetId = Constant.ASSET_ID_USDT;
                break;
            case Constant.ASSET_ID_BTC:
                mRbBtc.setChecked(true);
                mCurrentBaseAssetId = Constant.ASSET_ID_BTC;
                break;
        }
    }

    public void setOnWatchlistSelectListener(OnWatchlistSelectedListener listener){
        mListener = listener;
    }

    public interface OnWatchlistSelectedListener{
        void onWatchlistSelectDismiss();
    }
}
