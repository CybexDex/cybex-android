package io.enotes.sdk.repository.api.entity;

import android.text.TextUtils;

import io.enotes.sdk.utils.Utils;


public class EntFeesEntity extends BaseENotesEntity {
    private String fastest;
    private String fast;
    private String low;
    private String price;
    private String feerate;
    private String base;
    private String median;
    private String minimum;

    public String getFastest() {
        if (!TextUtils.isEmpty(median)) return Utils.hexToBigIntString(median);
        return fastest;
    }

    public void setFastest(String fastest) {
        this.fastest = fastest;
    }

    public String getFast() {
        if (!TextUtils.isEmpty(price)) return Utils.hexToBigIntString(price);
        if (!TextUtils.isEmpty(feerate)) return Utils.hexToBigIntString(feerate);
        if (!TextUtils.isEmpty(base)) return Utils.hexToBigIntString(base);
        return fast;
    }

    public void setFast(String fast) {
        this.fast = fast;
    }

    public String getLow() {
        if (!TextUtils.isEmpty(minimum)) return Utils.hexToBigIntString(minimum);
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getPrice() {
        return Utils.hexToBigIntString(price);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFeerate() {
        return Utils.hexToBigIntString(feerate);
    }

    public void setFeerate(String feerate) {
        this.feerate = feerate;
    }
}
