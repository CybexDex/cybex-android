package com.cybexmobile.Activities;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cybexmobile.API.BitsharesWalletWraper;
import com.cybexmobile.Adapter.OpenOrderRecyclerViewAdapter;
import com.cybexmobile.Exception.NetworkStatusException;
import com.cybexmobile.R;
import com.cybexmobile.graphene.chain.account_balance_object;
import com.cybexmobile.graphene.chain.asset_object;
import com.cybexmobile.graphene.chain.limit_order_object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

public class OpenOrdersActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private SegmentedGroup mSegmentedGroup;
    private RadioButton mAllSegment, mBuySegment, mSellSegment;
    private RecyclerView mRecyclerView;
    private OpenOrderRecyclerViewAdapter mOpenOrcerRecycerViewAdapter;
    private List<limit_order_object> mLimitOrderObjectList = new ArrayList<>();
    private String[] compareList = new String[]{"JADE.ETH", "JADE.BTC", "JADE.EOS", "CYB"};
    private List<Boolean> mBooleanList;
    private List<List<asset_object>> mAssetObjectList = new ArrayList<>();
    private HashMap<String, List<limit_order_object>> mLimitOrderHashMap = new HashMap<>();
    private HashMap<String, List<List<asset_object>>> mAssetObjectHashMap = new HashMap<>();
    private HashMap<String, List<Boolean>> mBooleanHashMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_orders);
        initWidget();
        mLimitOrderObjectList = BitsharesWalletWraper.getInstance().getMyFullAccountInstance().get(0).limit_orders;
        mBooleanList = isSell(mLimitOrderObjectList, compareList);
        mOpenOrcerRecycerViewAdapter = new OpenOrderRecyclerViewAdapter(mLimitOrderObjectList, mBooleanList, this, mAssetObjectList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mOpenOrcerRecycerViewAdapter);

        initEvent();
    }

    private void initWidget() {
        mSegmentedGroup = findViewById(R.id.open_orders_segmented_group);
        mAllSegment = findViewById(R.id.open_orders_segment_all);
        mBuySegment = findViewById(R.id.open_orders_segment_buy);
        mSellSegment = findViewById(R.id.open_orders_segment_sell);
        mRecyclerView = findViewById(R.id.open_orders_recycler_view);
        mSegmentedGroup.setOnCheckedChangeListener(this);

    }

    private void initEvent() {
        mapBuyOrSellSegment();
        mAllSegment.setChecked(true);
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
                break;

            case R.id.open_orders_segment_buy:
                mAssetObjectList.clear();
                mLimitOrderObjectList.clear();
                mBooleanList.clear();
                mAssetObjectList.addAll(mAssetObjectHashMap.get("Buy"));
                mLimitOrderObjectList.addAll(mLimitOrderHashMap.get("Buy"));
                mBooleanList.addAll(mBooleanHashMap.get("Buy"));
                mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
                break;

            case R.id.open_orders_segment_sell:
                mAssetObjectList.clear();
                mLimitOrderObjectList.clear();
                mBooleanList.clear();
                mAssetObjectList.addAll(mAssetObjectHashMap.get("Sell"));
                mLimitOrderObjectList.addAll(mLimitOrderHashMap.get("Sell"));
                mBooleanList.addAll(mBooleanHashMap.get("Sell"));
                mOpenOrcerRecycerViewAdapter.notifyDataSetChanged();
                break;

        }
    }

    private List<Boolean> isSell (List<limit_order_object> limitOrderList, String[] compareList) {
        List<Boolean> booleanList = new ArrayList<>();
        try {
            for (limit_order_object limit_order_object : limitOrderList) {
                List<asset_object> assetObjectList = new ArrayList<>();

                String baseId = limit_order_object.sell_price.base.asset_id.toString();
                String quoteId = limit_order_object.sell_price.quote.asset_id.toString();
                asset_object base_object = BitsharesWalletWraper.getInstance().get_objects(baseId);
                asset_object quote_object = BitsharesWalletWraper.getInstance().get_objects(quoteId);
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
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
        return booleanList;
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
        List<List<asset_object>> sellAssetObjectList = new ArrayList<>();
        List<limit_order_object> sellLimitOrderList = new ArrayList<>();
        List<List<asset_object>> buyAssetObjectList = new ArrayList<>();
        List<limit_order_object> buyLimitOrderList = new ArrayList<>();
        List<Boolean> sellList = new ArrayList<>();
        List<Boolean> buyList = new ArrayList<>();
        List<Boolean> allList = new ArrayList<>();
        List<List<asset_object>> allAssetObjectList = new ArrayList<>();
        List<limit_order_object> allLimitOrderList = new ArrayList<>();
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
