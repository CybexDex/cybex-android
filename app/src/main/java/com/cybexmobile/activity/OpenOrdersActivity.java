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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.OpenOrderRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;

public class OpenOrdersActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, OpenOrderRecyclerViewAdapter.getTotalValueInterface {

    private SegmentedGroup mSegmentedGroup;
    private RadioButton mAllSegment, mBuySegment, mSellSegment;
    private TextView mOpenOrderTotalValue, mTvOpenOrderTotalTitle;
    private RecyclerView mRecyclerView;
    private OpenOrderRecyclerViewAdapter mOpenOrcerRecycerViewAdapter;
    private List<LimitOrderObject> mLimitOrderObjectList = new ArrayList<>();
    private String[] mCompareList = new String[]{"JADE.ETH", "JADE.BTC", "JADE.EOS", "CYB"};
    private List<Boolean> mBooleanList = new ArrayList<>();
    private List<List<AssetObject>> mAssetObjectList = new ArrayList<>();
    private HashMap<String, List<LimitOrderObject>> mLimitOrderHashMap = new HashMap<>();
    private HashMap<String, List<List<AssetObject>>> mAssetObjectHashMap = new HashMap<>();
    private HashMap<String, List<Boolean>> mBooleanHashMap = new HashMap<>();
    private Toolbar mToolbar;
    private WebSocketService mWebSocketService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);
        EventBus.getDefault().register(this);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initViews();
        mOpenOrcerRecycerViewAdapter = new OpenOrderRecyclerViewAdapter(mLimitOrderObjectList, mBooleanList, this, mAssetObjectList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mOpenOrcerRecycerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAllSegment.setChecked(true);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection , BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mLimitOrderObjectList = mWebSocketService.getFullAccount().limit_orders;
            if(mLimitOrderObjectList == null || mLimitOrderObjectList.size() == 0){
                return;
            }
            mBooleanList = isSell(mLimitOrderObjectList, mCompareList);
            mapBuyOrSellSegment();
            mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initViews() {
        mSegmentedGroup = findViewById(R.id.open_orders_segmented_group);
        mAllSegment = findViewById(R.id.open_orders_segment_all);
        mBuySegment = findViewById(R.id.open_orders_segment_buy);
        mSellSegment = findViewById(R.id.open_orders_segment_sell);
        mRecyclerView = findViewById(R.id.open_orders_recycler_view);
        mOpenOrderTotalValue = findViewById(R.id.open_orders_total_value);
        mTvOpenOrderTotalTitle = findViewById(R.id.open_orders_title);
        mSegmentedGroup.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.open_orders_segment_all:
                mAssetObjectList.clear();
                mLimitOrderObjectList.clear();
                mBooleanList.clear();
                mAssetObjectList.addAll(mAssetObjectHashMap.get("All"));
                mLimitOrderObjectList.addAll(mLimitOrderHashMap.get("All"));
                mBooleanList.addAll(mBooleanHashMap.get("All"));
                mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_total_value);
                break;

            case R.id.open_orders_segment_buy:
                mAssetObjectList.clear();
                mLimitOrderObjectList.clear();
                mBooleanList.clear();
                mAssetObjectList.addAll(mAssetObjectHashMap.get("Buy"));
                mLimitOrderObjectList.addAll(mLimitOrderHashMap.get("Buy"));
                mBooleanList.addAll(mBooleanHashMap.get("Buy"));
                mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_buy_total_value);
                break;

            case R.id.open_orders_segment_sell:
                mAssetObjectList.clear();
                mLimitOrderObjectList.clear();
                mBooleanList.clear();
                mAssetObjectList.addAll(mAssetObjectHashMap.get("Sell"));
                mLimitOrderObjectList.addAll(mLimitOrderHashMap.get("Sell"));
                mBooleanList.addAll(mBooleanHashMap.get("Sell"));
                mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
                mTvOpenOrderTotalTitle.setText(R.string.open_orders_sell_total_value);
                break;

        }
    }

    @Override
    public void displayTotalValue(double total) {
        double rmbPrice = MarketStat.getInstance().getRMBPriceFromHashMap("CYB");
        int precision = 2;
        String form = "%." + precision + "f\n";
        mOpenOrderTotalValue.setText(String.format(Locale.US,"≈¥" + form, total * rmbPrice ));
    }

    private List<Boolean> isSell (List<LimitOrderObject> limitOrderList, String[] compareList) {
        List<Boolean> booleanList = new ArrayList<>();
        for (LimitOrderObject LimitOrderObject : limitOrderList) {
            List<AssetObject> assetObjectList = new ArrayList<>();

            String baseId = LimitOrderObject.sell_price.base.asset_id.toString();
            String quoteId = LimitOrderObject.sell_price.quote.asset_id.toString();

            AssetObject base_object = mWebSocketService.getAssetObject(baseId);
            AssetObject quote_object = mWebSocketService.getAssetObject(quoteId);

            String baseSymbol = base_object.symbol;
            String quoteSymbol = quote_object.symbol;
            boolean isSell = checkIsSell(baseSymbol, quoteSymbol, compareList);
            if (isSell) {
                assetObjectList.add(base_object);
                assetObjectList.add(quote_object);
            } else {
                assetObjectList.add(quote_object);
                assetObjectList.add(base_object);
            }

            booleanList.add(isSell);
            mAssetObjectList.add(assetObjectList);
        }
        return booleanList;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAsset(Event.LoadAsset event){

    }

    private boolean checkIsSell(String baseSymbol, String quoteSymbol, String[] compareList) {
        boolean isContainBase = Arrays.asList(compareList).contains(baseSymbol);
        boolean isContainQuote = Arrays.asList(compareList).contains(quoteSymbol);
        int baseIndex = Arrays.asList(compareList).indexOf(baseSymbol);
        int quoteIndex = Arrays.asList(compareList).indexOf(quoteSymbol);
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

    private void mapBuyOrSellSegment() {
        List<List<AssetObject>> sellAssetObjectList = new ArrayList<>();
        List<LimitOrderObject> sellLimitOrderList = new ArrayList<>();
        List<List<AssetObject>> buyAssetObjectList = new ArrayList<>();
        List<LimitOrderObject> buyLimitOrderList = new ArrayList<>();
        List<Boolean> sellList = new ArrayList<>();
        List<Boolean> buyList = new ArrayList<>();
        List<Boolean> allList = new ArrayList<>();
        List<List<AssetObject>> allAssetObjectList = new ArrayList<>();
        List<LimitOrderObject> allLimitOrderList = new ArrayList<>();
        for (int i = 0; i < mBooleanList.size(); i++) {
            if (mBooleanList.get(i)) {
                sellAssetObjectList.add(mAssetObjectList.get(i));
                sellLimitOrderList.add(mLimitOrderObjectList.get(i));
                sellList.add(mBooleanList.get(i));
            } else {
                buyAssetObjectList.add(mAssetObjectList.get(i));
                buyLimitOrderList.add(mLimitOrderObjectList.get(i));
                buyList.add(mBooleanList.get(i));
            }
            allList.add(mBooleanList.get(i));
            allAssetObjectList.add(mAssetObjectList.get(i));
            allLimitOrderList.add(mLimitOrderObjectList.get(i));

        }

        mLimitOrderHashMap.put("Sell", sellLimitOrderList);
        mLimitOrderHashMap.put("Buy", buyLimitOrderList);
        mLimitOrderHashMap.put("All", allLimitOrderList);
        mAssetObjectHashMap.put("Sell", sellAssetObjectList);
        mAssetObjectHashMap.put("Buy", buyAssetObjectList);
        mAssetObjectHashMap.put("All", allAssetObjectList);
        mBooleanHashMap.put("Sell", sellList);
        mBooleanHashMap.put("Buy", buyList);
        mBooleanHashMap.put("All", allList);
    }
}
