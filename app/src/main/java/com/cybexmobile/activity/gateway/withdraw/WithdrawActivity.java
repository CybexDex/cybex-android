package com.cybexmobile.activity.gateway.withdraw;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.ApolloQueryWatcher;
import com.apollographql.apollo.GetWithdrawInfo;
import com.apollographql.apollo.VerifyAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.fragment.WithdrawinfoObject;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.cybex.basemodule.constant.Constant;
import com.cybex.provider.SettingConfig;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.utils.SpUtil;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.records.DepositWithdrawRecordsActivity;
import com.cybexmobile.activity.address.AddTransferAccountActivity;
import com.cybex.provider.apollo.ApolloClientApi;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.activity.setting.enotes.SetCloudPasswordActivity;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.dialog.CommonSelectDialog;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.FullAccountObjectReply;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.PrivateKey;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.graphene.chain.Types;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybexmobile.shake.AntiShake;
import com.cybexmobile.utils.DecimalDigitsInputFilter;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.math.NumberUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_TAG;
import static com.cybex.basemodule.constant.Constant.PREF_SERVER;
import static com.cybex.basemodule.constant.Constant.SERVER_OFFICIAL;
import static com.cybex.basemodule.constant.Constant.PREF_ADDRESS_TO_PUB_MAP;
import static com.cybex.basemodule.constant.Constant.PREF_SERVER;
import static com.cybex.basemodule.constant.Constant.SERVER_OFFICIAL;
import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ADDRESS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_ID;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_MEMO;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_NAME;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_SELECTED_ITEM;

public class WithdrawActivity extends BaseActivity {

    public Unbinder mUnbinder;
    private String mAssetName;
    private String mAssetNameForGatewayRequest;
    private String mAssetId;
    private boolean mIsEnabled;
    private boolean mIsTag;
    private String mEnMsg;
    private String mCnMsg;
    private String mUserName;
    private double mAvailableAmount;
    private double mMinValue;
    private double mGatewayFee;
    private Integer mAssetPrecision;
    private String mToAccountId;
    private FullAccountObject mToAccountObject;
    private AccountObject mAccountObject;
    private FullAccountObject mFullAccountObject;
    private AssetObject mAssetObject;
    private Handler mHandler = new Handler();
    private WebSocketService mWebSocketService;
    private Operations.transfer_operation mTransferOperation;
    private SignedTransaction mSignedTransaction;
    private DynamicGlobalPropertyObject mDynamicGlobalPropertyObject;
    private List<Address> mAddresses;
    private FeeAmountObject mFeeAmountObject;
    private String mMemo;
    private String mWithdrawPrefix;
    private String mSignature;

