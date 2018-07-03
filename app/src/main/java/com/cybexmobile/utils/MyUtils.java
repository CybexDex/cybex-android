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

    public static int getVolUnitNum(float num) {

        int e = (int) Math.floor(Math.log10(num));

        if (e >= 8) {
            return 8;
        } else if (e >= 4) {
            return 4;
        } else {
            return 1;
        }
    }

    public static String getVolUnitText(int unit, float num) {
        DecimalFormat mFormat;
        if (unit == 1) {
            mFormat = new DecimalFormat("#0");
        } else {
            mFormat = new DecimalFormat("#0.00");
        }
        num = num / unit;
        if (num == 0) {
            return "0";
        }
        return mFormat.format(num);
    }

    public static String getDecimalFormatVol(float vol) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(vol);//getNumberKMGExpressionFormat 返回的是字符串
    }

    public static NumberFormat getSuitableDecimalFormat(String quoteSymbol) {
        if (quoteSymbol.equals("JADE.MT")) {
            return new DecimalFormat("##,##0.00000000");
        } else {
            return new DecimalFormat("##,##0.00000");
        }
    }

    public static String getPrecisedFormatter(int precision) {
        return "%." + precision + "f";
    }

    public static String getNumberKMGExpressionFormat(double number) {
        String result = null;
        int e = (int) Math.floor(Math.log10(number));

        if (e >= 3) {

            if (e >= 3) {
                number = number / Math.pow(10, 3);
                result = String.format(Locale.US, "%.2fK", number);
            } else if (e >= 6) {
                number = number / Math.pow(10, 6);
                result = String.format(Locale.US, "%.2fM", number);
            } else if (e >= 9) {
                number = number / Math.pow(10, 9);
                result = String.format(Locale.US, "%.2fB", number);
            }
        } else {
            NumberFormat formatter = new DecimalFormat("#,###0.00");
            result = String.valueOf(formatter.format(number));
        }
        return result;

    }

    public static String removeJadePrefix(String symbol) {
        if (symbol.contains("JADE")) {
            return symbol.substring(5, symbol.length());
        } else {
            return symbol;
        }
    }
}
