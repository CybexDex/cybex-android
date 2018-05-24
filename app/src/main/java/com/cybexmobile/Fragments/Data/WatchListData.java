package com.cybexmobile.Fragments.Data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class WatchListData implements Serializable{

    private double high;
    private double low;
    private double vol;
    private double quoteVol;
    private double currentPrice;
    private String base;
    private String quote;
    private String change;
    private long time;
    private String baseId;
    private String quoteId;
    private String subscribeId;

    public WatchListData(long time, double high, double low, double vol, double quoteVol, double currentPrice, String base, String quote, String change, String baseId, String quoteId, String subscribeId) {
        this.high = high;
        this.low = low;
        this.vol = vol;
        this.quoteVol = quoteVol;
        this.time = time;
        this.currentPrice = currentPrice;
        this.base = base;
        this.quote = quote;
        this.change = change;
        this.baseId = baseId;
        this.quoteId = quoteId;
        this.subscribeId = subscribeId;
    }

    public WatchListData() {

    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public double getQuoteVol() {
        return quoteVol;
    }

    public void setQuoteVol(double quoteVol) {
        this.quoteVol = quoteVol;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getCurrentPrice () {
        return this.currentPrice;
    }

    public void setCurrentPrice (double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getChange () {
        return this.change;
    }

    public void setChange (String change) {
        this.change = change;
    }

    public String getBase() {
        return this.base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return this.quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public void setSubscribeId(String subscribeId) {
        this.subscribeId = subscribeId;
    }

    public String getSubscribeId() {
        return subscribeId;
    }
}
