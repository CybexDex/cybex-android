package com.cybex.eto.activity.attendETO;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ETO_ATTEND_ETO;
import static com.cybex.provider.graphene.chain.Operations.ID_PARTICIPATE_EXCHANGE_OPERATION;
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
    private Operations.exchange_participate_operation mParticipateOperation;
    private FeeAmountObject mCybFeeAmountObject;
    private FeeAmountObject mCurrAssetFeeAmountObject;
    private AccountBalanceObject mCybAccountBalanceObject;
    private String mValue;

    private boolean mIsCybEnough;//cyb余额是否足够
    private boolean mIsBalanceEnough;//选择币种余额是否足够
    private double mRemainingAmount;
    private boolean mIsUseBaseToken = true;
    private double mRate;
    private double mUnit;

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
                loadTransferFee(ASSET_ID_CYB, false);
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
    TextView mEtoTransferDescriptionTv;
    TextView mEtoCalculateFormatTv;
    TextView mEtoInputUnitTv;
    TextView mEtoTotalRemainingTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_eto);
        etoActivityComponent().inject(this);
        mAttendETOPresenter.attachView(this);
        SoftKeyBoardListener.setListener(this, this);
        mEtoProject = (EtoProject) getIntent().getSerializableExtra(INTENT_PARAM_ETO_ATTEND_ETO);
        mUserName = mAttendETOPresenter.getUserName(this);
        mIsUseBaseToken = mEtoProject.getBase_token().equals(mEtoProject.getUser_buy_token());
        mRate = AssetUtil.divide(String.valueOf(mEtoProject.getBase_token_count()), String.valueOf(mEtoProject.getQuote_token_count()));
        initViews();
        setListener();
        setSupportActionBar(mToolbar);
        showDataFromEtoProject(mEtoProject);
        mAttendETOPresenter.getUserCurrent(mUserName, mEtoProject.getId());
        mQuantityEt.setFilters(new InputFilter[]{mQuantityFilter});
        mEtoCalculateFormatTv.setText(String.format("= 0 %s", mIsUseBaseToken ? AssetUtil.parseSymbol(mEtoProject.getUser_buy_token()) : mEtoProject.getBase_token_name()));

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
        mAttendETOPresenter.detachView();
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

    @Override
    public void onNoUserError(String message) {
        ToastMessage.showNotEnableDepositToastMessage(this, message, R.drawable.ic_error_16px);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {

        FullAccountObject fullAccountObject = event.getFullAccount();
        if (fullAccountObject == null) {
            return;
        }
        mFromAccountObject = fullAccountObject.account;

        showAvailableAmount(fullAccountObject);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshProjectStatus(Event.OnRefreshEtoProject refreshEtoProject) {
        EtoProject etoProject = refreshEtoProject.getEtoProject();
        if (!etoProject.getId().equals(mEtoProject.getId())) {
            return;
        }
        mEtoProject = etoProject;
        showDataFromEtoProject(mEtoProject);
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
                    loadTransferFee(mBaseToken.id.toString(), event.isToTransfer());
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
            /**
             * fix bug:CYM-505
             * 转账成功和失败清除数据
             */
            CybexDialog.showAttendEtoLoadingDialog(this, new CybexDialog.ConfirmationDialogClickListener() {
                @Override
                public void onClick(Dialog dialog) {
                    ToastMessage.showNotEnableDepositToastMessage(AttendETOActivity.this, getResources().getString(R.string.attend_eto_toast_message_success), R.drawable.ic_check_circle_green);
                    clearTransferData();
                }
            });
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.attend_eto_toast_message_failed), R.drawable.ic_error_16px);
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
        mEtoTransferDescriptionTv = findViewById(R.id.attend_eto_transfer_description_tv);
        mEtoCalculateFormatTv = findViewById(R.id.attend_eto_calculate_format_tv);
        mEtoInputUnitTv = findViewById(R.id.attend_eto_edit_text_unit);
        mEtoTotalRemainingTv = findViewById(R.id.attend_eto_current_remain_tv);
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

//                mQuantityEt.setText(String.format(String.format(Locale.US, "%%.%df",
//                        mEtoProject.getBase_accuracy()), Double.parseDouble(amountStr)));
                checkBalanceEnough(mQuantityEt.getText().toString().trim());
            }
        });

        mQuantityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String message = s.toString().trim();
                if (message.length() == 0) {
                    mEtoCalculateFormatTv.setText(String.format("= 0 %s", mIsUseBaseToken ? AssetUtil.parseSymbol(mEtoProject.getUser_buy_token()) : mEtoProject.getBase_token_name()));
                } else {
                    String value = mIsUseBaseToken ? String.valueOf(AssetUtil.divide(message, String.valueOf(mRate))) : String.valueOf(AssetUtil.multiply(message, String.valueOf(mRate)));
                    mEtoCalculateFormatTv.setText(String.format(Locale.US, "=%.4f %s", Double.parseDouble(value), mIsUseBaseToken ? AssetUtil.parseSymbol(mEtoProject.getUser_buy_token()) : mEtoProject.getBase_token_name()));
                    mValue = value;
                }

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
        double personalCap = AssetUtil.divide(etoProject.getBase_max_quote(), mRate);
        double minCap = AssetUtil.divide(etoProject.getBase_min_quote(), mRate);
        mToolbarTitleTv.setText(String.format("%s %s", etoProject.getName(), "ETO"));
        mPersonalCapTv.setText(String.format(Locale.US, "%s %s", mIsUseBaseToken ? String.valueOf(etoProject.getBase_max_quote()) : String.valueOf(personalCap), AssetUtil.parseSymbol(etoProject.getUser_buy_token())));
        mSubscribeUnitTv.setText(String.format("%." + (mIsUseBaseToken ? etoProject.getBase_accuracy() : 1) + "f %s", mIsUseBaseToken ? 1 / Math.pow(10, etoProject.getBase_accuracy()) : etoProject.getQuote_accuracy(), AssetUtil.parseSymbol(etoProject.getUser_buy_token())));
        mMinSubscriptionTv.setText(String.format(Locale.US, "%s %s", mIsUseBaseToken ? String.valueOf(etoProject.getBase_min_quote()) : String.valueOf(minCap), AssetUtil.parseSymbol(etoProject.getUser_buy_token())));
        if (Locale.getDefault().getLanguage().equals("zh")) {
            mEtoTransferDescriptionTv.setText(etoProject.getAdds_buy_desc());
        } else {
            mEtoTransferDescriptionTv.setText(etoProject.getAdds_buy_desc__lang_en());
        }
        mEtoInputUnitTv.setText(AssetUtil.parseSymbol(etoProject.getUser_buy_token()));
        mEtoTotalRemainingTv.setText(String.format(Locale.US, "%s %s", mIsUseBaseToken ? String.valueOf(etoProject.getCurrent_remain_quota_count() * mRate) : String.valueOf(etoProject.getCurrent_remain_quota_count()), AssetUtil.parseSymbol(mEtoProject.getUser_buy_token()) ));
        mUnit = mIsUseBaseToken ? 1 / Math.pow(10, etoProject.getBase_accuracy()) : etoProject.getQuote_accuracy();
    }

    private void showSubscribedAmount(EtoUserCurrentStatus etoUserCurrentStatus) {
        double baseMax = mIsUseBaseToken ? mEtoProject.getBase_max_quote() : AssetUtil.divide(mEtoProject.getBase_max_quote(), mRate);
        double currentToken = mIsUseBaseToken ? etoUserCurrentStatus.getCurrent_base_token_count() : AssetUtil.divide(etoUserCurrentStatus.getCurrent_base_token_count(), mRate);
        mSubscribeTv.setText(String.format(Locale.US, "%s %s", mIsUseBaseToken ? etoUserCurrentStatus.getCurrent_base_token_count() : etoUserCurrentStatus.getCurrent_base_token_count() / mRate, AssetUtil.parseSymbol(mEtoProject.getUser_buy_token())));
        mRemainingTv.setText(String.format("%s %s", String.valueOf(AssetUtil.subtract(baseMax, currentToken)), AssetUtil.parseSymbol(mEtoProject.getUser_buy_token())));
        mRemainingAmount = AssetUtil.subtract(baseMax, currentToken);
    }

    private void showAvailableAmount(FullAccountObject fullAccountObject) {
        List<AccountBalanceObject> accountBalanceObjectList = fullAccountObject.balances;
        for (AccountBalanceObject accountBalanceObject : accountBalanceObjectList) {
            AssetObject assetObject = mWebSocketService.getAssetObject(accountBalanceObject.asset_type.toString());
            if (assetObject != null) {
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
        if (mAvailableTv.getText().toString().isEmpty()) {
            mAvailableTv.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                    0,
                    AssetUtil.parseSymbol(mEtoProject.getBase_token_name())));
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
        mParticipateOperation = BitsharesWalletWraper.getInstance().getParticipateOperatin(
                0,
                ObjectId.<AssetObject>create_from_string(ASSET_ID_CYB),
                0,
                ObjectId.<AssetObject>create_from_string(feeAssetId),
                ObjectId.create_from_string(mEtoProject.getId()),
                mFromAccountObject.id);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(feeAssetId, ID_PARTICIPATE_EXCHANGE_OPERATION, mParticipateOperation, new MessageCallback<Reply<List<FeeAmountObject>>>() {
                @Override
                public void onMessage(Reply<List<FeeAmountObject>> reply) {
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
                String.format(Locale.US, "%.4f %s", mIsUseBaseToken ? Double.parseDouble(mQuantityEt.getText().toString().trim()) : Double.parseDouble(mValue),
                        AssetUtil.parseSymbol(mBaseToken.symbol)), mFeeTv.getText().toString().trim(),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        toTransfer();
                    }
                });
    }

    /**
     * 转账
     */
    private void toTransfer() {
        if (mFromAccountObject == null) {
            return;
        }
        showLoadDialog();

        final Operations.exchange_participate_operation participateOperation = BitsharesWalletWraper.getInstance().getParticipateOperatin(
                mIsCybEnough ? mCybFeeAmountObject.amount : mCurrAssetFeeAmountObject.amount,
                ObjectId.<AssetObject>create_from_string(mIsCybEnough ? mCybFeeAmountObject.asset_id : mCurrAssetFeeAmountObject.asset_id),
                mIsUseBaseToken ? (long) (Double.parseDouble(mQuantityEt.getText().toString().trim()) * Math.pow(10, mBaseToken.precision)) : (long) ((AssetUtil.multiply(mQuantityEt.getText().toString().trim(), String.valueOf(mRate))) * Math.pow(10, mBaseToken.precision)),
                mBaseToken.id,
                ObjectId.create_from_string(mEtoProject.getId()),
                mFromAccountObject.id);

        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFromAccountObject, participateOperation, ID_PARTICIPATE_EXCHANGE_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mTransferCallback);
                        showLoadDialog(true);
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
    private MessageCallback<Reply<String>> mTransferCallback = new MessageCallback<Reply<String>>() {

        @Override
        public void onMessage(Reply<String> reply) {
            EventBus.getDefault().post(new Event.Transfer(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.Transfer(false));
        }
    };

    private void clearTransferData() {
        mIsCybEnough = false;
        mIsBalanceEnough = false;
        mQuantityEt.setText("");
        /**
         * fix bug:CYM-555
         * 重置按钮状态
         */
        resetTransferButtonState();
        recreate();
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
        // mValue is the approximately (base/quote) value calculated from input amount
        BigDecimal balance = new BigDecimal(Double.toString(balanceAmount)).subtract(new BigDecimal(mValue)).subtract(new BigDecimal(Double.toString(fee)));
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
        if (amount.doubleValue() > (mIsUseBaseToken ? mEtoProject.getBase_max_quote() : AssetUtil.divide(mEtoProject.getBase_max_quote(), mRate))) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_beyond_personal_cap));
        } else if (amount.doubleValue() < (mIsUseBaseToken ? ((double) mEtoProject.getBase_min_quote()) : ((double) AssetUtil.divide(mEtoProject.getBase_min_quote(), mRate)))){
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_must_beyond_minimum_purchasing_unit));
        } else if (amount.doubleValue() > mRemainingAmount) {
            mIsBalanceEnough = false;
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_insufficient_remaining_quota));
        } else if (amount.doubleValue() % mUnit != 0) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTv.setText(getResources().getString(R.string.attend_eto_must_purchase_an_integer_multiple));
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
