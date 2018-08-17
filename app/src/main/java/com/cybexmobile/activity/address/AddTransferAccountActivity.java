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

import com.cybex.database.DBManager;
import com.cybex.database.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.toast.message.ToastMessage;
import com.cybexmobile.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

import static com.cybexmobile.utils.Constant.PREF_NAME;

public class AddTransferAccountActivity extends BaseActivity implements SoftKeyBoardListener.OnSoftKeyBoardChangeListener{

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.add_transfer_account_et_account)
    EditText mEtAccount;
    @BindView(R.id.add_transfer_account_et_label)
    EditText mEtLabel;
    @BindView(R.id.add_transfer_account_btn_add)
    Button mBtnAdd;
    @BindView(R.id.add_transfer_account_iv_account_check)
    ImageView mIvAccountCheck;

    private String mUserName;
    private Unbinder mUnbinder;
    private Disposable mAddTransferAccountDisposable;
    private boolean mIsAccountValid;
    private boolean mIsAccountExist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transfer_account);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        EventBus.getDefault().register(this);
        SoftKeyBoardListener.setListener(this, this);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
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
        if(mAddTransferAccountDisposable != null && !mAddTransferAccountDisposable.isDisposed()){
            mAddTransferAccountDisposable.dispose();
        }
    }

    @Override
    public void keyBoardShow(int height) {

    }

    @Override
    public void keyBoardHide(int height) {
        if(mEtAccount.isFocused()){
            mEtAccount.clearFocus();
        }
        if(mEtLabel.isFocused()){
            mEtLabel.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mEtAccount.isFocused()){
                manager.hideSoftInputFromWindow(mEtAccount.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtAccount.clearFocus();
            }
            if(mEtLabel.isFocused()){
                manager.hideSoftInputFromWindow(mEtLabel.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtLabel.clearFocus();
            }
        }
        return false;
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        mIvAccountCheck.setVisibility(View.VISIBLE);
        if(event.getAccountObject() == null){
            mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
            mIsAccountValid = false;
        } else {
            mIvAccountCheck.setImageResource(R.drawable.register_check);
            mIsAccountValid = true;
            checkAccountExist();
        }
        resetBtnState();
    }

    @OnTextChanged(value = R.id.add_transfer_account_et_label, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAccountNameTextChanged(Editable editable){
        resetBtnState();
    }

    @OnFocusChange(R.id.add_transfer_account_et_account)
    public void onAccountNameFocusChanged(View view, boolean isFocused){
        String accountName = mEtAccount.getText().toString().trim();
        if(isFocused){
            return;
        }
        if(TextUtils.isEmpty(accountName)){
            mBtnAdd.setEnabled(false);
            mIvAccountCheck.setVisibility(View.INVISIBLE);
            return;
        }
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

    @OnClick(R.id.add_transfer_account_btn_add)
    public void onAddTransferAccountClick(View view){
        Address address = new Address();
        address.setType(Address.TYPE_TRANSFER);
        address.setLabel(mEtLabel.getText().toString().trim());
        address.setAccount(mUserName);
        address.setAddress(mEtAccount.getText().toString().trim());

        addTransferAccount(address);
    }

    private void addTransferAccount(Address address){
        mAddTransferAccountDisposable = DBManager.getDbProvider(this).insertAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if(aLong != -1){
                            ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                    getResources().getString(R.string.text_add_transfer_account_successful),
                                    R.drawable.ic_check_circle_green);
                            finish();
                        } else {
                            ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                    getResources().getString(R.string.text_add_transfer_account_failed),
                                    R.drawable.ic_error_16px);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ToastMessage.showNotEnableDepositToastMessage(AddTransferAccountActivity.this,
                                getResources().getString(R.string.text_add_transfer_account_failed),
                                R.drawable.ic_error_16px);
                    }
                });
    }

    private void checkAccountExist(){
        mAddTransferAccountDisposable = DBManager.getDbProvider(this)
                .checkAddressExist(mUserName, mEtAccount.getText().toString().trim(), Address.TYPE_TRANSFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        mIsAccountExist = aBoolean;
                        if(aBoolean){
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
    }

    private void resetBtnState(){
        mBtnAdd.setEnabled(mIsAccountValid && !mIsAccountExist && !TextUtils.isEmpty(mEtLabel.getText().toString().trim()));
    }

}
