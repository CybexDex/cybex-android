package com.cybexmobile.graphene.chain;


public class Price {

    public Asset base;
    public Asset quote;

    public Price(Asset assetBase, Asset assetQuote) {
        base = assetBase;
        quote = assetQuote;
    }

    public static Price unit_price(ObjectId<AssetObject> assetObjectobjectId) {
        return new Price(new Asset(1, assetObjectobjectId), new Asset(1, assetObjectobjectId));
    }
}
