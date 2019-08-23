package com.cybex.provider.graphene.chain;

public class HtlcAdapterItemObject {
    private HtlcObject htlcObject;
    private AssetObject assetObject;
    private String from;
    private String to;

    public AssetObject getAssetObject() {
        return assetObject;
    }

    public void setAssetObject(AssetObject assetObject) {
        this.assetObject = assetObject;
    }

    public HtlcObject getHtlcObject() {
        return htlcObject;
    }

    public void setHtlcObject(HtlcObject htlcObject) {
        this.htlcObject = htlcObject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
