package com.cybexmobile.activity.gateway.deposit;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.GetDepositAddress;
import com.apollographql.apollo.NewDepositAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.CacheControl;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fragment.AccountAddressRecord;
import com.cybex.basemodule.constant.Constant;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.records.DepositWithdrawRecordsActivity;
import com.cybex.provider.apollo.ApolloClientApi;
import com.cybex.basemodule.base.BaseActivity;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybexmobile.activity.web.WebActivity;
import com.cybexmobile.utils.AntiMultiClick;
import com.cybex.basemodule.utils.DateUtils;
import com.cybexmobile.utils.QRCode;

import java.util.Locale;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class DepositActivity extends BaseActivity {
    private static int REQUEST_PERMISSION = 1;
    private static String EOS_NAME = "EOS";

    private Unbinder mUnbinder;
    private Handler mHandler = new Handler();
    private Context mContext;
    private AssetObject mAssetObject;

    private String mUserName;
    private String mAssetName;
    private String mEnMsg;
    private String mCnMsg;
    private String mEnInfo;
    private String mCnInfo;
    private boolean mIsEnabled;


    @BindView(R.id.deposit_linear_layout)
    LinearLayout mNormalLinearLayout;
    @BindView(R.id.deposit_eos_linear_layout)
    LinearLayout mEosLinearLayout;
    @BindView(R.id.deposit_eos_account)
    TextView mEosAccountNameTv;
    @BindView(R.id.deposit_eos_copy_account_name)
    LinearLayout mEosCopyAccountLinearLayout;
    @BindView(R.id.deposit_qr_code)
    ImageView mQRCodeView;
    @BindView(R.id.deposit_save_qr_address)
    TextView mSaveQRCodeView;
    @BindView(R.id.deposit_get_new_address_icon)
    ImageView mGetNewAddressIcon;
    @BindView(R.id.deposit_qr_address)
    TextView mQRAddressView;
    @BindView(R.id.deposit_copy_address)
    LinearLayout mCopyAddressLayout;
    @BindView(R.id.deposit_copy_address_tv)
    TextView mCopyAddressTv;
    @BindView(R.id.deposit_get_new_address)
    LinearLayout mGetNewAddress;
    @BindView(R.id.deposit_get_new_address_tv)
    TextView mGetNewAddressTv;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mContext = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = sharedPreferences.getString("name", "");
        Intent intent = getIntent();
        mAssetName = intent.getStringExtra("assetName");
        mIsEnabled = intent.getBooleanExtra("isEnabled", true);
        mEnMsg = intent.getStringExtra("enMsg");
        mCnMsg = intent.getStringExtra("cnMsg");
        mEnInfo = intent.getStringExtra("enInfo");
        mCnInfo = intent.getStringExtra("cnInfo");
        mAssetObject = (AssetObject) intent.getSerializableExtra("assetObject");
        mToolbarTextView.setText(String.format("%s " + getResources().getString(R.string.gate_way_deposit), mAssetName));
        mTvProtocolAddress.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        if (mIsEnabled) {
            if (mAssetName.equals(EOS_NAME)) {
                mEosLinearLayout.setVisibility(View.VISIBLE);
                mNormalLinearLayout.setVisibility(View.GONE);
                mCopyAddressTv.setText(getResources().getString(R.string.deposit_eos_copy_code));
                mGetNewAddressTv.setText(getResources().getString(R.string.deposit_eos_get_new_code));
            }
            getAddress(mUserName, mAssetName);
            requestDetailMessage();
        } else {
            if (Locale.getDefault().getLanguage().equals("zh")) {
                ToastMessage.showNotEnableDepositToastMessage(this, mCnMsg, R.drawable.ic_error_16px);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(this, mEnMsg, R.drawable.ic_error_16px);
            }
        }
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
    }

    @OnClick(R.id.deposit_copy_address)
    public void onClickCopyAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            if (mQRAddressView.getText() != null) {
                copyAddress(mQRAddressView.getText().toString());
            }
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_copied), R.drawable.ic_check_circle_green);
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
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_copied), R.drawable.ic_check_circle_green);
        }
    }

    @OnClick(R.id.deposit_save_qr_address)
    public void onClickSaveAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            checkImageGalleryPermission();
        }
    }

    @OnClick(R.id.deposit_get_new_address)
    public void onClickGetNewAddress(View view) {
        if (AntiMultiClick.isFastClick()) {
            Animation animation = getAnimation();
            mutateNewAddress(mUserName, mAssetName);
        }
    }

    private void requestDetailMessage() {
        if (Locale.getDefault().getLanguage().equals("zh")) {
            mTextMessage.setText(mCnInfo);
        } else {
            mTextMessage.setText(mEnInfo);
        }
    }

    private void getAddress(String userName, String assetName) {
        showLoadDialog(true);
        ApolloClientApi.getInstance().client().query(GetDepositAddress
                .builder()
                .accountName(userName)
                .asset(assetName)
                .build())
                .watcher().refetchCacheControl(CacheControl.NETWORK_FIRST)
                .enqueueAndWatch(new ApolloCall.Callback<GetDepositAddress.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetDepositAddress.Data> response) {
                        if (response.data() != null) {
                            if (response.data().getDepositAddress() != null) {
                                AccountAddressRecord accountAddressRecord = response.data().getDepositAddress().fragments().accountAddressRecord();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (assetName.equals(EOS_NAME)) {
                                            String eosAccountName = accountAddressRecord.address().substring(0, accountAddressRecord.address().indexOf("["));
                                            String verificationCode = accountAddressRecord.address().substring(accountAddressRecord.address().indexOf("[") + 1, accountAddressRecord.address().indexOf("]"));
                                            mEosAccountNameTv.setText(eosAccountName);
                                            mQRAddressView.setText(verificationCode);
                                        } else {
                                            if (mQRAddressView != null) {
                                                mQRAddressView.setText(accountAddressRecord.address());
                                                generateBarCode(accountAddressRecord.address());
                                            }
                                        }
                                        AccountAddressRecord.ProjectInfo projectInfo = accountAddressRecord.projectInfo();
                                        if(projectInfo != null){
                                            mTvProjectName.setText(TextUtils.isEmpty(projectInfo.projectName()) ? "" : projectInfo.projectName());
                                            mTvProtocolAddress.setText(TextUtils.isEmpty(projectInfo.contractAddress()) ? "" : projectInfo.contractAddress());
                                            mTvProtocolAddress.setTag(TextUtils.isEmpty(projectInfo.contractExplorerUrl()) ? "" : projectInfo.contractExplorerUrl());
                                            mLayoutProjectName.setVisibility(TextUtils.isEmpty(projectInfo.projectName()) ? View.GONE : View.VISIBLE);
                                            mLayoutProtocolAddress.setVisibility(TextUtils.isEmpty(projectInfo.contractAddress()) ? View.GONE : View.VISIBLE);
                                        } else {
                                            mLayoutProjectName.setVisibility(View.GONE);
                                            mLayoutProtocolAddress.setVisibility(View.GONE);
                                        }
                                        hideLoadDialog();
                                    }
                                });
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastMessage.showNotEnableDepositToastMessage((Activity) mContext, getResources().getString(R.string.snack_bar_please_retry), R.drawable.ic_error_16px);

                                    }
                                });
                                hideLoadDialog();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        hideLoadDialog();
                    }
                });
    }

    private void mutateNewAddress(String userName, String asset) {
        ApolloClientApi.getInstance().client().mutate(NewDepositAddress
                .builder()
                .accountName(userName)
                .asset(asset)
                .build())
                .enqueue(new ApolloCall.Callback<NewDepositAddress.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<NewDepositAddress.Data> response) {
                        if (response.data() != null) {
                            AccountAddressRecord accountAddressRecord = response.data().newDepositAddress().fragments().accountAddressRecord();
                            String address = accountAddressRecord.address();
                            if (DateUtils.formatToMillis(accountAddressRecord.createAt().toString()) != 0) {
                                if (mAssetName.equals(EOS_NAME)) {
                                    String eosAccountName = address.substring(0, address.indexOf("["));
                                    String verificationCode = address.substring(address.indexOf("[") + 1, address.indexOf("]"));
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mEosAccountNameTv.setText(eosAccountName);
                                            mQRAddressView.setText(verificationCode);
                                        }
                                    });
                                } else {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mQRAddressView.setText(address);
                                            generateBarCode(address);
                                        }
                                    });
                                }
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastMessage.showNotEnableDepositToastMessage((Activity) mContext, getResources().getString(R.string.snack_bar_please_retry), R.drawable.ic_error_16px);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.d("mutateAddress", e.getLocalizedMessage());
                    }
                });
    }

    private void generateBarCode(String barcode) {
        Bitmap bitmap = QRCode.createQRCodeWithLogo(barcode, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mQRCodeView.setImageBitmap(bitmap);
    }

    private Animation getAnimation() {
        Animation animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);
        return animation;
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

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
