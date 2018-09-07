package com.cybexmobile.activity.transfer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.activity.address.AddTransferAccountActivity;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.WebSocketClient;
import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.CommonSelectDialog;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.event.Event;
import com.cybex.provider.exception.NetworkStatusException;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.DynamicGlobalPropertyObject;
import com.cybex.provider.graphene.chain.FeeAmountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.ObjectId;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.graphene.chain.SignedTransaction;
import com.cybexmobile.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybexmobile.utils.SoftKeyBoardListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;
import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ADDRESS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_SELECTED_ITEM;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class TransferActivity extends BaseActivity implements
        CommonSelectDialog.OnAssetSelectedListener<AccountBalanceObjectItem>,
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
    @BindView(R.id.transfer_loading_progress_bar)
    ProgressBar mPbLoading;
    @BindView(R.id.transfer_iv_account_check)
    ImageView mIvAccountCheck;
    @BindView(R.id.transfer_tv_select_account)
    TextView mTvSelectAccount;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;

    private List<Address> mAddresses;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItems;
    private AccountBalanceObjectItem mSelectedAccountBalanceObjectItem;
    private AccountBalanceObjectItem mCybAccountBalanceObjectItem;
    private AccountObject mFromAccountObject;
    private AccountObject mToAccountObject;
    private FeeAmountObject mCybFeeAmountObject;
    private FeeAmountObject mCurrAssetFeeAmountObject;
    private AssetObject mCybAssetObject;
    private String mUserName;
    private Disposable mLoadAddressDisposable;
    private Disposable mCheckAddressExistDisposable;

    private Operations.transfer_operation mTransferOperationFee;//手续费TransferOperation

    private boolean mIsCybEnough;//cyb余额是否足够
    private boolean mIsBalanceEnough;//选择币种余额是否足够

    private boolean mIsActivityActive;//当前activity是否可见

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        EventBus.getDefault().register(this);
        SoftKeyBoardListener.setListener(this, this);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, null);
        mEtQuantity.setFilters(new InputFilter[]{mQuantityFilter});
        mAccountBalanceObjectItems = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_PARAM_ACCOUNT_BALANCE_ITEMS);
        mCybAccountBalanceObjectItem = findAccountBalanceObjectItem(ASSET_ID_CYB, mAccountBalanceObjectItems);
        /**
         * fix bug:CYM-551
         * 去除资产为0的币种
         */
        removeZeroBalance(mAccountBalanceObjectItems);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        loadAddress();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActivityActive = false;
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
        if(mLoadAddressDisposable != null && !mLoadAddressDisposable.isDisposed()){
            mLoadAddressDisposable.dispose();
        }
        if(mCheckAddressExistDisposable != null && !mCheckAddressExistDisposable.isDisposed()){
            mCheckAddressExistDisposable.dispose();
        }
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
            case R.id.action_records:
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
        String amountStr = mEtQuantity.getText().toString().trim();
        if(amountStr.length() > 0){
            mEtQuantity.setText(String.format(String.format(Locale.US, "%%.%df",
                    mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        }
        //选择币种时 CYB不够重新计算手续费并判断余额是否足够
        if(!mIsCybEnough){
            checkIsLockAndLoadTransferFee(mSelectedAccountBalanceObjectItem.assetObject.id.toString(), false);
        } else {
            checkBalanceEnough(mEtQuantity.getText().toString().trim());
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

    @OnClick(R.id.transfer_tv_select_account)
    public void onSelectAccountClick(View view) {
        if(mAddresses == null || mAddresses.size() == 0){
            Intent intent = new Intent(this, AddTransferAccountActivity.class);
            startActivity(intent);
            return;
        }
        CommonSelectDialog<Address> dialog = new CommonSelectDialog<Address>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ITEMS, (Serializable) mAddresses);
        bundle.putSerializable(INTENT_PARAM_SELECTED_ITEM, null);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), CommonSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(new CommonSelectDialog.OnAssetSelectedListener<Address>() {
            @Override
            public void onAssetSelected(Address address) {
                if(address == null){
                    return;
                }
                mEtAccountName.setText(address.getAddress());
                if(!mEtAccountName.isFocused()){
                    onAccountNameFocusChanged(mEtAccountName, false);
                }
            }
        });
    }

    @OnClick(R.id.transfer_tv_crypto)
    public void onCryptoClick(View view){
        CommonSelectDialog<AccountBalanceObjectItem> dialog = new CommonSelectDialog<AccountBalanceObjectItem>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ITEMS, (Serializable) mAccountBalanceObjectItems);
        bundle.putSerializable(INTENT_PARAM_SELECTED_ITEM, mSelectedAccountBalanceObjectItem);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), CommonSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(this);
    }

    @OnClick(R.id.transfer_btn_transfer)
    public void onTransferClick(View view){
        checkIsLockAndTransfer();
    }

    @OnTextChanged(value = R.id.transfer_et_account_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAccountNameTextChanged(Editable editable){
        if(editable.toString().length() == 0){
            mIvAccountCheck.setVisibility(View.INVISIBLE);
        }
    }

    @OnTextChanged(value = R.id.transfer_et_quantity, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onQuantityTextChanged(Editable editable){
        /**
         * fix bug:CYM-544
         * 不实时计判断余额是否足够
         */
        //checkBalanceEnough(editable.toString());
    }

    @OnFocusChange(R.id.transfer_et_account_name)
    public void onAccountNameFocusChanged(View view, boolean isFocused){
        String accountName = mEtAccountName.getText().toString().trim();
        if(isFocused){
            return;
        }
        if(TextUtils.isEmpty(accountName)){
            mToAccountObject = null;
            resetTransferButtonState();
            return;
        }
        mPbLoading.setVisibility(View.VISIBLE);
        mIvAccountCheck.setVisibility(View.GONE);
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

    @OnFocusChange(R.id.transfer_et_quantity)
    public void onQuantityFocusChanged(View view, boolean isFocused){
        if(isFocused){
            return;
        }
        String amountStr = mEtQuantity.getText().toString().trim();
        if(TextUtils.isEmpty(amountStr) || amountStr.equals(".")){
            resetTransferButtonState();
            return;
        }
        if(mSelectedAccountBalanceObjectItem == null){
            return;
        }
        mEtQuantity.setText(String.format(String.format(Locale.US, "%%.%df",
                mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        checkBalanceEnough(mEtQuantity.getText().toString().trim());
    }

    @OnFocusChange(R.id.transfer_et_remark)
    public void onRemarkFocusChanged(View view, boolean isFocused){
        if(isFocused){
            return;
        }
        //输入完备注 重新计算手续费
        checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        /**
         * fix bug:CYM-581
         * 修复账户名验证报错
         */
        if(!mIsActivityActive || (event.getAccountObject() != null &&
                !event.getAccountObject().name.equals(mEtAccountName.getText().toString().trim()))){
            return;
        }
        mToAccountObject = event.getAccountObject();
        resetTransferButtonState();
        mPbLoading.setVisibility(View.INVISIBLE);
        mIvAccountCheck.setVisibility(View.VISIBLE);
        if(mToAccountObject == null){
            mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
        } else {
            mIvAccountCheck.setImageResource(R.drawable.ic_check_success);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        /**
         * fix bug:CYM-577
         * 转账成功后刷新余额
         */
        FullAccountObject fullAccountObject = event.getFullAccount();
        if(fullAccountObject == null){
            return;
        }
        List<AccountBalanceObject> balances = fullAccountObject.balances;
        if(balances == null || balances.size() == 0 ||
                mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0){
            return;
        }
        for (AccountBalanceObjectItem item : mAccountBalanceObjectItems) {
            for (AccountBalanceObject balance : balances) {
                if(item.accountBalanceObject.asset_type.equals(balance.asset_type)){
                    item.accountBalanceObject = balance;
                    break;
                }
            }
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
        hideLoadDialog();
        if(event.isSuccess()){
            mCheckAddressExistDisposable = DBManager.getDbProvider(this).checkAddressExist(mUserName,
                    mEtAccountName.getText().toString().trim(), Address.TYPE_TRANSFER)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Address>() {
                        @Override
                        public void accept(Address address) throws Exception {
                            if(address != null){
                                ToastMessage.showNotEnableDepositToastMessage(
                                        TransferActivity.this,
                                        getResources().getString(R.string.toast_message_transfer_success),
                                        R.drawable.ic_check_circle_green);
                                /**
                                 * fix bug:CYM-505
                                 * 转账成功和失败清除数据
                                 */
                                clearTransferData();
                            } else {
                                showAddAddressDialog();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            showAddAddressDialog();
                        }
                    });
        } else {
            ToastMessage.showNotEnableDepositToastMessage(this, getResources().getString(
                    R.string.toast_message_transfer_failed), R.drawable.ic_error_16px);
        }
    }

    private void showAddAddressDialog(){
        CybexDialog.showAddAddressDialog(this,
                getResources().getString(R.string.toast_message_transfer_success),
                getResources().getString(R.string.toast_message_add_to_transfer_account_list),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        Intent intent = new Intent(TransferActivity.this, AddTransferAccountActivity.class);
                        intent.putExtra(INTENT_PARAM_ADDRESS, mEtAccountName.getText().toString().trim());
                        startActivity(intent);
                        /**
                         * fix bug:CYM-505
                         * 转账成功和失败清除数据
                         */
                        clearTransferData();
                    }
                },
                new CybexDialog.ConfirmationDialogCancelListener() {
                    @Override
                    public void onCancel(Dialog dialog) {
                        /**
                         * fix bug:CYM-505
                         * 转账成功和失败清除数据
                         */
                        clearTransferData();
                    }
                });
    }

    /**
     * 删除0资产币种
     * @param items
     */
    private void removeZeroBalance(List<AccountBalanceObjectItem> items){
        if(items == null || items.size() == 0){
            return;
        }
        Iterator<AccountBalanceObjectItem> it = items.iterator();
        while (it.hasNext()) {
            AccountBalanceObjectItem item = it.next();
            if (item.accountBalanceObject.balance == 0) {
                it.remove();
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mUserName);
            mFromAccountObject = fullAccountObject == null ? null : fullAccountObject.account;
            checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void loadAddress(){
        if(TextUtils.isEmpty(mUserName)){
            return;
        }
        mLoadAddressDisposable = DBManager.getDbProvider(this).getAddress(mUserName, Address.TYPE_TRANSFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        mAddresses = addresses;
                        if(mAddresses == null || mAddresses.size() == 0){
                            mTvSelectAccount.setText(getResources().getString(R.string.text_add_account));
                        } else {
                            mTvSelectAccount.setText(getResources().getString(R.string.text_select_account));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

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
        /**
         * fix bug:CYM-555
         * 重置按钮状态
         */
        resetTransferButtonState();
        checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费
     */
    private void checkIsLockAndLoadTransferFee(String feeAssetId, boolean isLoadFeeToTransfer){
        if(BitsharesWalletWraper.getInstance().is_locked()){
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject,
                    mFromAccountObject.name, new UnlockDialog.UnLockDialogClickListener() {
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
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject, mFromAccountObject.name, new UnlockDialog.UnLockDialogClickListener() {
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
        showLoadDialog();
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
        try {
            mBtnTransfer.setEnabled(
                    mToAccountObject != null &&
                            mSelectedAccountBalanceObjectItem != null &&
                            Double.parseDouble(mEtQuantity.getText().toString()) > 0 &&
                            mIsBalanceEnough);
        } catch (Exception e) {
            e.printStackTrace();
            mBtnTransfer.setEnabled(false);
        }
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
