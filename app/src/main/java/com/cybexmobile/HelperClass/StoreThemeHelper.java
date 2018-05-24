package com.cybexmobile.HelperClass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StoreThemeHelper {

    public static void setLocalThemePosition(Context context, int position){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        editor.putInt("ThemePosition", position);
        editor.apply();
    }

    public static int getLocalThemePosition(Context context) {
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int position = preferences.getInt("ThemePosition", 0);
        return position;
    }
}
