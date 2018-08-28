package com.cybex.provider.http.entity;

/**
 * 币RMB价格
 */
public class AssetRmbPrice {

    //币名称
    private String name;
    //币价
    private double value;
    //时间
    private long time;

    public AssetRmbPrice(){

    }

    public AssetRmbPrice(String name, double value, long time){
        this.name = name;
        this.value = value;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
