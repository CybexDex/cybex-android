package com.cybexmobile.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.OpenOrderRecyclerViewAdapter;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.item.OpenOrderItem;
import com.cybexmobile.event.Event;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.market.OpenOrder;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;

public class OpenOrdersActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, OpenOrderRecyclerViewAdapter.getTotalValueInterface {

    private static final String TAG = "OpenOrdersActivity";

    private SegmentedGroup mSegmentedGroup;
    private TextView mOpenOrderTotalValue, mTvOpenOrderTotalTitle;
    private RecyclerView mRecyclerView;
    private OpenOrderRecyclerViewAdapter mOpenOrderRecycerViewAdapter;
    private List<String> mCompareSymbol = Arrays.asList(new String[]{"JADE.ETH", "JADE.BTC", "JADE.EOS", "CYB"});
    private Toolbar mToolbar;
    private WebSocketService mWebSocketService;

    private List<OpenOrderItem> mOpenOrderItems = new ArrayList<>();

    private DecimalFormat format = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);
        EventBus.getDefault().register(this);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initViews();
        mOpenOrderRecycerViewAdapter = new OpenOrderRecyclerViewAdapter(mOpenOrderItems, this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mOpenOrderRecycerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection , BIND_AUTO_CREATE);
        showLoadDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            //AccountFragment已经获取了FullAccount数据
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(true);
            List<LimitOrderObject> limitOrderObjects = fullAccountObject == null ? null : fullAccountObject.limit_orders;
            if(limitOrderObjects == null || limitOrderObjects.size() == 0){
                hideLoadDialog();
                return;
            }
            for(LimitOrderObject limitOrderObject : limitOrderObjects){
                OpenOrderItem item = new OpenOrderItem();
                OpenOrder openOrder = new OpenOrder();
                openOrder.setLimitOrder(limitOrderObject);
                String baseId = limitOrderObject.sell_price.base.asset_id.toString();
                String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
                List<AssetObject> assetObjects = mWebSocketService.getAssetObjects(baseId, quoteId);
                if(assetObjects != null && assetObjects.size() == 2){
                    String baseSymbol = assetObjects.get(0).symbol;
                    String quoteSymbol = assetObjects.get(1).symbol;
                    item.isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                    openOrder.setBaseObject(item.isSell ? assetObjects.get(1) : assetObjects.get(0));
                    openOrder.setQuoteObject(item.isSell ? assetObjects.get(0) : assetObjects.get(1));
                }
                item.openOrder = openOrder;
                mOpenOrderItems.add(item);
            }
            hideLoadDialog();
            mOpenOrderRecycerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void initViews() {
        mSegmentedGroup = findViewById(R.id.open_orders_segmented_group);
        mRecyclerView = findViewById(R.id.open_orders_recycler_view);
        mOpenOrderTotalValue = findViewById(R.id.open_orders_total_value);
        mTvOpenOrderTotalTitle = findViewById(R.id.open_orders_title);
        mSegmentedGroup.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.open_orders_segment_all:
                mOpenOrderRecycerViewAdapter.getFilter().filter("All");
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_total_value);
                break;

            case R.id.open_orders_segment_buy:
                mOpenOrderRecycerViewAdapter.getFilter().filter("Buy");
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_buy_total_value);
                break;

            case R.id.open_orders_segment_sell:
                mOpenOrderRecycerViewAdapter.getFilter().filter("Sell");
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_sell_total_value);
                break;

        }
    }

    @Override
    public void displayTotalValue(double total) {
        double rmbPrice = mWebSocketService.getAssetRmbPrice("CYB") == null ? 0 : mWebSocketService.getAssetRmbPrice("CYB").getValue();
        Log.v(TAG, "total * rmbPrice=" + total * rmbPrice);
        Log.v(TAG, "format.format(total * rmbPrice)" + format.format(total * rmbPrice));
        String cybrmb = format.format(total * rmbPrice);
        mOpenOrderTotalValue.setText(String.format("≈¥%s", cybrmb));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAssets(Event.LoadAssets event){
        List<AssetObject> assetObjects = event.getData();
        if(assetObjects == null && assetObjects.size() != 2){
            return;
        }
        for(int i=0; i<mOpenOrderItems.size(); i++){
            LimitOrderObject limitOrderObject = mOpenOrderItems.get(i).openOrder.getLimitOrder();
            String baseId = limitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = limitOrderObject.sell_price.quote.asset_id.toString();
            if(baseId.equals(assetObjects.get(0).id.toString()) && quoteId.equals(assetObjects.get(1).id.toString())){
                String baseSymbol = assetObjects.get(0).symbol;
                String quoteSymbol = assetObjects.get(1).symbol;
                mOpenOrderItems.get(i).isSell = checkIsSell(baseSymbol, quoteSymbol, mCompareSymbol);
                mOpenOrderItems.get(i).openOrder.setBaseObject(mOpenOrderItems.get(i).isSell ? assetObjects.get(0) : assetObjects.get(1));
                mOpenOrderItems.get(i).openOrder.setQuoteObject(mOpenOrderItems.get(i).isSell ? assetObjects.get(1) : assetObjects.get(0));
                hideLoadDialog();
                mOpenOrderRecycerViewAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private boolean checkIsSell(String baseSymbol, String quoteSymbol, List<String> compareSymbol) {
        boolean isContainBase = compareSymbol.contains(baseSymbol);
        boolean isContainQuote = compareSymbol.contains(quoteSymbol);
        int baseIndex = compareSymbol.indexOf(baseSymbol);
        int quoteIndex = compareSymbol.indexOf(quoteSymbol);
        if (isContainBase) {
            if (!isContainQuote) {
                return false;
            } else {
                if (baseIndex < quoteIndex) {
                    return  false;
                } else {
                    return true;
                }
            }
        } else {
            if (isContainQuote) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

}
