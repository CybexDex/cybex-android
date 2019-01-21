package com.cybexmobile.activity.deploy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.utils.AntiMultiClick;
import com.cybexmobile.utils.QRCode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_QR_CODE_TRANCTION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_TRANSACTIONID;

public class QRCodeActivity extends BaseActivity {
    private static int REQUEST_PERMISSION = 1;

    private Unbinder mUnbinder;
    private ScheduledExecutorService mScheduled = Executors.newScheduledThreadPool(2);
    private ScheduledFuture mGetTransacionByIDFuture;
    private GetTransactionByIDWorker mGetTransactionByIDWorker;

    private String mID;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transaction_qr_code)
    ImageView mQRCodeView;
    @BindView(R.id.transaction_save_qr_address)
    TextView mSaveAddressTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        String message = getIntent().getStringExtra(INTENT_PARAM_QR_CODE_TRANCTION);
        mID = getIntent().getStringExtra(INTENT_PARAM_TRANSACTIONID);
        generateBarCode(message);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startToPollingTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelGetTransactionByIDWorkerSchedule();
        mScheduled.shutdownNow();
        mUnbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageView(mQRCodeView);
            }
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, "need permission", R.drawable.ic_error_16px);
        }
    }

    @OnClick(R.id.transaction_save_qr_address)
    public void onSaveClicked(View view) {
        if (AntiMultiClick.isFastClick()) {
            checkImageGalleryPermission();
        }
    }

    private void startToPollingTransaction() {
        if (TextUtils.isEmpty(mID)) {
            return;
        }
        if (mGetTransacionByIDFuture != null && !mGetTransacionByIDFuture.isCancelled()) {
            mGetTransacionByIDFuture.cancel(true);
        }
        if (mScheduled.isShutdown() || mScheduled.isTerminated()) {
            mScheduled = Executors.newScheduledThreadPool(2);
        }
        if (mGetTransactionByIDWorker == null) {
            mGetTransactionByIDWorker = new GetTransactionByIDWorker();
        }
        mGetTransacionByIDFuture = mScheduled.scheduleAtFixedRate(mGetTransactionByIDWorker, 0,
                3, TimeUnit.SECONDS);
    }

    private void cancelGetTransactionByIDWorkerSchedule() {
        if (mGetTransacionByIDFuture == null) {
            return;
        }
        if (mGetTransacionByIDFuture.isCancelled()) {
            return;
        }
        mGetTransacionByIDFuture.cancel(true);
        mGetTransacionByIDFuture = null;
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
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QRCode", "");
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(R.string.snack_bar_saved), R.drawable.ic_check_circle_green);
        }
    }


    private void generateBarCode(String barcode) {
        Bitmap bitmap = QRCode.createQRCodeWithLogo(barcode, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mQRCodeView.setImageBitmap(bitmap);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private class GetTransactionByIDWorker implements Runnable {

        @Override
        public void run() {
            loadTransactionByID(mID);
        }
    }

    private void loadTransactionByID(String id) {
        try {
            BitsharesWalletWraper.getInstance().get_recent_transaction_by_id(id, new MessageCallback<Reply<Object>>() {
                @Override
                public void onMessage(Reply<Object> reply) {
                    if (reply.result != null) {
                        ToastMessage.showNotEnableDepositToastMessage(QRCodeActivity.this, getResources().getString(R.string.text_ticket_get_message), R.drawable.ic_check_circle_green);
                        cancelGetTransactionByIDWorkerSchedule();
                        finish();
                    } else if (reply.error != null) {
                        ToastMessage.showNotEnableDepositToastMessage(QRCodeActivity.this, getResources().getString(R.string.text_ticket_wrong_message), R.drawable.ic_error_16px);
                    }
                }

                @Override
                public void onFailure() {
                    ToastMessage.showNotEnableDepositToastMessage(QRCodeActivity.this, getResources().getString(R.string.text_ticket_internet_wrong_message), R.drawable.ic_error_16px);

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }
}
