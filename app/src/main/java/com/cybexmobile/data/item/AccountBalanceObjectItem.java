package com.cybexmobile.data.item;

import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.MarketTicker;

import java.io.Serializable;

/**
 * Portfolio item
 */
public class AccountBalanceObjectItem implements Serializable {

    public AccountBalanceObject accountBalanceObject;
    public AssetObject assetObject;
    public MarketTicker marketTicker;
    public double cybPrice;
    public double frozenAmount;

}
