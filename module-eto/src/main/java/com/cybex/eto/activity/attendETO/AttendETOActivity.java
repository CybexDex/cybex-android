package com.cybex.eto.activity.attendETO;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.eto.R;
import com.cybex.eto.base.EtoBaseActivity;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.http.entity.EtoProject;
import com.cybex.provider.http.entity.EtoUserCurrentStatus;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ETO_ATTEND_ETO;
import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;

public class AttendETOActivity extends EtoBaseActivity implements AttendETOView,
        SoftKeyBoardListener.OnSoftKeyBoardChangeListener {

    @Inject
    AttendETOPresenter<AttendETOView> mAttendETOPresenter;
    private EtoProject mEtoProject;
    private String mUserName;
    private AssetObject mBaseToken;
    private WebSocketService mWebSocketService;
    private AccountObject mFromAccountObject;
    private AccountObject mToAccountObject;
    private Operations.transfer_operation mTransferOperationFee;
    private FeeAmountObject mCybFeeAmountObject;
    private FeeAmountObject mCurrAssetFeeAmountObject;
    private AccountBalanceObject mCybAccountBalanceObject;

    private boolean mIsCybEnough;//cyb余额是否足够
    private boolean mIsBalanceEnough;//选择币种余额是否足够
    private double mRemainingAmount;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mUserName);
            mBaseToken = mWebSocketService.getAssetObjectBySymbol(mEtoProject.getBase_token());
            mFromAccountObject = fullAccountObject == null ? null : fullAccountObject.account;
            if (fullAccountObject != null) {
                showAvailableAmount(fullAccountObject);
                checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false, fullAccountObject.account);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    Toolbar mToolbar;
    TextView mToolbarTitleTv;
    TextView mPersonalCapTv;
    TextView mSubscribeUnitTv;
    TextView mRemainingTv;
    TextView mMinSubscriptionTv;
    TextView mSubscribeTv;
    EditText mQuantityEt;
    TextView mAvailableTv;
    LinearLayout mErrorLinearLayout;
    TextView mErrorTv;
    TextView mFeeTv;
    Button mJoinETOButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_eto);
        etoActivityComponent().inject(this);
        EventBus.getDefault().register(this);
        mAttendETOPresenter.attachView(this);
        SoftKeyBoardListener.setListener(this, this);
        mEtoProject = (EtoProject) getIntent().getSerializableExtra(INTENT_PARAM_ETO_ATTEND_ETO);
        mUserName = mAttendETOPresenter.getUserName(this);
        initViews();
        setListener();
        setSupportActionBar(mToolbar);
        showDataFromEtoProject(mEtoProject);
        mAttendETOPresenter.getUserCurrent(mUserName, mEtoProject.getId());
        mQuantityEt.setFilters(new InputFilter[]{mQuantityFilter});
        getReceiveAccountObject(mEtoProject);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void refreshUserSubscribedToken(EtoUserCurrentStatus etoUserCurrentStatus) {
        showSubscribedAmount(etoUserCurrentStatus);
    }

    @Override
    public void onError() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {

        FullAccountObject fullAccountObject = event.getFullAccount();
        if (fullAccountObject == null) {
            return;
        }
        if (mFromAccountObject == null) {
            checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false, fullAccountObject.account);
        }
        mFromAccountObject = fullAccountObject.account;

        showAvailableAmount(fullAccountObject);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event) {
        mToAccountObject = event.getAccountObject();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadTransferFee(Event.LoadTransferFee event) {
        FeeAmountObject fee = event.getFee();
        if (fee.asset_id.equals(ASSET_ID_CYB)) {
            mCybFeeAmountObject = fee;
            if (fee.asset_id.equals(mBaseToken.id.toString())) {
                //只有当CYB不足时才会扣除当前币的手续费 而当前选择币种为CYB时 默认CYB不足
                mIsCybEnough = false;
                mCurrAssetFeeAmountObject = fee;
                mFeeTv.setText(String.format("%s %s",
                        AssetUtil.formatNumberRounding(fee.amount / Math.pow(10, 5),
                                5),
                        AssetUtil.parseSymbol(ASSET_SYMBOL_CYB)));
            } else {
                if (mCybAccountBalanceObject == null || mCybAccountBalanceObject.balance < fee.amount) {
                    mIsCybEnough = false;
                    checkIsLockAndLoadTransferFee(mBaseToken.id.toString(), event.isToTransfer(), mFromAccountObject);
                } else {
                    mIsCybEnough = true;
                    mFeeTv.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(fee.amount / Math.pow(10, 5),
                                    5),
                            AssetUtil.parseSymbol(ASSET_SYMBOL_CYB)));
                }
            }
        } else {
            mCurrAssetFeeAmountObject = fee;
            mFeeTv.setText(String.format("%s %s",
                    AssetUtil.formatNumberRounding(fee.amount / Math.pow(10, mBaseToken.precision),
                            mBaseToken.precision),
                    AssetUtil.parseSymbol(mBaseToken.symbol)));
        }

        if (event.isToTransfer() && mIsBalanceEnough) {
            showTransferConfirmationDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransfer(Event.Transfer event) {
        hideLoadDialog();
        if (event.isSuccess()) {
            ToastMessage.showNotEnableDepositToastMessage(
                    AttendETOActivity.this,
                    getResources().getString(R.string.toast_message_transfer_success),
                    R.drawable.ic_check_circle_green);
            /**
             * fix bug:CYM-505
             * 转账成功和失败清除数据
             */
            clearTransferData();
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_transfer_failed), R.drawable.ic_error_16px);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mQuantityEt.isFocused()) {
                manager.hideSoftInputFromWindow(mQuantityEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mQuantityEt.clearFocus();
            }
        }
        return false;
    }

    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitleTv = findViewById(R.id.attend_eto_tool_bar_title);
        mPersonalCapTv = findViewById(R.id.attend_eto_personal_cap_tv);
        mSubscribeUnitTv = findViewById(R.id.attend_eto_subscription_unit_tv);
        mRemainingTv = findViewById(R.id.attend_eto_remaining_tv);
        mMinSubscriptionTv = findViewById(R.id.attend_eto_min_subscription_tv);
        mSubscribeTv = findViewById(R.id.attend_eto_subscribe_tv);
        mQuantityEt = findViewById(R.id.attend_eto_quantity_et);
        mAvailableTv = findViewById(R.id.attend_eto_available_tv);
        mErrorLinearLayout = findViewById(R.id.attend_eto_error_layout);
        mErrorTv = findViewById(R.id.attend_eto_error_tv);
        mFeeTv = findViewById(R.id.attend_eto_final_fee);
        mJoinETOButton = findViewById(R.id.attend_eto_button);
    }

    private void setListener() {
        mQuantityEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean isFocused) {
                if (isFocused) {
                    return;
                }
                String amountStr = mQuantityEt.getText().toString().trim();
                if (TextUtils.isEmpty(amountStr) || amountStr.equals(".")) {
                    resetTransferButtonState();
                    return;
                }

                mQuantityEt.setText(String.format(String.format(Locale.US, "%%.%df",
                        mEtoProject.getBase_accuracy()), Double.parseDouble(amountStr)));
                checkBalanceEnough(mQuantityEt.getText().toString().trim());
            }
        });

        mJoinETOButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIsLockAndTransfer();
            }
        });
    }

    private void showDataFromEtoProject(EtoProject etoProject) {
        mToolbarTitleTv.setText(String.format("%s %s", etoProject.getName(), etoProject.getBase_token_name()));
        mPersonalCapTv.setText(String.format(Locale.US, "%s %s", etoProject.getBase_max_quote(), etoProject.getBase_token_name()));
        mSubscribeUnitTv.setText(String.format("%." + etoProject.getBase_accuracy() + "f %s", 1 / Math.pow(10, etoProject.getBase_accuracy()), etoProject.getBase_token_name()));
        mMinSubscriptionTv.setText(String.format(Locale.US, "%s %s", etoProject.getBase_min_quote(), etoProject.getBase_token_name()));
    }

    private void showSubscribedAmount(EtoUserCurrentStatus etoUserCurrentStatus) {
        BigDecimal baseMax = new BigDecimal(String.valueOf(mEtoProject.getBase_max_quote()));
        BigDecimal currentToken = new BigDecimal(String.valueOf(etoUserCurrentStatus.getCurrent_base_token_count()));
        mSubscribeTv.setText(String.format(Locale.US, "%s %s", etoUserCurrentStatus.getCurrent_base_token_count(), mEtoProject.getBase_token_name()));
        mRemainingTv.setText(String.format("%s %s", baseMax.subtract(currentToken).toString(), mEtoProject.getBase_token_name()));
        mRemainingAmount = baseMax.subtract(currentToken).doubleValue();
    }

    private void showAvailableAmount(FullAccountObject fullAccountObject) {
        List<AccountBalanceObject> accountBalanceObjectList = fullAccountObject.balances;
        for (AccountBalanceObject accountBalanceObject : accountBalanceObjectList) {
            AssetObject assetObject = mWebSocketService.getAssetObject(accountBalanceObject.asset_type.toString());
            if (assetObject.symbol.equals(ASSET_SYMBOL_CYB)) {
                mCybAccountBalanceObject = accountBalanceObject;
            }
            if (assetObject.symbol.equals(mEtoProject.getBase_token())) {
                mAvailableTv.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                        AssetUtil.formatNumberRounding(accountBalanceObject.balance /
                                Math.pow(10, assetObject.precision), assetObject.precision),
                        AssetUtil.parseSymbol(assetObject.symbol)));
                break;
            }
        }

    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费
     */
    private void checkIsLockAndLoadTransferFee(final String feeAssetId, final boolean isLoadFeeToTransfer, AccountObject accountObject) {
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), accountObject,
                    accountObject.name, new UnlockDialog.UnLockDialogClickListener() {
                        @Override
                        public void onUnLocked(String password) {
                            loadTransferFee(feeAssetId, isLoadFeeToTransfer);
                        }
                    });
        } else {
            loadTransferFee(feeAssetId, isLoadFeeToTransfer);
        }
    }

    /**
     * 加载转账手续费
     *
     * @param feeAssetId
     * @param isLoadFeeToTransfer 是否加载完手续费之后 自动转账
     */
    private void loadTransferFee(String feeAssetId, final boolean isLoadFeeToTransfer) {
        if (mFromAccountObject == null) {
            return;
        }
        mTransferOperationFee = BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mFromAccountObject.id,
                ObjectId.<AssetObject>create_from_string(ASSET_ID_CYB),
                0,
                ObjectId.<AssetObject>create_from_string(feeAssetId),
                0,
                null,
                mFromAccountObject.options.memo_key,
                mFromAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(feeAssetId, ID_TRANSER_OPERATION, mTransferOperationFee, new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FeeAmountObject>>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<List<FeeAmountObject>> reply) {
                    EventBus.getDefault().post(new Event.LoadTransferFee(reply.result.get(0), isLoadFeeToTransfer));
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转账确认
     */
    private void showTransferConfirmationDialog() {
        CybexDialog.showAttendETOConfirmDialog(this, getResources().getString(R.string.attend_eto_dialog_title), String.format(getResources().getString(R.string.attend_eto_dialog_message), mEtoProject.getName()),
                String.format("%s %s", mQuantityEt.getText().toString().trim(),
                        AssetUtil.parseSymbol(mBaseToken.symbol)), mFeeTv.getText().toString().trim(),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        CybexDialog.showAttendEtoLoadingDialog(AttendETOActivity.this, new CybexDialog.ConfirmationDialogClickListener() {
                            @Override
                            public void onClick(Dialog dialog) {
                                toTransfer();
                            }
                        });
                    }
                });
    }

    /**
     * 转账
     */
    private void toTransfer() {
        if (mFromAccountObject == null || mToAccountObject == null) {
            return;
        }
        showLoadDialog();
        final Operations.base_operation transferOperation = BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mToAccountObject.id,
                mBaseToken.id,
                mIsCybEnough ? mCybFeeAmountObject.amount : mCurrAssetFeeAmountObject.amount,
                ObjectId.<AssetObject>create_from_string(mIsCybEnough ? mCybFeeAmountObject.asset_id : mCurrAssetFeeAmountObject.asset_id),
                (long) (Double.parseDouble(mQuantityEt.getText().toString().trim()) * Math.pow(10, mBaseToken.precision)),
                null,
                mFromAccountObject.options.memo_key,
                mToAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFromAccountObject, transferOperation, ID_TRANSER_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mTransferCallback);
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

    /**
     * 转账callback
     */
    private WebSocketClient.MessageCallback mTransferCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<String>>() {

        @Override
        public void onMessage(WebSocketClient.Reply<String> reply) {
            EventBus.getDefault().post(new Event.Transfer(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.Transfer(false));
        }
    };

    /**
     * 获取转账地址
     */
    private void getReceiveAccountObject(EtoProject etoProject) {
        try {
            BitsharesWalletWraper.getInstance().get_account_object(etoProject.getReceive_address(), new WebSocketClient.MessageCallback<WebSocketClient.Reply<AccountObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    EventBus.getDefault().post(new Event.LoadAccountObject(accountObject));
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void clearTransferData() {
        mIsCybEnough = false;
        mIsBalanceEnough = false;
        mTransferOperationFee = null;
        mToAccountObject = null;
        mQuantityEt.setText("");
        /**
         * fix bug:CYM-555
         * 重置按钮状态
         */
        resetTransferButtonState();
    }


    /**
     * reset 转账状态
     */
    private void resetTransferButtonState() {
        /**
         * fix bug:CYM-507
         * 转账金额必须大于0
         */
        try {
            mJoinETOButton.setEnabled(
                    mToAccountObject != null &&
                            Double.parseDouble(mQuantityEt.getText().toString()) > 0 &&
                            mIsBalanceEnough);
        } catch (Exception e) {
            e.printStackTrace();
            mJoinETOButton.setEnabled(false);
        }
    }

    /**
     * 检查资产是否足够
     *
     * @param amountStr
     */
    private void checkBalanceEnough(String amountStr) {
        if (TextUtils.isEmpty(amountStr) || amountStr.endsWith(".")) {
            return;
        }
        if (!mIsCybEnough && mCurrAssetFeeAmountObject == null) {
            mJoinETOButton.setEnabled(true);
        }
        double fee = mIsCybEnough || mCurrAssetFeeAmountObject == null ? 0 : mCurrAssetFeeAmountObject.amount /
                Math.pow(10, mBaseToken.precision);
        double balanceAmount = Double.parseDouble(mAvailableTv.getText().toString().trim().split(" ")[1]);
        BigDecimal balance = new BigDecimal(Double.toString(balanceAmount)).subtract(new BigDecimal(amountStr)).subtract(new BigDecimal(Double.toString(fee)));
        if (balance.doubleValue() < 0) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.text_not_enough));
            resetTransferButtonState();
        } else {
            checkQuantityEtStatus(amountStr);
        }
    }

    /**
     * 错误检测
     */
    private void checkQuantityEtStatus(String amountStr) {
        if (TextUtils.isEmpty(amountStr) || amountStr.endsWith(".")) {
            return;
        }
        BigDecimal amount = new BigDecimal(amountStr);
        if (amount.floatValue() > mEtoProject.getBase_max_quote()) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_beyond_personal_cap));
        } else if (amount.floatValue() < (double) mEtoProject.getBase_min_quote()) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_must_beyond_minimum_purchasing_unit));
        } else if (amount.doubleValue() > mRemainingAmount) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_insufficient_remaining_quota));
        } else {
            mErrorLinearLayout.setVisibility(View.GONE);
            mIsBalanceEnough = true;
        }
        resetTransferButtonState();
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费 -> 转账
     */
    private void checkIsLockAndTransfer() {
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject, mFromAccountObject.name, new UnlockDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    showTransferConfirmationDialog();
                }
            });
        } else {
            showTransferConfirmationDialog();
        }
    }


    /**
     * 金额过滤器
     */
    private InputFilter mQuantityFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (dest.length() == 0 && source.equals(".")) {
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == mEtoProject.getBase_accuracy()) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void keyBoardShow(int height) {

    }

    @Override
    public void keyBoardHide(int height) {
        if (mQuantityEt.isFocused()) {
            mQuantityEt.clearFocus();
        }
    }
}
