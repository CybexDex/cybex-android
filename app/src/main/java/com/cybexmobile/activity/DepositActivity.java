package com.cybexmobile.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.GetDepositAddress;
import com.apollographql.apollo.NewDepositAddress;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.cybexmobile.R;
import com.cybexmobile.api.ApolloClientApi;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.helper.StoreLanguageHelper;
import com.cybexmobile.toast.message.ToastMessage;
import com.cybexmobile.utils.SnackBarUtils;
import com.github.sumimakito.awesomeqr.AwesomeQRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


public class DepositActivity extends BaseActivity {
    private static int REQUEST_PERMISSION = 1;

    private Unbinder mUnbinder;
    private ApolloClient mApolloClient;
    private Handler mHandler = new Handler();
    private String mUserName;
    private String mAssetName;
    private String mEnMsg;
    private String mCnMsg;
    private boolean mIsEnabled;

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
    @BindView(R.id.deposit_get_new_address)
    LinearLayout mGetNewAddress;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.deposit_toolbar_text_view)
    TextView mToolbarTextView;
    @BindView(R.id.deposit_coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.deposit_detail_message)
    TextView mTextMessage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mApolloClient = ApolloClientApi.getApolloClient();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserName = sharedPreferences.getString("name", "");
        Intent intent = getIntent();
        mAssetName = intent.getStringExtra("assetName");
        mIsEnabled = intent.getBooleanExtra("isEnabled", true);
        mEnMsg = intent.getStringExtra("enMsg");
        mCnMsg = intent.getStringExtra("cnMsg");
        mToolbarTextView.setText(String.format("%s" + getResources().getString(R.string.gate_way_deposit), mAssetName));
        if (mIsEnabled) {
            getAddress(mUserName, mAssetName);
            requestDetailMessage();
        } else {
            Log.v("language", Locale.getDefault().getLanguage());
            if (Locale.getDefault().getLanguage().equals("zh")) {
                ToastMessage.showNotEnableDepositToastMessage(this, mCnMsg);
            } else {
                ToastMessage.showNotEnableDepositToastMessage(this, mEnMsg);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageView(mQRCodeView);
                SnackBarUtils.getInstance().showSnackbar(getResources().getString(R.string.snack_bar_saved), mCoordinatorLayout, this, R.drawable.ic_check_circle_green);
            }
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @OnClick(R.id.deposit_copy_address)
    public void onClickCopyAddress(View view) {
        if (mQRAddressView.getText() != null) {
            copyAddress(mQRAddressView.getText().toString());
        }
        SnackBarUtils.getInstance().showSnackbar(getResources().getString(R.string.snack_bar_copied), mCoordinatorLayout, this, R.drawable.ic_check_circle_green);

    }

    @OnClick(R.id.deposit_save_qr_address)
    public void onClickSaveAddress(View view) {
        checkImageGalleryPermission();
    }

    @OnClick(R.id.deposit_get_new_address)
    public void onClickGetNewAddress(View view) {
        Animation animation = getAnimation();
        mGetNewAddressIcon.startAnimation(animation);
        mutateNewAddress(mUserName, mAssetName);
    }

    private void requestDetailMessage() {
        RetrofitFactory.getInstance()
                .api()
                .getDepositMsg()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody.string());
                            String enMsg = jsonObject.getString("enMsg");
                            String cnMsg = jsonObject.getString("cnMsg");
                            if (Locale.getDefault().getLanguage().equals("zh")) {
                                mTextMessage.setText(cnMsg.replace("$asset", mAssetName));
                            } else {
                                mTextMessage.setText(enMsg.replace("$asset", mAssetName));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void getAddress(String userName, String assetName) {
        mApolloClient.query(GetDepositAddress
                .builder()
                .accountName(userName)
                .asset(assetName)
                .build())
                .enqueue(new ApolloCall.Callback<GetDepositAddress.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetDepositAddress.Data> response) {
                        if (response.data() != null) {
                            if (response.data().getDepositAddress() != null) {
                                String address = response.data().getDepositAddress().fragments().accountAddressRecord().address();
                                Log.e("DepositAddress", address);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mQRAddressView != null){
                                            mQRAddressView.setText(address);
                                            generateBarCode(address);
                                        }
                                    }
                                });
                            }
                        } else {
                            SnackBarUtils.getInstance().showSnackbar(getResources().getString(R.string.snack_bar_please_retry), mCoordinatorLayout,  getApplicationContext(), R.drawable.ic_error_16px);
                            mGetNewAddressIcon.clearAnimation();

                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }

    private void mutateNewAddress(String userName, String asset) {
        mApolloClient.mutate(NewDepositAddress
                .builder()
                .accountName(userName)
                .asset(asset)
                .build())
                .enqueue(new ApolloCall.Callback<NewDepositAddress.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<NewDepositAddress.Data> response) {
                        if (response.data() != null) {
                            Log.v("mutateAddress", response.data().newDepositAddress().fragments().accountAddressRecord().address());
                            String address = response.data().newDepositAddress().fragments().accountAddressRecord().address();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mQRAddressView.setText(address);
                                    generateBarCode(address);
                                    mGetNewAddressIcon.clearAnimation();
                                }
                            });
                        } else {
                            Log.v("mutateAddress", "error");
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    SnackBarUtils.getInstance().showSnackbar(getResources().getString(R.string.snack_bar_please_retry), mCoordinatorLayout,  getApplicationContext(), R.drawable.ic_error_16px);
                                    mGetNewAddressIcon.clearAnimation();
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.d("mutateAddress", e.getLocalizedMessage());
                    }
                });
    }

    private void generateBarCode(String barcode) {
        Bitmap bitmapLogo = BitmapFactory.decodeResource(getResources(), R.mipmap.icon);
        Bitmap qrCode = AwesomeQRCode.create(barcode, 150, 5, 0.3f, Color.BLACK, Color.WHITE, bitmapLogo, true, false);
        mQRCodeView.setImageBitmap(qrCode);
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
            SnackBarUtils.getInstance().showSnackbar(getResources().getString(R.string.snack_bar_saved), mCoordinatorLayout, this, R.drawable.ic_check_circle_green);

        }
    }

    private void saveImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QRCode", "");
        Log.e("imageUrl", savedImageURL);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
