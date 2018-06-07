package com.cybexmobile.event;

import com.cybexmobile.data.AssetRmbPrice;
import com.cybexmobile.fragment.data.WatchlistData;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.market.HistoryPrice;
import com.cybexmobile.market.MarketStat;
import com.cybexmobile.market.MarketTrade;
import com.cybexmobile.market.OrderBook;

import java.util.List;

public class Event {

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
        private List<WatchlistData> data;

        public UpdateWatchlists(List<WatchlistData> data) {
            this.data = data;
        }

        public List<WatchlistData> getData() {
            return data;
        }

        public void setData(List<WatchlistData> data) {
            this.data = data;
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
}
