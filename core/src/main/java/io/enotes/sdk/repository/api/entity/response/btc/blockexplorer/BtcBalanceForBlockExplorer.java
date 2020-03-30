package io.enotes.sdk.repository.api.entity.response.btc.blockexplorer;

import java.math.BigInteger;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;

public class BtcBalanceForBlockExplorer implements BaseThirdEntity {

    private String addrStr;
    private String balanceSat;
    private String unconfirmedBalanceSat;

    public String getAddrStr() {
        return addrStr;
    }

    public void setAddrStr(String addrStr) {
        this.addrStr = addrStr;
    }

    public String getBalanceSat() {
        return balanceSat;
    }

    public void setBalanceSat(String balanceSat) {
        this.balanceSat = balanceSat;
    }

    public String getUnconfirmedBalanceSat() {
        return unconfirmedBalanceSat;
    }

    public void setUnconfirmedBalanceSat(String unconfirmedBalanceSat) {
        this.unconfirmedBalanceSat = unconfirmedBalanceSat;
    }

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity = new EntBalanceEntity();
        entBalanceEntity.setBalance(new BigInteger(balanceSat).toString(16));
        entBalanceEntity.setAddress(addrStr);
        return entBalanceEntity;
    }
}
