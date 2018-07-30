package com.cybexmobile.data;

import com.cybexmobile.graphene.chain.AssetObject;

import java.util.List;

public class GatewayDepositWithdrawRecordsItem {
    AssetObject itemAsset;
    Record record;

    public AssetObject getItemAsset() {
        return itemAsset;
    }

    public void setItemAsset(AssetObject itemAsset) {
        this.itemAsset = itemAsset;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}
