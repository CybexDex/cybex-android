package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.utils.Utils;

public class BtcBalanceForBlockCypher implements BaseThirdEntity {
    private String address;
    private int final_n_tx;
    private int total_received;
    private int total_sent;
    private String final_balance;
    private String balance;

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity = new EntBalanceEntity();
        entBalanceEntity.setBalance(Utils.intToHexString(balance));
        entBalanceEntity.setAddress(address);
        return entBalanceEntity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFinal_n_tx() {
        return final_n_tx;
    }

    public void setFinal_n_tx(int final_n_tx) {
        this.final_n_tx = final_n_tx;
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
