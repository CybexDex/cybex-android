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
    private String projectName;
    private String gatewayAccount;
    private String minWithdraw;
    private String precision;
    private String withdrawFee;
    private boolean tag;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setTag(boolean tag) {
        this.tag = tag;
    }

    public boolean isTag() {
        return tag;
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

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public String getMinWithdraw() {
        return minWithdraw;
    }

    public void setMinWithdraw(String minWithdraw) {
        this.minWithdraw = minWithdraw;
    }

    public String getGatewayAccount() {
        return gatewayAccount;
    }

    public void setGatewayAccount(String gatewayAccount) {
        this.gatewayAccount = gatewayAccount;
    }

    public String getWithdrawFee() {
        return withdrawFee;
    }

    public void setWithdrawFee(String withdrawFee) {
        this.withdrawFee = withdrawFee;
    }
}
