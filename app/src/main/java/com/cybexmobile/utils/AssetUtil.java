package com.cybexmobile.utils;

import java.util.Locale;

public class AssetUtil {

    /**
     * 解析币名称 去除JADE.前缀
     * @param assetSymbol
     * @return
     */
    public static String parseSymbol(String assetSymbol){
        if(assetSymbol == null){
            return null;
        }
        String[] symbolArr = assetSymbol.split("\\.");
        if(symbolArr.length == 1){
            return symbolArr[0];
        }
        if(symbolArr.length == 2){
            return symbolArr[1];
        }
        return "";
    }

    /**
     * 格式化价格
     * price >= 1          保留4位
     * 1 > price >= 0.0001 保留6位
     * price < 0.0001      保留8位
     * @param price
     * @return
     */
    public static String formatPrice(double price){
        String format;
        if(price >= 1){
            format = "%.4f";
        }else if(price >= 0.0001){
            format = "%.6f";
        }else {
            format = "%.8f";
        }
        return format;
    }

    /**
     * 格式化数量
     * price >= 1          保留6位
     * 1 > price >= 0.0001 保留4位
     * price < 0.0001      保留2位
     * @param price
     * @return
     */
    public static String formatAmount(double price){
        String format;
        if(price >= 1){
            format = "%.6f";
        }else if(price >= 0.0001){
            format = "%.4f";
        }else {
            format = "%.2f";
        }
        return format;
    }

}
