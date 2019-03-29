package com.cybex.provider.http.gateway.entity;

public class Data {
    private String addresses;

    private String exEvents;

    private DataInside data;

    private boolean depositSwitch;

    private String lowWaterLevel;

    private String description;

    private String depositFee;

    private String cybname;

    private String cybid;

    private String gatewayAccount;

    private String precision;

    private String withdrawFee;

    private String cybOrder;

    private String highWaterLevel;

    private String jpOrders;

    private String balances;

    private Blockchain blockchain;

    private String smartContract;

    private String name;

    private String orders;

    private String sweepTo;

    private boolean withdrawSwith;

    private String decimal;

    private String blockchainID;

    private String minWithdraw;

    private String minDeposit;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getExEvents() {
        return exEvents;
    }

    public void setExEvents(String exEvents) {
        this.exEvents = exEvents;
    }

    public DataInside getData() {
        return data;
    }

    public void setData(DataInside data) {
        this.data = data;
    }

    public boolean getDepositSwitch() {
        return depositSwitch;
    }

    public void setDepositSwitch(boolean depositSwitch) {
        this.depositSwitch = depositSwitch;
    }

    public String getLowWaterLevel() {
        return lowWaterLevel;
    }

    public void setLowWaterLevel(String lowWaterLevel) {
        this.lowWaterLevel = lowWaterLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepositFee() {
        return depositFee;
    }

    public void setDepositFee(String depositFee) {
        this.depositFee = depositFee;
    }

    public String getCybname() {
        return cybname;
    }

    public void setCybname(String cybname) {
        this.cybname = cybname;
    }

    public String getCybid() {
        return cybid;
    }

    public void setCybid(String cybid) {
        this.cybid = cybid;
    }

    public String getWithdrawFee() {
        return withdrawFee;
    }

    public void setWithdrawFee(String withdrawFee) {
        this.withdrawFee = withdrawFee;
    }

    public String getCybOrder() {
        return cybOrder;
    }

    public void setCybOrder(String cybOrder) {
        this.cybOrder = cybOrder;
    }

    public String getHighWaterLevel() {
        return highWaterLevel;
    }

    public void setHighWaterLevel(String highWaterLevel) {
        this.highWaterLevel = highWaterLevel;
    }

    public String getJpOrders() {
        return jpOrders;
    }

    public void setJpOrders(String jpOrders) {
        this.jpOrders = jpOrders;
    }

    public String getBalances() {
        return balances;
    }

    public void setBalances(String balances) {
        this.balances = balances;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public String getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(String smartContract) {
        this.smartContract = smartContract;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getSweepTo() {
        return sweepTo;
    }

    public void setSweepTo(String sweepTo) {
        this.sweepTo = sweepTo;
    }

    public boolean getWithdrawSwith() {
        return withdrawSwith;
    }

    public void setWithdrawSwith(boolean withdrawSwith) {
        this.withdrawSwith = withdrawSwith;
    }

    public String getDecimal() {
        return decimal;
    }

    public void setDecimal(String decimal) {
        this.decimal = decimal;
    }

    public String getBlockchainID() {
        return blockchainID;
    }

    public void setBlockchainID(String blockchainID) {
        this.blockchainID = blockchainID;
    }

    public String getGatewayAccount() {
        return gatewayAccount;
    }

    public void setGatewayAccount(String gatewayAccount) {
        this.gatewayAccount = gatewayAccount;
    }

    public String getMinDeposit() {
        return minDeposit;
    }

    public void setMinDeposit(String minDeposit) {
        this.minDeposit = minDeposit;
    }

    public String getMinWithdraw() {
        return minWithdraw;
    }

    public void setMinWithdraw(String minWithdraw) {
        this.minWithdraw = minWithdraw;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    class DataInside {
        private String hashlink;

        private Project project;

        public String getHashlink() {
            return hashlink;
        }

        public void setHashlink(String hashlink) {
            this.hashlink = hashlink;
        }

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        @Override
        public String toString() {
            return "ClassPojo [hashlink = " + hashlink + ", project = " + project + "]";
        }
    }

    public class Project {
        private String logourl;

        public String getLogourl() {
            return logourl;
        }

        public void setLogourl(String logourl) {
            this.logourl = logourl;
        }

        @Override
        public String toString() {
            return "ClassPojo [logourl = " + logourl + "]";
        }
    }


    @Override
    public String toString() {
        return "ClassPojo [addresses = " + addresses + ", exEvents = " + exEvents + ", data = " + data + ", depositSwitch = " + depositSwitch + ", lowWaterLevel = " + lowWaterLevel + ", description = " + description + ", depositFee = " + depositFee + ", cybname = " + cybname + ", cybid = " + cybid + ", withdrawFee = " + withdrawFee + ", cybOrder = " + cybOrder + ", highWaterLevel = " + highWaterLevel + ", jpOrders = " + jpOrders + ", balances = " + balances + ", blockchain = " + blockchain + ", smartContract = " + smartContract + ", name = " + name + ", orders = " + orders + ", sweepTo = " + sweepTo + ", withdrawSwith = " + withdrawSwith + ", decimal = " + decimal + ", blockchainID = " + blockchainID + "]";
    }
}
