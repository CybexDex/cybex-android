package com.cybexmobile.toast.message;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cybexmobile.R;

public class ToastMessage {

    public static void showNotEnableDepositToastMessage(Activity context, String message, Drawable drawable) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_unenable_deposit, (ViewGroup) context.findViewById(R.id.toast_layout_deposit_root));
        TextView text = layout.findViewById(R.id.toast_center_message_text_view);
        ImageView imageView = layout.findViewById(R.id.toast_center_message_icon);
        text.setText(message);
        imageView.setBackground(drawable);
        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0 ,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
