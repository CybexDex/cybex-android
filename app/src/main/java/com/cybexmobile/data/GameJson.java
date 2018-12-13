package com.cybexmobile.data;

public class GameJson extends GatewayLogInRecordRequest {
    private double balance;
    private double fee_balance;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getFee_balance() {
        return fee_balance;
    }

    public void setFee_balance(double fee_balance) {
        this.fee_balance = fee_balance;
    }
}
