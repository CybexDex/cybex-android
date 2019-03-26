package com.cybex.basemodule.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.basemodule.R;
import com.cybex.basemodule.R2;
import com.cybex.basemodule.BitsharesWalletWraper;
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

    @BindView(R2.id.dialog_confirm_tv_title)
    TextView mTvTitle;
    @BindView(R2.id.dialog_confirm_et_password)
    EditText mEtPassword;
    @BindView(R2.id.dialog_confirm_tv_error)
    TextView mTvUnlockError;
    @BindView(R2.id.dialog_confirm_btn_cancel)
    Button mBtnCancel;
    @BindView(R2.id.dialog_confirm_btn_confirm)
    Button mBtnConfirm;
    @BindView(R2.id.dialog_confirm_pb_loading)
    ProgressBar mPbLoading;

    private Unbinder mUnbinder;

    private AccountObject mAccountObject;
    private String mUserName;
    private UnLockDialogClickListener mUnLockListener;
    private OnDismissListener mOnDismissListener;

    private Disposable mDisposable;

    private int result = -1; // -1用户主动取消dialog 1解锁后关闭dialog

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
        mEtPassword.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText(getResources().getString(R.string.unlock_wallet_dialog_title));
//        mUseEnotesTitle.setVisibility(View.INVISIBLE);
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mOnDismissListener != null){
            mOnDismissListener.onDismiss(result);
        }
    }

    @OnClick(R2.id.dialog_confirm_btn_cancel)
    public void onDialogCancel(View view){
        this.dismiss();
    }

    @OnClick(R2.id.dialog_confirm_btn_confirm)
    public void onDialogConfirm(View view){
        String password = mEtPassword.getText().toString().trim();
        if (mUnLockListener != null && !TextUtils.isEmpty(password)) {
            mBtnConfirm.setEnabled(false);
            mPbLoading.setVisibility(View.VISIBLE);
            verifyPassword(mAccountObject, mUserName, password);
        }
    }

    private void verifyPassword(final AccountObject accountObject, final String username, final String password){
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
                    mTvUnlockError.setVisibility(View.GONE);
                    mPbLoading.setVisibility(View.GONE);
                    if(mUnLockListener != null){
                        mUnLockListener.onUnLocked(password);
                    }
                    result = 1;
                    dismiss();
                } else {
                    mTvUnlockError.setVisibility(View.VISIBLE);
                    mPbLoading.setVisibility(View.GONE);
                    mBtnConfirm.setEnabled(true);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                mTvUnlockError.setVisibility(View.VISIBLE);
                mPbLoading.setVisibility(View.GONE);
                mBtnConfirm.setEnabled(true);
            }
        });
    }

    @OnEditorAction(R2.id.dialog_confirm_et_password)
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

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public interface UnLockDialogClickListener {
        void onUnLocked(String password);
    }

    public interface OnDismissListener {
        void onDismiss(int result);
    }

}
