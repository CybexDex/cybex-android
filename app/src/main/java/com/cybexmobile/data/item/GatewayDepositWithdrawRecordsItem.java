package com.cybexmobile.data.item;

import com.cybex.provider.http.entity.Record;
import com.cybex.provider.graphene.chain.AssetObject;

public class GatewayDepositWithdrawRecordsItem {
    private AssetObject itemAsset;
    private Record record;
    private String note;
    private String explorerLink;

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
