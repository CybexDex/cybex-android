package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class BtcConfirmedForBlockCypher implements BaseThirdEntity {
    private String hash;
    private int confirmations;
    private String block_hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getBlock_hash() {
        return block_hash;
    }

    public void setBlock_hash(String block_hash) {
        this.block_hash = block_hash;
    }

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity = new EntConfirmedEntity();
        entConfirmedEntity.setConfirmations(confirmations + "");
        entConfirmedEntity.setTxid(hash);
        return entConfirmedEntity;
    }
}
