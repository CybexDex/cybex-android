package io.enotes.sdk.repository.api.entity;

import android.text.TextUtils;

import io.enotes.sdk.utils.Utils;

public class EntGasPriceEntity extends BaseENotesEntity {
    private String fastest;
    private String fast;
    private String stander;
    private String low;
    private String price;
    private String feerate;

    public String getFastest() {
        return fastest;
    }

    public void setFastest(String fastest) {
        this.fastest = fastest;
    }

    public String getFast() {
        if (!TextUtils.isEmpty(price)) return Utils.hexToBigIntString(price);
        if (!TextUtils.isEmpty(feerate)) return Utils.hexToBigIntString(feerate);
        return fast;
    }

    public void setFast(String fast) {
        this.fast = fast;
    }

    public String getStander() {
        return stander;
    }

    public void setStander(String stander) {
        this.stander = stander;
    }

    public String getLow() {
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
