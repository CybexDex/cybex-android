package com.cybexmobile.market;

import java.io.Serializable;
import java.util.Date;

public class HistoryPrice implements Serializable {

    public double high;
    public double low;
    public double open;
    public double close;
    public double volume;
    public double quoteVolume;
    public Date date;
}
