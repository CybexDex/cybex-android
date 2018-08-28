package com.cybex.provider.graphene.chain;

import java.io.Serializable;

public class MarketTicker implements Serializable {
    public String base;
    public String quote;
    public double latest;
    public double lowest_ask;
    public double highest_bid;
    public String percent_change;
    public double base_volume;
    public double quote_volume;
}
