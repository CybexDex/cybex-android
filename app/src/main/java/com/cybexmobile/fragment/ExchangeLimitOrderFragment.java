package com.cybexmobile.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.adapter.BuySellOrderRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseFragment;
import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.LimitOrderObject;
import com.cybexmobile.graphene.chain.Price;
import com.cybexmobile.market.Order;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.MyUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_WATCHLIST;

/**
 * 交易界面所有用户当前交易对委单
 */
public class ExchangeLimitOrderFragment extends BaseFragment implements BuySellOrderRecyclerViewAdapter.OnItemClickListener{

    @BindView(R.id.buysell_rv_sell)
    RecyclerView mRvSell;
    @BindView(R.id.buysell_rv_buy)
    RecyclerView mRvBuy;

    @BindView(R.id.buysell_tv_order_price)
    TextView mTvOrderPrice;
    @BindView(R.id.buysell_tv_order_amount)
    TextView mTvOrderAmount;
    @BindView(R.id.buysell_tv_quote_price)
    TextView mTvQuotePrice;
    @BindView(R.id.buysell_tv_quote_rmb_price)
    TextView mTvQuoteRmbPrice;

    private List<Order> mBuyOrders = new ArrayList<>();
    private List<Order> mSellOrders = new ArrayList<>();

    private BuySellOrderRecyclerViewAdapter mBuyOrderAdapter;
    private BuySellOrderRecyclerViewAdapter mSellOrderAdapter;

    private Unbinder mUnbinder;

    private WatchlistData mWatchlistData;

