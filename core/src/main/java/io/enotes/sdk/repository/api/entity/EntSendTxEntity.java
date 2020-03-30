package io.enotes.sdk.repository.api.entity;


public class EntSendTxEntity extends BaseENotesEntity {
    private String rawtx;
    private String txid;

    public String getRawtx() {
        return rawtx;
    }

    public void setRawtx(String rawtx) {
        this.rawtx = rawtx;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }
}
