package com.cybexmobile.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.exception.ErrorCodeException;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.helper.ActionBarTitleHelper;
import com.cybexmobile.R;
import com.cybexmobile.faucet.CreateAccountException;
import com.pixplicity.sharp.Sharp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.webkit.WebViewClient.ERROR_FILE_NOT_FOUND;
import static com.cybexmobile.constant.ErrorCode.ERROR_ACCOUNT_OBJECT_EXIST;
import static com.cybexmobile.constant.ErrorCode.ERROR_FILE_READ_FAIL;
import static com.cybexmobile.constant.ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
import static com.cybexmobile.constant.ErrorCode.ERROR_NETWORK_FAIL;
import static com.cybexmobile.constant.ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
import static com.cybexmobile.constant.ErrorCode.ERROR_PASSWORD_CONFIRM_FAIL;
import static com.cybexmobile.constant.ErrorCode.ERROR_PASSWORD_INVALID;
import static com.cybexmobile.constant.ErrorCode.ERROR_SERVER_CREATE_ACCOUNT_FAIL;
import static com.cybexmobile.constant.ErrorCode.ERROR_SERVER_RESPONSE_FAIL;
import static com.cybexmobile.constant.ErrorCode.ERROR_UNKNOWN;

public class RegisterActivity extends AppCompatActivity {
    ImageView mCloudWalletIntroductionQuestionMarker, mPinCodeImageView;
    TextView mAlreadyHaveAccountTextView, mRegisterErrorText;
    AutoCompleteTextView mUserNameTextView;
    EditText mPassWordTextView, mConfirmationTextView, mPinCodeTextView;
    Button mSignInButton;
    String mCapId;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            ActionBarTitleHelper.centeredActionBarTitle(this);
            setActionBarTitle();
        }
        setContentView(R.layout.activity_register);
        initViews();
        requestForPinCode();
        setViews();
        setViewValue();
        setOnClickListener();
    }

    private void setActionBarTitle() {
        if (getSupportActionBar() != null) {
            ImageView backButton = getSupportActionBar().getCustomView().findViewById(R.id.action_bar_close_button);
            ImageView settingButton = getSupportActionBar().getCustomView().findViewById(R.id.action_bar_setting_button);
            settingButton.setVisibility(View.GONE);
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        mCloudWalletIntroductionQuestionMarker = findViewById(R.id.register_cloud_wallet_question_marker);
        mAlreadyHaveAccountTextView = findViewById(R.id.have_account_log_in);
        mUserNameTextView = findViewById(R.id.user_name);
        mPassWordTextView = findViewById(R.id.password);
        mConfirmationTextView = findViewById(R.id.password_confirm);
        mSignInButton = findViewById(R.id.email_sign_in_button);
        mPinCodeImageView = findViewById(R.id.register_pin_code_image);
        mPinCodeTextView = findViewById(R.id.register_pin_code);
        mRegisterErrorText = findViewById(R.id.register_error_text);
    }

    private void setViews() {

    }

    private void requestForPinCode() {
        new Thread(() -> {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://faucet.cybex.io/captcha")
                    .build();
            JSONObject jsonObject;
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    jsonObject = new JSONObject(response.body().string());
                    final String svgString = jsonObject.getString("data");
                    mCapId = jsonObject.getString("id");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Sharp.loadString(svgString).into(mPinCodeImageView);
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setViewValue() {
        setViewValueForLogInTextView();
        setButtonConfigure();
    }

    private void setViewValueForLogInTextView() {
        String text1 = getResources().getString(R.string.have_account_login);
        String text2 = getResources().getString(R.string.action_sign_in);
        mAlreadyHaveAccountTextView.setText(Html.fromHtml("<font color=\"#FFFFFF\">" + text1 + "</font>  " + "<font color=\"#ff9143\">" + text2 + "</font>"));
    }

    private void setButtonConfigure() {
        mSignInButton.setText(getResources().getString(R.string.create_account));
    }

    private void setOnClickListener() {
        mCloudWalletIntroductionQuestionMarker.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, WalletIntroductionActivity.class);
            startActivity(intent);
        });
        mAlreadyHaveAccountTextView.setOnClickListener(v -> onBackPressed());
        mSignInButton.setOnClickListener(v -> {
            String account = mUserNameTextView.getText().toString();
            String password = mPassWordTextView.getText().toString();
            String passwordConfirm = mConfirmationTextView.getText().toString();
            String pinCode = mPinCodeTextView.getText().toString();

            boolean bError = false;
            if (account.isEmpty()) {
                mRegisterErrorText.setText(R.string.create_account_account_name_empty);
                bError = true;
            }

            if (!account.isEmpty() && password.isEmpty()) {
                mRegisterErrorText.setText(R.string.create_account_password_empty);
                bError = true;
            }

            if (!account.isEmpty() && !password.isEmpty() && passwordConfirm.isEmpty()) {
                mRegisterErrorText.setText(R.string.create_account_password_confirm_empty);
                bError = true;
            }

            if (!account.isEmpty() && !password.isEmpty() && !passwordConfirm.isEmpty() && pinCode.isEmpty()) {
                mRegisterErrorText.setText(R.string.create_account_pin_code_empty);
            }

            if (!bError && mRegisterErrorText.getText().length() == 0 && mRegisterErrorText.getText().length() == 0) {
                processCreateAccount(account, password, passwordConfirm, pinCode, mCapId);

            } else {

            }

        });

        mPinCodeImageView.setOnClickListener(v -> requestForPinCode());
    }

    @SuppressLint("CheckResult")
    private void processCreateAccount(final String strAccount, final String strPassword, String strPasswordConfirm, String pinCode, String capId) {
        if (strPassword.compareTo(strPasswordConfirm) != 0) {
            processErrorCode(ERROR_PASSWORD_CONFIRM_FAIL);
            return;
        }

        Flowable.just(0)
                .subscribeOn(Schedulers.io())
                .map(integer -> {
                    int nRet = BitsharesWalletWraper.getInstance().build_connect();
                    if (nRet != 0) {
                        throw new NetworkStatusException("it failed to connect to server");
                    }

                    nRet = BitsharesWalletWraper.getInstance().create_account_with_password(
                            strAccount,
                            strPassword,
                            pinCode,
                            capId
                    );
                    if (nRet != 0) {
                        Observable.error(new ErrorCodeException(nRet, "it failed to create account"));
                    }
                    return nRet;
                }).map(result -> {
            int nCount = 0;
            int nRet;
            do {
                // 进入导入帐号流程
                nRet = BitsharesWalletWraper.getInstance().import_account_password(
                        strAccount,
                        strPassword
                );
                nCount++;
                if (nRet == ERROR_NO_ACCOUNT_OBJECT) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (nRet == ERROR_NO_ACCOUNT_OBJECT && nCount < 10);
            if (nRet != 0) { // 一切就绪，进入首页
                Observable.error(new ErrorCodeException(nRet, "it failed to import account"));
            }
            return nRet;
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Intent intent = new Intent(RegisterActivity.this, BottomNavigationActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }, throwable -> {
                    if (throwable instanceof NetworkStatusException) {
                        processErrorCode(ERROR_NETWORK_FAIL);
                    } else if (throwable instanceof CreateAccountException) {
                        //processExceptionMessage(throwable.getMessage());
                    } else if (throwable instanceof ErrorCodeException) {
                        ErrorCodeException errorCodeException = (ErrorCodeException) throwable;
                        processErrorCode(errorCodeException.getErrorCode());
                    }
                });

    }

    private void processErrorCode(final int nErrorCode) {
        final TextView textView = findViewById(R.id.register_error_text);
        switch (nErrorCode) {
            case ERROR_NETWORK_FAIL:
                textView.setText(R.string.create_account_activity_network_fail);
                break;
            case ERROR_ACCOUNT_OBJECT_EXIST:
                textView.setText(R.string.create_account_activity_account_object_exist);
                break;
            case ERROR_SERVER_RESPONSE_FAIL:
                textView.setText(R.string.create_account_activity_response_fail);
                break;
            case ERROR_SERVER_CREATE_ACCOUNT_FAIL:
                textView.setText(R.string.create_account_activity_create_fail);
                break;
            case ERROR_FILE_NOT_FOUND:
                textView.setText(R.string.import_activity_file_failed);
                break;
            case ERROR_FILE_READ_FAIL:
                textView.setText(R.string.import_activity_file_failed);
                break;
            case ERROR_NO_ACCOUNT_OBJECT:
                textView.setText(R.string.import_activity_account_name_invalid);
                break;
            case ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY:
                textView.setText(R.string.import_activity_private_key_invalid);
                break;
            case ERROR_PASSWORD_INVALID:
                textView.setText(R.string.import_activity_password_invalid);
                break;
            case ERROR_UNKNOWN:
                textView.setText(R.string.import_activity_unknown_error);
                break;
            default:
                textView.setText(R.string.import_activity_unknown_error);
                break;
        }
    }
}
