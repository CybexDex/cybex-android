package com.cybex.basemodule.cache;

import com.cybex.provider.graphene.chain.AssetsPair;

import java.util.List;
import java.util.Map;

public class AssetPairCache {

    private Map<String, List<AssetsPair>> assetPairCache;

    private AssetPairCache(){}

    private static class Factory {
        private static AssetPairCache cache = new AssetPairCache();
    }

    public static AssetPairCache getInstance() {
        return Factory.cache;
    }

    public void setAssetPairCache(Map<String, List<AssetsPair>> assetPairCache) {
        this.assetPairCache = assetPairCache;
    }

    public AssetsPair.Config getAssetPairConfig(String baseId, String quoteId) {
        List<AssetsPair> assetsPairs = assetPairCache.get(baseId);
        if(assetsPairs == null){
            return null;
        }
        for(AssetsPair assetsPair : assetsPairs){
            if(assetsPair.getQuote().equals(quoteId)){
                return assetsPair.getConfig();
            }
        }
        return null;
    }
}
