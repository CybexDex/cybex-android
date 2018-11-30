package com.cybexmobile.faucet;

import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AssetObject;

public class DepositAndWithdrawObject {
    private String id;
    private boolean enable;
    private String enMsg;
    private String cnMsg;
    private String enInfo;
    private String cnInfo;
    private long count;
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

    public void setEnInfo(String enInfo) {
        this.enInfo = enInfo;
    }

    public String getEnInfo() {
        return enInfo;
    }

    public void setCnInfo(String cnInfo) {
        this.cnInfo = cnInfo;
    }

    public String getCnInfo() {
        return cnInfo;
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
