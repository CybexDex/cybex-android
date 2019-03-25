package com.cybex.provider.http.gateway.entity;

import com.cybex.provider.graphene.chain.AssetObject;

public class GatewayNewDepositWithdrawRecordItem {
    private AssetObject itemAsset;
    private GatewayNewRecord record;
    private String note;
    private String explorerLink;

    public AssetObject getItemAsset() {
        return itemAsset;
    }

    public void setItemAsset(AssetObject itemAsset) {
        this.itemAsset = itemAsset;
    }

    public GatewayNewRecord getRecord() {
        return record;
    }

    public void setRecord(GatewayNewRecord record) {
        this.record = record;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public String getExplorerLink() {
        return explorerLink;
    }

    public void setExplorerLink(String explorerLink) {
        this.explorerLink = explorerLink;
    }
}
