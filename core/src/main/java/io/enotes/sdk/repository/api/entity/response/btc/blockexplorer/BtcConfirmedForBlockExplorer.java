package io.enotes.sdk.repository.api.entity.response.btc.blockexplorer;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class BtcConfirmedForBlockExplorer implements BaseThirdEntity {
    private String txid;
    private int confirmations;
    private String blockhash;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getBlockhash() {
        return blockhash;
    }

    public void setBlockhash(String blockhash) {
        this.blockhash = blockhash;
    }

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity = new EntConfirmedEntity();
        entConfirmedEntity.setConfirmations(confirmations + "");
        entConfirmedEntity.setTxid(txid);
        return entConfirmedEntity;
    }
}
