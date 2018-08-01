package com.cybexmobile.activity.transfer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.R;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.AssetSelectDialog;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.Asset;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.DynamicGlobalPropertyObject;
import com.cybexmobile.graphene.chain.FeeAmountObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.graphene.chain.SignedTransaction;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.toast.message.ToastMessage;
import com.cybexmobile.utils.AssetUtil;
import com.cybexmobile.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import static com.cybexmobile.graphene.chain.Operations.ID_TRANSER_OPERATION;
import static com.cybexmobile.utils.Constant.ASSET_ID_CYB;
import static com.cybexmobile.utils.Constant.ASSET_SYMBOL_CYB;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEM;
import static com.cybexmobile.utils.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;
import static com.cybexmobile.utils.Constant.PREF_NAME;

public class TransferActivity extends BaseActivity implements AssetSelectDialog.OnAssetSelectedListener,
        SoftKeyBoardListener.OnSoftKeyBoardChangeListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_et_account_name)
    EditText mEtAccountName;//账户名
    @BindView(R.id.transfer_tv_crypto)
    TextView mTvCrypto;//币种
    @BindView(R.id.transfer_et_quantity)
    EditText mEtQuantity;//金额
    @BindView(R.id.transfer_tv_available)
    TextView mTvAvailable;
    @BindView(R.id.transfer_et_remark)
    EditText mEtRemark;//备注
    @BindView(R.id.transfer_tv_fee)
    TextView mTvFee;//手续费
    @BindView(R.id.transfer_btn_transfer)
    Button mBtnTransfer;
    @BindView(R.id.transfer_pb_load_account)
    ProgressBar mPbLoadAccount;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;

    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems;
    private AccountBalanceObjectItem mSelectedAccountBalanceObjectItem;
    private AccountBalanceObjectItem mCybAccountBalanceObjectItem;
    private AccountObject mFromAccountObject;
    private AccountObject mToAccountObject;
    private FeeAmountObject mCybFeeAmountObject;
    private FeeAmountObject mCurrAssetFeeAmountObject;
    private AssetObject mCybAssetObject;

    private Operations.transfer_operation mTransferOperationFee;

    private boolean mIsCybEnough;
    private boolean mIsBalanceEnough;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        EventBus.getDefault().register(this);
        SoftKeyBoardListener.setListener(this, this);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mEtQuantity.setFilters(new InputFilter[]{mQuantityFilter});
        mAccountBalanceObjectItems = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS);
        mCybAccountBalanceObjectItem = findAccountBalanceObjectItem(ASSET_ID_CYB, mAccountBalanceObjectItems);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transfer_records, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transfer_records:
                Intent intent = new Intent(this, TransferRecordsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAssetSelected(AccountBalanceObjectItem accountBalanceObjectItem) {
        mSelectedAccountBalanceObjectItem = accountBalanceObjectItem;
        if(mSelectedAccountBalanceObjectItem == null){
            return;
        }
        mTvCrypto.setText(AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol));
        mTvAvailable.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                AssetUtil.formatNumberRounding(accountBalanceObjectItem.accountBalanceObject.balance /
                        Math.pow(10, accountBalanceObjectItem.assetObject.precision), accountBalanceObjectItem.assetObject.precision),
                AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol)));
        //选择币种时 CYB不够重新计算手续费
        if(!mIsCybEnough){
            checkIsLockAndLoadTransferFee(mSelectedAccountBalanceObjectItem.assetObject.id.toString(), false);
        }
        resetTransferButtonState();
    }

    @Override
    public void keyBoardShow(int height) {

    }

    @Override
    public void keyBoardHide(int height) {
        if(mEtAccountName.isFocused()){
            mEtAccountName.clearFocus();
        }
        if(mEtQuantity.isFocused()){
            mEtQuantity.clearFocus();
        }
        if(mEtRemark.isFocused()){
            mEtRemark.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mEtAccountName.isFocused()){
                manager.hideSoftInputFromWindow(mEtAccountName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtAccountName.clearFocus();
            }
            if(mEtQuantity.isFocused()){
                manager.hideSoftInputFromWindow(mEtQuantity.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtQuantity.clearFocus();
            }
            if(mEtRemark.isFocused()){
                manager.hideSoftInputFromWindow(mEtRemark.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                mEtRemark.clearFocus();
            }
        }
        return false;
    }

    @OnClick(R.id.transfer_tv_crypto)
    public void onCryptoClick(View view){
        AssetSelectDialog dialog = new AssetSelectDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS, (Serializable) mAccountBalanceObjectItems);
        bundle.putSerializable(INTENT_PARAM_ACCOUNT_BALANCE_ITEM, mSelectedAccountBalanceObjectItem);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), AssetSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(this);
    }

    @OnClick(R.id.transfer_btn_transfer)
    public void onTransferClick(View view){
        checkIsLockAndTransfer();
    }

    @OnTextChanged(value = R.id.transfer_et_quantity, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onQuantityTextChanged(Editable editable){
        /**
         * fix bug:CYM-544
         * 不实时计判断余额是否足够
         */
        //checkBalanceEnough(editable.toString());
    }

    @OnFocusChange(R.id.transfer_et_quantity)
    public void onQuantityFocusChanged(View view, boolean isFocused){
        String amountStr = mEtQuantity.getText().toString().trim();
        if(!isFocused && !TextUtils.isEmpty(amountStr)){
            if(amountStr.equals(".")){
                mEtQuantity.setText("");
            } else {
                if(mSelectedAccountBalanceObjectItem == null){
                    return;
                }
                mEtQuantity.setText(String.format(String.format(Locale.US, "%%.%df",
                        mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
                checkBalanceEnough(mEtQuantity.getText().toString().trim());
            }
        }
    }

    @OnFocusChange(R.id.transfer_et_remark)
    public void onRemarkFocusChanged(View view, boolean isFocused){
        if(isFocused){
            return;
        }
        //输入完备注 重新计算手续费
        checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
    }

    @OnFocusChange(R.id.transfer_et_account_name)
    public void onAccountNameFocusChanged(View view, boolean isFocused){
        String accountName = mEtAccountName.getText().toString().trim();
        if(isFocused){
            return;
        }
        if(TextUtils.isEmpty(accountName)){
            mToAccountObject = null;
            return;
        }
        mPbLoadAccount.setVisibility(View.VISIBLE);
        try {
            BitsharesWalletWraper.getInstance().get_account_object(accountName, new WebSocketClient.MessageCallback<WebSocketClient.Reply<AccountObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<AccountObject> reply) {
                    AccountObject accountObject = reply.result;
                    EventBus.getDefault().post(new Event.LoadAccountObject(accountObject));
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        mToAccountObject = event.getAccountObject();
        mPbLoadAccount.setVisibility(View.GONE);
        resetTransferButtonState();
        if(mToAccountObject == null){
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadTransferFee(Event.LoadTransferFee event){
        FeeAmountObject fee = event.getFee();
        if(fee.asset_id.equals(ASSET_ID_CYB)){
            mCybFeeAmountObject = fee;
            //未选择币种时 手续费默认显示CYB
            if(mSelectedAccountBalanceObjectItem == null){
                mIsCybEnough = mCybAccountBalanceObjectItem != null && mCybAccountBalanceObjectItem.accountBalanceObject.balance >= fee.amount;
                /**
                 * fix bug:CYM-543
                 * 解决账户无资产时不显示手续费
                 */
                if(mCybAccountBalanceObjectItem == null){
                    if(mCybAssetObject == null){
                        mCybAssetObject = mWebSocketService.getAssetObject(fee.asset_id);
                    }
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mCybAssetObject == null ? 5 : mCybAssetObject.precision),
                                    mCybAssetObject == null ? 5 : mCybAssetObject.precision),
                            mCybAssetObject == null ? ASSET_SYMBOL_CYB : AssetUtil.parseSymbol(mCybAssetObject.symbol)));
                } else {
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mCybAccountBalanceObjectItem.assetObject.precision),
                                    mCybAccountBalanceObjectItem.assetObject.precision),
                            AssetUtil.parseSymbol(mCybAccountBalanceObjectItem.assetObject.symbol)));
                }
            } else if (fee.asset_id.equals(mSelectedAccountBalanceObjectItem.assetObject.id.toString())){
                //只有当CYB不足时才会扣除当前币的手续费 而当前选择币种为CYB时 默认CYB不足
                mIsCybEnough = false;
                mCurrAssetFeeAmountObject = fee;
                mTvFee.setText(String.format("%s %s",
                        AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                                mSelectedAccountBalanceObjectItem.assetObject.precision),
                        AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
            } else {
                if(mCybAccountBalanceObjectItem == null || mCybAccountBalanceObjectItem.accountBalanceObject.balance < fee.amount){
                    mIsCybEnough = false;
                    checkIsLockAndLoadTransferFee(mSelectedAccountBalanceObjectItem.assetObject.id.toString(), event.isToTransfer());
                } else {
                    mIsCybEnough = true;
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mCybAccountBalanceObjectItem.assetObject.precision),
                                    mCybAccountBalanceObjectItem.assetObject.precision),
                            AssetUtil.parseSymbol(mCybAccountBalanceObjectItem.assetObject.symbol)));
                }
            }
        } else {
            mCurrAssetFeeAmountObject = fee;
            mTvFee.setText(String.format("%s %s",
                    AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                            mSelectedAccountBalanceObjectItem.assetObject.precision),
                    AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
        }
        checkBalanceEnough(mEtQuantity.getText().toString().trim());
        if(event.isToTransfer() && mIsBalanceEnough){
            showTransferConfirmationDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransfer(Event.Transfer event){
        /**
         * fix bug:CYM-505
         * 转账成功和失败清除数据
         */
        clearTransferData();
        if(event.isSuccess()){
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_transfer_success), R.drawable.ic_check_circle_green);
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_transfer_failed), R.drawable.ic_error_16px);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(TransferActivity.this);
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(pref.getString(PREF_NAME, null));
            mFromAccountObject = fullAccountObject == null ? null : fullAccountObject.account;
            checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    /**
     * fix bug:CYM-505
     * 转账完清除数据
     */
    private void clearTransferData(){
        mIsCybEnough = false;
        mIsBalanceEnough = false;
        mTransferOperationFee = null;
        mSelectedAccountBalanceObjectItem = null;
        mToAccountObject = null;
        mEtQuantity.setText("");
        mEtRemark.setText("");
        mEtAccountName.setText("");
        mTvAvailable.setText("");
        mTvCrypto.setText("");
        checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费
     */
    private void checkIsLockAndLoadTransferFee(String feeAssetId, boolean isLoadFeeToTransfer){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(this, mFromAccountObject, mFromAccountObject.name, new CybexDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    loadTransferFee(feeAssetId, isLoadFeeToTransfer);
                }
            });
        } else {
            loadTransferFee(feeAssetId, isLoadFeeToTransfer);
        }
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费 -> 转账
     */
    private void checkIsLockAndTransfer(){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(this, mFromAccountObject, mFromAccountObject.name, new CybexDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    showTransferConfirmationDialog();
                }
            });
        } else {
            showTransferConfirmationDialog();
        }
    }

    /**
     * 转账确认
     */
    private void showTransferConfirmationDialog(){
        CybexDialog.showTransferConfirmationDialog(this, mEtAccountName.getText().toString().trim(),
                String.format("%s %s", mEtQuantity.getText().toString().trim(),
                        AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)),
                mTvFee.getText().toString().trim(),
                mEtRemark.getText().toString().trim(),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        toTransfer();
                    }
                });
    }

    /**
     * 转账
     */
    private void toTransfer(){
        if(mFromAccountObject == null || mToAccountObject == null || mSelectedAccountBalanceObjectItem == null){
            return;
        }
        Operations.base_operation transferOperation =  BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mToAccountObject.id,
                mSelectedAccountBalanceObjectItem.assetObject.id,
                mIsCybEnough ? mCybFeeAmountObject.amount : mCurrAssetFeeAmountObject.amount,
                ObjectId.create_from_string(mIsCybEnough ? mCybFeeAmountObject.asset_id : mCurrAssetFeeAmountObject.asset_id),
                (long) (Double.parseDouble(mEtQuantity.getText().toString().trim()) * Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision)),
                mEtRemark.getText().toString().trim(),
                mFromAccountObject.options.memo_key,
                mToAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFromAccountObject, transferOperation, ID_TRANSER_OPERATION, reply.result);
                    try {
                        BitsharesWalletWraper.getInstance().broadcast_transaction_with_callback(signedTransaction, mTransferCallback);
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

    /**
     * 加载转账手续费
     * @param feeAssetId
     * @param isLoadFeeToTransfer 是否加载完手续费之后 自动转账
     */
    private void loadTransferFee(String feeAssetId, boolean isLoadFeeToTransfer){
        if(mFromAccountObject == null){
            return;
        }
        mTransferOperationFee =  BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mFromAccountObject.id,
                ObjectId.create_from_string(ASSET_ID_CYB),
                0,
                ObjectId.create_from_string(feeAssetId),
                0,
                mEtRemark.getText().toString().trim(),
                mFromAccountObject.options.memo_key,
                mFromAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(feeAssetId, ID_TRANSER_OPERATION, mTransferOperationFee, new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FeeAmountObject>>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<List<FeeAmountObject>> reply) {
                    EventBus.getDefault().post(new Event.LoadTransferFee(reply.result.get(0), isLoadFeeToTransfer));
                }

                @Override
                public void onFailure() {

                }
            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private AccountBalanceObjectItem findAccountBalanceObjectItem(String assetId, List<AccountBalanceObjectItem> items){
        if(TextUtils.isEmpty(assetId) || items == null || items.size() == 0){
            return null;
        }
        for (AccountBalanceObjectItem item : items) {
            if(assetId.equals(item.accountBalanceObject.asset_type.toString())){
                return item;
            }
        }
        return null;
    }

    /**
     * 检查资产是否足够
     * @param amountStr
     */
    private void checkBalanceEnough(String amountStr){
        if(mSelectedAccountBalanceObjectItem == null ||
                TextUtils.isEmpty(amountStr) || amountStr.endsWith(".")){
            return;
        }
        if(!mIsCybEnough && mCurrAssetFeeAmountObject == null){
            mBtnTransfer.setEnabled(true);
        }
        double fee = mIsCybEnough || mCurrAssetFeeAmountObject == null ? 0 : mCurrAssetFeeAmountObject.amount /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        double balanceAmount = mSelectedAccountBalanceObjectItem.accountBalanceObject.balance /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        BigDecimal balance = new BigDecimal(Double.toString(balanceAmount)).subtract(new BigDecimal(amountStr)).subtract(new BigDecimal(Double.toString(fee)));
        if(balance.doubleValue() < 0){
            mIsBalanceEnough = false;
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_not_enough), R.drawable.ic_error_16px);
        } else {
            mIsBalanceEnough = true;
        }
        resetTransferButtonState();
    }

    /**
     * reset 转账状态
     */
    private void resetTransferButtonState(){
        /**
         * fix bug:CYM-507
         * 转账金额必须大于0
         */
        mBtnTransfer.setEnabled(mIsBalanceEnough && Double.parseDouble(mEtQuantity.getText().toString()) > 0 && mSelectedAccountBalanceObjectItem != null &&
                mToAccountObject != null);
    }

    /**
     * 转账callback
     */
    private WebSocketClient.MessageCallback mTransferCallback = new WebSocketClient.MessageCallback<WebSocketClient.Reply<String>>(){

        @Override
        public void onMessage(WebSocketClient.Reply<String> reply) {
            EventBus.getDefault().post(new Event.Transfer(reply.result == null && reply.error == null));
        }

        @Override
        public void onFailure() {
            EventBus.getDefault().post(new Event.Transfer(false));
        }
    };

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
}
