package com.cybexmobile.utils;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MyUtils {

    public static String getVolUnit(float num) {

        int e = (int) Math.floor(Math.log10(num));

        if (e >= 8) {
            return "亿手";
        } else if (e >= 4) {
            return "万手";
        } else {
            return "手";
        }
    }

    public static String getDecimalFormatVol(float vol) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(vol);//getNumberKMGExpressionFormat 返回的是字符串
    }

    public static String removeJadePrefix(String symbol) {
        if (symbol.contains("JADE")) {
            return symbol.substring(5, symbol.length());
        } else {
            return symbol;
        }
    }
}
