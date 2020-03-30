package io.enotes.sdk.repository.api.entity.response.bch.bitpay;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.utils.Utils;

public class BchUtxoForBitpay implements BaseThirdEntity {
    private String address;
    private String txid;
    private String scriptPubKey;
    private String satoshis;
    private int confirmations;
    private int vout;
    @Override
    public EntUtxoEntity parseToENotesEntity() {
        EntUtxoEntity entUtxoEntity = new EntUtxoEntity();
        entUtxoEntity.setBalance(Utils.intToHexString(satoshis));
        entUtxoEntity.setIndex(Utils.intToHexString(vout + ""));
        entUtxoEntity.setScript(scriptPubKey);
        entUtxoEntity.setTxid(txid);
        entUtxoEntity.setComfirmed(confirmations>0);
        entUtxoEntity.setPositive(true);
        return entUtxoEntity;
    }
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

    public String getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    public String getSatoshis() {
        return satoshis;
    }

    public void setSatoshis(String satoshis) {
        this.satoshis = satoshis;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        this.vout = vout;
    }
}
