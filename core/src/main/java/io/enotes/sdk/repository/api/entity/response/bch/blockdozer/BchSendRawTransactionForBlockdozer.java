package io.enotes.sdk.repository.api.entity.response.bch.blockdozer;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;

public class BchSendRawTransactionForBlockdozer implements BaseThirdEntity {
    private String txid;

    @Override
    public EntSendTxEntity parseToENotesEntity() {
        if (txid == null) return null;
        EntSendTxEntity entSendTxEntity = new EntSendTxEntity();
        entSendTxEntity.setTxid(txid);
        return entSendTxEntity;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }
}
