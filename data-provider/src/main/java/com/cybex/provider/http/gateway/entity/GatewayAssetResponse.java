package com.cybex.provider.http.gateway.entity;

public class GatewayAssetResponse {
    private long id;
    private String createdAt;
    private String updatedAt;
    private Object deletedAt;
    private String name;
    private String blockchain;
    private String projectname;
    private String cybname;
    private String cybid;
    private String confirmation;
    private String smartContract;
    private String gatewayAccount;
    private String withdrawPrefix;
    private boolean depositSwitch;
    private boolean withdrawSwitch;
    private String minDeposit;
    private String minWithdraw;
    private String withdrawFee;
    private String depositFee;
    private String precision;
    private String imgURL;
    private String hashLink;
    private Info info;
    private boolean useMemo;

    public long getID() { return id; }
    public void setID(long value) { this.id = value; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String value) { this.createdAt = value; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String value) { this.updatedAt = value; }

    public Object getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Object value) { this.deletedAt = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getCybid() {
        return cybid;
    }

    public void setCybid(String cybid) {
        this.cybid = cybid;
    }

    public String getBlockchain() { return blockchain; }
    public void setBlockchain(String value) { this.blockchain = value; }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public String getCybname() { return cybname; }
    public void setCybname(String value) { this.cybname = value; }

    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String value) { this.confirmation = value; }

    public String getSmartContract() { return smartContract; }
    public void setSmartContract(String value) { this.smartContract = value; }

    public String getGatewayAccount() { return gatewayAccount; }
    public void setGatewayAccount(String value) { this.gatewayAccount = value; }

    public String getWithdrawPrefix() { return withdrawPrefix; }
    public void setWithdrawPrefix(String value) { this.withdrawPrefix = value; }

    public boolean getDepositSwitch() { return depositSwitch; }
    public void setDepositSwitch(boolean value) { this.depositSwitch = value; }

    public boolean getWithdrawSwitch() { return withdrawSwitch; }
    public void setWithdrawSwitch(boolean value) { this.withdrawSwitch = value; }

    public String getMinDeposit() { return minDeposit; }
    public void setMinDeposit(String value) { this.minDeposit = value; }

    public String getMinWithdraw() { return minWithdraw; }
    public void setMinWithdraw(String value) { this.minWithdraw = value; }

    public String getWithdrawFee() { return withdrawFee; }
    public void setWithdrawFee(String value) { this.withdrawFee = value; }

    public String getDepositFee() { return depositFee; }
    public void setDepositFee(String value) { this.depositFee = value; }

    public String getPrecision() { return precision; }
    public void setPrecision(String value) { this.precision = value; }

    public String getImgURL() { return imgURL; }
    public void setImgURL(String value) { this.imgURL = value; }

    public String getHashLink() { return hashLink; }
    public void setHashLink(String value) { this.hashLink = value; }

    public Info getInfo() { return info; }
    public void setInfo(Info value) { this.info = value; }

    public boolean isUseMemo() {
        return useMemo;
    }

    public void setUseMemo(boolean useMemo) {
        this.useMemo = useMemo;
    }

    public class Info {
        private Deposit deposit;
        private Withdraw withdraw;

        public Deposit getDeposit() { return deposit; }
        public void setDeposit(Deposit value) { this.deposit = value; }

        public Withdraw getWithdraw() { return withdraw; }
        public void setWithdraw(Withdraw value) { this.withdraw = value; }
    }

    public class Deposit {
        private String cnMsg;
        private String enMsg;
        private String projectName;

        public String getCNMsg() { return cnMsg; }
        public void setCNMsg(String value) { this.cnMsg = value; }

        public String getEnMsg() { return enMsg; }
        public void setEnMsg(String value) { this.enMsg = value; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String value) { this.projectName = value; }
    }

    public class Withdraw {
        private String cnMsg;
        private String enMsg;

        public String getCNMsg() { return cnMsg; }
        public void setCNMsg(String value) { this.cnMsg = value; }

        public String getEnMsg() { return enMsg; }
        public void setEnMsg(String value) { this.enMsg = value; }
    }
}
