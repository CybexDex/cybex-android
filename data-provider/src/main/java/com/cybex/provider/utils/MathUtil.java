package com.cybex.provider.utils;

public class MathUtil {

    public static double max(double a, double b) {
        if (a == Double.POSITIVE_INFINITY) return b;
        if (b == Double.POSITIVE_INFINITY) return a;
        return Math.max(a, b);
    }

    public static double min(double a, double b) {
        if (a == 0) return b;
        if (b == 0) return a;
        return Math.min(a, b);
    }

}
