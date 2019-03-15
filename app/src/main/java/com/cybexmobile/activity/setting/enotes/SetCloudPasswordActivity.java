package com.cybexmobile.activity.setting.enotes;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialogWithEnotes;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.Authority;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.PrivateKey;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.utils.SpUtil;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.enotes.sdk.repository.card.CommandException;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.utils.ReaderUtils;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.graphene.chain.Operations.ID_UPDATE_ACCOUNT_OPERATION;

public class SetCloudPasswordActivity extends BaseActivity {

    @BindView(R.id.et_set_cloud_password)
    EditText mEtSetCloudPassword;
    @BindView(R.id.et_set_cloud_password_confirmation)
    EditText mEtSetCloudPasswordConfirm;
    @BindView(R.id.set_cloud_pass_ll_error)
    LinearLayout mSetCloudPassLlError;
    @BindView(R.id.set_cloud_pass_button)
    Button mSetCloudPassButton;
    @BindView(R.id.set_cloud_pass_error_text)
    TextView mSetCloudPassErrorText;
    @BindView(R.id.password_check)
    ImageView mPasswordChecker;
    @BindView(R.id.password_confirm_check)
    ImageView mPasswordConfirmChecker;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private static final String TAG = "SetCloudPasswordActivity";
    private Unbinder mUnbinder;
    private final String passwordRegPattern = "(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[^a-zA-Z0-9])" + ".{12,}";
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;
    private String mName;
    private Types.public_key_type mPublicKeyType;
    private Card mCard;
    private UnlockDialogWithEnotes unlockDialogWithEnotes;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_cloud_password);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unlockDialogWithEnotes = null;
        unbindService(mConnection);
    }

    @Override
    protected void nfcStartReadCard() {
        if (unlockDialogWithEnotes != null) {
            unlockDialogWithEnotes.showProgress();
            unlockDialogWithEnotes.showNormalText();
        } else {
            super.nfcStartReadCard();
        }
    }

    @Override
    protected void readCardOnSuccess(Card card) {
        mCard = card;
        if (unlockDialogWithEnotes != null) {
            try {
                if (cardManager.getTransactionPinStatus() == 0) {
                    unlockDialogWithEnotes.dismiss();
                    unlockDialogWithEnotes = null;
                    toUpdateAccount();
                } else {
                    final Map<Long, String> cardIdToCardPasswordMap = SpUtil.getMap(this, "eNotesCardMap");
                    if (cardManager.verifyTransactionPin(cardIdToCardPasswordMap.get(card.getId()))) {
                        unlockDialogWithEnotes.dismiss();
                        unlockDialogWithEnotes = null;
                        toUpdateAccount();
                    }
                }
            } catch (CommandException e) {
                e.printStackTrace();
            }
        } else {
            super.readCardOnSuccess(card);
        }
    }

    @Override
    protected void readCardError(int code, String message) {
        super.readCardError(code, message);
        if (unlockDialogWithEnotes != null) {
            unlockDialogWithEnotes.hideProgress();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadFullAccount(Event.UpdateFullAccount event) {
        if (mAccountObject == null) {
            mAccountObject = event.getFullAccount().account;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateAccount(Event.UpdateAccount updateAccount) {
        if (updateAccount.isSuccess()) {
            ToastMessage.showNotEnableDepositToastMessage(
                    this,
                    getResources().getString(R.string.toast_message_update_account_succeeded),
                    R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_update_account_failed), R.drawable.ic_error_16px);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
            if (fullAccountObject != null) {
                mAccountObject = fullAccountObject.account;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @OnTextChanged(value = R.id.et_set_cloud_password, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPasswordChanged(Editable s) {
        String password = mEtSetCloudPassword.getText().toString();
        String repeatPass = mEtSetCloudPasswordConfirm.getText().toString();
        checkPasswordFormat(password, repeatPass);
    }

    @OnTextChanged(value = R.id.et_set_cloud_password_confirmation, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onRepeatPasswordChanged(Editable s) {
        String password = mEtSetCloudPassword.getText().toString();
        String repeatPass = mEtSetCloudPasswordConfirm.getText().toString();
        checkPasswordFormat(password, repeatPass);
    }

    @OnClick(R.id.set_cloud_pass_button)
    public void onCloudPassButtonClicked(View view) {
        showEnotesWaitingDialog();
    }

    /**
     * eNotes等待弹窗
     */
    private void showEnotesWaitingDialog() {
        unlockDialogWithEnotes = CybexDialog.showUnlockWithEnotesWalletDialog(
                getSupportFragmentManager(),
                mAccountObject,
                mAccountObject.name,
                new UnlockDialogWithEnotes.UnLockDialogClickListener() {
                    @Override
                    public void onUnLocked(String password) {
                    }
                },
                new UnlockDialogWithEnotes.OnDismissListener() {
                    @Override
                    public void onDismiss(int result) {

                    }
                }
        );
    }

    MessageCallback<Reply<String>> mUpdateAccountCallback = new MessageCallback<Reply<String>>() {
        @Override
        public void onMessage(Reply<String> reply) {
            hideLoadDialog();
            if (reply.result == null && reply.error == null) {
                mEtSetCloudPassword.setText("");
                mEtSetCloudPasswordConfirm.setText("");
                mPasswordChecker.setVisibility(View.GONE);
                mPasswordConfirmChecker.setVisibility(View.GONE);
                mSetCloudPassLlError.setVisibility(View.GONE);
                mSetCloudPassButton.setEnabled(false);
                ToastMessage.showNotEnableDepositToastMessage(
                        SetCloudPasswordActivity.this,
                        getResources().getString(R.string.toast_message_update_account_succeeded),
                        R.drawable.ic_check_circle_green);
                mWebSocketService.loadFullAccount(mName);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Constant.RESULT_CODE_UPDATE_ACCOUNT);
                        finish();
                    }
                }, 2000);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(SetCloudPasswordActivity.this, getResources().getString(
                        R.string.toast_message_update_account_failed), R.drawable.ic_error_16px);

            }
        }

        @Override
        public void onFailure() {
            ToastMessage.showNotEnableDepositToastMessage(SetCloudPasswordActivity.this, getResources().getString(
                    R.string.toast_message_update_account_failed), R.drawable.ic_error_16px);

        }
    };

    private void toUpdateAccount() {
        if (isLoginFromENotes()) {
            if (!cardManager.isConnected()) {
                if (ReaderUtils.supportNfc(this)) {
                    showToast(this, getResources().getString(R.string.error_connect_card));
                } else {
                    showToast(this, getString(R.string.error_connect_card_ble));
                }
                return;
            }
            if (!cardManager.isPresent()) {
                if (ReaderUtils.supportNfc(this))
                    showToast(this, getString(R.string.error_connect_card));
                else
                    showToast(this, getString(R.string.error_connect_card_ble));
                return;
            }
            if (mCard != null && !mCard.getCurrencyPubKey().equals(getLoginPublicKey())) {
                showToast(this, getString(R.string.please_right_card));
                return;
            }
            if (BaseActivity.cardApp != null) {
                mCard = BaseActivity.cardApp;
            }
        }
        showLoadDialog(true);
        Authority authority = mAccountObject.active;
        authority.putNewKeys(mPublicKeyType, 1);
        Types.account_options options = mAccountObject.options;
        options.setMemo_key(mPublicKeyType);
        Operations.account_update_operation account_update_operation =
                BitsharesWalletWraper.getInstance().getAccountUpdateOperation(
                        ObjectId.create_from_string("1.3.0"),
                        105,
                        mAccountObject.id,
                        authority,
                        options,
                        mPublicKeyType);
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransactionByENotes(
                            cardManager,
                            mCard,
                            mAccountObject,
                            account_update_operation,
                            ID_UPDATE_ACCOUNT_OPERATION,
                            reply.result
                    );
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mUpdateAccountCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });

        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void checkPasswordFormat(String password, String repeatPass) {
        if (TextUtils.isEmpty(password) || !password.matches(passwordRegPattern)) {
            //密码为空或者不符合规则
            mSetCloudPassLlError.setVisibility(View.VISIBLE);
            mSetCloudPassErrorText.setText(getResources().getString(R.string.create_account_password_error));
            mPasswordChecker.setVisibility(View.GONE);
            setConfirmButtonEnable(false);
        } else {
            //符合规则
            mPasswordChecker.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(repeatPass) && isPasswordMatch(password, repeatPass)) {
                //两次输入密码一致
                mSetCloudPassLlError.setVisibility(View.GONE);
                mPasswordConfirmChecker.setVisibility(View.VISIBLE);
                mPublicKeyType = getPublicKeyFromPassword(password);
                setConfirmButtonEnable(true);
            } else {
                //两次输入密码不一致
                mPasswordConfirmChecker.setVisibility(View.GONE);
                mSetCloudPassLlError.setVisibility(View.VISIBLE);
                mSetCloudPassErrorText.setText(getResources().getString(R.string.create_account_password_confirm_error));
                setConfirmButtonEnable(false);
            }
        }
    }

    private boolean isPasswordMatch(String password, String repeatPass) {
        return password.equals(repeatPass);
    }

    private void setConfirmButtonEnable(boolean enabled) {
        mSetCloudPassButton.setEnabled(enabled);
    }

    private Types.public_key_type getPublicKeyFromPassword(String password) {
        PrivateKey privateActiveKey = PrivateKey.from_seed(mName + "active" + password);
        return new Types.public_key_type(privateActiveKey.get_public_key(true), true);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