    //private boolean

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.withdraw_toolbar_text_view)
    TextView mToolbarTextView;
    @BindView(R.id.withdraw_available_amount)
    TextView mAvailableAmountTextView;
    @BindView(R.id.withdraw_address_tv)
    TextView mWithdrawAddressTv;
    @BindView(R.id.withdraw_withdrawal_address)
    EditText mWithdrawAddress;
    @BindView(R.id.withdraw_loading_progress_bar)
    ProgressBar mPbLoading;
    @BindView(R.id.withdraw_iv_address_check)
    ImageView mIvAddressCheck;
    @BindView(R.id.withdraw_tv_select_address)
    TextView mTvWithdrawSelectAddress;
    @BindView(R.id.withdraw_amount)
    EditText mWithdrawAmountEditText;
    @BindView(R.id.withdraw_memo_eos_layout)
    LinearLayout mWithdrawMemoEosLayout;
    @BindView(R.id.withdraw_eos_xrp_tag_memo_title)
    TextView mWithdrawEosXrpTagMemoTitleTv;
    @BindView(R.id.withdraw_memo_eos_et)
    EditText mWithdrawMemoEosEditText;
    @BindView(R.id.withdraw_error)
    LinearLayout mErrorLinearLayout;
    @BindView(R.id.withdraw_error_text)
    TextView mErrorTextView;
    @BindView(R.id.withdraw_gateway_fee)
    TextView mGateWayFeeTextView;
    @BindView(R.id.withdraw_transfer_fee)
    TextView mTransferFeeTextView;
    @BindView(R.id.withdraw_message)
    TextView mWithdrawMessage;
    @BindView(R.id.withdraw_receive_amount)
    TextView mReceiveAmountTextView;
    @BindView(R.id.withdraw_button)
    Button mWithdrawButton;
    @BindView(R.id.withdraw_all_button)
    TextView mWithdrawAllButton;

    private boolean mIsAddressInvalidate;
    private boolean mIsFeeLoaded;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = sharedPreferences.getString("name", null);
        Intent intent = getIntent();
        mAssetNameForGatewayRequest = intent.getStringExtra("assetNameForGatewayRequest");
        mAssetName = intent.getStringExtra("assetName");
        mAssetId = intent.getStringExtra("assetId");
        mIsEnabled = intent.getBooleanExtra("isEnabled", true);
        mIsTag = intent.getBooleanExtra("tag", false);
        mEnMsg = intent.getStringExtra("enMsg");
        mCnMsg = intent.getStringExtra("cnMsg");
        mAvailableAmount = intent.getDoubleExtra("availableAmount", 0);
        mAssetObject = (AssetObject) intent.getSerializableExtra("assetObject");
        if (SettingConfig.getInstance().isGateway2()) {
            mGatewayFee = Double.parseDouble(intent.getStringExtra("withdrawFee"));
            mMinValue = Double.parseDouble(intent.getStringExtra("minWithdraw"));
            mAssetPrecision = !intent.getStringExtra("precision").isEmpty() ? NumberUtils.createInteger(intent.getStringExtra("precision")) : null;
            mToAccountId = intent.getStringExtra("gatewayAccount");
            mWithdrawPrefix = intent.getStringExtra("withdrawPrefix");
        }
        mToolbarTextView.setText(String.format("%s " + getResources().getString(R.string.gate_way_withdraw), mAssetName));
        if (mIsTag) {
            mWithdrawMemoEosLayout.setVisibility(View.VISIBLE);
            mWithdrawEosXrpTagMemoTitleTv.setText(getResources().getString(R.string.withdraw_xrp_tag));
        }
        setAvailableAmount(mAvailableAmount, mAssetName);
        requestDetailMessage();
        setMinWithdrawAmountAndGateWayFee();
        setKeyboardListener();
        loadAddress();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadAddress();
    }

    @OnTouch(R.id.withdraw_scrollview)
    public boolean onTouchEvent(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mWithdrawAddress.isFocused()) {
                manager.hideSoftInputFromWindow(mWithdrawAddress.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mWithdrawAddress.clearFocus();
            }
            if (mWithdrawAmountEditText.isFocused()) {
                manager.hideSoftInputFromWindow(mWithdrawAmountEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mWithdrawAmountEditText.clearFocus();
            }
            if (mWithdrawMemoEosEditText.isFocused()) {
                manager.hideSoftInputFromWindow(mWithdrawMemoEosEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mWithdrawMemoEosEditText.clearFocus();
            }
        }
        return false;
    }

    private UnlockDialog.UnLockDialogClickListener mUnLockDialogListener = new UnlockDialog.UnLockDialogClickListener() {
        @Override
        public void onUnLocked(String password) {
            displayFee();
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mFullAccountObject = mWebSocketService.getFullAccount(mUserName);
            mAccountObject = mFullAccountObject.account;
            if (mIsEnabled) {
                if (isLoginFromENotes() && mAccountObject.active.key_auths.size() < 2) {
                    CybexDialog.showLimitOrderCancelConfirmationDialog(
                            WithdrawActivity.this,
                            getResources().getString(R.string.nfc_dialog_add_cloud_password_content),
                            getResources().getString(R.string.nfc_dialog_add_cloud_password_button),
                            new CybexDialog.ConfirmationDialogClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    Intent intent = new Intent(WithdrawActivity.this, SetCloudPasswordActivity.class);
                                    startActivityForResult(intent, Constant.REQUEST_CODE_UPDATE_ACCOUNT);
                                }
                            });
                } else {
                    displayFee();
                }
            } else {
                if (Locale.getDefault().getLanguage().equals("zh")) {
                    ToastMessage.showNotEnableDepositToastMessage((Activity) getApplicationContext(), mCnMsg, R.drawable.ic_error_16px);
                } else {
                    ToastMessage.showNotEnableDepositToastMessage((Activity) getApplicationContext(), mEnMsg, R.drawable.ic_error_16px);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @OnClick(R.id.withdraw_tv_select_address)
    public void onSelectAddressClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (mAddresses == null || mAddresses.size() == 0) {
            Intent intent = new Intent(this, AddTransferAccountActivity.class);
            intent.putExtra(INTENT_PARAM_CRYPTO_NAME, mAssetName);
            intent.putExtra(INTENT_PARAM_CRYPTO_ID, mAssetObject.id.toString());
            intent.putExtra(INTENT_PARAM_ADDRESS, mWithdrawAddress.getText().toString().trim());
            intent.putExtra("tag", mIsTag);
            if (mIsTag) {
                intent.putExtra(INTENT_PARAM_CRYPTO_TAG, mWithdrawMemoEosEditText.getText().toString().trim());
            }
            startActivity(intent);
            return;
        }
        CommonSelectDialog<Address> dialog = new CommonSelectDialog<Address>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ITEMS, (Serializable) mAddresses);
        bundle.putSerializable(INTENT_PARAM_SELECTED_ITEM, null);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), CommonSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(new CommonSelectDialog.OnAssetSelectedListener<Address>() {
            @Override
            public void onAssetSelected(Address address) {
                if (address == null) {
                    return;
                }
                mWithdrawAddress.setText(address.getAddress());
                if (mIsTag) {
                    if (address.getTag() != null && !address.getTag().isEmpty()) {
                        mWithdrawMemoEosEditText.setText(address.getTag());
                    }
                }
                if (!mWithdrawAddress.isFocused()) {
                    onWithdrawAddressFocusChanged(mWithdrawAddress, false);
                }
            }
        });
    }

    @OnClick(value = R.id.withdraw_all_button)
    public void onAllButtonClicked(View view) {
        mWithdrawAmountEditText.setText(String.valueOf(mAvailableAmount));
        if (mAvailableAmount < mMinValue) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_less_than_minimum));
        } else {
            displayFee();
        }
    }

    @OnFocusChange(R.id.withdraw_withdrawal_address)
    public void onWithdrawAddressFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        if (!TextUtils.isEmpty(mWithdrawAddress.getText().toString())) {
            mIvAddressCheck.setVisibility(View.GONE);
            mPbLoading.setVisibility(View.VISIBLE);
        }
        verifyAddress(mWithdrawAddress.getText().toString().trim());
    }

    @OnFocusChange(R.id.withdraw_amount)
    public void onWithdrawAmountFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            mIsFeeLoaded = false;
            return;
        }
        displayFee();
    }

    @OnFocusChange(R.id.withdraw_memo_eos_et)
    public void onWithdrawMemoEosEtFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        displayFee();
    }

    @OnTextChanged(value = R.id.withdraw_withdrawal_address, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onWithdrawAddressEditTextChanged(Editable editable) {
        if (editable.toString().length() == 0) {
            mIvAddressCheck.setVisibility(View.INVISIBLE);
            mErrorLinearLayout.setVisibility(View.GONE);
        }
        resetWithdrawBtnState();
    }

    @OnTextChanged(value = R.id.withdraw_amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onWithdrawAmountEditTextChanged(Editable editable) {
        if (!editable.toString().isEmpty() && editable.toString().startsWith(".")) {
            mWithdrawAmountEditText.setText("0" + editable.toString());
            mWithdrawAmountEditText.setSelection(editable.length() + 1);
        }
        resetWithdrawBtnState();
    }

    private void resetWithdrawBtnState() {
        if (TextUtils.isEmpty(mWithdrawAddress.getText().toString().trim())) {
            mWithdrawButton.setEnabled(false);
            return;
        }
        if (mIsAddressInvalidate) {
            mErrorLinearLayout.setVisibility(View.GONE);
        } else {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_invalid_address));
            mWithdrawButton.setEnabled(false);
            return;
        }
        if (!mIsFeeLoaded) {
            mWithdrawButton.setEnabled(false);
            return;
        }
        String amountStr = mWithdrawAmountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            mWithdrawButton.setEnabled(false);
            return;
        }
        double amount = Double.valueOf(amountStr);
        if (amount > mAvailableAmount) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_not_enough));
            mWithdrawButton.setEnabled(false);
            mReceiveAmountTextView.setText(getResources().getString(R.string.text_empty));
        } else if (amount < mMinValue) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_less_than_minimum));
            mWithdrawButton.setEnabled(false);
            mReceiveAmountTextView.setText(getResources().getString(R.string.text_empty));
        } else {
            mErrorLinearLayout.setVisibility(View.GONE);
            mWithdrawButton.setEnabled(true);
        }
    }

    @OnClick(value = R.id.withdraw_button)
    public void onWithdrawButtonClicked(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        CybexDialog.showConfirmationDialog(this, new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        if (isLoginFromENotes()) {
                            if (mAccountObject.active.key_auths.size() < 2) {
                                CybexDialog.showLimitOrderCancelConfirmationDialog(
                                        WithdrawActivity.this,
                                        getResources().getString(R.string.nfc_dialog_add_cloud_password_content),
                                        getResources().getString(R.string.nfc_dialog_add_cloud_password_button),
                                        new CybexDialog.ConfirmationDialogClickListener() {
                                            @Override
                                            public void onClick(Dialog dialog) {
                                                Intent intent = new Intent(WithdrawActivity.this, SetCloudPasswordActivity.class);
                                                startActivityForResult(intent, Constant.REQUEST_CODE_UPDATE_ACCOUNT);
                                            }
                                        });
                            } else {
                                CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                                    @Override
                                    public void onUnLocked(String password) {
                                        if (checkWithdrawAuthority(mAccountObject, password)) {
                                            toWithdraw();
                                        }

                                    }
                                });
                            }
                        } else {
                            if (BitsharesWalletWraper.getInstance().is_locked()) {
                                CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                                    @Override
                                    public void onUnLocked(String password) {
                                        if (checkWithdrawAuthority(mAccountObject, password)) {
                                            toWithdraw();
                                        }
                                    }
                                });
                            } else {
                                if (checkWithdrawAuthority(mAccountObject, BitsharesWalletWraper.getInstance().getPassword())) {
                                    toWithdraw();
                                }
                            }
                        }

                    }
                }, mWithdrawAddress.getText().toString().trim(),
                mWithdrawAmountEditText.getText().toString().trim() + " " + mAssetName,
                mGateWayFeeTextView.getText().toString(),
                mTransferFeeTextView.getText().toString(),
                mReceiveAmountTextView.getText().toString(),
                mWithdrawMemoEosEditText.getText().toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWithdraw(Event.Withdraw withdrawEvent) {
        if (withdrawEvent.isSuccess()) {
            mCompositeDisposable.add(DBManager.getDbProvider(this).checkWithdrawAddressExist(mUserName,
                    mWithdrawAddress.getText().toString().trim(), mAssetObject.id.toString(), Address.TYPE_WITHDRAW)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                ToastMessage.showNotEnableDepositToastMessage(
                                        WithdrawActivity.this,
                                        getResources().getString(R.string.toast_message_withdraw_sent),
                                        R.drawable.ic_check_circle_green);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 3500);

                            } else {
                                showAddAddressDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            ToastMessage.showNotEnableDepositToastMessage(
                                    WithdrawActivity.this,
                                    getResources().getString(R.string.toast_message_withdraw_sent),
                                    R.drawable.ic_check_circle_green);
                        }
                    }));
        }
    }

    private void toWithdraw() {
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    mDynamicGlobalPropertyObject = reply.result;
                    if (mFeeAmountObject.amount <= getBalance(mFullAccountObject, ASSET_ID_CYB)) {
                        mTransferOperation = getTransferOperation(mAccountObject, mToAccountObject, mAssetObject, mMemo, mWithdrawAmountEditText.getText().toString().trim(), mFeeAmountObject.asset_id, mFeeAmountObject.amount);
                    } else {
                        mTransferOperation = getTransferOperation(mAccountObject, mToAccountObject, mAssetObject, mMemo, getSubmitAmount(mFeeAmountObject), mFeeAmountObject.asset_id, mFeeAmountObject.amount);
                    }
                    mSignedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(mAccountObject, mTransferOperation, ID_TRANSER_OPERATION, mDynamicGlobalPropertyObject);
                    broadCastTransaction(mSignedTransaction);
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void showAddAddressDialog() {
        CybexDialog.showAddAddressDialog(this,
                getResources().getString(R.string.toast_message_withdraw_sent),
                getResources().getString(R.string.toast_message_add_this_account_to_list),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        Intent intent = new Intent(WithdrawActivity.this, AddTransferAccountActivity.class);
                        intent.putExtra(INTENT_PARAM_ADDRESS, mWithdrawAddress.getText().toString().trim());
                        intent.putExtra(INTENT_PARAM_CRYPTO_NAME, mAssetName);
                        intent.putExtra(INTENT_PARAM_CRYPTO_ID, mAssetObject.id.toString());
                        if (mIsTag) {
                            intent.putExtra(INTENT_PARAM_CRYPTO_TAG, mWithdrawMemoEosEditText.getText().toString().trim());
                            intent.putExtra("tag", mIsTag);
                        }
                        startActivity(intent);
                        finish();
                    }
                },
                new CybexDialog.ConfirmationDialogCancelListener() {
                    @Override
                    public void onCancel(Dialog dialog) {
                        finish();
                    }
                });
    }

    private void setKeyboardListener() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {

            }

            @Override
            public void keyBoardHide(int height) {
                if (mWithdrawAddress.isFocused()) {
                    mWithdrawAddress.clearFocus();
                }
                if (mWithdrawAmountEditText.isFocused()) {
                    mWithdrawAmountEditText.clearFocus();
                }
                if (mWithdrawMemoEosEditText.isFocused()) {
                    mWithdrawMemoEosEditText.clearFocus();
                }
            }
        });
    }

    private void displayFee() {
        String address = mWithdrawAddress.getText().toString().trim();
        String amount = mWithdrawAmountEditText.getText().toString().trim();
        String eosMemo = mWithdrawMemoEosEditText.getText().toString().trim();
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(amount) ||
                mAccountObject == null || mToAccountObject == null) {
            return;
        }
        mMemo = SettingConfig.getInstance().isGateway2() ? getMemoNew(address, mAssetName, eosMemo) : getMemo(address, mAssetName, eosMemo);
        Log.e("memo", mMemo);
        Operations.base_operation transferOperation = getTransferOperation(mAccountObject, mToAccountObject, mAssetObject, mMemo, amount, ASSET_ID_CYB, 0);
        double cybBalance = getBalance(mFullAccountObject, ASSET_ID_CYB);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(ASSET_ID_CYB, ID_TRANSER_OPERATION, transferOperation, new MessageCallback<Reply<List<FeeAmountObject>>>() {
                @Override
                public void onMessage(Reply<List<FeeAmountObject>> reply) {
                    if (mHandler == null) {
                        return;
                    }
                    mFeeAmountObject = reply.result.get(0);
                    if (mFeeAmountObject.amount <= cybBalance) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTransferFeeTextView.setText(String.format("%s CYB", String.valueOf(mFeeAmountObject.amount / Math.pow(10, 5))));
                                calculateReceiveAmount(mFeeAmountObject);
                                mIsFeeLoaded = true;
                                resetWithdrawBtnState();
                            }
                        });
                    } else {
                        getNoneCybFee(mMemo);
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

    private void broadCastTransaction(SignedTransaction signedTransaction) {
        try {
            BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, new MessageCallback<Reply<String>>() {
                @Override
                public void onMessage(Reply<String> reply) {
                    EventBus.getDefault().post(new Event.Withdraw(reply.result == null && reply.error == null));
                }

                @Override
                public void onFailure() {
                    EventBus.getDefault().post(new Event.Withdraw(false));
                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void calculateReceiveAmount(FeeAmountObject feeAmountObject) {
        double amount = Double.parseDouble(mWithdrawAmountEditText.getText().toString());
        double fee = (feeAmountObject.amount / Math.pow(10, mAssetObject.precision));
        double receiveAmount = 0;
        if (feeAmountObject.asset_id.equals(ASSET_ID_CYB)) {
            receiveAmount = amount - mGatewayFee;
        } else {
            if (amount + fee > mAvailableAmount) {
                receiveAmount = amount - mGatewayFee - fee;
            } else {
                receiveAmount = amount - mGatewayFee;
            }
        }
        mReceiveAmountTextView.setText(String.format("%." + (mAssetPrecision != null ? mAssetPrecision : mAssetObject.precision) + "f %s", receiveAmount, mAssetName));
    }

    private long getBalance(FullAccountObject fullAccountObject, String assetId) {
        for (AccountBalanceObject accountBalanceObject : fullAccountObject.balances) {
            if (accountBalanceObject.asset_type.toString().equals(assetId)) {
                return accountBalanceObject.balance;
            }
        }
        return 0;
    }

    private String getMemo(String address, String assetName, String memo) {
        if (mIsTag) {
            return "withdraw:" + "CybexGateway:" + assetName + ":" + address + "[" + memo + "]";
        }
        return "withdraw:" + "CybexGateway:" + assetName + ":" + address;
    }

    private String getMemoNew(String address, String assetName, String memo) {
        if (mIsTag) {
            return mWithdrawPrefix + ":" + assetName + ":" + address + "[" + memo + "]";
        }
        return mWithdrawPrefix + ":" + assetName + ":" + address;
    }

    private void getNoneCybFee(String memo) {
        Operations.base_operation transferOperation = getTransferOperation(mAccountObject, mToAccountObject, mAssetObject, memo,
                mWithdrawAmountEditText.getText().toString().trim(), mAssetObject.id.toString(), 0);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(mAssetObject.id.toString(), ID_TRANSER_OPERATION, transferOperation, new MessageCallback<Reply<List<FeeAmountObject>>>() {
                @Override
                public void onMessage(Reply<List<FeeAmountObject>> reply) {
                    if (mHandler == null) {
                        return;
                    }
                    mFeeAmountObject = reply.result.get(0);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTransferFeeTextView.setText(String.format("%." + (mAssetPrecision != null ? mAssetPrecision : mAssetObject.precision) + "f " + mAssetName, (mFeeAmountObject.amount / Math.pow(10, mAssetObject.precision))));
                            calculateReceiveAmount(mFeeAmountObject);
                            mIsFeeLoaded = true;
                            resetWithdrawBtnState();
                        }
                    });

                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private String getSubmitAmount(FeeAmountObject feeAmountObject) {
        double amount = Double.parseDouble(mWithdrawAmountEditText.getText().toString().trim());
        double fee = (feeAmountObject.amount / Math.pow(10, mAssetObject.precision));
        double submitAmount = 0;
        if (amount + fee > mAvailableAmount) {
            submitAmount = amount - fee;
        } else {
            submitAmount = amount;
        }
        return String.valueOf(submitAmount);
    }

    private void verifyAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        if (SettingConfig.getInstance().isGateway2()) {
            mCompositeDisposable.add(
                    RetrofitFactory.getInstance()
                            .apiGateway()
                            .verifyAddress(
                                    "application/json",
                                    "bearer " + mSignature,
                                    mAssetName,
                                    address)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    jsonObject -> {
                                        if (jsonObject == null) {
                                            return;
                                        }
                                        if (jsonObject.get("valid").getAsBoolean()) {
                                            mIsAddressInvalidate = true;
                                            mPbLoading.setVisibility(View.INVISIBLE);
                                            mIvAddressCheck.setVisibility(View.VISIBLE);
                                            mIvAddressCheck.setImageResource(R.drawable.ic_check_success);
                                            resetWithdrawBtnState();
                                            displayFee();
                                        } else {
                                            mIsAddressInvalidate = false;
                                            mPbLoading.setVisibility(View.INVISIBLE);
                                            mIvAddressCheck.setVisibility(View.VISIBLE);
                                            mIvAddressCheck.setImageResource(R.drawable.ic_close_red_24_px);
                                            resetWithdrawBtnState();
                                        }
                                    },
                                    throwable -> {
                                        mIsAddressInvalidate = false;
                                        mPbLoading.setVisibility(View.INVISIBLE);
                                        mIvAddressCheck.setVisibility(View.VISIBLE);
                                        mIvAddressCheck.setImageResource(R.drawable.ic_close_red_24_px);
                                        resetWithdrawBtnState();
                                    }
                            )

            );
        } else {
            ApolloQueryWatcher<VerifyAddress.Data> apolloQueryWatcher = ApolloClientApi.getInstance().client()
                    .query(VerifyAddress.builder().address(address).asset(mAssetName).accountName(mUserName).build())
                    .watcher().refetchCacheControl(CacheControl.NETWORK_FIRST);
            mCompositeDisposable.add(Rx2Apollo.from(apolloQueryWatcher)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<VerifyAddress.Data>>() {
                        @Override
                        public void accept(Response<VerifyAddress.Data> response) throws Exception {
                            VerifyAddress.Data verifyAddressData = response.data();
                            if (verifyAddressData == null) {
                                return;
                            }
                            if (!verifyAddressData.verifyAddress().fragments().withdrawAddressInfo().valid()) {
                                mIsAddressInvalidate = false;
                                mPbLoading.setVisibility(View.INVISIBLE);
                                mIvAddressCheck.setVisibility(View.VISIBLE);
                                mIvAddressCheck.setImageResource(R.drawable.ic_close_red_24_px);
                                resetWithdrawBtnState();
                            } else {
                                mIsAddressInvalidate = true;
                                mPbLoading.setVisibility(View.INVISIBLE);
                                mIvAddressCheck.setVisibility(View.VISIBLE);
                                mIvAddressCheck.setImageResource(R.drawable.ic_check_success);
                                resetWithdrawBtnState();
                                displayFee();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    }));
        }
    }

    private void setAvailableAmount(double availableAmount, String assetName) {
        mAvailableAmountTextView.setText(String.format(Locale.US, "%f %s", availableAmount, assetName));
    }

    private void requestDetailMessage() {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .api()
                .getWithdrawDetails(mAssetId + ".json")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        if (Locale.getDefault().getLanguage().equals("zh")) {
                            JSONArray asset_notice = jsonObject.getJSONObject("notice_cn").getJSONArray("adds");
                            StringBuilder builder = new StringBuilder();
                            if (asset_notice != null && asset_notice.length() > 0) {
                                for (int i = 0; i < asset_notice.length(); i++) {
                                    builder.append(asset_notice.getJSONObject(i).getString("text"));
                                    builder.append("\n");
                                }
                                mWithdrawMessage.setText(builder.toString());
                            }
                        } else {
                            JSONArray asset_notice = jsonObject.getJSONObject("notice_en").getJSONArray("adds");
                            StringBuilder builder = new StringBuilder();
                            if (asset_notice != null && asset_notice.length() > 0) {
                                for (int i = 0; i < asset_notice.length(); i++) {
                                    builder.append(asset_notice.getJSONObject(i).getString("text"));
                                    builder.append("\n");
                                }
                                mWithdrawMessage.setText(builder.toString());
                            }
                        }


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }));
    }

    private void setMinWithdrawAmountAndGateWayFee() {
        if (SettingConfig.getInstance().isGateway2()) {
            getToAccountMemoKey(mToAccountId);
            mWithdrawAmountEditText.setHint(getResources().getString(R.string.withdraw_minimum_hint) + String.valueOf(mMinValue));
            mGateWayFeeTextView.setText(String.format(Locale.US, "%." + (mAssetPrecision != null ? mAssetPrecision : mAssetObject.precision) + "f %s", mGatewayFee, mAssetName));
            mWithdrawAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(mAssetPrecision != null ? mAssetPrecision.intValue() : mAssetObject.precision)});
        } else {
            ApolloQueryCall<GetWithdrawInfo.Data> apolloQueryCall = ApolloClientApi.getInstance().client()
                    .query(GetWithdrawInfo.builder().type(mAssetName).build());
            mCompositeDisposable.add(Rx2Apollo.from(apolloQueryCall)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Response<GetWithdrawInfo.Data>>() {
                        @Override
                        public void accept(Response<GetWithdrawInfo.Data> response) throws Exception {
                            GetWithdrawInfo.Data withdrawInfoData = response.data();
                            if (withdrawInfoData == null) {
                                return;
                            }
                            if (withdrawInfoData.withdrawInfo().fragments().withdrawinfoObject() != null) {
                                WithdrawinfoObject withdrawinfoObject = withdrawInfoData.withdrawInfo().fragments().withdrawinfoObject();
                                mMinValue = withdrawinfoObject.minValue();
                                mGatewayFee = withdrawinfoObject.fee();
                                mToAccountId = withdrawinfoObject.gatewayAccount();
                                mAssetPrecision = withdrawinfoObject.precision();
                                getToAccountMemoKey(mToAccountId);
                                mWithdrawAmountEditText.setHint(getResources().getString(R.string.withdraw_minimum_hint) + String.valueOf(mMinValue));
                                mGateWayFeeTextView.setText(String.format(Locale.US, "%." + (mAssetPrecision != null ? mAssetPrecision : mAssetObject.precision) + "f %s", mGatewayFee, mAssetName));
                                mWithdrawAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(mAssetPrecision != null ? mAssetPrecision.intValue() : mAssetObject.precision)});
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    }));
        }

    }

    private void getToAccountMemoKey(String accountId) {
        List<String> accountIds = new ArrayList<>();
        accountIds.add(accountId);
        try {
            BitsharesWalletWraper.getInstance().get_full_accounts(accountIds, true, new MessageCallback<Reply<List<FullAccountObjectReply>>>() {
                @Override
                public void onMessage(Reply<List<FullAccountObjectReply>> reply) {
                    mToAccountObject = reply.result.get(0).fullAccountObject;
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private void checkIfLocked() {
        if (BitsharesWalletWraper.getInstance().is_locked() && !isLoginFromENotes()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, mUnLockDialogListener);
        } else {
            displayFee();
        }
    }

    private boolean checkWithdrawAuthority(AccountObject accountObject, String password) {
        Map<String, Types.public_key_type> map = SpUtil.getMap(this, PREF_ADDRESS_TO_PUB_MAP);
        Types.public_key_type memoKey = accountObject.options.memo_key;
        for (Map.Entry<String, Types.public_key_type> entry : map.entrySet()) {
            if (memoKey.equals(entry.getValue())) {
                return true;
            }
        }
        ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.toast_message_can_not_withdraw), R.drawable.ic_error_16px);
        return false;
    }

    private Operations.transfer_operation getTransferOperation(AccountObject accountObject, FullAccountObject toAccountObject, AssetObject assetObject, String memo, String amount, String feeAssetId, long feeAmount) {
        return BitsharesWalletWraper.getInstance().getTransferOperation(
                accountObject.id,
                toAccountObject.account.id,
                assetObject.id,
                feeAmount,
                ObjectId.create_from_string(feeAssetId),
                (long) (Double.parseDouble(amount) * Math.pow(10, assetObject.precision)),
                memo,
                accountObject.options.memo_key,
                toAccountObject.account.options.memo_key);
    }

    private void loadAddress() {
        if (TextUtils.isEmpty(mUserName)) {
            return;
        }
        mCompositeDisposable.add(DBManager.getDbProvider(this).getAddress(mUserName, mAssetObject.id.toString(), Address.TYPE_WITHDRAW)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        mAddresses = addresses;
                        if (mAddresses == null || mAddresses.size() == 0) {
                            mTvWithdrawSelectAddress.setText(getResources().getString(R.string.text_add_address));
                        } else {
                            mTvWithdrawSelectAddress.setText(getResources().getString(R.string.text_select_address));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_withdraw_records, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AntiShake.check(item.getItemId())) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_records:
                Intent intent = new Intent(this, DepositWithdrawRecordsActivity.class);
                intent.putExtra("assetObject", mAssetObject);
                intent.putExtra("fundType", "WITHDRAW");
                intent.putExtra("assetNameForGatewayRequest", mAssetNameForGatewayRequest);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unbindService(mConnection);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_UPDATE_ACCOUNT && resultCode == Constant.RESULT_CODE_UPDATE_ACCOUNT) {
            mAccountObject = mWebSocketService.getFullAccount(mUserName).account;
        }
    }
}
