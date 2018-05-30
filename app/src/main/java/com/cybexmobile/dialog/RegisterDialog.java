package com.cybexmobile.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.cybexmobile.R;
import com.cybexmobile.activity.BottomNavigationActivity;
import com.cybexmobile.activity.RegisterActivity;

public class RegisterDialog {

    public static void showDialog(Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if (activity instanceof RegisterActivity) {
            dialog.setContentView(R.layout.register_account_dialog);
            Button dialogButton = dialog.findViewById(R.id.register_dialog_button);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } else if (activity instanceof BottomNavigationActivity) {
            dialog.setContentView(R.layout.account_balance_dialog_layout);
            Button dialogButton = dialog.findViewById(R.id.account_balance_dialog_button);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();

    }

}
