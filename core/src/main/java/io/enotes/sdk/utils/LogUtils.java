package io.enotes.sdk.utils;

import android.text.TextUtils;
import android.util.Log;

import io.enotes.sdk.core.ENotesSDK;


public class LogUtils {

    public static void i(String tag, String msg) {
        if (ENotesSDK.config.debugCard && !TextUtils.isEmpty(msg))
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (ENotesSDK.config.debugCard && !TextUtils.isEmpty(msg))
            Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (ENotesSDK.config.debugCard && !TextUtils.isEmpty(msg))
            Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (ENotesSDK.config.debugCard && !TextUtils.isEmpty(msg))
            Log.d(tag, msg, tr);
    }
}
