package com.cybex.basemodule.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.R;
import com.cybex.basemodule.constant.Constant;
import com.cybex.provider.db.entity.Address;
import com.cybex.provider.graphene.chain.AccountObject;

import java.util.Locale;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT;

public class CybexDialog {

    public interface ConfirmationDialogClickListener {
        void onClick(Dialog dialog);
    }

    public interface ConfirmationDialogCancelListener {
        void onCancel(Dialog dialog);
    }

    public interface ConfirmationDialogClickWithButtonTimerListener {
        void onClick(Dialog dialog, Button button, EditText editText, TextView textView);
    }

    public static void showRegisterDialog(Context context, String message, final View.OnClickListener listener) {
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

    public static void showBalanceDialog(Context context, String title, String content) {
        showBalanceDialog(context, title, content, null);
    }

    public static void showBalanceDialog(Context context, String title, String content, final View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_account_balance);
        Button dialogButton = dialog.findViewById(R.id.account_balance_dialog_button);
        TextView dialogTitle = dialog.findViewById(R.id.notify_dialog_title_tv);
        TextView dialogContent = dialog.findViewById(R.id.notify_dialog_content_tv);
        dialogTitle.setText(title);
        dialogContent.setText(content);
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

    public static void showConfirmationDialog(Context context, final ConfirmationDialogClickListener listener, String withdrawAddress, String withdrawAmount,
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
                                                              String total, final ConfirmationDialogClickListener listener){
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

    public static void showLimitOrderCancelConfirmationDialog(Context context, String content, final ConfirmationDialogClickListener listener){
        final Dialog dialog = new Dialog(context);
        /**
         * fix bug:CYM-503
         * 点击空白地方dialog不能消失
         */
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_limit_order_cancel_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_text_title_limit_order_cancel_confirmation));
        TextView tvContent = dialog.findViewById(R.id.cancel_content_tv);
        tvContent.setText(content);
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
                                                      String fee, String memo, final ConfirmationDialogClickListener listener){
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

    public static void showBalanceClaimDialog(Context context, String quantity, String account, final ConfirmationDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_balance_claim_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(context.getResources().getString(R.string.dialog_balance_claim_title));
        TextView tvQuantity = dialog.findViewById(R.id.dialog_balance_claim_tv_quantity);
        TextView tvAccount = dialog.findViewById(R.id.dialog_balance_claim_tv_account);
        tvQuantity.setText(quantity);
        tvAccount.setText(account);

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

    public static void showVersionUpdateDialog(Context context, String updateMessage, final ConfirmationDialogClickListener listener) {
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

    public static void showVersionUpdateDialogForced(Context context, String updateMessage, final ConfirmationDialogClickListener listener) {
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

    public static UnlockDialog showUnlockWalletDialog(FragmentManager fragmentManager,
                                              AccountObject accountObject,
                                              String username,
                                              UnlockDialog.UnLockDialogClickListener unLockListener,
                                              UnlockDialog.OnDismissListener onDismissListener){
        UnlockDialog dialog = new UnlockDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_TRANSFER_MY_ACCOUNT, accountObject);
        bundle.putString(INTENT_PARAM_NAME, username);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager, UnlockDialog.class.getSimpleName());
        dialog.setUnLockListener(unLockListener);
        dialog.setOnDismissListener(onDismissListener);
        return dialog;
    }

    public static UnlockDialog showUnlockWalletDialog(FragmentManager fragmentManager,
                                              AccountObject accountObject,
                                              String username,
                                              UnlockDialog.UnLockDialogClickListener unLockListener){
        return showUnlockWalletDialog(fragmentManager, accountObject, username, unLockListener, null);
    }

    public static void showAddAddressDialog(Context context, String message, String subMessage,
                                            final ConfirmationDialogClickListener confirmListener,
                                            final ConfirmationDialogCancelListener cancelListener){
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
                                            final ConfirmationDialogClickListener confirmListener,
                                            final ConfirmationDialogCancelListener cancelListener){
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
        TextView memoTitle = dialog.findViewById(R.id.dialog_delete_address_layout_memo_title_tv);
        TextView tvMemo = dialog.findViewById(R.id.dialog_delete_address_tv_memo);
        if (TextUtils.isEmpty(address.getToken()) || address.getToken().equals("1.3.4")) {
            tvAccountLabel.setText(context.getResources().getString(R.string.text_account_dot));
        } else {
            tvAccountLabel.setText(context.getResources().getString(R.string.text_address_dot));
        }
        if (!TextUtils.isEmpty(address.getMemo())) {
            layoutMemo.setVisibility(View.VISIBLE);
            memoTitle.setText(context.getResources().getString(R.string.text_memo_dot));
            tvMemo.setText(address.getMemo());
        }

        if (!TextUtils.isEmpty(address.getTag())) {
            layoutMemo.setVisibility(View.VISIBLE);
            memoTitle.setText(context.getResources().getString(R.string.text_tag_dot));
            tvMemo.setText(address.getTag());
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

    public static void showVerifyPinCodeETODialog(final Dialog dialog, String title, final ConfirmationDialogClickWithButtonTimerListener confirmationDialogClickWithButtonTimerListener,
                                                  final ConfirmationDialogCancelListener confirmationDialogCancelListener, final Handler handler, Runnable runnable) {

        if (runnable == null) {
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_eto_pin_code);
            TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
            tvTitle.setText(title);
            final EditText editText = dialog.findViewById(R.id.dialog_confirm_et_eto_pin_code);
            final TextView errorCode = dialog.findViewById(R.id.dialog_confirm_layout_eto_pin_code_error_tv);
            Button cancelButton = dialog.findViewById(R.id.dialog_confirm_btn_cancel);
            final Button confirmButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (confirmationDialogClickWithButtonTimerListener != null) {
                        confirmationDialogClickWithButtonTimerListener.onClick(dialog, confirmButton, editText, errorCode);
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.hide();
                    if (confirmationDialogCancelListener != null) {
                        confirmationDialogCancelListener.onCancel(dialog);
                    }
                }
            });
        }
        dialog.show();
    }

    public static void showETOReserveSucessDialog(Context context, String message) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_eto_reservation_sucess);
        TextView textView = dialog.findViewById(R.id.dialog_eto_success_tv);
        textView.setText(message);
        Button okButton = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void showAttendETOConfirmDialog(Context context, String title, String message, String quantity, String fee, final ConfirmationDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_eto_transfer_confirmation);
        TextView tvTitle = dialog.findViewById(R.id.dialog_confirm_tv_title);
        tvTitle.setText(title);
        TextView joinEtoTv = dialog.findViewById(R.id.dialog_eto_confirm_message);
        joinEtoTv.setText(message);
        TextView quantityTv = dialog.findViewById(R.id.dialog_eto_confirm_quantity_tv);
        quantityTv.setText(quantity);
        TextView feeTv = dialog.findViewById(R.id.dialog_eto_confirm_fee_tv);
        feeTv.setText(fee);
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

    public static void showAttendEtoLoadingDialog(final Context context, final ConfirmationDialogClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_eto_transfer_sucess_countdown_time);
        final ImageView imageView = dialog.findViewById(R.id.attend_eto_loading_iv);
        final Button button = dialog.findViewById(R.id.dialog_confirm_btn_confirm);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.img_rotate_anim);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
        imageView.startAnimation(animation);
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                button.setText(String.format(Locale.US, "%d%s", millisUntilFinished / 1000, context.getResources().getString(R.string.text_seconds)));
            }

            @Override
            public void onFinish() {
                imageView.clearAnimation();
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(dialog);
                }
            }
        }.start();
        dialog.show();
    }

}
