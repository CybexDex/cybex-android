package io.enotes.sdk.repository.api.entity.request;

import io.enotes.sdk.repository.api.entity.BaseENotesEntity;

public class EntSendTxListRequest extends BaseENotesEntity {
    private String rawtx;

    public String getRawtx() {
        return rawtx;
    }

    public void setRawtx(String rawtx) {
        this.rawtx = rawtx;
    }
}
