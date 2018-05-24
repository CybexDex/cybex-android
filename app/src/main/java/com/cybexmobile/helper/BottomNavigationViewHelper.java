package com.cybexmobile.helper;


import android.annotation.SuppressLint;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;

import java.lang.reflect.Field;

public class BottomNavigationViewHelper {

    @SuppressLint("RestrictedApi")
    public static void removeShiftMode(BottomNavigationView view){
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shifitingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shifitingMode.setAccessible(true);
            shifitingMode.setBoolean(menuView, false);
            shifitingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }

        } catch (NoSuchFieldException e) {
            Log.e("NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ILLEGAL ALG", "Unable to change value of shift mode");
        }
    }
}
