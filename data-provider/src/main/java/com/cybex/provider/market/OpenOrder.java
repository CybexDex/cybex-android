package com.cybex.provider.market;


import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.LimitOrderObject;

public class OpenOrder {

    private LimitOrderObject limitOrder;
    private AssetObject baseObject;
    private AssetObject quoteObject;
    private double price;

    public OpenOrder() {
    }

    public LimitOrderObject getLimitOrder() {
        return limitOrder;
    }

    public void setLimitOrder(LimitOrderObject limitOrder) {
        this.limitOrder = limitOrder;
    }

    public AssetObject getBaseObject() {
        return baseObject;
    }

    public void setBaseObject(AssetObject baseObject) {
        this.baseObject = baseObject;
    }

    public AssetObject getQuoteObject() {
        return quoteObject;
    }

    public void setQuoteObject(AssetObject quoteObject) {
        this.quoteObject = quoteObject;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
