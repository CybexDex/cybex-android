package io.enotes.sdk.repository.api.entity.response.bch.bitpay;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class BchConfirmedForBitpay implements BaseThirdEntity {
    private String txid;
    private long confirmations;

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity = new EntConfirmedEntity();
        entConfirmedEntity.setConfirmations(confirmations + "");
        entConfirmedEntity.setTxid(txid);
        return entConfirmedEntity;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }
}
