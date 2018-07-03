package com.cybexmobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cybexmobile.R;

public class CybexDialog {

    public interface UnLockDialogClickListener {
        void onClick(String password, Dialog dialog);
    }

    public static void showRegisterDialog(Context context, String message, View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_register_account);
        TextView content = dialog.findViewById(R.id.register_dialog_content_password);
        content.setText(message);
        Button btn = dialog.findViewById(R.id.register_dialog_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        dialog.show();

    }

    public static void showBalanceDialog(Context context) {
        showBalanceDialog(context, null);
    }

    public static void showBalanceDialog(Context context, View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_account_balance);
        Button dialogButton = dialog.findViewById(R.id.account_balance_dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        dialog.show();
    }

    public static void showUnlockWalletDialog(Context context, UnLockDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_unclock_wallet);
        Button confirmButton = dialog.findViewById(R.id.unlock_wallet_dialog_confirm);
        Button cancelButton = dialog.findViewById(R.id.unlock_wallet_dialog_cancel);
        EditText passwordEditText = dialog.findViewById(R.id.unlock_wallet_dialog_edit_text);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(passwordEditText.getText().toString(), dialog);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showConfirmationDialog(Context context, View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_withdraw_confirmation);
        TextView withdrawAddress = dialog.findViewById(R.id.confirm_dialog_withdraw_address);
        TextView withdrawAmount = dialog.findViewById(R.id.confirm_dialog_withdraw_amount);
        TextView withdrawFee = dialog.findViewById(R.id.confirm_dialog_withdraw_withdraw_fee);
        TextView gatewayFee = dialog.findViewById(R.id.confirm_dialog_gateway_fee);
        TextView receiveAmount = dialog.findViewById(R.id.confirm_dialog_receive_amount);
        Button confirmButton = dialog.findViewById(R.id.confirm_dialog_confirm_button);
        Button cancelButton = dialog.findViewById(R.id.confirm_dialog_cancel_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
