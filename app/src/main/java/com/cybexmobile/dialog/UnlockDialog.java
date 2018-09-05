package com.cybexmobile.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.graphene.chain.AccountObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_NAME;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT;

public class UnlockDialog extends DialogFragment{

    @BindView(R.id.dialog_confirm_tv_title)
    TextView mTvTitle;
    @BindView(R.id.dialog_confirm_et_password)
    EditText mEtPassword;
    @BindView(R.id.dialog_confirm_layout_unlock_error)
    LinearLayout mLayoutUnlockError;
    @BindView(R.id.dialog_confirm_btn_cancel)
    Button mBtnCancel;
    @BindView(R.id.dialog_confirm_btn_confirm)
    Button mBtnConfirm;
    @BindView(R.id.dialog_confirm_pb_loading)
    ProgressBar mPbLoading;

    private Unbinder mUnbinder;

    private AccountObject mAccountObject;
    private String mUserName;
    private UnLockDialogClickListener mUnLockListener;

    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        Bundle bundle = getArguments();
        mAccountObject = (AccountObject) bundle.getSerializable(INTENT_PARAM_TRANSFER_MY_ACCOUNT);
        mUserName = bundle.getString(INTENT_PARAM_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_unclock_wallet, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText(getResources().getString(R.string.unlock_wallet_dialog_title));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    @OnClick(R.id.dialog_confirm_btn_cancel)
    public void onDialogCancel(View view){
        this.dismiss();
    }

    @OnClick(R.id.dialog_confirm_btn_confirm)
    public void onDialogConfirm(View view){
        String password = mEtPassword.getText().toString().trim();
        if (mUnLockListener != null && !TextUtils.isEmpty(password)) {
            mBtnConfirm.setEnabled(false);
            mPbLoading.setVisibility(View.VISIBLE);
            verifyPassword(mAccountObject, mUserName, password);
        }
    }

    private void verifyPassword(AccountObject accountObject, String username, String password){
        mDisposable = Observable.create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int result = BitsharesWalletWraper.getInstance().import_account_password(accountObject, username, password);
                if(!emitter.isDisposed()){
                    emitter.onNext(result);
                    emitter.onComplete();
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                if(integer == 0){
                    mLayoutUnlockError.setVisibility(View.GONE);
                    mPbLoading.setVisibility(View.GONE);
                    mUnLockListener.onUnLocked(password);
                    dismiss();
                } else {
                    mLayoutUnlockError.setVisibility(View.VISIBLE);
                    mPbLoading.setVisibility(View.GONE);
                    mBtnConfirm.setEnabled(true);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                mLayoutUnlockError.setVisibility(View.VISIBLE);
                mPbLoading.setVisibility(View.GONE);
                mBtnConfirm.setEnabled(true);
            }
        });
    }

    @OnEditorAction(R.id.dialog_confirm_et_password)
    public boolean onUnlockEditorAction(TextView textView, int actionId, KeyEvent event){
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
            mBtnConfirm.performClick();
            return true;
        }
        return false;
    }

    public void setUnLockListener(UnLockDialogClickListener lockListener){
        mUnLockListener = lockListener;
    }

    public interface UnLockDialogClickListener {
        void onUnLocked(String password);
    }

}
