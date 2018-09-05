package com.cybexmobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybex.provider.graphene.chain.AccountObject;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT;

public class CybexDialog {

    public interface ConfirmationDialogClickListener {
        void onClick(Dialog dialog);
    }

    public interface ConfirmationDialogCancelListener {
        void onCancel(Dialog dialog);
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

    public static void showConfirmationDialog(Context context, ConfirmationDialogClickListener listener, String withdrawAddress, String withdrawAmount,
                                              String transferFee, String gatewayFee, String receiveAmount, String memo) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_withdraw_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.withdraw_confirmation));
        TextView withdrawAddressView = dialog.findViewById(R.id.confirm_dialog_withdraw_address);
        LinearLayout withdrawMemoLayout = dialog.findViewById(R.id.confirm_dialog_withdraw_memo_layout);
        TextView withdrawMemoView = dialog.findViewById(R.id.confirm_dialog_withdraw_memo);
        TextView withdrawAmountView = dialog.findViewById(R.id.confirm_dialog_withdraw_amount);
        TextView withdrawFeeView = dialog.findViewById(R.id.confirm_dialog_withdraw_withdraw_fee);
        TextView gatewayFeeView = dialog.findViewById(R.id.confirm_dialog_gateway_fee);
        TextView receiveAmountView = dialog.findViewById(R.id.confirm_dialog_receive_amount);
        withdrawAddressView.setText(withdrawAddress);
        if (TextUtils.isEmpty(memo)) {
            withdrawMemoLayout.setVisibility(View.GONE);
        } else {
            withdrawMemoLayout.setVisibility(View.VISIBLE);
            withdrawMemoView.setText(memo);
        }
        withdrawAmountView.setText(withdrawAmount);
        withdrawFeeView.setText(transferFee);
        gatewayFeeView.setText(gatewayFee);
        receiveAmountView.setText(receiveAmount);
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
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
        /**
         * fix bug:CYM-503
         * 点击空白地方dialog不能消失
         */
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_limit_order_create_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_text_title_limit_order_create_confirmation));
        TextView tvPrice = dialog.findViewById(R.id.dialog_limit_order_create_tv_price);
        TextView tvAmount = dialog.findViewById(R.id.dialog_limit_order_create_tv_amount);
        TextView tvTotal = dialog.findViewById(R.id.dialog_limit_order_create_tv_total);
        tvPrice.setText(price);
        tvAmount.setText(amount);
        tvTotal.setText(total);
        tvPrice.setTextColor(context.getResources().getColor(isBuy ? R.color.increasing_color : R.color.decreasing_color));
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
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
        /**
         * fix bug:CYM-503
         * 点击空白地方dialog不能消失
         */
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_limit_order_cancel_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_text_title_limit_order_cancel_confirmation));
        TextView tvPrice = dialog.findViewById(R.id.dialog_limit_order_create_tv_price);
        TextView tvAmount = dialog.findViewById(R.id.dialog_limit_order_create_tv_amount);
        TextView tvTotal = dialog.findViewById(R.id.dialog_limit_order_create_tv_total);
        TextView tvFee = dialog.findViewById(R.id.dialog_limit_order_create_tv_cancellation_fee);
        tvPrice.setText(price);
        tvAmount.setText(amount);
        tvTotal.setText(total);
        tvFee.setText(fee);
        tvPrice.setTextColor(context.getResources().getColor(isBuy ? R.color.increasing_color : R.color.decreasing_color));
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
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

    public static void showTransferConfirmationDialog(Context context, String account, String quantity,
                                                      String fee, String memo, ConfirmationDialogClickListener listener){
        final Dialog dialog = new Dialog(context);
        /**
         * fix bug:CYM-503
         * 点击空白地方dialog不能消失
         */
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_transfer_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_text_title_transfer_confirmation));
        TextView tvAccount = dialog.findViewById(R.id.dialog_transfer_tv_account);
        TextView tvQuantity = dialog.findViewById(R.id.dialog_transfer_tv_quantity);
        TextView tvFee = dialog.findViewById(R.id.dialog_transfer_tv_fee);
        TextView tvMemo = dialog.findViewById(R.id.dialog_transfer_tv_memo);
        LinearLayout layoutMemo = dialog.findViewById(R.id.dialog_transfer_confirmation_layout_memo);
        tvAccount.setText(account);
        tvQuantity.setText(quantity);
        tvFee.setText(fee);
        if(TextUtils.isEmpty(memo)){
            layoutMemo.setVisibility(View.GONE);
        } else {
            layoutMemo.setVisibility(View.VISIBLE);
            tvMemo.setText(memo);
        }
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
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

    public static void showVersionUpdateDialog(Context context, String updateMessage, ConfirmationDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_update_version_dialog);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_version_update));
        TextView message  = dialog.findViewById(R.id.dialog_version_update_text_view);
        message.setText(updateMessage);
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
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

    public static void showVersionUpdateDialogForced(Context context, String updateMessage, ConfirmationDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_update_version_forced);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_version_update));
        TextView message  = dialog.findViewById(R.id.dialog_version_update_text_view);
        message.setText(updateMessage);
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog);
                }
            }
        });
        dialog.show();
    }

    public static void showUnlockWalletDialog(FragmentManager fragmentManager, AccountObject accountObject,
                                       String username,UnlockDialog.UnLockDialogClickListener unLockListener){
        UnlockDialog dialog = new UnlockDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_TRANSFER_MY_ACCOUNT, accountObject);
        bundle.putString(INTENT_PARAM_NAME, username);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, UnlockDialog.class.getSimpleName());
        dialog.setUnLockListener(unLockListener);
    }

    public static void showAddAddressDialog(Context context, String message, String subMessage,
                                            ConfirmationDialogClickListener confirmListener,
                                            ConfirmationDialogCancelListener cancelListener){
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_add_address);
        TextView tvMessage = dialog.findViewById(R.id.dialog_add_address_tv_message);
        TextView tvSubMessage = dialog.findViewById(R.id.dialog_add_address_tv_sub_message);
        tvMessage.setText(message);
        tvSubMessage.setText(subMessage);
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (confirmListener != null) {
                    confirmListener.onClick(dialog);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (cancelListener != null) {
                    cancelListener.onCancel(dialog);
                }
            }
        });
        dialog.show();
    }

    public static void showDeleteConfirmDialog(Context context, String title, String message, Address address,
                                            ConfirmationDialogClickListener confirmListener,
                                            ConfirmationDialogCancelListener cancelListener){
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_delete_address);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(title);
        TextView tvMessage = dialog.findViewById(R.id.dialog_delete_address_tv_message);
        tvMessage.setText(message);
        TextView tvNote = dialog.findViewById(R.id.dialog_delete_address_tv_note);
        tvNote.setText(address.getNote());
        TextView tvAccount = dialog.findViewById(R.id.dialog_delete_address_tv_account);
        tvAccount.setText(address.getAddress());
        TextView tvAccountLabel = dialog.findViewById(R.id.dialog_delete_address_tv_account_label);
        LinearLayout layoutMemo = dialog.findViewById(R.id.dialog_delete_address_layout_memo);
        TextView tvMemo = dialog.findViewById(R.id.dialog_delete_address_tv_memo);
        if (TextUtils.isEmpty(address.getToken()) || address.getToken().equals("1.3.4")) {
            tvAccountLabel.setText(context.getResources().getString(R.string.text_account_dot));
        } else {
            tvAccountLabel.setText(context.getResources().getString(R.string.text_address_dot));
        }
        if (!TextUtils.isEmpty(address.getMemo())) {
            layoutMemo.setVisibility(View.VISIBLE);
            tvMemo.setText(address.getMemo());
        }
        Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (confirmListener != null) {
                    confirmListener.onClick(dialog);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (cancelListener != null) {
                    cancelListener.onCancel(dialog);
                }
            }
        });
        dialog.show();
    }

}
