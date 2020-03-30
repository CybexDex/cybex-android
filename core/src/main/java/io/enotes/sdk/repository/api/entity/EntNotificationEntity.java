package io.enotes.sdk.repository.api.entity;


public class EntNotificationEntity extends BaseENotesEntity {
    private String cid;
    private String event="txreceipt";
    private String txid;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

}
