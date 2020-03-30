package io.enotes.sdk.repository.api.entity;

import io.enotes.sdk.utils.Utils;

public class EntNonceEntity extends BaseENotesEntity {
    private String nonce;

    public String getNonce() {
        return Utils.hexToBigIntString(nonce);
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