    public static ExchangeLimitOrderFragment getInstance(WatchlistData watchlistData){
        ExchangeLimitOrderFragment fragment = new ExchangeLimitOrderFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_WATCHLIST, watchlistData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if(bundle != null){
            mWatchlistData = (WatchlistData) bundle.getSerializable(INTENT_PARAM_WATCHLIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exchange_limit_order, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mRvSell.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvBuy.setLayoutManager(new LinearLayoutManager(getContext()));
        mBuyOrderAdapter = new BuySellOrderRecyclerViewAdapter(getContext(), BuySellOrderRecyclerViewAdapter.TYPE_BUY, mBuyOrders);
        mSellOrderAdapter = new BuySellOrderRecyclerViewAdapter(getContext(), BuySellOrderRecyclerViewAdapter.TYPE_SELL, mSellOrders);
        mBuyOrderAdapter.setOnItemClickListener(this);
        mSellOrderAdapter.setOnItemClickListener(this);
        mRvBuy.setAdapter(mBuyOrderAdapter);
        mRvSell.setAdapter(mSellOrderAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewData();
        loadBuySellOrder();
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateBuySellOrders(Event.UpdateBuySellOrders event){
        mBuyOrders.clear();
        mBuyOrders.addAll(event.getBuyOrders());
        mSellOrders.clear();
        mSellOrders.addAll(event.getSellOrders());
        mBuyOrderAdapter.notifyDataSetChanged();
        mSellOrderAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubcribeMarket(Event.SubscribeMarket event) {
        if(event.getCallId() == mWatchlistData.getSubscribeId()) {
            loadBuySellOrder();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateWatchlist(Event.UpdateWatchlist event) {
        WatchlistData data = event.getData();
        if(data == null || mWatchlistData == null){
            return;
        }
        if(data.getBaseId().equals(mWatchlistData.getBaseId()) && data.getQuoteId().equals(mWatchlistData.getQuoteId())){
            mWatchlistData = data;
            initQuotePriceText();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateRmbPrice(Event.UpdateRmbPrice event) {
        List<AssetRmbPrice> assetRmbPrices = event.getData();
        if (assetRmbPrices == null || assetRmbPrices.size() == 0) {
            return;
        }
        AssetRmbPrice assetRmbPrice = null;
        for (AssetRmbPrice rmbPrice : assetRmbPrices) {
            if (mWatchlistData.getBaseSymbol().contains(rmbPrice.getName())) {
                assetRmbPrice = rmbPrice;
                break;
            }
        }
        if (assetRmbPrice == null) {
            return;
        }
        mWatchlistData.setRmbPrice(assetRmbPrice.getValue());
        initQuotePriceText();
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onItemClick(Order order) {
        EventBus.getDefault().post(new Event.LimitOrderClick(order.price));
    }

    @OnClick({R.id.buysell_tv_quote_price, R.id.buysell_tv_quote_rmb_price})
    public void onQuotePriceClick(View view){
        EventBus.getDefault().post(new Event.LimitOrderClick(mWatchlistData.getCurrentPrice()));
    }

    private void loadBuySellOrder(){
        if(mWatchlistData != null){
            try {
                BitsharesWalletWraper.getInstance().get_limit_orders(mWatchlistData.getBaseAsset().id, mWatchlistData.getQuoteAsset().id, 200, mLimitOrderCallback);
            } catch (NetworkStatusException e) {
                e.printStackTrace();
            }
        }
    }

    private WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LimitOrderObject>>> mLimitOrderCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<LimitOrderObject>>>() {
        @Override
        public void onMessage(WebSocketClient.Reply<List<LimitOrderObject>> reply) {
            List<LimitOrderObject> limitOrders = reply.result;
            if(limitOrders == null || limitOrders.size() == 0){
                return;
            }
            List<Order> buyOrders = new ArrayList<>();
            List<Order> sellOrders = new ArrayList<>();
            Order order = null;
            for(LimitOrderObject limitOrder : limitOrders){
                if(buyOrders.size() == 5 && sellOrders.size() == 5){
                    break;
                }
                if (limitOrder.sell_price.base.asset_id.equals(mWatchlistData.getBaseAsset().id)) {
                    if(buyOrders.size() == 5){
                        continue;
                    }
                    order = new Order();
                    order.price = priceToReal(limitOrder.sell_price);
                    order.quoteAmount = ((double) limitOrder.for_sale * (double) limitOrder.sell_price.quote.amount)
                            / (double) limitOrder.sell_price.base.amount
                            / Math.pow(10, mWatchlistData.getQuotePrecision());
                    order.baseAmount = limitOrder.for_sale / Math.pow(10, mWatchlistData.getBasePrecision());
                    buyOrders.add(order);
                } else {
                    if(sellOrders.size() == 5){
                        continue;
                    }
                    order = new Order();
                    order.price = priceToReal(limitOrder.sell_price);
                    order.quoteAmount = limitOrder.for_sale / Math.pow(10, mWatchlistData.getQuotePrecision());
                    order.baseAmount = (double) limitOrder.for_sale * (double) limitOrder.sell_price.quote.amount
                            / limitOrder.sell_price.base.amount
                            / Math.pow(10, mWatchlistData.getBasePrecision());
                    sellOrders.add(order);
                }
            }
            Collections.sort(buyOrders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return (o1.price - o2.price) < 0 ? 1 : -1;
                }
            });
            Collections.sort(sellOrders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return (o1.price - o2.price) < 0 ? 1 : -1;
                }
            });
            EventBus.getDefault().post(new Event.UpdateBuySellOrders(buyOrders, sellOrders));
        }

        @Override
        public void onFailure() {

        }
    };

    private double priceToReal(Price p) {
        if (p.base.asset_id.equals(mWatchlistData.getBaseAsset().id)) {
            return assetToReal(p.base, mWatchlistData.getBasePrecision())
                    / assetToReal(p.quote, mWatchlistData.getQuotePrecision());
        } else {
            return assetToReal(p.quote, mWatchlistData.getBasePrecision())
                    / assetToReal(p.base, mWatchlistData.getQuotePrecision());
        }
    }

    private double assetToReal(Asset a, long p) {
        return (double) a.amount / Math.pow(10, p);
    }

    private void initViewData(){
        if(mWatchlistData == null){
            return;
        }
        initQuotePriceText();
        String baseSymbol = AssetUtil.parseSymbol(mWatchlistData.getBaseSymbol());
        String quoteSymbol = AssetUtil.parseSymbol(mWatchlistData.getQuoteSymbol());
        mTvOrderPrice.setText(getResources().getString(R.string.text_asset_price).replace("--", baseSymbol));
        mTvOrderAmount.setText(getResources().getString(R.string.text_asset_amount).replace("--", quoteSymbol));
    }

    private void initQuotePriceText(){
        mTvQuotePrice.setText(String.format(AssetUtil.formatPrice(mWatchlistData.getCurrentPrice()), mWatchlistData.getCurrentPrice()));
        mTvQuotePrice.setTextColor(getResources().getColor(Double.parseDouble(mWatchlistData.getChange()) > 0 ? R.color.decreasing_color : R.color.increasing_color));
        mTvQuoteRmbPrice.setText(String.format(Locale.US, "≈¥ %.2f", mWatchlistData.getCurrentPrice() * mWatchlistData.getRmbPrice()));
    }

    public void changeWatchlist(WatchlistData watchlist){
        this.mWatchlistData = watchlist;
        initViewData();
        loadBuySellOrder();
    }
}
