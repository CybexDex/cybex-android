package com.cybexmobile.utils;

import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.Utils;
import com.cybexmobile.market.HistoryPrice;
import com.cybexmobile.market.MarketStat;

import java.util.List;


public final class PriceUtil {

    /**
     * 获取最高价
     * @param historyPrices
     * @return
     */
    public static double getHighPrice(List<HistoryPrice> historyPrices) {
        double max = 0;
        for (HistoryPrice historyPrice : historyPrices) {
            max = Math.max(historyPrice.high, max);
        }
        return max;
    }

    /**
     * 获取最低价
     * @param historyPrices
     * @return
     */
    public static double getLowPrice(List<HistoryPrice> historyPrices) {
        double min = historyPrices.get(0).low;
        for (HistoryPrice historyPrice : historyPrices) {
            min = Math.min(historyPrice.low, min);
        }
        return min;
    }

    /**
     * 获取当前价格 关盘价
     * @param historyPriceList
     * @return
     */
    public static double getCurrentPrice(List<HistoryPrice> historyPriceList) {
        return historyPriceList.get(historyPriceList.size() - 1).close;
    }

    /**
     * 获取涨跌幅
     * @param historyPriceList
     * @return
     */
    public static String getChange(List<HistoryPrice> historyPriceList) {
        String change;
        double open = historyPriceList.get(0).open;
        double close = historyPriceList.get(historyPriceList.size() - 1).close;
        change = String.valueOf((close - open) / open);
        return change;
    }

    /**
     * 获取历史价格
     * @param baseAsset
     * @param quoteAsset
     * @param bucket
     * @return
     */
    public static HistoryPrice priceFromBucket(AssetObject baseAsset, AssetObject quoteAsset, BucketObject bucket) {
       HistoryPrice price = new HistoryPrice();
        price.date = bucket.key.open;
        if (bucket.key.quote.equals(quoteAsset.id)) {
            price.high = Utils.get_asset_price(bucket.high_base, baseAsset,
                    bucket.high_quote, quoteAsset);
            price.low = Utils.get_asset_price(bucket.low_base, baseAsset,
                    bucket.low_quote, quoteAsset);
            price.open = Utils.get_asset_price(bucket.open_base, baseAsset,
                    bucket.open_quote, quoteAsset);
            price.close = Utils.get_asset_price(bucket.close_base, baseAsset,
                    bucket.close_quote, quoteAsset);
            price.volume = Utils.get_asset_amount(bucket.base_volume, baseAsset);
            price.quoteVolume = Utils.get_asset_amount(bucket.quote_volume, baseAsset);
        } else {
            price.low = Utils.get_asset_price(bucket.high_quote, baseAsset,
                    bucket.high_base, quoteAsset);
            price.high = Utils.get_asset_price(bucket.low_quote, baseAsset,
                    bucket.low_base, quoteAsset);
            price.open = Utils.get_asset_price(bucket.open_quote, baseAsset,
                    bucket.open_base, quoteAsset);
            price.close = Utils.get_asset_price(bucket.close_quote, baseAsset,
                    bucket.close_base, quoteAsset);
            price.volume = Utils.get_asset_amount(bucket.base_volume, quoteAsset);
            price.quoteVolume = Utils.get_asset_amount(bucket.quote_volume, baseAsset);
        }
        if (price.low == 0) {
            price.low = MathUtil.min(price.open, price.close);
        }
        if (price.high == Double.NaN || price.high == Double.POSITIVE_INFINITY) {
            price.high = MathUtil.max(price.open, price.close);
        }
        if (price.close == Double.POSITIVE_INFINITY || price.close == 0) {
            price.close = price.open;
        }
        if (price.open == Double.POSITIVE_INFINITY || price.open == 0) {
            price.open = price.close;
        }
        if (price.high > 1.3 * ((price.open + price.close) / 2)) {
            price.high = MathUtil.max(price.open, price.close);
        }
        if (price.low < 0.7 * ((price.open + price.close) / 2)) {
            price.low = MathUtil.min(price.open, price.close);
        }
        return price;
    }

    private double getVolFromPriceList(List<HistoryPrice> historyPrices) {
        double vol = 0;
        for (HistoryPrice historyPrice : historyPrices) {
            vol += historyPrice.volume;
        }
        return vol;
    }

    private double getQuoteVolFromPriceList(List<HistoryPrice> historyPrices) {
        double vol = 0;
        for (HistoryPrice historyPrice : historyPrices) {
            vol += historyPrice.quoteVolume;
        }
        return vol;
    }

}
