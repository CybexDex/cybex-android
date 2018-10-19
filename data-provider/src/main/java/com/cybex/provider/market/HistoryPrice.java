package com.cybex.provider.market;

import java.io.Serializable;
import java.util.Date;

public class HistoryPrice implements Serializable {

    //最高价
    public double high;
    //最低价
    public double low;
    //开盘价
    public double open;
    //关盘价
    public double close;
    //base交易量
    public double baseVolume;
    //quote交易量
    public double quoteVolume;
    //日期
    public Date date;
}
