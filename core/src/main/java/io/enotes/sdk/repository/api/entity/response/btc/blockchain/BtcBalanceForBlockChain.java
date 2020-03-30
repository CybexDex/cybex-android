package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.utils.Utils;

public class BtcBalanceForBlockChain implements BaseThirdEntity {
    private String hash160;
    private String address;
    private int n_tx;
    private int n_unredeemed;
    private int total_received;
    private int total_sent;
    private String final_balance;

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity = new EntBalanceEntity();
        entBalanceEntity.setBalance(Utils.intToHexString(final_balance));
        entBalanceEntity.setAddress(address);
        return entBalanceEntity;
    }


    public String getHash160() {
        return hash160;
    }

    public void setHash160(String hash160) {
        this.hash160 = hash160;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getN_tx() {
        return n_tx;
    }

    public void setN_tx(int n_tx) {
        this.n_tx = n_tx;
    }

    public int getN_unredeemed() {
        return n_unredeemed;
    }

    public void setN_unredeemed(int n_unredeemed) {
        this.n_unredeemed = n_unredeemed;
    }

    public int getTotal_received() {
        return total_received;
    }

    public void setTotal_received(int total_received) {
        this.total_received = total_received;
    }

    public int getTotal_sent() {
        return total_sent;
    }

    public void setTotal_sent(int total_sent) {
        this.total_sent = total_sent;
    }

    public String getFinal_balance() {
        return final_balance;
    }

    public void setFinal_balance(String final_balance) {
        this.final_balance = final_balance;
    }
}
