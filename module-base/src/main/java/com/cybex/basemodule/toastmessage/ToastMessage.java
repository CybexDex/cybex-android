package com.cybex.basemodule.toastmessage;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cybex.basemodule.R;


public class ToastMessage {

    public static void showNotEnableDepositToastMessage(Activity context, String message, int resId) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_unenable_deposit, null, false);
        TextView text = layout.findViewById(R.id.toast_center_message_text_view);
        ImageView imageView = layout.findViewById(R.id.toast_center_message_icon);
        text.setText(message);
        imageView.setImageResource(resId);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0 ,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
