package com.cybexmobile.activity.setting.enotes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class SetEnotesPasswordActivity extends BaseActivity {

    @BindView(R.id.et_set_enotes_password) EditText mEtSetEnotesPassword;
    @BindView(R.id.et_set_enotes_password_confirmation) EditText mEtSetEnotesPasswordConfirm;
    @BindView(R.id.set_enotes_pass_ll_error) LinearLayout mSetEnotesPassLlError;
    @BindView(R.id.set_enotes_pass_button) Button mSetEnotesPassButton;
    @BindView(R.id.set_enotes_pass_error_text) TextView mSetEnotesPassErrorText;
    @BindView(R.id.password_check) ImageView mPasswordChecker;
    @BindView(R.id.password_confirm_check)
    ImageView mPasswordConfirmChecker;

    private static final String TAG = "SetEnotesPasswordActivity";
    private Unbinder mUnbinder;
    private final String passwordRegPattern = "(?=.*[0-9])" + ".{6}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_enotes_password);
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnTextChanged(value = R.id.et_set_enotes_password, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPasswordChanged(Editable s) {
        String password = mEtSetEnotesPassword.getText().toString();
        String repeatPass = mEtSetEnotesPasswordConfirm.getText().toString();
        checkPasswordFormat(password, repeatPass);
    }

    @OnTextChanged(value = R.id.et_set_enotes_password_confirmation, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onRepeatPasswordChanged(Editable s) {
        String password = mEtSetEnotesPassword.getText().toString();
        String repeatPass = mEtSetEnotesPasswordConfirm.getText().toString();
        checkPasswordFormat(password, repeatPass);
    }

    private void checkPasswordFormat(String password, String repeatPass){
        if (TextUtils.isEmpty(password) || !password.matches(passwordRegPattern)){
            //密码为空或者不符合规则
            mSetEnotesPassLlError.setVisibility(View.VISIBLE);
            mSetEnotesPassErrorText.setText(getResources().getString(R.string.set_enotes_password_error));
            mPasswordChecker.setVisibility(View.GONE);
            setConfirmButtonEnable(false);
        }else {
            //符合规则
            mPasswordChecker.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(repeatPass) && isPasswordMatch(password, repeatPass)){
                //两次输入密码一致
                mSetEnotesPassLlError.setVisibility(View.GONE);
                mPasswordConfirmChecker.setVisibility(View.VISIBLE);
                setConfirmButtonEnable(true);
            }else {
                //两次输入密码不一致
                mPasswordConfirmChecker.setVisibility(View.GONE);
                mSetEnotesPassLlError.setVisibility(View.VISIBLE);
                mSetEnotesPassErrorText.setText(getResources().getString(R.string.create_account_password_confirm_error));
                setConfirmButtonEnable(false);
            }
        }
    }

    private boolean isPasswordMatch(String password, String repeatPass){
        return password.equals(repeatPass);
    }

    private void setConfirmButtonEnable(boolean enabled) {
        mSetEnotesPassButton.setEnabled(enabled);
    }


    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
