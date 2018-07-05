package com.cybexmobile.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.GetWithdrawInfo;
import com.apollographql.apollo.VerifyAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.cybexmobile.R;
import com.cybexmobile.api.ApolloClientApi;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FeeAmountObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.FullAccountObjectReply;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.graphene.chain.PrivateKey;
import com.cybexmobile.graphene.chain.Types;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.toast.message.ToastMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.cybexmobile.graphene.chain.Operations.ID_TRANSER_OPERATION;

public class WithdrawActivity extends BaseActivity {

    public Unbinder mUnbinder;
    private String mAssetName;
    private boolean mIsEnabled;
    private String mEnMsg;
    private String mCnMsg;
    private String mUserName;
    private String mAddress;
    private String mAmount;
    private double mAvailableAmount;
    private double mMinValue;
    private String mToAccountId;
    private FullAccountObject mToAccountObject;
    private AccountObject mAccountObject;
    private AssetObject mAssetObject;
    private ApolloClient mApolloClient;
    private Handler mHandler = new Handler();
    private WebSocketService mWebSocketService;
    private Operations.transfer_operation mTransferOperation;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.withdraw_toolbar_text_view)
    TextView mToolbarTextView;
    @BindView(R.id.withdraw_available_amount)
    TextView mAvailableAmountTextView;
    @BindView(R.id.withdraw_withdrawal_address)
    EditText mWithdrawAddress;
    @BindView(R.id.withdraw_amount)
    EditText mWithdrawAmount;
    @BindView(R.id.withdraw_error)
    LinearLayout mErrorLinearLayout;
    @BindView(R.id.withdraw_error_text)
    TextView mErrorTextView;
    @BindView(R.id.withdraw_gateway_fee)
    TextView mGateWayFee;
    @BindView(R.id.withdraw_transfer_fee)
    TextView mTransferFee;
    @BindView(R.id.withdraw_message)
    TextView mWithdrawMessage;
    @BindView(R.id.withdraw_receive_amount)
    TextView mReceiveAmount;
    @BindView(R.id.withdraw_button)
    Button mWithdrawButton;
    @BindView(R.id.withdraw_all_button)
    TextView mWithdrawAllButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mApolloClient = ApolloClientApi.getApolloClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = sharedPreferences.getString("name", null);
        Intent intent = getIntent();
        mAssetName = intent.getStringExtra("assetName");
        mIsEnabled = intent.getBooleanExtra("isEnabled", true);
        mEnMsg = intent.getStringExtra("enMsg");
        mCnMsg = intent.getStringExtra("cnMsg");
        mAvailableAmount = intent.getDoubleExtra("availableAmount", 0);
        mAssetObject = (AssetObject) intent.getSerializableExtra("assetObject");
        mToolbarTextView.setText(String.format("%s " + getResources().getString(R.string.gate_way_withdraw), mAssetName));
        setAvailableAmount(mAvailableAmount, mAssetName);
        requestDetailMessage();
        setMinWithdrawAmountAndGateWayFee();
        setEditTextOnClickListener();
        if (mIsEnabled) {
            checkIfLocked();
        } else {
            if (Locale.getDefault().getLanguage().equals("zh")) {
                ToastMessage.showNotEnableDepositToastMessage(this, mCnMsg);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(this, mEnMsg);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mAccountObject = mWebSocketService.getFullAccount(mUserName).account;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void setEditTextOnClickListener() {
        mWithdrawAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyAddress(s.toString());
            }
        });

        mWithdrawAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWithdrawAmount.setText(String.valueOf(mAvailableAmount));
                if (mAvailableAmount < mMinValue) {
                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                    mErrorTextView.setText(getResources().getString(R.string.withdraw_error_less_than_minimum));
                }
            }
        });
        mWithdrawAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mAddress != null && mAmount != null && mAccountObject != null && mToAccountObject != null) {
                        String memo = getMemo(mAddress, mAssetName);
                        mTransferOperation = getTransferOperation(mAccountObject, mToAccountObject, mAssetObject, memo, mAmount);
                        try {
                            BitsharesWalletWraper.getInstance().get_requried_fees("1.3.0", ID_TRANSER_OPERATION, mTransferOperation, new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FeeAmountObject>>>() {
                                @Override
                                public void onMessage(WebSocketClient.Reply<List<FeeAmountObject>> reply) {
                                    FeeAmountObject feeAmountObject = reply.result.get(0);
                                    Log.e("asset_id", feeAmountObject.asset_id);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            mTransferFee.setText(String.format("%sCYB", String.valueOf(feeAmountObject.amount / Math.pow(10, 5))));
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
                }
            }
        });

    }

    @OnTextChanged(value = R.id.withdraw_amount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onWithdrawAmountEditTextChanged(Editable editable) {
        double amount = editable.toString().length() > 0 ? Double.valueOf(editable.toString()) : 0;
        if (amount > mAvailableAmount) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_not_enough));
        } else if (amount < mMinValue) {
            mErrorLinearLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText(getResources().getString(R.string.withdraw_error_less_than_minimum));
        } else {
            mAmount = editable.toString();
            mErrorLinearLayout.setVisibility(View.GONE);
        }
    }

    private String getMemo(String address, String assetName) {
        return "withdraw:" + "CybexGateway:" + assetName + ":" + address;
    }

    private void verifyAddress(String s) {
        mApolloClient.query(VerifyAddress
                .builder()
                .address(s)
                .asset(mAssetName)
                .accountName(mUserName)
                .build())
                .enqueue(new ApolloCall.Callback<VerifyAddress.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<VerifyAddress.Data> response) {
                        if (response.data() != null) {
                            if (!response.data().verifyAddress().fragments().withdrawAddressInfo().valid()) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mErrorLinearLayout.setVisibility(View.VISIBLE);
                                        mErrorTextView.setText(getResources().getString(R.string.withdraw_error_invalid_address));
                                    }
                                });
                            } else {
                                mAddress = s;
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mErrorLinearLayout.setVisibility(View.GONE);

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }


    private void setAvailableAmount(double availableAmount, String assetName) {
        mAvailableAmountTextView.setText(String.format(Locale.US, "%f %s", availableAmount, assetName));
    }

    private void requestDetailMessage() {
        RetrofitFactory.getInstance()
                .api()
                .getWithdrawMsg()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            String enMsg = jsonObject.getString("enMsg");
                            String cnMsg = jsonObject.getString("cnMsg");
                            if (Locale.getDefault().getLanguage().equals("zh")) {
                                mWithdrawMessage.setText(cnMsg.replace("$asset", mAssetName));
                            } else {
                                mWithdrawMessage.setText(enMsg.replace("$asset", mAssetName));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void setMinWithdrawAmountAndGateWayFee() {
        mApolloClient.query(GetWithdrawInfo
                .builder()
                .type(mAssetName)
                .build())
                .enqueue(new ApolloCall.Callback<GetWithdrawInfo.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetWithdrawInfo.Data> response) {
                        if (response.data() != null) {
                            if (response.data().withdrawInfo().fragments().withdrawinfoObject() != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMinValue = response.data().withdrawInfo().fragments().withdrawinfoObject().minValue();
                                        mWithdrawAmount.setHint(String.valueOf(mMinValue) + "Min");
                                        mToAccountId = response.data().withdrawInfo().fragments().withdrawinfoObject().gatewayAccount();
                                        getToAccountMemoKey(mToAccountId);
                                        mGateWayFee.setText(String.format("%s %s", String.valueOf(response.data().withdrawInfo().fragments().withdrawinfoObject().fee()), mAssetName));

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }

    private void getToAccountMemoKey(String accountId) {
        List<String> accountIds = new ArrayList<>();
        accountIds.add(accountId);
        try {
            BitsharesWalletWraper.getInstance().get_full_accounts(accountIds, true, new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FullAccountObjectReply>>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<List<FullAccountObjectReply>> reply) {
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
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(this, new CybexDialog.UnLockDialogClickListener() {
                @Override
                public void onClick(String password, Dialog dialog) {
                    showLoadDialog(true);
                    try {
                        BitsharesWalletWraper.getInstance().get_account_object(mUserName, new WebSocketClient.MessageCallback<WebSocketClient.Reply<AccountObject>>() {
                            @Override
                            public void onMessage(WebSocketClient.Reply<AccountObject> reply) {
                                mAccountObject = reply.result;
                                int result = BitsharesWalletWraper.getInstance().import_account_password(mAccountObject, mUserName, password);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result == 0) {
                                            hideLoadDialog();
                                            dialog.dismiss();
                                            checkWithdrawAuthority(mAccountObject, password);
                                        } else {
                                            hideLoadDialog();
                                            LinearLayout errorLayout = dialog.findViewById(R.id.unlock_wallet_dialog_error_layout);
                                            errorLayout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure() {
                                hideLoadDialog();
                            }
                        });
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            checkWithdrawAuthority(mAccountObject, BitsharesWalletWraper.getInstance().getPassword());
        }
    }

    private void checkWithdrawAuthority(AccountObject accountObject, String password) {
        Types.public_key_type memoKey = accountObject.options.memo_key;
        PrivateKey privateMemoKey = PrivateKey.from_seed(mUserName + "memo" + password);
        PrivateKey privateActiveKey = PrivateKey.from_seed(mUserName + "active" + password);
        PrivateKey privateOwnerKey = PrivateKey.from_seed(mUserName + "owner" + password);
        Types.public_key_type publicMemoKeyType = new Types.public_key_type(privateMemoKey.get_public_key(true), true);
        Types.public_key_type publicActiveKeyType = new Types.public_key_type(privateActiveKey.get_public_key(true), true);
        Types.public_key_type publicOwnerKeyType = new Types.public_key_type(privateOwnerKey.get_public_key(true), true);
        if (!memoKey.toString().equals(publicMemoKeyType.toString()) && !accountObject.active.is_public_key_type_exist(publicActiveKeyType) &&
                !accountObject.active.is_public_key_type_exist(publicOwnerKeyType)) {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.toast_message_can_not_withdraw));
        }


    }

    private Operations.transfer_operation getTransferOperation(AccountObject accountObject, FullAccountObject toAccountObject, AssetObject assetObject, String memo, String amount) {
        mTransferOperation = BitsharesWalletWraper.getInstance().getTransferOperation(accountObject.id, toAccountObject.account.id, assetObject, amount, memo, accountObject.options.memo_key, toAccountObject.account.options.memo_key);
        return mTransferOperation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unbindService(mConnection);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
