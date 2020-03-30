package io.enotes.sdk.repository.api.entity.response.bch.bitpay;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.utils.Utils;

public class BchBalanceForBitpay implements BaseThirdEntity {
    private String addrStr;
    private String balance;
    private String balanceSat;

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity = new EntBalanceEntity();
        entBalanceEntity.setBalance(Utils.intToHexString(balanceSat));
        entBalanceEntity.setAddress(addrStr);
        return entBalanceEntity;
    }

    public String getAddrStr() {
        return addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBalanceSat() {
        return balanceSat;
    }

    public void setBalanceSat(String balanceSat) {
        this.balanceSat = balanceSat;
    }
}
