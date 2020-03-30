package io.enotes.sdk.repository.api.entity.request;

import io.enotes.sdk.repository.api.entity.BaseENotesEntity;

public class EntConfirmedListRequest extends BaseENotesEntity {
    private String txid;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }
}
