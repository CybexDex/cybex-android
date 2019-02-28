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

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

public class SetCloudPasswordActivity extends BaseActivity {

    @BindView(R.id.et_set_cloud_password) EditText mEtSetCloudPassword;
    @BindView(R.id.et_set_cloud_password_confirmation) EditText mEtSetCloudPasswordConfirm;
    @BindView(R.id.set_cloud_pass_ll_error) LinearLayout mSetCloudPassLlError;
    @BindView(R.id.set_cloud_pass_button) Button mSetCloudPassButton;
    @BindView(R.id.set_cloud_pass_error_text) TextView mSetCloudPassErrorText;
    @BindView(R.id.password_check) ImageView mPasswordChecker;
    @BindView(R.id.password_confirm_check)
    ImageView mPasswordConfirmChecker;
    
    private static final String TAG = "SetCloudPasswordActivity";
    private  Unbinder mUnbinder;
    private final String passwordRegPattern = "(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[^a-zA-Z0-9])" + ".{12,}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_cloud_password);
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

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

    private void checkPasswordFormat(String password, String repeatPass){
        if (TextUtils.isEmpty(password) || !password.matches(passwordRegPattern)){
            //密码为空或者不符合规则
            mSetCloudPassLlError.setVisibility(View.VISIBLE);
            mSetCloudPassErrorText.setText(getResources().getString(R.string.create_account_password_error));
            mPasswordChecker.setVisibility(View.GONE);
            setConfirmButtonEnable(false);
        }else {
            //符合规则
            mPasswordChecker.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(repeatPass) && isPasswordMatch(password, repeatPass)){
                //两次输入密码一致
                mSetCloudPassLlError.setVisibility(View.GONE);
                mPasswordConfirmChecker.setVisibility(View.VISIBLE);
                setConfirmButtonEnable(true);
            }else {
                //两次输入密码不一致
                mPasswordConfirmChecker.setVisibility(View.GONE);
                mSetCloudPassLlError.setVisibility(View.VISIBLE);
                mSetCloudPassErrorText.setText(getResources().getString(R.string.create_account_password_confirm_error));
                setConfirmButtonEnable(false);
            }
        }
    }

    private boolean isPasswordMatch(String password, String repeatPass){
        return password.equals(repeatPass);
    }

    private void setConfirmButtonEnable(boolean enabled) {
        mSetCloudPassButton.setEnabled(enabled);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
