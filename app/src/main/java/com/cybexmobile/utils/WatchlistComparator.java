package com.cybexmobile.utils;

import com.cybex.provider.market.WatchlistData;

import java.util.Comparator;

public class WatchlistComparator implements Comparator<WatchlistData> {

    public static final int VOL_UP = 1;
    public static final int VOL_DOWN = 2;

    public static final int PRICE_UP = 3;
    public static final int PRICE_DOWN = 4;

    public static final int CHANGE_UP = 5;
    public static final int CHANGE_DOWN = 6;

    private int mSort;

    public WatchlistComparator(int sort){
        mSort = sort;
    }

    public void setSort(int sort){
        mSort = sort;
    }

    @Override
    public int compare(WatchlistData o1, WatchlistData o2) {
        if(mSort == VOL_DOWN){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return o1.getBaseVol() > o2.getBaseVol() ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;//置顶
        }
        if(mSort == VOL_UP){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return o1.getBaseVol() < o2.getBaseVol() ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;
        }
        if(mSort == PRICE_DOWN){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return o1.getCurrentPrice() > o2.getCurrentPrice() ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;
        }
        if(mSort == PRICE_UP){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return o1.getCurrentPrice() < o2.getCurrentPrice() ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;
        }
        if(mSort == CHANGE_DOWN){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return Double.parseDouble(o1.getChange()) > Double.parseDouble(o2.getChange()) ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;
        }
        if(mSort == CHANGE_UP){
            if(o1.getOrder() == 0 && o2.getOrder() == 0){
                return Double.parseDouble(o1.getChange()) < Double.parseDouble(o2.getChange()) ? -1 : 1;
            }
            return o1.getOrder() > o2.getOrder() ? -1 : 1;
        }
        return 0;
    }
}
