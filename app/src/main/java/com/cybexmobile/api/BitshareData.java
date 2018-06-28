package com.cybexmobile.api;

import android.util.Pair;

import com.cybexmobile.graphene.chain.AccountHistoryObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BucketObject;
import com.cybexmobile.graphene.chain.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BitshareData {

    public List<Asset> listBalances;
    public List<Pair<AccountHistoryObject, Date>> listHistoryObject;
    //public Map<ObjectId<AccountObject>, AccountObject> mapId2AccountObject;
    public Map<ObjectId<AssetObject>, AssetObject> mapId2AssetObject;
    public AssetObject assetObjectCurrency; // 当前做汇率标志的货币
    public Map<ObjectId<AssetObject>, BucketObject> mapAssetId2Bucket;

    public class TotalBalances {
        public String strTotalBalances;
        public String strTotalCurrency;
        public String strExchangeRate;
    };

    public TotalBalances getTotalAmountBalances() {
        final AssetObject assetObjectBase = mapId2AssetObject.get(new ObjectId<AssetObject>(0, AssetObject.class));

        long lTotalAmount = 0;
        for (Asset assetBalances : listBalances) {
            long lBaseAmount = convert_asset_to_base(assetBalances, assetObjectBase).amount;
            lTotalAmount += lBaseAmount;
        }

        final AssetObject.asset_object_legible assetObjectLegible = assetObjectBase.get_legible_asset_object(lTotalAmount);
        final double fResult = (double)assetObjectLegible.lDecimal / assetObjectLegible.scaled_precision + assetObjectLegible.lCount;
        int nResult = (int)Math.rint(fResult);

        TotalBalances totalBalances = new TotalBalances();
        totalBalances.strTotalBalances = String.format(Locale.ENGLISH, "%d %s", nResult, assetObjectBase.symbol);

        long lTotalCurrency = assetObjectCurrency.convert_exchange_from_base(lTotalAmount);
        AssetObject.asset_object_legible legibleCurrency = assetObjectCurrency.get_legible_asset_object(lTotalCurrency);
        double fCurrency = (double)legibleCurrency.lDecimal / legibleCurrency.scaled_precision + legibleCurrency.lCount;
        int nCurrencyResult = (int)Math.rint(fCurrency);
        totalBalances.strTotalCurrency = String.format(Locale.ENGLISH, "%d %s", nCurrencyResult, assetObjectCurrency.symbol);

        double fExchange = get_base_exchange_rate(assetObjectCurrency, assetObjectBase);

        totalBalances.strExchangeRate = String.format(
                Locale.ENGLISH,
                "%.4f %s/%s",
                fExchange,
                assetObjectCurrency.symbol,
                assetObjectBase.symbol
        );

        return totalBalances;
    }

    public Asset convert_asset_to_base(Asset assetAmount, AssetObject assetObjectBase) {
        if (assetAmount.asset_id.equals(assetObjectBase.id)) {
            return assetAmount;
        }

        BucketObject bucketObject = mapAssetId2Bucket.get(assetAmount.asset_id);

        // // TODO: 06/09/2017 这里需要用整数计算提高精度

        long lBaseAmount = (long)(assetAmount.amount * ((double)bucketObject.close_base / bucketObject.close_quote));

        return new Asset(lBaseAmount, assetObjectBase.id);
    }

    public double get_base_exchange_rate(AssetObject assetObject, AssetObject assetObjectBase) {
        BucketObject bucketObject = mapAssetId2Bucket.get(assetObject.id);

        double fExchange =
                (double)bucketObject.close_quote /
                        bucketObject.close_base *
                        assetObjectBase.get_scaled_precision() /
                        assetObject.get_scaled_precision();

        return fExchange;

    }
}
