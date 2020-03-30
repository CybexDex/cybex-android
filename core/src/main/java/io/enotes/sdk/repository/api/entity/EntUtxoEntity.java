package io.enotes.sdk.repository.api.entity;


import io.enotes.sdk.utils.Utils;

public class EntUtxoEntity extends BaseENotesEntity {
    private String address;
    private String txid;
    private String index;
    private String height;
    private String script;
    private String balance;
    private String unit;
    private boolean comfirmed;
    private boolean positive = true;
    private String prevtxid;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getBalance() {
        return Utils.hexToBigIntString(balance);
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getOutput_no() {
        return Utils.hexToBigInt(index);
    }

    public boolean isComfirmed() {
        return comfirmed;
    }

    public void setComfirmed(boolean comfirmed) {
        this.comfirmed = comfirmed;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public String getPrevtxid() {
        return prevtxid;
    }

    public void setPrevtxid(String prevtxid) {
        this.prevtxid = prevtxid;
    }
}
