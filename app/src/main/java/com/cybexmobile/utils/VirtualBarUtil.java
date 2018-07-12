package com.cybexmobile.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * 虚拟导航栏
 */
public class VirtualBarUtil {

    /**
     * 获取虚拟导航栏的高度
     * @param context
     * @return
     */
    public static int getHeight(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        display.getMetrics(metrics);
        int height = metrics.heightPixels;
        return realHeight - height;
    }
}
