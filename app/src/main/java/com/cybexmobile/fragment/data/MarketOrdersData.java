package com.cybexmobile.fragment.data;


public class MarketOrdersData {
    private double baseVolume;
    private double quoteVolue;
    private double buy;
    private double sell;


    public MarketOrdersData() {

    };

    public double getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(double baseVolume) {
        this.baseVolume = baseVolume;
    }

    public double getBuy() {
        return buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getQuoteVolue() {
        return quoteVolue;
    }

    public void setQuoteVolue(double quoteVolue) {
        this.quoteVolue = quoteVolue;
    }

    public double getSell() {
        return sell;
    }

    public void setSell(double sell) {
        this.sell = sell;
    }
}
