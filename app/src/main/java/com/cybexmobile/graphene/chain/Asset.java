package com.cybexmobile.graphene.chain;

import java.math.BigInteger;

public class Asset {

    public long amount;
    public ObjectId<AssetObject> asset_id;

    public Asset(long lAmount, ObjectId<AssetObject> assetObjectobjectId) {
        amount = lAmount;
        asset_id = assetObjectobjectId;
    }

    public Asset multipy(Price priceObject) {
        BigInteger bigAmount = BigInteger.valueOf(amount);
        BigInteger bigQuoteAmount = BigInteger.valueOf(priceObject.quote.amount);
        BigInteger bigBaseAmount = BigInteger.valueOf(priceObject.base.amount);
        if (asset_id.equals(priceObject.base.asset_id)) {
            BigInteger bigResult = bigAmount.multiply(bigQuoteAmount).divide(bigBaseAmount);
            return new Asset(bigResult.longValue(), priceObject.quote.asset_id);

        } else if (asset_id.equals(priceObject.quote.asset_id)) {
            BigInteger bigResult = bigAmount.multiply(bigBaseAmount).divide(bigQuoteAmount  );
            return new Asset(bigResult.longValue(), priceObject.base.asset_id);
        } else {
            throw new RuntimeException("invalid Price object");
        }
    }
}
