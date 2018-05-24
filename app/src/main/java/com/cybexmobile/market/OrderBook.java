package com.cybexmobile.market;


import java.io.Serializable;
import java.util.List;

public class OrderBook implements Serializable {

    public String base;
    public String quote;
    public List<Order> bids;
    public List<Order> asks;
}
