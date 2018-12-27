package com.cybex.basemodule.cache;

import com.cybex.provider.graphene.chain.AssetsPair;

import java.util.List;
import java.util.Map;

public class AssetPairCache {

    private Map<String, List<AssetsPair>> assetPairCache;
    private Map<String, String> evaProjectNames;

    public void setEvaProjectNames(Map<String, String> evaProjectNames) {
        this.evaProjectNames = evaProjectNames;
    }

    public String getEvaProjectNameFromToken(String tokenName) {
        return evaProjectNames.get(tokenName);
    }


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

    public AssetsPair getAssetPair(String baseId, String quoteId) {
        List<AssetsPair> assetsPairs = assetPairCache.get(baseId);
        if(assetsPairs != null) {
            for(AssetsPair assetsPair : assetsPairs) {
                if(assetsPair.getQuote().equals(quoteId)){
                    return assetsPair;
                }
            }
        }
        assetsPairs = assetPairCache.get(quoteId);
        if(assetsPairs != null) {
            for(AssetsPair assetsPair : assetsPairs) {
                if(assetsPair.getQuote().equals(baseId)){
                    return assetsPair;
                }
            }
        }
        return null;
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
