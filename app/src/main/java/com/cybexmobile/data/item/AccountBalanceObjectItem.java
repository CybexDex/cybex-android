package com.cybexmobile.data.item;

import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.market.MarketTicker;

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
