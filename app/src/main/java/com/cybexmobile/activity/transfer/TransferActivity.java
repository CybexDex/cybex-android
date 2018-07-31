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
    private FeeAmountObject mCurrAssetFeeAmountBoject;

    private Operations.transfer_operation mTransferOperation;

    private boolean mIsCybEnough;
    private boolean mIsBalanceEngouh;

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
        checkIsLockAndLoadTransferFee();
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
        checkBalanceEnough(editable.toString());
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
            }
        }
    }

    @OnFocusChange(R.id.transfer_et_remark)
    public void onRemarkFocusChanged(View view, boolean isFocused){
        if(isFocused){
            return;
        }
        checkIsLockAndLoadTransferFee();
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
        } else {
            checkIsLockAndLoadTransferFee();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadTransferFee(Event.LoadTransferFee event){
        FeeAmountObject fee = event.getFee();
        mTransferOperation.fee = new Asset(fee.amount, ObjectId.create_from_string(fee.asset_id));
        if(fee.asset_id.equals(ASSET_ID_CYB)){
            mCybFeeAmountObject = fee;
            //当前选择币种为CYB时
            if(fee.asset_id.equals(mSelectedAccountBalanceObjectItem.assetObject.id.toString())){
                //只有当CYB不足时才会扣除当前币的手续费 而当前选择币种为CYB时 默认CYB不足
                mIsCybEnough = false;
                mCurrAssetFeeAmountBoject = fee;
                mTvFee.setText(String.format("%s %s",
                        AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                                mSelectedAccountBalanceObjectItem.assetObject.precision),
                        AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
            } else {
                if(mCybAccountBalanceObjectItem == null || mCybAccountBalanceObjectItem.accountBalanceObject.balance < fee.amount){
                    mIsCybEnough = false;
                    loadTransferFee(mSelectedAccountBalanceObjectItem.assetObject.id.toString(), event.isToTransfer());
                } else {
                    mIsCybEnough = true;
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mCybAccountBalanceObjectItem.assetObject.precision),
                                    mCybAccountBalanceObjectItem.assetObject.precision),
                            AssetUtil.parseSymbol(mCybAccountBalanceObjectItem.assetObject.symbol)));
                }
            }
        } else {
            mCurrAssetFeeAmountBoject = fee;
            mTvFee.setText(String.format("%s %s",
                    AssetUtil.formatNumberRounding(fee.amount/Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                            mSelectedAccountBalanceObjectItem.assetObject.precision),
                    AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
        }
        checkBalanceEnough(mEtQuantity.getText().toString().trim());
        if(event.isToTransfer() && mIsBalanceEngouh){
            showTransferConfirmationDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransfer(Event.Transfer event){
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    /**
     * 检查钱包锁定状态 -> 加载转账手续费 -> 转账
     */
    private void checkIsLockAndTransfer(){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(this, mFromAccountObject, mFromAccountObject.name, new CybexDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    if(mTransferOperation == null){
                        loadTransferFee(ASSET_ID_CYB, true);
                    } else {
                        showTransferConfirmationDialog();
                    }
                }
            });
        } else {
            if(mTransferOperation == null){
                loadTransferFee(ASSET_ID_CYB, true);
            } else {
                showTransferConfirmationDialog();
            }

        }
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费
     */
    private void checkIsLockAndLoadTransferFee(){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(this, mFromAccountObject, mFromAccountObject.name, new CybexDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    loadTransferFee(ASSET_ID_CYB, false);
                }
            });
        } else {
            loadTransferFee(ASSET_ID_CYB, false);
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
        mTransferOperation.amount = mSelectedAccountBalanceObjectItem.assetObject.amount_from_string(
                mEtQuantity.getText().toString().trim());
        try {
            BitsharesWalletWraper.getInstance().get_dynamic_global_properties(new WebSocketClient.MessageCallback<WebSocketClient.Reply<DynamicGlobalPropertyObject>>() {
                @Override
                public void onMessage(WebSocketClient.Reply<DynamicGlobalPropertyObject> reply) {
                    SignedTransaction signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                            mFromAccountObject, mTransferOperation, ID_TRANSER_OPERATION, reply.result);
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
        if(mFromAccountObject == null || mToAccountObject == null || mSelectedAccountBalanceObjectItem == null){
            return;
        }
        mTransferOperation =  BitsharesWalletWraper.getInstance().getTransferOperation(
                mFromAccountObject.id,
                mToAccountObject.id,
                mSelectedAccountBalanceObjectItem.assetObject,
                0,
                feeAssetId,
                TextUtils.isEmpty(mEtQuantity.getText().toString().trim()) ? "0" : mEtQuantity.getText().toString().trim(),
                mEtRemark.getText().toString().trim(),
                mFromAccountObject.options.memo_key,
                mToAccountObject.options.memo_key);
        try {
            BitsharesWalletWraper.getInstance().get_required_fees(feeAssetId, ID_TRANSER_OPERATION, mTransferOperation, new WebSocketClient.MessageCallback<WebSocketClient.Reply<List<FeeAmountObject>>>() {
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
        if(!mIsCybEnough && mCurrAssetFeeAmountBoject == null){
            mBtnTransfer.setEnabled(true);
        }
        double amount = Double.parseDouble(amountStr);
        double fee = mIsCybEnough || mCurrAssetFeeAmountBoject == null ? 0 : mCurrAssetFeeAmountBoject.amount /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        double balanceAmount = mSelectedAccountBalanceObjectItem.accountBalanceObject.balance /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        BigDecimal balance = new BigDecimal(balanceAmount).subtract(new BigDecimal(amount)).subtract(new BigDecimal(fee));
        if(balance.doubleValue() < 0){
            mIsBalanceEngouh = false;
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_not_enough), R.drawable.ic_error_16px);
        } else {
            mIsBalanceEngouh = true;
        }
        resetTransferButtonState();
    }

    /**
     * reset 转账状态
     */
    private void resetTransferButtonState(){
        mBtnTransfer.setEnabled(mIsBalanceEngouh && mSelectedAccountBalanceObjectItem != null && mToAccountObject != null);
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
