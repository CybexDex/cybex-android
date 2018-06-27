package com.cybexmobile.event;

import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.market.HistoryPrice;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.market.MarketTrade;
import com.cybexmobile.market.Order;
import com.cybexmobile.market.OrderBook;

import java.util.List;

public class Event {

    //更新币价格
    public static class UpdateRmbPrice{
        private List<AssetRmbPrice> data;

        public UpdateRmbPrice(List<AssetRmbPrice> data) {
            this.data = data;
        }

        public List<AssetRmbPrice> getData() {
            return data;
        }

        public void setData(List<AssetRmbPrice> data) {
            this.data = data;
        }
    }

    public static class UpdateFullAccount{
        private FullAccountObject data;

        public UpdateFullAccount(FullAccountObject data) {
            this.data = data;
        }

        public FullAccountObject getData() {
            return data;
        }

        public void setData(FullAccountObject data) {
            this.data = data;
        }
    }

    //更新所有行情数据
    public static class UpdateWatchlists {
        private String baseAssetId;

        private List<WatchlistData> data;

        public UpdateWatchlists(String baseAssetId, List<WatchlistData> data) {
            this.baseAssetId = baseAssetId;
            this.data = data;
        }

        public List<WatchlistData> getData() {
            return data;
        }

        public void setData(List<WatchlistData> data) {
            this.data = data;
        }

        public String getBaseAssetId() {
            return baseAssetId;
        }

        public void setBaseAssetId(String baseAssetId) {
            this.baseAssetId = baseAssetId;
        }
    }

    //更新单条行情数据
    public static class UpdateWatchlist {
        private WatchlistData data;

        public UpdateWatchlist(WatchlistData data) {
            this.data = data;
        }

        public WatchlistData getData() {
            return data;
        }

        public void setData(WatchlistData data) {
            this.data = data;
        }
    }

    //
    public static class UpdateOrderBook{
        private OrderBook data;

        public UpdateOrderBook(OrderBook data) {
            this.data = data;
        }

        public OrderBook getData() {
            return data;
        }

        public void setData(OrderBook data) {
            this.data = data;
        }
    }

    public static class UpdateMarketTrade {
        private List<MarketTrade> data;

        public UpdateMarketTrade(List<MarketTrade> data) {
            this.data = data;
        }

        public List<MarketTrade> getData() {
            return data;
        }

        public void setData(List<MarketTrade> data) {
            this.data = data;
        }
    }

    //更新K线图
    public static class UpdateKLineChar{
        private List<HistoryPrice> data;

        public UpdateKLineChar() {

        }

        public UpdateKLineChar(List<HistoryPrice> data) {
            this.data = data;
        }

        public List<HistoryPrice> getData() {
            return data;
        }

        public void setData(List<HistoryPrice> data) {
            this.data = data;
        }
    }

    //加载单个AssertAbject
    public static class LoadAsset{
        private AssetObject data;

        public LoadAsset(AssetObject data) {
            this.data = data;
        }

        public AssetObject getData() {
            return data;
        }

        public void setData(AssetObject data) {
            this.data = data;
        }
    }

    //加载多个AssertAbject
    public static class LoadAssets{
        private List<AssetObject> data;

        public LoadAssets(List<AssetObject> data) {
            this.data = data;
        }

        public List<AssetObject> getData() {
            return data;
        }

        public void setData(List<AssetObject> data) {
            this.data = data;
        }
    }

    //登出
    public static class LoginOut{

    }

    //http请求超时
    public static class HttpTimeOut{

    }

    //websocket请求超时
    public static class WebSocketTimeOut{

    }

    //线程调度
    public static class ThreadScheduler<T>{
        private T data;

        public ThreadScheduler(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public static class NetWorkStateChanged{
        private boolean isAvailable;

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }

        public NetWorkStateChanged(boolean isAvailable){
            this.isAvailable = isAvailable;

        }
    }

    public static class SubscribeMarket{
        private int callId;

        public SubscribeMarket(int callId) {
            this.callId = callId;
        }

        public int getCallId() {
            return callId;
        }

        public void setCallId(int callId) {
            this.callId = callId;
        }
    }

    public static class ConfigChanged{
        private String configName;

        public ConfigChanged(String configName) {
            this.configName = configName;
        }

        public String getConfigName() {
            return configName;
        }

        public void setConfigName(String configName) {
            this.configName = configName;
        }
    }

    public static class UpdateBuySellOrders{
        private List<Order> buyOrders;
        private List<Order> sellOrders;

        public UpdateBuySellOrders(List<Order> buyOrders, List<Order> sellOrders) {
            this.buyOrders = buyOrders;
            this.sellOrders = sellOrders;
        }

        public List<Order> getBuyOrders() {
            return buyOrders;
        }

        public List<Order> getSellOrders() {
            return sellOrders;
        }
    }

    /**
     * 通过K线图界面快捷买入卖出
     */
    public static class MarketIntentToExchange{
        private String action;
        private WatchlistData watchlist;

        public MarketIntentToExchange(String action, WatchlistData watchlist) {
            this.action = action;
            this.watchlist = watchlist;
        }

        public String getAction() {
            return action;
        }

        public WatchlistData getWatchlist() {
            return watchlist;
        }
    }

    /**
     * 交易界面委单被点击 委单价格设置到EditText
     */
    public static class LimitOrderClick{
        private double price;

        public LimitOrderClick(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }
    }
}

