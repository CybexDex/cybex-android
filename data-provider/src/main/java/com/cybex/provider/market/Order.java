package com.cybex.provider.market;


import java.io.Serializable;

public class Order implements Serializable {
    //quote相对base价格
    public double price;
    //quote数量
    public double quoteAmount;
    //asset数量
    public double baseAmount;
}
