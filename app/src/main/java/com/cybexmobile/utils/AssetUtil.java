package com.cybexmobile.utils;

import java.math.RoundingMode;
import java.text.NumberFormat;

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
     * 格式化数据
     * @param number
     * @param mode 数据取舍模式
     * @return
     */
    public static String formatNumberRounding(double number, int scale, RoundingMode mode){
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(scale);
        format.setMaximumFractionDigits(scale);
        /**
         * fix bug:CYM-584
         * 取消千分位
         */
        format.setGroupingUsed(false);
        format.setRoundingMode(mode);
        return format.format(number);
    }

    /**
     *  格式化数据 默认RoundingMode.DOWN 不四舍五入
     * @param number
     * @param scale
     * @return
     */
    public static String formatNumberRounding(double number, int scale){
        return formatNumberRounding(number, scale, RoundingMode.DOWN);
    }

    /**
     * 价格精度
     * price >= 1          保留4位
     * 1 > price >= 0.0001 保留6位
     * price < 0.0001      保留8位
     * @param price
     * @return
     */
    public static int pricePrecision(double price){
        if(price >= 1){
            return 4;
        }else if(price >= 0.0001){
            return 6;
        }else {
            return 8;
        }
    }

    /**
     * 数量精度
     * price >= 1          保留6位
     * 1 > price >= 0.0001 保留4位
     * price < 0.0001      保留2位
     * @param price
     * @return
     */
    public static int amountPrecision(double price){
        if(price >= 1){
            return 6;
        }else if(price >= 0.0001){
            return 4;
        }else {
            return 2;
        }
    }

    /**
     * 数量格式化成 K M B
     * @param scale
     * @param number
     * @return
     */
    public static String formatAmountToKMB(double number, int scale) {
        NumberFormat format = NumberFormat.getInstance();
        format.setRoundingMode(RoundingMode.DOWN);
        int e = (int) Math.floor(Math.log10(number));
        if(e < 3){
            format.setMaximumFractionDigits(scale);
            format.setMinimumFractionDigits(scale);
            return format.format(number);
        }
        if (e < 6) {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return format.format(number / Math.pow(10, 3)) + "K";
        }
        if(e < 9){
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            return format.format(number / Math.pow(10, 6)) + "M";
        }
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(number / Math.pow(10, 9)) + "B";
    }

}
