package com.cybex.provider.utils;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MyUtils {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String getVolUnit(float num) {

        int e = (int) Math.floor(Math.log10(num));

        if (e >= 6) {
            return "m";
        } else if (e >= 3) {
            return "k";
        } else {
            return "";
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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
