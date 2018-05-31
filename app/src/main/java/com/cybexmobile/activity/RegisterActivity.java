package com.cybexmobile.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.exception.ErrorCodeException;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.faucet.CreateAccountException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.pixplicity.sharp.Sharp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

public class RegisterActivity extends BaseActivity {
    ImageView mCloudWalletIntroductionQuestionMarker, mPinCodeImageView, mUserNameChecker, mPasswordChecker, mPasswordConfirmChecker, mRegisterErrorSign;
    ImageView mUserNameicon, mPasswordIcon, mPasswordConfirmIcon, mPinCodeIcon;
    TextView mTvLoginIn, mRegisterErrorText;
    EditText mPassWordTextView, mConfirmationTextView, mPinCodeTextView, mUserNameTextView;
    Button mSignInButton;
    private Toolbar mToolbar;
    String mCapId;
    Timer mTimer = new Timer();
    Task mTask = new Task();
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();
        mTimer.schedule(mTask, 0, 120 * 1000);
        setViews();
        setOnClickListener();
    }

    public class Task extends TimerTask {
        @Override
        public void run() {
            requestForPinCode();
        }
    }

    private void initViews() {
        mCloudWalletIntroductionQuestionMarker = findViewById(R.id.register_cloud_wallet_question_marker);
        mTvLoginIn = findViewById(R.id.tv_login_in);
        mUserNameTextView = findViewById(R.id.register_et_account_name);
        mPassWordTextView = findViewById(R.id.register_et_password);
        mConfirmationTextView = findViewById(R.id.register_et_password_confirmation);
        mSignInButton = findViewById(R.id.email_sign_in_button);
        mPinCodeImageView = findViewById(R.id.register_pin_code_image);
        mPinCodeTextView = findViewById(R.id.register_et_pin_code);
        mRegisterErrorText = findViewById(R.id.register_error_text);
        mUserNameChecker = findViewById(R.id.user_name_check);
        mPasswordChecker = findViewById(R.id.password_check);
        mPasswordConfirmChecker = findViewById(R.id.password_confirm_check);
        mRegisterErrorSign = findViewById(R.id.register_error_sign);
        mToolbar = findViewById(R.id.toolbar);
        mUserNameicon = findViewById(R.id.register_account_icon);
        mPasswordIcon = findViewById(R.id.register_password_icon);
        mPasswordConfirmIcon = findViewById(R.id.register_password_confirmation_icon);
        mPinCodeIcon = findViewById(R.id.register_pin_code_icon);
        setSupportActionBar(mToolbar);
    }

    private void setViews() {
        mUserNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strAccountName = s.toString();
                if (strAccountName.isEmpty()) {
                    return;
                }
                if (!Character.isLetter(strAccountName.charAt(0))) {
                    mRegisterErrorText.setText(R.string.create_account_account_name_error_start_letter);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                    mUserNameChecker.setVisibility(View.GONE);
                } else if (strAccountName.length() < 3) {
                    mRegisterErrorText.setText(R.string.create_account_account_name_too_short);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                    mUserNameChecker.setVisibility(View.GONE);
                } else if (strAccountName.contains("--")) {
                    mRegisterErrorText.setText(R.string.create_account_account_name_should_not_contain_continuous_dashes);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                    mUserNameChecker.setVisibility(View.GONE);
                } else if (strAccountName.endsWith("-")) {
                    mRegisterErrorText.setText(R.string.create_account_account_name_error_dash_end);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                    mUserNameChecker.setVisibility(View.GONE);
                } else {
                    if (strAccountName.matches("^[A-Za-z]+$")) {
                        mRegisterErrorText.setText(R.string.create_account_account_name_error_full_letter);
                        mRegisterErrorSign.setVisibility(View.VISIBLE);
                        mUserNameChecker.setVisibility(View.GONE);
                    } else {
                        mRegisterErrorText.setText("");
                        mRegisterErrorSign.setVisibility(View.GONE);
                        mUserNameChecker.setVisibility(View.VISIBLE);
                        processCheckAccount(strAccountName);
                    }
                    setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                            mPasswordChecker.getVisibility() == View.VISIBLE &&
                            mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                            !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
                }


            }
        });

        mUserNameTextView.setOnFocusChangeListener(onFocusChangeListener);

        mPassWordTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPassword = s.toString();
                if (strPassword.length() < 12) {
                    mRegisterErrorText.setText(R.string.create_account_password_must_at_least_12_characters);
                    mPasswordChecker.setVisibility(View.GONE);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                } else {
                    mRegisterErrorText.setText("");
                    mPasswordChecker.setVisibility(View.VISIBLE);
                    mRegisterErrorSign.setVisibility(View.GONE);
                }
                setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                        mPasswordChecker.getVisibility() == View.VISIBLE &&
                        mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                        !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
            }
        });

        mPassWordTextView.setOnFocusChangeListener(onFocusChangeListener);

        mConfirmationTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPassword = mPassWordTextView.getText().toString();
                String strPasswordConfirm = s.toString();
                if (strPassword.compareTo(strPasswordConfirm) == 0) {
                    mPasswordConfirmChecker.setVisibility(View.VISIBLE);
                    mRegisterErrorText.setText("");
                    mRegisterErrorSign.setVisibility(View.GONE);
                } else {
                    mPasswordConfirmChecker.setVisibility(View.INVISIBLE);
                    mRegisterErrorText.setText(R.string.create_account_password_confirm_error);
                    mRegisterErrorSign.setVisibility(View.VISIBLE);
                }
                setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                        mPasswordChecker.getVisibility() == View.VISIBLE &&
                        mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                        !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
            }
        });

        mConfirmationTextView.setOnFocusChangeListener(onFocusChangeListener);
        mPinCodeTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                        mPasswordChecker.getVisibility() == View.VISIBLE &&
                        mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                        !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
            }
        });

        mPinCodeTextView.setOnFocusChangeListener(onFocusChangeListener);

    }

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.register_et_account_name:
                    mUserNameicon.setAlpha(hasFocus ? 1f : 0.5f);
                    break;
                case R.id.register_et_password:
                    mPasswordIcon.setAlpha(hasFocus ? 1f : 0.5f);
                    break;
                case R.id.register_et_password_confirmation:
                    mPasswordConfirmIcon.setAlpha(hasFocus ? 1f : 0.5f);
                    break;
                case R.id.register_et_pin_code:
                    mPinCodeIcon.setAlpha(hasFocus ? 1f : 0.5f);
                    break;
            }
        }
    };

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

    private void setOnClickListener() {
        mCloudWalletIntroductionQuestionMarker.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, WalletIntroductionActivity.class);
            startActivity(intent);
        });
        mTvLoginIn.setOnClickListener(v -> onBackPressed());
        mSignInButton.setOnClickListener(v -> {
            showLoadDialog();
            String account = mUserNameTextView.getText().toString().trim();
            String password = mPassWordTextView.getText().toString().trim();
            String passwordConfirm = mConfirmationTextView.getText().toString();
            String pinCode = mPinCodeTextView.getText().toString().trim();
            if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(passwordConfirm) || TextUtils.isEmpty(pinCode)) {
                return;
            }
            processCreateAccount(account, password, passwordConfirm, pinCode, mCapId);
        });

        mPinCodeImageView.setOnClickListener(v -> requestForPinCode());
    }

    private void setRegisterButtonEnable(boolean enabled) {
        mSignInButton.setEnabled(enabled);
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
                    hideLoadDialog();
                    CybexDialog.showRegisterDialog(this, strPassword, new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RegisterActivity.this, BottomNavigationActivity.class);
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();
                            sharedPreferences.edit().putString("name", strAccount).apply();
                            sharedPreferences.edit().putString("password", strPassword).apply();
                            startActivity(intent);
                        }
                    });
                }, throwable -> {
                    hideLoadDialog();
                    if (throwable instanceof NetworkStatusException) {
                        processErrorCode(ERROR_NETWORK_FAIL);
                    } else if (throwable instanceof CreateAccountException) {
                        processExceptionMessage(throwable.getMessage());
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

    private void processExceptionMessage(final String strMessage) {
        mRegisterErrorText.setText(strMessage);
        mRegisterErrorSign.setVisibility(View.VISIBLE);
    }

    private void processCheckAccount(final String strAccount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int nRet = BitsharesWalletWraper.getInstance().build_connect();
                if (nRet == 0) {
                    try {
                        final AccountObject accountObect = BitsharesWalletWraper.getInstance().get_account_object(strAccount);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (accountObect == null) {
                                    mUserNameChecker.setVisibility(View.VISIBLE);
                                    mRegisterErrorSign.setVisibility(View.GONE);
                                } else {
                                    if (strAccount.compareTo(accountObect.name) == 0) {
                                        mRegisterErrorText.setText(R.string.create_account_activity_account_object_exist);
                                        mRegisterErrorSign.setVisibility(View.VISIBLE);
                                        mUserNameChecker.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });

                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
