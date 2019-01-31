package com.cybexmobile.activity.register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.activity.main.BottomNavigationActivity;
import com.cybexmobile.activity.introduction.WalletIntroductionActivity;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybexmobile.exception.ErrorCodeException;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybexmobile.exception.CreateAccountException;
import com.cybexmobile.faucet.CreateAccountRequest;
import com.cybex.provider.http.response.CreateAccountResponse;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.PrivateKey;
import com.cybex.provider.graphene.chain.Types;
import com.cybexmobile.utils.VirtualBarUtil;
import com.google.gson.Gson;
import com.pixplicity.sharp.Sharp;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static android.webkit.WebViewClient.ERROR_FILE_NOT_FOUND;
import static com.cybex.provider.constant.ErrorCode.ERROR_ACCOUNT_OBJECT_EXIST;
import static com.cybex.provider.constant.ErrorCode.ERROR_FILE_READ_FAIL;
import static com.cybex.provider.constant.ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
import static com.cybex.provider.constant.ErrorCode.ERROR_NETWORK_FAIL;
import static com.cybex.provider.constant.ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
import static com.cybex.provider.constant.ErrorCode.ERROR_PASSWORD_CONFIRM_FAIL;
import static com.cybex.provider.constant.ErrorCode.ERROR_PASSWORD_INVALID;
import static com.cybex.provider.constant.ErrorCode.ERROR_SERVER_CREATE_ACCOUNT_FAIL;
import static com.cybex.provider.constant.ErrorCode.ERROR_SERVER_RESPONSE_FAIL;
import static com.cybex.provider.constant.ErrorCode.ERROR_UNKNOWN;
import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_PASSWORD;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";
    @BindView(R.id.register_cloud_wallet_question_marker)
    ImageView mCloudWalletIntroductionQuestionMarker;
    @BindView(R.id.register_pin_code_image)
    SVGImageView mPinCodeImageView;
    @BindView(R.id.user_name_check)
    ImageView mUserNameChecker;
    @BindView(R.id.password_check)
    ImageView mPasswordChecker;
    @BindView(R.id.password_confirm_check)
    ImageView mPasswordConfirmChecker;
    @BindView(R.id.register_error_sign)
    ImageView mRegisterErrorSign;
    @BindView(R.id.register_password_icon)
    ImageView mPasswordIcon;
    @BindView(R.id.register_password_confirmation_icon)
    ImageView mPasswordConfirmIcon;
    @BindView(R.id.register_pin_code_icon)
    ImageView mPinCodeIcon;
    @BindView(R.id.register_account_icon)
    ImageView mUserNameicon;
    @BindView(R.id.tv_login_in)
    TextView mTvLoginIn;
    @BindView(R.id.register_error_text)
    TextView mRegisterErrorText;
    @BindView(R.id.register_et_account_name)
    EditText mEtUserName;
    @BindView(R.id.register_et_password)
    EditText mEtPassWord;
    @BindView(R.id.register_et_password_confirmation)
    EditText mEtPasswordConfirm;
    @BindView(R.id.register_et_pin_code)
    EditText mPinCodeTextView;
    @BindView(R.id.email_sign_in_button)
    Button mSignInButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.register_ll_container)
    LinearLayout mLayoutContainer;
    @BindView(R.id.register_ll_error)
    LinearLayout mLayoutError;

    private Disposable mDisposablePinCode;

    String mCapId;
    private int mLastScrollHeight;
    private int mVirtualBarHeight = -1;
    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setOnClickListener();
        requestForPinCode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDisposablePinCode != null && !mDisposablePinCode.isDisposed()){
            mDisposablePinCode.dispose();
        }
        mUnbinder.unbind();
    }

    private void checkUserName(String username){
        if(TextUtils.isEmpty(username)){
            checkPassword(mEtPassWord.getText().toString());
        } else if (!Character.isLetter(username.charAt(0))) {
            mRegisterErrorText.setText(R.string.create_account_account_name_error_start_letter);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else if(!username.matches("^[a-z0-9-]+$")){
            mRegisterErrorText.setText(R.string.create_account_account_name_should_only_contain_letter_dash_and_numbers);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else if (username.length() < 3) {
            mRegisterErrorText.setText(R.string.create_account_account_name_too_short);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else if (username.contains("--")) {
            mRegisterErrorText.setText(R.string.create_account_account_name_should_not_contain_continuous_dashes);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else if (username.endsWith("-")) {
            mRegisterErrorText.setText(R.string.create_account_account_name_error_dash_end);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else if (username.matches("^[a-z]+$")) {
            mRegisterErrorText.setText(R.string.create_account_account_name_error_full_letter);
            mRegisterErrorSign.setVisibility(View.VISIBLE);
            mUserNameChecker.setVisibility(View.GONE);
        } else {
            processCheckAccount(username);
        }
        setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                mPasswordChecker.getVisibility() == View.VISIBLE &&
                mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
    }

    private void checkPassword(String password){
        if(TextUtils.isEmpty(password)){
            checkPasswordConfirm(mEtPasswordConfirm.getText().toString());
        } else if (!password.matches("(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[^a-zA-Z0-9]).{12,}")) {
            if(mUserNameChecker.getVisibility() == View.VISIBLE || TextUtils.isEmpty(mEtUserName.getText().toString())){
                mRegisterErrorText.setText(getResources().getString(R.string.create_account_password_error));
                mRegisterErrorSign.setVisibility(View.VISIBLE);
            }
            mPasswordChecker.setVisibility(View.GONE);
            mPasswordConfirmChecker.setVisibility(View.GONE);
        } else {
            mRegisterErrorText.setText("");
            mPasswordChecker.setVisibility(View.VISIBLE);
            mRegisterErrorSign.setVisibility(View.GONE);
            checkPasswordConfirm(mEtPasswordConfirm.getText().toString());
        }
        setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                mPasswordChecker.getVisibility() == View.VISIBLE &&
                mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
    }

    private void checkPasswordConfirm(String passwordConfirm){
        if (mPasswordChecker.getVisibility() == View.GONE) { return; }
        String strPassword = mEtPassWord.getText().toString();
        if (!strPassword.equals(passwordConfirm)) {
            //提示密码不一致
            if((mUserNameChecker.getVisibility() == View.VISIBLE || TextUtils.isEmpty(mEtUserName.getText().toString()))
                    && (mPasswordChecker.getVisibility() == View.VISIBLE || TextUtils.isEmpty(mEtPassWord.getText().toString()))){
                mRegisterErrorText.setText(R.string.create_account_password_confirm_error);
                mRegisterErrorSign.setVisibility(View.VISIBLE);
            }
            mPasswordConfirmChecker.setVisibility(View.INVISIBLE);
        } else {
            if(!TextUtils.isEmpty(passwordConfirm)){
                mPasswordConfirmChecker.setVisibility(View.VISIBLE);
                mRegisterErrorText.setText("");
                mRegisterErrorSign.setVisibility(View.GONE);
            }
        }
        setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                mPasswordChecker.getVisibility() == View.VISIBLE &&
                mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
    }

    @OnTextChanged(value = R.id.register_et_account_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onUserNameEditChanged(Editable editable){
        checkUserName(editable.toString());
    }

    @OnTextChanged(value = R.id.register_et_password, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPasswordEditChanged(Editable editable){
        checkPassword(editable.toString());
    }

    @OnTextChanged(value = R.id.register_et_password_confirmation, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPasswordConfirmationEditChanged(Editable editable){
        checkPasswordConfirm(editable.toString());
    }

    @OnTextChanged(value = R.id.register_et_pin_code, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onPinCodeEditChanged(Editable editable){
        setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                mPasswordChecker.getVisibility() == View.VISIBLE &&
                mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
    }

    @OnFocusChange({R.id.register_et_account_name, R.id.register_et_password,
            R.id.register_et_password_confirmation, R.id.register_et_pin_code})
    public void onUserNameTextFocusChanged(View view, boolean hasFocus){
        switch (view.getId()) {
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

    @OnClick(R.id.register_cloud_wallet_question_marker)
    public void onCloudWalletQuestionMarkerClick(View view){
        Intent intent = new Intent(RegisterActivity.this, WalletIntroductionActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.email_sign_in_button)
    public void onSignInClick(View view){
        String account = mEtUserName.getText().toString().trim();
        String password = mEtPassWord.getText().toString().trim();
        String passwordConfirm = mEtPasswordConfirm.getText().toString();
        String pinCode = mPinCodeTextView.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(passwordConfirm) || TextUtils.isEmpty(pinCode)) {
            return;
        }
        processCreateAccount(account, password, passwordConfirm, pinCode, mCapId);
    }

    @OnClick(R.id.tv_login_in)
    public void onLoginInClick(View view){
        onBackPressed();
    }

    @OnClick(R.id.register_pin_code_image)
    public void onRegisterPinCodeClick(View view){
        requestForPinCode();
    }

    /**
     * fix online crash
     * java.lang.NullPointerException: Attempt to invoke virtual method
     * 'void android.view.View.setBackground(android.graphics.drawable.Drawable)' on a null object reference
     */
    private void requestForPinCode() {
        if(mDisposablePinCode != null && !mDisposablePinCode.isDisposed()){
            mDisposablePinCode.dispose();
        }
        mDisposablePinCode = Flowable.interval(0, 90, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<ResponseBody>>() {
                    @Override
                    public Publisher<ResponseBody> apply(Long aLong) throws Exception {
                        return RetrofitFactory.getInstance().apiFaucet().getPinCode();
                    }
                })
                .retry()
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(responseBody.string());
                            String svgString = jsonObject.getString("data");
                            mCapId = jsonObject.getString("id");
                            SVG svg = SVG.getFromString(svgString);
                            mPinCodeImageView.setSVG(svg, "rect {opacity: 0;}");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SVGParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void setOnClickListener() {
        mLayoutContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(mVirtualBarHeight == -1){
                    mVirtualBarHeight = VirtualBarUtil.getHeight(RegisterActivity.this);
                }
                /**
                 * fix bug
                 * 计算虚拟导航栏的高度
                 */
                Rect rect = new Rect();
                mLayoutContainer.getWindowVisibleDisplayFrame(rect);
                int invisibleHeight = mLayoutContainer.getRootView().getHeight() - (rect.bottom + mVirtualBarHeight);
                if(invisibleHeight > 100){
                    int[] location = new int[2];
                    mLayoutError.getLocationInWindow(location);
                    int scrollHeight = location[1] + mLayoutError.getHeight() - rect.bottom;
                    mLastScrollHeight += scrollHeight;
                    mLayoutContainer.scrollTo(0, mLastScrollHeight);
                }else{
                    mLayoutContainer.scrollTo(0, 0);
                    mLastScrollHeight = 0;
                }
            }
        });
    }

    private void setRegisterButtonEnable(boolean enabled) {
        mSignInButton.setEnabled(enabled);
    }


    private String parseAccount(String strAccountName,
                                              String strPassword,
                                              String pinCode,
                                              String capId){
        PrivateKey privateActiveKey = PrivateKey.from_seed(strAccountName + "active" + strPassword);
        PrivateKey privateOwnerKey = PrivateKey.from_seed(strAccountName + "owner" + strPassword);
        Types.public_key_type publicActiveKeyType = new Types.public_key_type(privateActiveKey.get_public_key(true), true);
        Types.public_key_type publicOwnerKeyType = new Types.public_key_type(privateOwnerKey.get_public_key(true), true);
        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        CreateAccountRequest.Account account = new CreateAccountRequest.Account();
        CreateAccountRequest.Cap cap = new CreateAccountRequest.Cap();
        cap.id = capId;
        cap.captcha = pinCode;
        createAccountRequest.cap = cap;
        account.name = strAccountName;
        account.active_key = publicActiveKeyType;
        account.owner_key = publicOwnerKeyType;
        account.memo_key = publicActiveKeyType;
        account.refcode = null;
        account.referrer = null;
        createAccountRequest.account = account;
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        Log.v(TAG, gson.toJson(createAccountRequest));
        return gson.toJson(createAccountRequest);
    }

    private void processCreateAccount(final String strAccount, final String strPassword, String strPasswordConfirm, String pinCode, String capId) {
        if (strPassword.compareTo(strPasswordConfirm) != 0) {
            processErrorCode(ERROR_PASSWORD_CONFIRM_FAIL);
            return;
        }
        showLoadDialog();
        RetrofitFactory.getInstance()
                .apiFaucet()
                .register(RequestBody.create(MediaType.parse("application/json"), parseAccount(strAccount, strPassword, pinCode, capId)))
                .map(new Function<CreateAccountResponse, CreateAccountResponse>() {
                    @Override
                    public CreateAccountResponse apply(CreateAccountResponse createAccountResponse) {
                        if(!TextUtils.isEmpty(createAccountResponse.error)){
                            Observable.error(new CreateAccountException(createAccountResponse.error));
                        }
                        return createAccountResponse;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.v(TAG, "processCreateAccount: onSubscribe");
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.v(TAG, "processCreateAccount: onNext");
                        login(strAccount, strPassword);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(TAG, "processCreateAccount: onError");
                        hideLoadDialog();
                        if (e instanceof NetworkStatusException) {
                            processErrorCode(ERROR_NETWORK_FAIL);
                        } else if (e instanceof CreateAccountException) {
                            processExceptionMessage(e.getMessage());
                        } else if (e instanceof ErrorCodeException) {
                            ErrorCodeException errorCodeException = (ErrorCodeException) e;
                            processErrorCode(errorCodeException.getErrorCode());
                        } else if (e instanceof HttpException) {
                            processExceptionMessage(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.v(TAG, "processCreateAccount: onComplete");

                    }
                });
    }

    private void login(String account, String password){
        try {
            BitsharesWalletWraper.getInstance().get_account_object(account, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    int result = BitsharesWalletWraper.getInstance().import_account_password(accountObject, account, password);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadDialog();
                            if (result == 0) {
                                hideLoadDialog();
                                CybexDialog.showRegisterDialog(RegisterActivity.this, password, new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(RegisterActivity.this, BottomNavigationActivity.class);
                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                        sharedPreferences.edit().putBoolean(PREF_IS_LOGIN_IN, true).apply();
                                        sharedPreferences.edit().putString(PREF_NAME, account).apply();
                                        sharedPreferences.edit().putString(PREF_PASSWORD, password).apply();
                                        startActivity(intent);
                                    }
                                });
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
        if (strMessage.contains("403")) {
            mRegisterErrorText.setText(getResources().getString(R.string.create_account_pin_code_not_correct));
        } else {
            mRegisterErrorText.setText(getResources().getString(R.string.create_account_pin_code_not_correct));
        }
        mRegisterErrorSign.setVisibility(View.VISIBLE);
    }

    private void processCheckAccount(final String strAccount) {
        try {
            BitsharesWalletWraper.getInstance().get_account_object(strAccount, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    if (accountObject != null && strAccount.equals(accountObject.name)) {
                        mRegisterErrorText.setText(R.string.create_account_activity_account_object_exist);
                        mRegisterErrorSign.setVisibility(View.VISIBLE);
                        mUserNameChecker.setVisibility(View.GONE);
                    } else {
                        mRegisterErrorText.setText("");
                        mUserNameChecker.setVisibility(View.VISIBLE);
                        mRegisterErrorSign.setVisibility(View.GONE);
                        checkPassword(mEtPassWord.getText().toString());
                    }
                    setRegisterButtonEnable(mUserNameChecker.getVisibility() == View.VISIBLE &&
                            mPasswordChecker.getVisibility() == View.VISIBLE &&
                            mPasswordConfirmChecker.getVisibility() == View.VISIBLE &&
                            !TextUtils.isEmpty(mPinCodeTextView.getText().toString().trim()));
                }

                @Override
                public void onFailure() {

                }
            });


        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
