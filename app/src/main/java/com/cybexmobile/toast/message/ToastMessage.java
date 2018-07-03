package com.cybexmobile.toast.message;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cybexmobile.R;

public class ToastMessage {

    public static void showNotEnableDepositToastMessage(Activity context, String message) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_unenable_deposit, (ViewGroup) context.findViewById(R.id.toast_layout_deposit_root));
        TextView text = layout.findViewById(R.id.toast_deposit_text_view);
        text.setText(message);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0 ,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
