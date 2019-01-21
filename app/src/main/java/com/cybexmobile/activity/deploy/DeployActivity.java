package com.cybexmobile.activity.deploy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.cache.AssetPairCache;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.BlockHeader;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybex.provider.utils.MyUtils;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.CommonSelectDialog;
import com.cybexmobile.shake.AntiShake;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.Unbinder;
import mrd.bitlib.lambdaworks.crypto.Base64;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_FULL_ACCOUNT_OBJECT;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_QR_CODE_TRANCTION;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_SELECTED_ITEM;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_TRANSACTIONID;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.SCAN_RESULT;
import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;
import static com.cybexmobile.activity.deploy.ScanActivity.RESULT_CODE;

public class DeployActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks,
        CommonSelectDialog.OnAssetSelectedListener<AccountBalanceObjectItem>, SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_SCAN_ACTIVITY = 2;
    private String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    private Unbinder mUnbinder;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems;
    private AccountBalanceObjectItem mSelectedAccountBalanceObjectItem;
    private FullAccountObject mFullAccount;
    private AccountObject mToAccountObject;
    private Operations.base_operation mTransferOperation;


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.deploy_account_for_verification_et)
    EditText mDeployAccountEt;
    @BindView(R.id.deploy_scan_iv)
    ImageView mScanIv;
    @BindView(R.id.deploy_deploy_asset_et)
    EditText mDeployAssetEt;
    @BindView(R.id.deploy_select_asset_iv)
    ImageView mSelectAssetIv;
    @BindView(R.id.deploy_quantity_to_deploy_et)
    EditText mQuantityToDeployEt;
    @BindView(R.id.deploy_available_tv)
    TextView mDeployAvailableTv;
    @BindView(R.id.deploy_transaction_fee_tv)
    TextView mTransactionFeeTv;
    @BindView(R.id.deploy_deploy_bt)
    Button mDeployButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy);
        mUnbinder = ButterKnife.bind(this);
        SoftKeyBoardListener.setListener(this, this);
        setSupportActionBar(mToolbar);
        mQuantityToDeployEt.setFilters(new InputFilter[]{mQuantityFilter});
        mAccountBalanceObjectItems = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS);

        mFullAccount = (FullAccountObject) getIntent().getSerializableExtra(INTENT_PARAM_FULL_ACCOUNT_OBJECT);
        if (mAccountBalanceObjectItems != null) {
            removeZeroBalance(mAccountBalanceObjectItems);
        }
        mTransactionFeeTv.setText(String.format("%s %s", "0.01000", ASSET_SYMBOL_CYB));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void keyBoardShow(int height) {

    }

    @Override
    public void keyBoardHide(int height) {
        if (mDeployAccountEt.isFocused()) {
            mDeployAccountEt.clearFocus();
        }
        if (mDeployAssetEt.isFocused()) {
            mDeployAssetEt.clearFocus();
        }
        if (mQuantityToDeployEt.isFocused()) {
            mQuantityToDeployEt.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                if (mDeployAccountEt.isFocused()) {
                    manager.hideSoftInputFromWindow(mDeployAccountEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mDeployAccountEt.clearFocus();
                }
                if (mDeployAssetEt.isFocused()) {
                    manager.hideSoftInputFromWindow(mDeployAssetEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mDeployAssetEt.clearFocus();
                }
                if (mQuantityToDeployEt.isFocused()) {
                    manager.hideSoftInputFromWindow(mQuantityToDeployEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mQuantityToDeployEt.clearFocus();
                }
            }
        }
        return false;
    }

    @OnClick(R.id.deploy_scan_iv)
    public void OnScanClicked(View view) {
        if (!EasyPermissions.hasPermissions(this, perms)) {
            requestCodeQRCodePermissions();
        } else {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN_ACTIVITY);
        }

    }

    @OnClick(R.id.deploy_select_asset_iv)
    public void onSelectAssetClicked(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        CommonSelectDialog<AccountBalanceObjectItem> dialog = new CommonSelectDialog<AccountBalanceObjectItem>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ITEMS, (Serializable) mAccountBalanceObjectItems);
        bundle.putSerializable(INTENT_PARAM_SELECTED_ITEM, mSelectedAccountBalanceObjectItem);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), CommonSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(this);
    }

    @OnFocusChange(R.id.deploy_account_for_verification_et)
    public void onAccountNameFocusChanged(View view, boolean isFocused) {
        String accountName = mDeployAccountEt.getText().toString().trim();
        if (isFocused) {
            return;
        }
        if (TextUtils.isEmpty(accountName)) {
            mToAccountObject = null;
            resetDeployButtonState();
            return;
        }
        try {
            BitsharesWalletWraper.getInstance().get_account_object(accountName, new MessageCallback<Reply<AccountObject>>() {
                @Override
                public void onMessage(Reply<AccountObject> reply) {
                    mToAccountObject = reply.result;
                    resetDeployButtonState();
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @OnFocusChange(R.id.deploy_deploy_asset_et)
    public void onAssetToDeployFocusChanged(View view, boolean isFocused) {
        String asset = mDeployAssetEt.getText().toString().trim();
        if (isFocused) {
            return;
        }
        for (AccountBalanceObjectItem accountBalanceObjectItem : mAccountBalanceObjectItems) {
            if (AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol).equals(asset)) {
                mSelectedAccountBalanceObjectItem = accountBalanceObjectItem;
                mDeployAvailableTv.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                        AssetUtil.formatNumberRounding(accountBalanceObjectItem.accountBalanceObject.balance /
                                Math.pow(10, accountBalanceObjectItem.assetObject.precision), accountBalanceObjectItem.assetObject.precision),
                        AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol)));
                String amountStr = mQuantityToDeployEt.getText().toString().trim();
                if (amountStr.length() > 0) {
                    mQuantityToDeployEt.setText(String.format(String.format(Locale.US, "%%.%df",
                            accountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
                }
                break;
            }
        }
        resetDeployButtonState();

    }

    @OnFocusChange(R.id.deploy_quantity_to_deploy_et)
    public void onQuantityToDeployFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        String amountStr = mQuantityToDeployEt.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr) || amountStr.equals(".")) {
            resetDeployButtonState();
            return;
        }
        if (mSelectedAccountBalanceObjectItem == null) {
            return;
        }
        mQuantityToDeployEt.setText(String.format(String.format(Locale.US, "%%.%df",
                mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        resetDeployButtonState();
    }

    @Override
    public void onAssetSelected(AccountBalanceObjectItem accountBalanceObjectItem) {
        mSelectedAccountBalanceObjectItem = accountBalanceObjectItem;
        if (mSelectedAccountBalanceObjectItem == null) {
            return;
        }
        mDeployAssetEt.setText(AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol));
        mDeployAvailableTv.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                AssetUtil.formatNumberRounding(accountBalanceObjectItem.accountBalanceObject.balance /
                        Math.pow(10, accountBalanceObjectItem.assetObject.precision), accountBalanceObjectItem.assetObject.precision),
                AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol)));
        String amountStr = mQuantityToDeployEt.getText().toString().trim();
        if (amountStr.length() > 0) {
            mQuantityToDeployEt.setText(String.format(String.format(Locale.US, "%%.%df",
                    mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        }
        resetDeployButtonState();
    }

    @OnClick(R.id.deploy_deploy_bt)
    public void onClickDeployButton(View view) {
        checkIsLockAndTransfer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN_ACTIVITY && resultCode == RESULT_CODE) {
            if (data != null) {
                mDeployAccountEt.setText(data.getStringExtra(SCAN_RESULT));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Please turn on camera permission", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    private void checkIsLockAndTransfer() {
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFullAccount.account, mFullAccount.account.name, password -> {
                toTransfer();
            });
        } else {
            toTransfer();
        }
    }


    private void removeZeroBalance(List<AccountBalanceObjectItem> items) {
        if (items == null || items.size() == 0) {
            return;
        }
        Iterator<AccountBalanceObjectItem> it = items.iterator();
        List<String> ticketFilterInfo = AssetPairCache.getInstance().getValidTickets();
        while (it.hasNext()) {
            AccountBalanceObjectItem item = it.next();
            if (item.accountBalanceObject.balance == 0) {
                it.remove();
            }
            if (ticketFilterInfo != null && ticketFilterInfo.contains(AssetUtil.getPrefix(item.assetObject.symbol))) {
                it.remove();
            }
        }
    }

    /**
     * reset 转账状态
     */
    private void resetDeployButtonState() {
        try {
            mDeployButton.setEnabled(
                    !mDeployAccountEt.getText().toString().isEmpty() &&
                            !mDeployAssetEt.getText().toString().isEmpty() &&
                            Double.parseDouble(mQuantityToDeployEt.getText().toString()) > 0);
        } catch (Exception e) {
            e.printStackTrace();
            mDeployButton.setEnabled(false);
        }
    }

    private void toTransfer() {
        if (mToAccountObject == null) {
            ToastMessage.showNotEnableDepositToastMessage(this, "To Account is Wrong", R.drawable.ic_error_16px);
            return;
        }
        if (mSelectedAccountBalanceObjectItem == null) {
            ToastMessage.showNotEnableDepositToastMessage(this, "Don't have enough balance", R.drawable.ic_error_16px);
            return;
        }
        if (mFullAccount.account == null) {
            return;
        }
        mTransferOperation = BitsharesWalletWraper.getInstance().getTransferOperation(
                mFullAccount.account.id,
                mToAccountObject.id,
                mSelectedAccountBalanceObjectItem.assetObject.id,
                (long) 1000,
                ObjectId.create_from_string(ASSET_ID_CYB),
                (long) (Double.parseDouble(mQuantityToDeployEt.getText().toString().trim()) * Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision)),
                null,
                null,
                null);
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                    showLoadDialog();
                    DynamicGlobalPropertyObject dynamicGlobalPropertyObject = reply.result;
                    try {
                        BitsharesWalletWraper.getInstance().get_block_header(dynamicGlobalPropertyObject.last_irreversible_block_num + 1, mBlockHeaderCallback);
                    } catch (NetworkStatusException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private MessageCallback<Reply<BlockHeader>> mBlockHeaderCallback = new MessageCallback<Reply<BlockHeader>>() {
        @Override
        public void onMessage(Reply<BlockHeader> reply) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransactinForTickets(
                                mFullAccount.account, mTransferOperation, ID_TRANSER_OPERATION, reply.result);
                        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                        String json = gson.toJson(signedTransaction);
                        String transactionId = signedTransaction.getTransactionID();
                        Log.e("JsonData", json);
                        Log.e("TransactionId", transactionId);

                        String strMessage = compressTransaction((Operations.transfer_operation) mTransferOperation, signedTransaction, reply.result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoadDialog();
                            }
                        });
                        jumpToOtherActivity(strMessage, transactionId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onFailure() {

        }
    };

    private void jumpToOtherActivity(String message, String transactionId) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra(INTENT_PARAM_QR_CODE_TRANCTION, message);
        intent.putExtra(INTENT_PARAM_TRANSACTIONID, transactionId);
        startActivity(intent);
    }

    private String compressTransaction(Operations.transfer_operation operation, SignedTransaction signedTransaction, BlockHeader blockHeader) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int from = operation.from.get_instance();
        int to = operation.to.get_instance();
        int assetId = operation.amount.asset_id.get_instance();
        int ref_block_num = (int) signedTransaction.getRef_block_num();
        long amount = operation.amount.amount;
        double timeInterval = (double) (signedTransaction.getExpiration().getTime()) / 1000;
        long prefix_block_num = signedTransaction.getRef_block_prefix();
        byteBuffer.putInt(from);
        byteBuffer.putInt(to);
        byteBuffer.putInt(assetId);
        byteBuffer.putInt(ref_block_num);
        byteBuffer.putLong(amount);
        byteBuffer.putDouble(timeInterval);
        byte[] firstByteArray = byteBuffer.array();
        byte[] prefix_block_num_byte_array = MyUtils.hexToBytes(String.valueOf(prefix_block_num));
        byte[] resultPeriodOne = new byte[firstByteArray.length + prefix_block_num_byte_array.length];
        System.arraycopy(firstByteArray, 0, resultPeriodOne, 0, firstByteArray.length);
        System.arraycopy(prefix_block_num_byte_array, 0, resultPeriodOne, firstByteArray.length, prefix_block_num_byte_array.length);
        byte[] sig = signedTransaction.SignaturesBuffer.get(0).data;
        byte[] result = new byte[resultPeriodOne.length + sig.length];
        System.arraycopy(resultPeriodOne, 0, result, 0, resultPeriodOne.length);
        System.arraycopy(sig, 0, result, resultPeriodOne.length, sig.length);

        return Base64.encodeToString(result, false);
    }


    /**
     * 金额过滤器
     */
    private InputFilter mQuantityFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if(dest.length() == 0 && source.equals(".")){
                return "0.";
            }
            String destStr = dest.toString();
            String[] destArr = destStr.split("\\.");
            if (destArr.length > 1) {
                String dotValue = destArr[1];
                if (dotValue.length() == (mSelectedAccountBalanceObjectItem == null ?
                        5 : mSelectedAccountBalanceObjectItem.assetObject.precision)) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
