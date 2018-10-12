package com.cybex.provider.graphene.chain;

import java.io.Serializable;

public class MarketTicker implements Serializable {
    //base id
    public String base;
    //quote id
    public String quote;
    //最新成交价
    public double latest;
    //最低价
    public double lowest_ask;
    //最高价
    public double highest_bid;
    //涨跌幅
    public double percent_change;
    //base成交量
    public double base_volume;
    //quote成交量
    public double quote_volume;
}
