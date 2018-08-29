package com.cybex.provider.market;


import java.io.Serializable;
import java.util.List;

public class OrderBook implements Serializable {

    public String base;
    public String quote;
    //买单
    public List<Order> buyOrders;
    //卖单
    public List<Order> sellOrders;
}
