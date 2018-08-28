package com.cybexmobile.cache;

import com.cybexmobile.data.item.AccountBalanceObjectItem;

import java.util.List;

public class BalanceCache {

    //Balance Account page
    private double mTotalCybBalance;
    private double mTotalRmbBalance;

    List<AccountBalanceObjectItem> mAccountBalanceObjectItemList;

    private BalanceCache() {

    }

    private static class Factory {
        private static BalanceCache cache = new BalanceCache();
    }

    public static BalanceCache getInstance() {
        return Factory.cache;
    }

    public double getmTotalCybBalance() {
        return mTotalCybBalance;
    }

    public void setmTotalCybBalance(double mTotalCybBalance) {
        this.mTotalCybBalance = mTotalCybBalance;
    }

    public List<AccountBalanceObjectItem> getmAccountBalanceObjectItemList() {
        return mAccountBalanceObjectItemList;
    }

    public void setmAccountBalanceObjectItemList(List<AccountBalanceObjectItem> mAccountBalanceObjectItemList) {
        this.mAccountBalanceObjectItemList = mAccountBalanceObjectItemList;
    }

    public double getmTotalRmbBalance() {
        return mTotalRmbBalance;
    }

    public void setmTotalRmbBalance(double mTotalRmbBalance) {
        this.mTotalRmbBalance = mTotalRmbBalance;
    }
}
