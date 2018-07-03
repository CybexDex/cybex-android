package com.cybexmobile.faucet;

import com.cybexmobile.graphene.chain.AccountBalanceObject;
import com.cybexmobile.graphene.chain.AssetObject;

public class DepositAndWithdrawObject {
    public String id;
    public boolean enable;
    public String enMsg;
    public String cnMsg;
    public AssetObject assetObject;
    private AccountBalanceObject accountBalanceObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCnMsg() {
        return cnMsg;
    }

    public void setCnMsg(String cnMsg) {
        this.cnMsg = cnMsg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public AssetObject getAssetObject() {
        return assetObject;
    }

    public void setAssetObject(AssetObject assetObject) {
        this.assetObject = assetObject;
    }

    public AccountBalanceObject getAccountBalanceObject() {
        return accountBalanceObject;
    }

    public void setAccountBalanceObject(AccountBalanceObject accountBalanceObject) {
        this.accountBalanceObject = accountBalanceObject;
    }
}
