package io.enotes.sdk.repository.api.entity.response.btc.blockexplorer;

import java.math.BigDecimal;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.utils.Utils;

public class BtcUtxoForBlockExplorer implements BaseThirdEntity {
    private String address;
    private String txid;
    private int vout;
    private int ts;
    private String scriptPubKey;
    private String amount;
    private int confirmations;

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

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        this.vout = vout;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    @Override
    public EntUtxoEntity parseToENotesEntity() {
        EntUtxoEntity entUtxoEntity = new EntUtxoEntity();
        entUtxoEntity.setBalance(Utils.intToHexString(new BigDecimal(amount).multiply(new BigDecimal("100000000")).toBigInteger().toString()));
        entUtxoEntity.setIndex(Utils.intToHexString(vout + ""));
        entUtxoEntity.setScript(scriptPubKey);
        entUtxoEntity.setTxid(txid);
        entUtxoEntity.setComfirmed(confirmations>0);
        entUtxoEntity.setPositive(true);
        return entUtxoEntity;
    }
}
