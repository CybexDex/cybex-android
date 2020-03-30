package io.enotes.sdk.repository.api.entity.response.btc.blockexplorer;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;

public class BtcSendRawTransactionForBlockExplorer implements BaseThirdEntity {
    private String txid;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    @Override
    public EntSendTxEntity parseToENotesEntity() {
        EntSendTxEntity entSendTxEntity = new EntSendTxEntity();
        entSendTxEntity.setTxid(txid);
        return entSendTxEntity;
    }
}
