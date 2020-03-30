package io.enotes.sdk.repository.api.entity;


import android.text.TextUtils;

import io.enotes.sdk.utils.Utils;


public class EntConfirmedEntity extends BaseENotesEntity {
    public static final int STATUS_NO_TRANSACTION = 0;
    public static final int STATUS_DOING_TRANSACTION = 1;
    public static final int STATUS_DONE_TRANSACTION = 2;
    private String txid;
    private int status;
    private String height;
    private String confirmations;

    public int getStatus() {
        if(TextUtils.isEmpty(confirmations))return STATUS_NO_TRANSACTION;
        return Utils.hexToBigInt(confirmations) >= 1 ? STATUS_DONE_TRANSACTION : STATUS_DOING_TRANSACTION;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(String confirmations) {
        this.confirmations = confirmations;
    }
}


