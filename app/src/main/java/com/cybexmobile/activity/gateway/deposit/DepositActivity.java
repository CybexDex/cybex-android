package com.cybexmobile.activity.gateway.deposit;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryWatcher;
import com.apollographql.apollo.GetDepositAddress;
import com.apollographql.apollo.NewDepositAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.fragment.AccountAddressRecord;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.http.GatewayHttpApi;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.records.DepositWithdrawRecordsActivity;
import com.cybex.provider.apollo.ApolloClientApi;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybexmobile.activity.web.WebActivity;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.shake.AntiShake;
import com.cybexmobile.utils.AntiMultiClick;
import com.cybex.basemodule.utils.DateUtils;
import com.cybexmobile.utils.QRCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class DepositActivity extends BaseActivity {
    private static int REQUEST_PERMISSION = 1;
    private static String EOS_NAME = "EOS";
    private static String XRP_NAME = "XRP";
    private static String ATOM_NAME = "ATOM";
    private static String IRIS_NAME = "IRIS";

    private Unbinder mUnbinder;
    private Context mContext;
    private AssetObject mAssetObject;

    private String mUserName;
    private String mAssetName;
    private String mAssetId;
    private String mEnMsg;
    private String mCnMsg;
    private boolean mIsEnabled;
    private String mSignature;
    private boolean mIsTag;


    @BindView(R.id.deposit_linear_layout)
    LinearLayout mNormalLinearLayout;
    @BindView(R.id.deposit_xrp_copy_address_linear_layout)
    LinearLayout mXrpCopyAddressLinearLayout;
    @BindView(R.id.deposit_xrp_qr_address)
    TextView mXrpAddressTv;
    @BindView(R.id.deposit_xrp_copy_address_button_linear_layout)
    LinearLayout mXrpCopyAddressClickButtonLinearLayout;
    @BindView(R.id.deposit_eos_linear_layout)
    LinearLayout mEosLinearLayout;
    @BindView(R.id.eos_xrp_verification_code_linear_layout)
    LinearLayout mEosXrpTextLayout;
    @BindView(R.id.eos_xrp_verification_tag_tv)
    TextView mEosXrpVerificationCodeTagTv;
    @BindView(R.id.eos_xrp_verification_warning_red_tv)
    TextView mEosXrpWarningRedTv;
    @BindView(R.id.deposit_eos_account)
    TextView mEosAccountNameTv;
    @BindView(R.id.deposit_eos_copy_account_name)
    LinearLayout mEosCopyAccountLinearLayout;
    @BindView(R.id.deposit_qr_code)
    ImageView mQRCodeView;
    @BindView(R.id.deposit_save_qr_address)
    TextView mSaveQRCodeView;
    @BindView(R.id.deposit_qr_address)
    TextView mQRAddressView;
    @BindView(R.id.deposit_copy_address)
    LinearLayout mCopyAddressLayout;
    @BindView(R.id.deposit_copy_address_tv)
    TextView mCopyAddressTv;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.deposit_toolbar_text_view)
    TextView mToolbarTextView;
    @BindView(R.id.deposit_coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.deposit_detail_message)
    TextView mTextMessage;
    @BindView(R.id.deposit_tv_project_name)
    TextView mTvProjectName;
    @BindView(R.id.deposit_tv_protocol_address)
    TextView mTvProtocolAddress;
    @BindView(R.id.deposit_layout_project_name)
    LinearLayout mLayoutProjectName;
    @BindView(R.id.deposit_layout_protocol_address)
    LinearLayout mLayoutProtocolAddress;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mUserName);
            if (fullAccountObject != null) {
                mAccountObject = fullAccountObject.account;
            }
            if (BitsharesWalletWraper.getInstance().is_locked()) {
                CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName,
                        (UnlockDialog.UnLockDialogClickListener) password -> setDepositInfo());
            } else {
                setDepositInfo();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Intent serviceIntent = new Intent(this, WebSocketService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        mContext = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = sharedPreferences.getString("name", "");
        Intent intent = getIntent();
        mAssetName = intent.getStringExtra("assetName");
        mAssetId = intent.getStringExtra("assetId");
        mIsEnabled = intent.getBooleanExtra("isEnabled", true);
        mEnMsg = intent.getStringExtra("enMsg");
        mCnMsg = intent.getStringExtra("cnMsg");
        mIsTag = intent.getBooleanExtra("tag", false);
        mAssetObject = (AssetObject) intent.getSerializableExtra("assetObject");
        mToolbarTextView.setText(String.format("%s " + getResources().getString(R.string.gate_way_deposit), mAssetName));
        mTvProtocolAddress.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageView(mQRCodeView);
            }
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deposit_records, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AntiShake.check(item.getItemId())) { return false; }
        switch (item.getItemId()) {
            case R.id.action_records:
                Intent intent = new Intent(this, DepositWithdrawRecordsActivity.class);
                intent.putExtra("assetObject", mAssetObject);
                intent.putExtra("fundType", "DEPOSIT");
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unbindService(mConnection);
        if(!mCompositeDisposable.isDisposed()){
            mCompositeDisposable.dispose();
        }
    }

    @OnClick(R.id.deposit_copy_address)
    public void onClickCopyAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            if (mQRAddressView.getText() != null) {
                copyAddress(mQRAddressView.getText().toString());
            }
            if (mIsTag) {
                ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_xrp_tag_copied), R.drawable.ic_check_circle_green);

            } else {
                ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_copied), R.drawable.ic_check_circle_green);
            }
        }
    }

    @OnClick(R.id.deposit_tv_protocol_address)
    public void onProtocolAddressClick(View view) {
        String contractAddress = mTvProtocolAddress.getText().toString();
        String contractExplorerUrl = (String) mTvProtocolAddress.getTag();
        if(TextUtils.isEmpty(contractAddress) || TextUtils.isEmpty(contractExplorerUrl)){
            return;
        }
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(Constant.INTENT_PARAM_URL, contractExplorerUrl);
        startActivity(intent);
    }

    @OnClick(R.id.deposit_eos_copy_account_name)
    public void onClickCopyEosAccountName(View view) {
        if (AntiMultiClick.isFastClick()) {
            if (mEosAccountNameTv.getText() != null) {
                copyAddress(mEosAccountNameTv.getText().toString());
            }
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_account_name_copied), R.drawable.ic_check_circle_green);
        }
    }

    @OnClick(R.id.deposit_xrp_copy_address_button_linear_layout)
    public void onClickXrpCopyAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            if (mXrpAddressTv.getText() != null) {
                copyAddress(mXrpAddressTv.getText().toString());
            }
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_copied), R.drawable.ic_check_circle_green);
        }
    }

    @OnClick(R.id.deposit_save_qr_address)
    public void onClickSaveAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            checkImageGalleryPermission();
        }
    }

    private void requestDetailMessage() {
        mCompositeDisposable.add(RetrofitFactory.getInstance()
                .api()
                .getDepositDetails(mAssetId + ".json")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        JSONObject jsonObject = new JSONObject(responseBody.string());
                        if (Locale.getDefault().getLanguage().equals("zh")) {
                            JSONArray project_msg = jsonObject.getJSONArray("msg_cn");
                            JSONArray asset_notice = jsonObject.getJSONObject("notice_cn").getJSONArray("adds");
                            StringBuilder builder = new StringBuilder();
                            if (project_msg != null && project_msg.length() > 0) {
                                mTvProjectName.setText(TextUtils.isEmpty(project_msg.getJSONObject(0).getString("value")) ? "" : project_msg.getJSONObject(0).getString("value"));
                                mTvProtocolAddress.setText(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("value")) ? "" : project_msg.getJSONObject(1).getString("value"));
                                mTvProtocolAddress.setTag(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("link")) ? "" : project_msg.getJSONObject(1).getString("link"));
                                mLayoutProjectName.setVisibility(TextUtils.isEmpty(project_msg.getJSONObject(0).getString("value")) ? View.GONE : View.VISIBLE);
                                mLayoutProtocolAddress.setVisibility(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("value")) ? View.GONE : View.VISIBLE);
                            } else {
                                mLayoutProjectName.setVisibility(View.GONE);
                                mLayoutProtocolAddress.setVisibility(View.GONE);
                            }

                            if (asset_notice != null && asset_notice.length() > 0) {
                                for (int i = 0; i < asset_notice.length(); i++) {
                                    builder.append(asset_notice.getJSONObject(i).getString("text"));
                                    builder.append("\n");
                                }
                                mTextMessage.setText(builder.toString());
                            }
                        } else {
                            JSONArray project_msg = jsonObject.getJSONArray("msg_en");
                            JSONArray asset_notice = jsonObject.getJSONObject("notice_en").getJSONArray("adds");
                            StringBuilder builder = new StringBuilder();
                            if (project_msg != null && project_msg.length() > 0) {
                                mTvProjectName.setText(TextUtils.isEmpty(project_msg.getJSONObject(0).getString("value")) ? "" : project_msg.getJSONObject(0).getString("value"));
                                mTvProtocolAddress.setText(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("value")) ? "" : project_msg.getJSONObject(1).getString("value"));
                                mTvProtocolAddress.setTag(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("link")) ? "" : project_msg.getJSONObject(1).getString("link"));
                                mLayoutProjectName.setVisibility(TextUtils.isEmpty(project_msg.getJSONObject(0).getString("value")) ? View.GONE : View.VISIBLE);
                                mLayoutProtocolAddress.setVisibility(TextUtils.isEmpty(project_msg.getJSONObject(1).getString("value")) ? View.GONE : View.VISIBLE);
                            } else {
                                mLayoutProjectName.setVisibility(View.GONE);
                                mLayoutProtocolAddress.setVisibility(View.GONE);
                            }

                            if (asset_notice != null && asset_notice.length() > 0) {
                                for (int i = 0; i < asset_notice.length(); i++) {
                                    builder.append(asset_notice.getJSONObject(i).getString("text"));
                                    builder.append("\n");
                                }
                                mTextMessage.setText(builder.toString());
                            }
                        }


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }));
    }

    private void getAddress1(String userName, String assetName) {
        showLoadDialog(true);
        mCompositeDisposable.add(Observable.create((ObservableOnSubscribe<String>) e -> {
                    String expiration = String.valueOf(new Date().getTime() / 1000);
                    String message = expiration + userName;
                    mSignature = BitsharesWalletWraper.getInstance().getChatMessageSignature(mAccountObject, message);
                    String mToken = expiration + "." + userName + "." + mSignature;
                    if (!e.isDisposed()) {
                        e.onNext(mToken);
                        e.onComplete();
                    }
                })
                        .concatMap((Function<String, Observable<JsonObject>>) token ->
                                RetrofitFactory.getInstance()
                                        .apiGateway()
                                        .getDepositAddress(
                                                "application/json",
                                                "bearer " + token,
                                                mUserName,
                                                mAssetName
                                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                jsonObject -> {
                                    if (jsonObject == null) {
                                        ToastMessage.showNotEnableDepositToastMessage((Activity) mContext, getResources().getString(R.string.snack_bar_please_retry), R.drawable.ic_error_16px);
                                        hideLoadDialog();
                                        return;
                                    }
                                    String address = jsonObject.get("address").getAsString();
                                    if (address == null) {
                                        hideLoadDialog();
                                        return;
                                    }

                                    if (assetName.equals(EOS_NAME)) {
                                        String eosAccountName = address.substring(0, address.indexOf("["));
                                        String verificationCode = address.substring(address.indexOf("[") + 1, address.indexOf("]"));
                                        mEosAccountNameTv.setText(eosAccountName);
                                        mQRAddressView.setText(verificationCode);
                                    } else if (assetName.equals(XRP_NAME)) {
                                        String xrpAddress = address.substring(0, address.indexOf("["));
                                        String xrpTag = address.substring(address.indexOf("[") + 1, address.indexOf("]"));
                                        mXrpAddressTv.setText(xrpAddress);
                                        mQRAddressView.setText(xrpTag);
                                        generateBarCode(xrpAddress);
                                    } else {
                                        mQRAddressView.setText(address);
                                        generateBarCode(address);
                                    }
                                    hideLoadDialog();


                                },
                                throwable -> {
                                    hideLoadDialog();
                                }
                        )
        );
    }


    private void getAddress(String userName, String assetName) {
        showLoadDialog(true);
        mCompositeDisposable.add(Observable.create((ObservableOnSubscribe<Operations.gateway_login_operation>) e -> {
                    Date expiration = getExpiration();
                    Operations.gateway_login_operation operation = BitsharesWalletWraper.getInstance().getGatewayLoginOperation(userName, expiration);
                    mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
                    if (!e.isDisposed()) {
                        e.onNext(operation);
                        e.onComplete();
                    }
                })
                        .concatMap((Function<Operations.gateway_login_operation, ObservableSource<ResponseBody>>) gateway_login_operation -> {
                            GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(gateway_login_operation, mSignature);
                            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                            Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
                            return RetrofitFactory.getInstance()
                                    .apiGateway()
                                    .gatewayLogIn(RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));
                        })
                        .concatMap((Function<ResponseBody, Observable<JsonObject>>) responseBody ->
                                RetrofitFactory.getInstance()
                                        .apiGateway()
                                        .getDepositAddress(
                                                "application/json",
                                                "bearer " + mSignature,
                                                mUserName,
                                                mAssetName
                                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                jsonObject -> {
                                    if (jsonObject == null) {
                                        ToastMessage.showNotEnableDepositToastMessage((Activity) mContext, getResources().getString(R.string.snack_bar_please_retry), R.drawable.ic_error_16px);
                                        hideLoadDialog();
                                        return;
                                    }
                                    String address = jsonObject.get("address").getAsString();
                                    if (address == null) {
                                        hideLoadDialog();
                                        return;
                                    }

                                    if (mIsTag) {
                                        String xrpAddress = address.substring(0, address.indexOf("["));
                                        String xrpTag = address.substring(address.indexOf("[") + 1, address.indexOf("]"));
                                        mXrpAddressTv.setText(xrpAddress);
                                        mQRAddressView.setText(xrpTag);
                                        generateBarCode(xrpAddress);
                                    }  else {
                                        mQRAddressView.setText(address);
                                        generateBarCode(address);
                                    }
                                    hideLoadDialog();


                                },
                                throwable -> {
                                    hideLoadDialog();
                                }
                        )
        );
    }
        /**
         * fix online bug
         * java.lang.NullPointerException: Attempt to invoke virtual method
         * 'void android.widget.TextView.setText(java.lang.CharSequence)' on a null object reference
         */
//        ApolloQueryWatcher<GetDepositAddress.Data> watcher = ApolloClientApi.getInstance().client()
//                .query(GetDepositAddress.builder().accountName(userName).asset(assetName).build())
//                .watcher()
//                .refetchCacheControl(CacheControl.NETWORK_FIRST);
//        mCompositeDisposable.add(Rx2Apollo.from(watcher)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Response<GetDepositAddress.Data>>() {
//                    @Override
//                    public void accept(Response<GetDepositAddress.Data> response) throws Exception {
//                        GetDepositAddress.Data depositAddressData = response.data();
//                        if(depositAddressData == null){
//                            ToastMessage.showNotEnableDepositToastMessage((Activity) mContext, getResources().getString(R.string.snack_bar_please_retry), R.drawable.ic_error_16px);
//                            hideLoadDialog();
//                            return;
//                        }
//                        GetDepositAddress.GetDepositAddress1 depositAddress = depositAddressData.getDepositAddress();
//                        if(depositAddress == null){
//                            hideLoadDialog();
//                            return;
//                        }
//                        AccountAddressRecord accountAddressRecord = depositAddress.fragments().accountAddressRecord();
//                        if (mIsTag) {
//                            String xrpAddress = accountAddressRecord.address().substring(0, accountAddressRecord.address().indexOf("["));
//                            String xrpTag = accountAddressRecord.address().substring(accountAddressRecord.address().indexOf("[") + 1, accountAddressRecord.address().indexOf("]"));
//                            mXrpAddressTv.setText(xrpAddress);
//                            mQRAddressView.setText(xrpTag);
//                            generateBarCode(xrpAddress);
//                        } else {
//                            mQRAddressView.setText(accountAddressRecord.address());
//                            generateBarCode(accountAddressRecord.address());
//                        }
//                        AccountAddressRecord.ProjectInfo projectInfo = accountAddressRecord.projectInfo();
//                        hideLoadDialog();
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        hideLoadDialog();
//                    }
//                }));
    //}

    private void generateBarCode(String barcode) {
        Bitmap bitmap = QRCode.createQRCodeWithLogo(barcode, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mQRCodeView.setImageBitmap(bitmap);
    }

    private void copyAddress(String address) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", address);
        clipboard.setPrimaryClip(clip);
    }

    private void checkImageGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            saveImageView(mQRCodeView);
        }
    }

    private void saveImageView(ImageView imageView) {
        if (imageView.getDrawable() != null) {
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QRCode", "");
            Log.e("imageUrl", savedImageURL);
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_saved), R.drawable.ic_check_circle_green);
        }
    }

    private Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        return calendar.getTime();
    }

    private GatewayLogInRecordRequest createLogInRequest(Operations.gateway_login_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }

    private void setDepositInfo() {
        if (mIsEnabled) {
            if (mIsTag) {
                mXrpCopyAddressLinearLayout.setVisibility(View.VISIBLE);
                mEosXrpTextLayout.setVisibility(View.VISIBLE);
                mEosXrpVerificationCodeTagTv.setText(getResources().getString(R.string.deposit_xrp_tag_text));
                mEosXrpWarningRedTv.setText(getResources().getString(R.string.deposit_xrp_tag_warning_message));
                mCopyAddressTv.setText(getResources().getString(R.string.deposit_xrp_copy_tag));
            }
            getAddress1(mUserName, mAssetName);
            requestDetailMessage();
        } else {
            if (Locale.getDefault().getLanguage().equals("zh")) {
                ToastMessage.showNotEnableDepositToastMessage(DepositActivity.this, mCnMsg, R.drawable.ic_error_16px);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(DepositActivity.this, mEnMsg, R.drawable.ic_error_16px);
            }
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
