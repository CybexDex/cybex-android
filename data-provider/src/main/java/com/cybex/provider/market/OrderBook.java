package com.cybex.provider.market;


import java.io.Serializable;
import java.util.LinkedList;

public class OrderBook implements Serializable {

    public String base;
    public String quote;
    //买单
    public LinkedList<Order> buyOrders;
    //卖单
    public LinkedList<Order> sellOrders;
}
