package com.cybex.basemodule.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
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

    public static String parseSymbolWithTransactionTest(String assetSymbol){
        if(assetSymbol == null){
            return null;
        }
        String[] symbolArr = assetSymbol.split("\\.");
        if(symbolArr.length == 1){
            return symbolArr[0];
        }
        if(symbolArr.length == 2){
            if (symbolArr[0].equals("ARENA")) {
                return assetSymbol;
            }
            return symbolArr[1];
        }
        return "";
    }

    /**
     * 获取币种前缀
     *
     */
    public static String getPrefix(String assetSymbol) {
        if (assetSymbol == null) {
            return null;
        }
        String[] symbolArr = assetSymbol.split("\\.");
        return symbolArr[0] + ".";
    }

    /**
     * 格式化数据 取消科学计数法
     * @param number
     * @param mode 数据取舍模式
     * @return
     */
    public static String formatNumberRounding(double number, int scale, RoundingMode mode){
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(number));
        bigDecimal = bigDecimal.setScale(scale, mode);
        String result = bigDecimal.toPlainString();
        bigDecimal = null;
        return result;
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
    @Deprecated
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

    /**
     * 加法精确运算
     * @param num1
     * @param num2
     * @return
     */
    public static double add(double num1, double num2) {
        return add(String.valueOf(num1), String.valueOf(num2));
    }

    public static double add(long num1, long num2) {
        return add(String.valueOf(num1), String.valueOf(num2));
    }

    public static double add(String num1, String num2) {
        BigDecimal bigDecimal1 = new BigDecimal(num1);
        BigDecimal bigDecimal2 = new BigDecimal(num2);
        return bigDecimal1.add(bigDecimal2).doubleValue();
    }

    /**
     * 减法精确运算
     * @param num1
     * @param num2
     * @return
     */
    public static double subtract(double num1, double num2) {
        return subtract(String.valueOf(num1), String.valueOf(num2));
    }

    public static double subtract(long num1, long num2) {
        return subtract(String.valueOf(num1), String.valueOf(num2));
    }

    public static double subtract(String num1, String num2) {
        BigDecimal bigDecimal1 = new BigDecimal(num1);
        BigDecimal bigDecimal2 = new BigDecimal(num2);
        return bigDecimal1.subtract(bigDecimal2).doubleValue();
    }

    /**
     * 乘法精确运算
     * @param num1
     * @param num2
     * @return
     */
    public static double multiply(double num1, double num2) {
        return multiply(String.valueOf(num1), String.valueOf(num2));
    }

    public static double multiply(long num1, long num2) {
        return multiply(String.valueOf(num1), String.valueOf(num2));
    }

    public static double multiply(long num1, double num2) {
        return multiply(String.valueOf(num1), String.valueOf(num2));
    }

    public static double multiply(String num1, String num2) {
        BigDecimal bigDecimal1 = new BigDecimal(num1);
        BigDecimal bigDecimal2 = new BigDecimal(num2);
        return bigDecimal1.multiply(bigDecimal2).doubleValue();
    }

    /**
     * 除法精确运算
     * @param num1
     * @param num2
     * @return
     */
    public static double divide(double num1, double num2) {
        return divide(String.valueOf(num1), String.valueOf(num2));
    }

    public static double divide(long num1, long num2) {
        return divide(String.valueOf(num1), String.valueOf(num2));
    }

    public static double divide(long num1, double num2) {
        return divide(String.valueOf(num1), String.valueOf(num2));
    }

    public static double divide(String num1, String num2) {
        BigDecimal bigDecimal1 = new BigDecimal(num1);
        BigDecimal bigDecimal2 = new BigDecimal(num2);
        return bigDecimal1.divide(bigDecimal2, 16, RoundingMode.DOWN).doubleValue();
    }

    /**
     * Format Double
     * @param d
     * @return
     */
    public static String fmt(double d)
    {
        if(d == (long) d)
            return String.format(Locale.US,"%d",(long)d);
        else
            return String.format("%s",d);
    }

}
