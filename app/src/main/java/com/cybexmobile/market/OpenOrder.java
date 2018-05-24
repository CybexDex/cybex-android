package com.cybexmobile.market;


import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.LimitOrderObject;

public class OpenOrder {

    public LimitOrderObject limitOrder;
    public AssetObject base;
    public AssetObject quote;
    public double price;
}
