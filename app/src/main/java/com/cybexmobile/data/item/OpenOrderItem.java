package com.cybexmobile.data.item;

import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.LimitOrder;

/**
 * Open Order item
 */
public class OpenOrderItem {

    public LimitOrder limitOrder;
    public AssetObject baseAsset;
    public AssetObject quoteAsset;
    public boolean isSell;
    public double itemRMBPrice;
}
