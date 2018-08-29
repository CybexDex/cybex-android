package com.cybexmobile.activity.address;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.VerifyAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybex.provider.apollo.ApolloClientApi;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybexmobile.toast.message.ToastMessage;
import com.cybexmobile.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybexmobile.utils.Constant.INTENT_PARAM_ADDRESS;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_CRYPTO_ID;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_CRYPTO_MEMO;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_CRYPTO_NAME;
import static com.cybexmobile.utils.Constant.PREF_NAME;

public class AddTransferAccountActivity extends BaseActivity implements SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
    private static String EOS = "EOS";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.add_transfer_account_toolbar_title)
    TextView mTvToolbarTitle;
    @BindView(R.id.add_transfer_account_crypto_layout)
    LinearLayout mLinearLayoutCrypto;
    @BindView(R.id.add_transfer_account_crypto_tv)
    TextView mTvCrypto;
    @BindView(R.id.add_transfer_account_tv_account_address)
    TextView mTvAccountAddress;
    @BindView(R.id.add_transfer_account_et_account)
    EditText mEtAccount;
    @BindView(R.id.add_transfer_account_et_label)
    EditText mEtLabel;
    @BindView(R.id.add_transfer_account_memo_layout)
    LinearLayout mLinearLayoutMemo;
    @BindView(R.id.add_transfer_account_et_memo)
    EditText mEtMemo;
    @BindView(R.id.add_transfer_account_btn_add)
    Button mBtnAdd;
    @BindView(R.id.add_transfer_account_iv_account_check)
    ImageView mIvAccountCheck;

    private String mUserName;
    private Unbinder mUnbinder;
    private Disposable mAddTransferAccountDisposable;
    private ApolloClient mApolloClient;

    private boolean mIsAccountValid;
    private boolean mIsAccountExist;
    private String mTokenName;
    private String mTokenId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transfer_account);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        EventBus.getDefault().register(this);
        SoftKeyBoardListener.setListener(this, this);
        mApolloClient = ApolloClientApi.getApolloClient();
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        String address = getIntent().getStringExtra(INTENT_PARAM_ADDRESS);
        String eosMemo = getIntent().getStringExtra(INTENT_PARAM_CRYPTO_MEMO);
        mTokenName = getIntent().getStringExtra(INTENT_PARAM_CRYPTO_NAME);
        mTokenId = getIntent().getStringExtra(INTENT_PARAM_CRYPTO_ID);
        if(!TextUtils.isEmpty(address)){
            mEtAccount.setText(address);
            onAccountNameFocusChanged(mEtAccount, false);
        }

        if (mTokenName != null) {
            mLinearLayoutCrypto.setVisibility(View.VISIBLE);
            mTvCrypto.setText(mTokenName);
            if (mTokenName.equals(EOS)) {
                mLinearLayoutMemo.setVisibility(View.VISIBLE);
                mTvToolbarTitle.setText(getResources().getString(R.string.text_add_withdraw_account));
                if (!TextUtils.isEmpty(eosMemo)) {
                    mEtMemo.setText(eosMemo);
                }
            } else {
                mLinearLayoutMemo.setVisibility(View.GONE);
                mTvAccountAddress.setText(getResources().getString(R.string.text_address));
                mTvToolbarTitle.setText(getResources().getString(R.string.text_add_withdraw_address));
            }
        } else {
            mLinearLayoutCrypto.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        EventBus.getDefault().unregister(this);
        if (mAddTransferAccountDisposable != null && !mAddTransferAccountDisposable.isDisposed()) {
            mAddTransferAccountDisposable.dispose();
        }
    }

    @Override
    public void keyBoardShow(int height) {

    }

    @Override
    public void keyBoardHide(int height) {
        if (mEtAccount.isFocused()) {
            mEtAccount.clearFocus();
        }
        if (mEtLabel.isFocused()) {
            mEtLabel.clearFocus();
        }
        if (mEtMemo.isFocused()) {
            mEtMemo.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mEtAccount.isFocused()) {
                manager.hideSoftInputFromWindow(mEtAccount.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtAccount.clearFocus();
            }
            if (mEtLabel.isFocused()) {
                manager.hideSoftInputFromWindow(mEtLabel.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtLabel.clearFocus();
            }
            if (mEtMemo.isFocused()) {
                manager.hideSoftInputFromWindow(mEtMemo.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtMemo.clearFocus();
            }
        }
        return false;
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event) {
        mIvAccountCheck.setVisibility(View.VISIBLE);
        if (event.getAccountObject() == null) {
            mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
            mIsAccountValid = false;
        } else {
            mIvAccountCheck.setImageResource(R.drawable.ic_check_success);
            mIsAccountValid = true;
            checkAccountExist();
        }
        resetBtnState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVerifyAddress(Event.VerifyAddress verifyAddress) {
        mIvAccountCheck.setVisibility(View.VISIBLE);
        if (!verifyAddress.isValid()) {
            mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
            if (mTokenName.equals(EOS)) {
                ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.text_address_not_exist), R.drawable.ic_error_16px);
            }
            mIsAccountValid = false;
        } else {
            mIvAccountCheck.setImageResource(R.drawable.ic_check_success);
            mIsAccountValid = true;
            checkAccountExist();
        }
        resetBtnState();
    }

    @OnTextChanged(value = R.id.add_transfer_account_et_label, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAccountNameTextChanged(Editable editable) {
        resetBtnState();
    }

    @OnFocusChange(R.id.add_transfer_account_et_account)
    public void onAccountNameFocusChanged(View view, boolean isFocused) {
        String accountName = mEtAccount.getText().toString().trim();
        if (isFocused) {
            return;
        }
        if (TextUtils.isEmpty(accountName)) {
            mBtnAdd.setEnabled(false);
            mIvAccountCheck.setVisibility(View.INVISIBLE);
            return;
        }
        if (mTokenName != null) {
            mApolloClient.query(VerifyAddress
                    .builder()
                    .address(accountName)
                    .asset(mTokenName)
                    .accountName(mUserName)
                    .build())
                    .watcher().refetchCacheControl(CacheControl.NETWORK_FIRST)
                    .enqueueAndWatch(new ApolloCall.Callback<VerifyAddress.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<VerifyAddress.Data> response) {
                            if (response.data() != null) {
                                EventBus.getDefault().post(new Event.VerifyAddress(response.data().verifyAddress().fragments().withdrawAddressInfo().valid()));
                            }
                        }

                        @Override
                        public void onFailure(@Nonnull ApolloException e) {

                        }
                    });
        } else {
            try {
                BitsharesWalletWraper.getInstance().get_account_object(accountName, new WebSocketClient.MessageCallback<WebSocketClient.Reply<AccountObject>>() {
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
    }

    @OnClick(R.id.add_transfer_account_btn_add)
    public void onAddTransferAccountClick(View view) {
        Address address = new Address();
        if (mTokenName != null) {
            address.setType(Address.TYPE_WITHDRAW);
            address.setNote(mEtLabel.getText().toString());
            address.setToken(mTokenId);
            address.setAddress(mEtAccount.getText().toString().trim());
            address.setAccount(mUserName);
            address.setCreateTime(System.currentTimeMillis());
            if (mTokenName.equals(EOS)) {
                address.setMemo(mEtMemo.getText().toString().trim());
            }
            addTransferAccount(address);
        } else {
            address.setType(Address.TYPE_TRANSFER);
            address.setNote(mEtLabel.getText().toString());
            address.setAccount(mUserName);
            address.setAddress(mEtAccount.getText().toString().trim());
            address.setCreateTime(System.currentTimeMillis());
            addTransferAccount(address);
        }
    }

    private void addTransferAccount(Address address) {
        mAddTransferAccountDisposable = DBManager.getDbProvider(this).insertAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (aLong != -1) {
                            ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                    getResources().getString(R.string.text_added),
                                    R.drawable.ic_check_circle_green);
                            finish();
                        } else {
                            ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                    getResources().getString(R.string.text_added_failed),
                                    R.drawable.ic_error_16px);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                getResources().getString(R.string.text_added_failed),
                                R.drawable.ic_error_16px);
                    }
                });
    }

    private void checkAccountExist() {
        if (mTokenName != null) {
            mAddTransferAccountDisposable = DBManager.getDbProvider(this)
                    .checkWithdrawAddressExist(mUserName, mEtAccount.getText().toString().trim(), mTokenId, Address.TYPE_WITHDRAW)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            mIsAccountExist = aBoolean;
                            if (aBoolean) {
                                ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                        getResources().getString(R.string.text_transfer_account_already_exists),
                                        R.drawable.ic_error_16px);
                                mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
                            }
                            resetBtnState();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            mIsAccountExist = false;
                            resetBtnState();
                        }
                    });

        } else {
            mAddTransferAccountDisposable = DBManager.getDbProvider(this)
                    .checkAddressExist(mUserName, mEtAccount.getText().toString().trim(), Address.TYPE_TRANSFER)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Address>() {
                        @Override
                        public void accept(Address address) throws Exception {
                            if (address != null) {
                                mIsAccountExist = true;
                                ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                        getResources().getString(R.string.text_transfer_account_already_exists),
                                        R.drawable.ic_error_16px);
                                mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
                            } else {
                                mIsAccountExist = false;
                            }
                            resetBtnState();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            mIsAccountExist = false;
                            resetBtnState();
                        }
                    });
        }
    }

    private void resetBtnState(){
        mBtnAdd.setEnabled(mIsAccountValid && !mIsAccountExist && !TextUtils.isEmpty(mEtLabel.getText().toString()));
    }

}
