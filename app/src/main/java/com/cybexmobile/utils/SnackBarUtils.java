package com.cybexmobile.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.cybexmobile.R;

public class SnackBarUtils {

    private static SnackBarUtils INSTANCE = null;

    private SnackBarUtils() {
    }

    public static SnackBarUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SnackBarUtils();
        }
        return INSTANCE;
    }


    public void showSnackbar(String message, View view, Context context, int drawableId) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
        textView.setCompoundDrawablePadding(context.getResources().getDimensionPixelOffset(R.dimen.text_splash_margin_bottom));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackBarView.setLayoutParams(params);
        snackBarView.setBackgroundColor(context.getResources().getColor(R.color.snack_bar_background_color));
        snackbar.show();
    }
}
