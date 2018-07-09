package com.cybexmobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
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

    public interface ConfirmationDialogClickListener {
        void onClick(Dialog dialog);
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
                String password = passwordEditText.getText().toString();
                if (listener != null && !TextUtils.isEmpty(password)) {
                    listener.onClick(password, dialog);
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

    public static void showConfirmationDialog(Context context, ConfirmationDialogClickListener listener, String withdrawAddress, String withdrawAmount,
                                              String transferFee, String gatewayFee, String receiveAmount ) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_withdraw_confirmation);
        TextView withdrawAddressView = dialog.findViewById(R.id.confirm_dialog_withdraw_address);
        TextView withdrawAmountView = dialog.findViewById(R.id.confirm_dialog_withdraw_amount);
        TextView withdrawFeeView = dialog.findViewById(R.id.confirm_dialog_withdraw_withdraw_fee);
        TextView gatewayFeeView = dialog.findViewById(R.id.confirm_dialog_gateway_fee);
        TextView receiveAmountView = dialog.findViewById(R.id.confirm_dialog_receive_amount);
        withdrawAddressView.setText(withdrawAddress);
        withdrawAmountView.setText(withdrawAmount);
        withdrawFeeView.setText(transferFee);
        gatewayFeeView.setText(gatewayFee);
        receiveAmountView.setText(receiveAmount);
        Button confirmButton = dialog.findViewById(R.id.confirm_dialog_confirm_button);
        Button cancelButton = dialog.findViewById(R.id.confirm_dialog_cancel_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog);
                }
                dialog.dismiss();
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

    public static void showLimitOrderCreateConfirmationDialog(Context context, boolean isBuy, String price, String amount,
                                                              String total, ConfirmationDialogClickListener listener){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_limit_order_create_confirmation);
        TextView tvPrice = dialog.findViewById(R.id.dialog_limit_order_create_tv_price);
        TextView tvAmount = dialog.findViewById(R.id.dialog_limit_order_create_tv_amount);
        TextView tvTotal = dialog.findViewById(R.id.dialog_limit_order_create_tv_total);
        tvPrice.setText(price);
        tvAmount.setText(amount);
        tvTotal.setText(total);
        tvPrice.setTextColor(context.getResources().getColor(isBuy ? R.color.increasing_color : R.color.decreasing_color));
        Button confirmButton = dialog.findViewById(R.id.confirm_dialog_confirm_button);
        Button cancelButton = dialog.findViewById(R.id.confirm_dialog_cancel_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog);
                }
                dialog.dismiss();
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

    public static void showLimitOrderCancelConfirmationDialog(Context context, boolean isBuy, String price, String amount,
                                                              String total, String fee, ConfirmationDialogClickListener listener){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_limit_order_cancel_confirmation);
        TextView tvPrice = dialog.findViewById(R.id.dialog_limit_order_create_tv_price);
        TextView tvAmount = dialog.findViewById(R.id.dialog_limit_order_create_tv_amount);
        TextView tvTotal = dialog.findViewById(R.id.dialog_limit_order_create_tv_total);
        TextView tvFee = dialog.findViewById(R.id.dialog_limit_order_create_tv_cancellation_fee);
        tvPrice.setText(price);
        tvAmount.setText(amount);
        tvTotal.setText(total);
        tvFee.setText(fee);
        tvPrice.setTextColor(context.getResources().getColor(isBuy ? R.color.increasing_color : R.color.decreasing_color));
        Button confirmButton = dialog.findViewById(R.id.confirm_dialog_confirm_button);
        Button cancelButton = dialog.findViewById(R.id.confirm_dialog_cancel_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog);
                }
                dialog.dismiss();
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
