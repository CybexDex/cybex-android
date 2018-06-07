package com.cybexmobile.faucet;

import com.cybexmobile.graphene.chain.AssetObject;

//交易对
public class AssetsPair {

    private String base;
    private String quote;
    private AssetObject baseAsset;
    private AssetObject quoteAsset;

    public AssetsPair(String base, String quote) {
        this.base = base;
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public AssetObject getBaseAsset() {
        return baseAsset;
    }

    public void setBaseAsset(AssetObject baseAsset) {
        this.baseAsset = baseAsset;
    }

    public AssetObject getQuoteAsset() {
        return quoteAsset;
    }

    public void setQuoteAsset(AssetObject quoteAsset) {
        this.quoteAsset = quoteAsset;
    }
}
