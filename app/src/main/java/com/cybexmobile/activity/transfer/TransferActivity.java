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
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.dialog.UnlockDialogWithEnotes;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.basemodule.utils.SoftKeyBoardListener;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
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
import com.cybex.provider.graphene.chain.Types;
import com.cybex.provider.utils.NetworkUtils;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybexmobile.R;
import com.cybexmobile.activity.address.AddTransferAccountActivity;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.dialog.CommonSelectDialog;
import com.cybexmobile.shake.AntiShake;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.enotes.sdk.repository.db.entity.Card;
import io.enotes.sdk.utils.ReaderUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.ASSET_ID_CYB;
import static com.cybex.basemodule.constant.Constant.ASSET_SYMBOL_CYB;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ACCOUNT_BALANCE_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ADDRESS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_ITEMS;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_SELECTED_ITEM;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.graphene.chain.Operations.ID_TRANSER_OPERATION;
import static com.cybex.provider.utils.NetworkUtils.TYPE_NOT_CONNECTED;

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
    @BindView(R.id.transfer_lock_time_switch)
    SwitchCompat mSwLockTime;
    @BindView(R.id.transfer_iv_question_marker)
    ImageView mQuestionMarker;
    @BindView(R.id.transfer_lock_time_layout)
    LinearLayout mLinearLayoutTransferLockTime;
    @BindView(R.id.transfer_et_lock_time)
    EditText mEtTransferLockTime;
    @BindView(R.id.transfer_lock_time_spinner)
    MaterialSpinner mMsTransferLockTime;
    @BindView(R.id.transfer_ll_public_key)
    LinearLayout mLlPublicKey;
    @BindView(R.id.transfer_tv_public_key)
    TextView mTvPublicKey;
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
    private List<String> mAssetWhiteList;
    private List<Types.public_key_type> mLockTimePublicKeys;
    private Types.public_key_type mSelectedLockTimePublicKey;
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
    private Card mCard;
    private UnlockDialog unlockDialog;
    private UnlockDialogWithEnotes unlockDialogWithEnotes;

    private Operations.transfer_operation mTransferOperationFee;//手续费TransferOperation

    private boolean mIsCybEnough;//cyb余额是否足够
    private boolean mIsBalanceEnough;//选择币种余额是否足够

    private boolean mIsActivityActive;//当前activity是否可见

    private boolean mIsUsedCloudPassword = false;//当前卡登陆但有memo要用云密码

    private int mTotalLockTime;
    private int mLockTimeUnit = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        SoftKeyBoardListener.setListener(this, this);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, null);
        mEtQuantity.setFilters(new InputFilter[] {mQuantityFilter});
        mAccountBalanceObjectItems = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(
                INTENT_PARAM_ACCOUNT_BALANCE_ITEMS);
        if (mAccountBalanceObjectItems != null) {
            mCybAccountBalanceObjectItem = findAccountBalanceObjectItem(ASSET_ID_CYB, mAccountBalanceObjectItems);
            /**
             * fix bug:CYM-551
             * 去除资产为0的币种
             */
            removeZeroBalance(mAccountBalanceObjectItems);
        }
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
        unlockDialogWithEnotes = null;
        unbindService(mConnection);
        if (mLoadAddressDisposable != null && !mLoadAddressDisposable.isDisposed()) {
            mLoadAddressDisposable.dispose();
        }
        if (mCheckAddressExistDisposable != null && !mCheckAddressExistDisposable.isDisposed()) {
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
        if (AntiShake.check(item.getItemId())) {
            return false;
        }
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
        if (mSelectedAccountBalanceObjectItem == null) {
            return;
        }
        mTvCrypto.setText(AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol));
        mTvAvailable.setText(String.format("%s %s %s", getResources().getString(R.string.text_available),
                AssetUtil.formatNumberRounding(accountBalanceObjectItem.accountBalanceObject.balance /
                                Math.pow(10, accountBalanceObjectItem.assetObject.precision),
                        accountBalanceObjectItem.assetObject.precision),
                AssetUtil.parseSymbol(accountBalanceObjectItem.assetObject.symbol)));
        String amountStr = mEtQuantity.getText().toString().trim();
        if (amountStr.length() > 0) {
            mEtQuantity.setText(String.format(String.format(Locale.US, "%%.%df",
                    mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        }
        //选择币种时 CYB不够重新计算手续费并判断余额是否足够
        if (!mIsCybEnough) {
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
        if (mEtAccountName.isFocused()) {
            mEtAccountName.clearFocus();
        }
        if (mEtQuantity.isFocused()) {
            mEtQuantity.clearFocus();
        }
        if (mEtRemark.isFocused()) {
            mEtRemark.clearFocus();
        }
        if (mEtTransferLockTime.isFocused()) {
            mEtTransferLockTime.clearFocus();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                if (mEtAccountName.isFocused()) {
                    manager.hideSoftInputFromWindow(mEtAccountName.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    mEtAccountName.clearFocus();
                }
                if (mEtQuantity.isFocused()) {
                    manager.hideSoftInputFromWindow(mEtQuantity.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mEtQuantity.clearFocus();
                }
                if (mEtRemark.isFocused()) {
                    manager.hideSoftInputFromWindow(mEtRemark.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mEtRemark.clearFocus();
                }
                if (mEtTransferLockTime.isFocused()) {
                    manager.hideSoftInputFromWindow(mEtTransferLockTime.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    mEtTransferLockTime.clearFocus();
                }
            }
        }
        return false;
    }

    @OnClick(R.id.transfer_tv_select_account)
    public void onSelectAccountClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        if (mAddresses == null || mAddresses.size() == 0) {
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
                if (address == null) {
                    return;
                }
                mEtAccountName.setText(address.getAddress());
                if (!mEtAccountName.isFocused()) {
                    onAccountNameFocusChanged(mEtAccountName, false);
                }
            }
        });
    }

    @OnClick(R.id.transfer_tv_crypto)
    public void onCryptoClick(View view) {
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

    @OnCheckedChanged(R.id.transfer_lock_time_switch)
    public void onSwitchClicked(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            mLinearLayoutTransferLockTime.setVisibility(View.VISIBLE);
            mMsTransferLockTime.setDrawableLevelValue(5000);
            mMsTransferLockTime.notifyItemsWithIndex(
                    Arrays.asList(getResources().getStringArray(R.array.transfer_time_period)),
                    mMsTransferLockTime.getSelectedIndex());
            mMsTransferLockTime.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                    view.setTextColor(getResources().getColor(R.color.btn_orange_end));
                    mLockTimeUnit = getTimeUnit(item);
                    if (mEtTransferLockTime.getText() != null && !mEtTransferLockTime.getText().toString().isEmpty()) {
                        mTotalLockTime = (int) Double.parseDouble(mEtTransferLockTime.getText().toString().trim())
                                * mLockTimeUnit;
                    }
                }
            });
        } else {
            mLinearLayoutTransferLockTime.setVisibility(View.GONE);
        }
        resetTransferButtonState();
    }

    @OnClick(R.id.transfer_iv_question_marker)
    public void onClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        CybexDialog.showBalanceDialog(this, getResources().getString(R.string.text_transfer_dialog_title),
                getResources().getString(R.string.text_transfer_dialog_content));
    }

    @OnClick(R.id.transfer_tv_public_key)
    public void onInputPublicKeyClicked(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        CommonSelectDialog<AccountBalanceObjectItem> dialog = new CommonSelectDialog<AccountBalanceObjectItem>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_ITEMS, (Serializable) mLockTimePublicKeys);
        bundle.putSerializable(INTENT_PARAM_SELECTED_ITEM, mSelectedLockTimePublicKey);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), CommonSelectDialog.class.getSimpleName());
        dialog.setOnAssetSelectedListener(new CommonSelectDialog.OnAssetSelectedListener<Types.public_key_type>() {
            @Override
            public void onAssetSelected(Types.public_key_type item) {
                mSelectedLockTimePublicKey = item;
                if (mSelectedLockTimePublicKey == null) {
                    return;
                }
                mTvPublicKey.setText(mSelectedLockTimePublicKey.toString());
                resetTransferButtonState();
            }
        });
    }

    @OnClick(R.id.transfer_btn_transfer)
    public void onTransferClick(View view) {
        if (AntiShake.check(view.getId())) {
            return;
        }
        checkIsLockAndTransfer();
    }

    @OnTextChanged(value = R.id.transfer_et_account_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onAccountNameTextChanged(Editable editable) {
        if (editable.toString().length() == 0) {
            mIvAccountCheck.setVisibility(View.INVISIBLE);
        }
    }

    @OnTextChanged(value = R.id.transfer_et_quantity, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onQuantityTextChanged(Editable editable) {
        /**
         * fix bug:CYM-544
         * 不实时计判断余额是否足够
         */
        //checkBalanceEnough(editable.toString());
    }

    @OnFocusChange(R.id.transfer_et_account_name)
    public void onAccountNameFocusChanged(View view, boolean isFocused) {
        String accountName = mEtAccountName.getText().toString().trim();
        if (isFocused) {
            return;
        }
        if (TextUtils.isEmpty(accountName)) {
            mToAccountObject = null;
            mTvPublicKey.setText("");
            mLockTimePublicKeys = null;
            mSelectedLockTimePublicKey = null;
            mLlPublicKey.setVisibility(View.GONE);
            resetTransferButtonState();
            return;
        }
        mPbLoading.setVisibility(View.VISIBLE);
        mIvAccountCheck.setVisibility(View.GONE);
        try {
            BitsharesWalletWraper.getInstance()
                    .get_account_object(accountName, new MessageCallback<Reply<AccountObject>>() {
                        @Override
                        public void onMessage(Reply<AccountObject> reply) {
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
    public void onQuantityFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        String amountStr = mEtQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr) || amountStr.equals(".")) {
            resetTransferButtonState();
            return;
        }
        if (mSelectedAccountBalanceObjectItem == null) {
            return;
        }
        mEtQuantity.setText(String.format(String.format(Locale.US, "%%.%df",
                mSelectedAccountBalanceObjectItem.assetObject.precision), Double.parseDouble(amountStr)));
        checkBalanceEnough(mEtQuantity.getText().toString().trim());
    }

    @OnFocusChange(R.id.transfer_et_remark)
    public void onRemarkFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        //输入完备注 重新计算手续费
        checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
    }

    @OnFocusChange(R.id.transfer_et_lock_time)
    public void onLockTimeFocusChanged(View view, boolean isFocused) {
        if (isFocused) {
            return;
        }
        if (mEtTransferLockTime.getText() != null && !mEtTransferLockTime.getText().toString().isEmpty()) {
            mTotalLockTime = (int) Double.parseDouble(mEtTransferLockTime.getText().toString().trim()) * mLockTimeUnit;
        }
        resetTransferButtonState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event) {
        /**
         * fix bug:CYM-581
         * 修复账户名验证报错
         */
        if (!mIsActivityActive || (event.getAccountObject() != null &&
                !event.getAccountObject().name.equals(mEtAccountName.getText().toString().trim()))) {
            return;
        }
        mToAccountObject = event.getAccountObject();
        resetTransferButtonState();
        mPbLoading.setVisibility(View.INVISIBLE);
        mIvAccountCheck.setVisibility(View.VISIBLE);
        if (mToAccountObject == null) {
            mIvAccountCheck.setImageResource(R.drawable.ic_close_red_24_px);
            ToastMessage.showNotEnableDepositToastMessage(this,
                    getResources().getString(R.string.text_account_not_exist), R.drawable.ic_error_16px);
        } else {
            mIvAccountCheck.setImageResource(R.drawable.ic_check_success);
            mLockTimePublicKeys = mToAccountObject.active.get_keys();
            if (mToAccountObject.active.get_keys().size() > 1) {
                mLlPublicKey.setVisibility(View.VISIBLE);
            } else {
                mLlPublicKey.setVisibility(View.GONE);
                mSelectedLockTimePublicKey = mLockTimePublicKeys.get(0);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event) {
        /**
         * fix bug:CYM-577
         * 转账成功后刷新余额
         */
        FullAccountObject fullAccountObject = event.getFullAccount();
        if (fullAccountObject == null) {
            return;
        }
        List<AccountBalanceObject> balances = fullAccountObject.balances;
        if (balances == null || balances.size() == 0 ||
                mAccountBalanceObjectItems == null || mAccountBalanceObjectItems.size() == 0) {
            return;
        }
        for (AccountBalanceObjectItem item : mAccountBalanceObjectItems) {
            for (AccountBalanceObject balance : balances) {
                if (item.accountBalanceObject.asset_type.equals(balance.asset_type)) {
                    item.accountBalanceObject = balance;
                    break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadTransferFee(Event.LoadTransferFee event) {
        FeeAmountObject fee = event.getFee();
        if (fee.asset_id.equals(ASSET_ID_CYB)) {
            mCybFeeAmountObject = fee;
            //未选择币种时 手续费默认显示CYB
            if (mSelectedAccountBalanceObjectItem == null) {
                mIsCybEnough = mCybAccountBalanceObjectItem != null
                        && mCybAccountBalanceObjectItem.accountBalanceObject.balance >= fee.amount;
                /**
                 * fix bug:CYM-543
                 * 解决账户无资产时不显示手续费
                 */
                if (mCybAccountBalanceObjectItem == null) {
                    if (mCybAssetObject == null) {
                        mCybAssetObject = mWebSocketService.getAssetObject(fee.asset_id);
                    }
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(
                                    fee.amount / Math.pow(10, mCybAssetObject == null ? 5 : mCybAssetObject.precision),
                                    mCybAssetObject == null ? 5 : mCybAssetObject.precision),
                            mCybAssetObject == null ? ASSET_SYMBOL_CYB :
                                    AssetUtil.parseSymbol(mCybAssetObject.symbol)));
                } else {
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(
                                    fee.amount / Math.pow(10, mCybAccountBalanceObjectItem.assetObject.precision),
                                    mCybAccountBalanceObjectItem.assetObject.precision),
                            AssetUtil.parseSymbol(mCybAccountBalanceObjectItem.assetObject.symbol)));
                }
            } else if (fee.asset_id.equals(mSelectedAccountBalanceObjectItem.assetObject.id.toString())) {
                //只有当CYB不足时才会扣除当前币的手续费 而当前选择币种为CYB时 默认CYB不足
                mIsCybEnough = false;
                mCurrAssetFeeAmountObject = fee;
                mTvFee.setText(String.format("%s %s",
                        AssetUtil.formatNumberRounding(
                                fee.amount / Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                                mSelectedAccountBalanceObjectItem.assetObject.precision),
                        AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
            } else {
                if (mCybAccountBalanceObjectItem == null
                        || mCybAccountBalanceObjectItem.accountBalanceObject.balance < fee.amount) {
                    mIsCybEnough = false;
                    checkIsLockAndLoadTransferFee(mSelectedAccountBalanceObjectItem.assetObject.id.toString(),
                            event.isToTransfer());
                } else {
                    mIsCybEnough = true;
                    mTvFee.setText(String.format("%s %s",
                            AssetUtil.formatNumberRounding(
                                    fee.amount / Math.pow(10, mCybAccountBalanceObjectItem.assetObject.precision),
                                    mCybAccountBalanceObjectItem.assetObject.precision),
                            AssetUtil.parseSymbol(mCybAccountBalanceObjectItem.assetObject.symbol)));
                }
            }
        } else {
            mCurrAssetFeeAmountObject = fee;
            mTvFee.setText(String.format("%s %s",
                    AssetUtil.formatNumberRounding(
                            fee.amount / Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision),
                            mSelectedAccountBalanceObjectItem.assetObject.precision),
                    AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)));
        }
        checkBalanceEnough(mEtQuantity.getText().toString().trim());
        if (event.isToTransfer() && mIsBalanceEnough) {
            showTransferConfirmationDialog();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransfer(Event.Transfer event) {
        hideLoadDialog();
        if (event.isSuccess()) {
            mCheckAddressExistDisposable = DBManager.getDbProvider(this).checkAddressExist(mUserName,
                    mEtAccountName.getText().toString().trim(), Address.TYPE_TRANSFER)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Address>() {
                        @Override
                        public void accept(Address address) throws Exception {
                            if (address != null) {
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

    private int getTimeUnit(String unit) {
        if (unit.equals(getResources().getString(R.string.text_lock_time_transfer_period_seconds))) {
            return 1;
        } else if (unit.equals(getResources().getString(R.string.text_lock_time_transfer_period_minutes))) {
            return 60;
        } else if (unit.equals(getResources().getString(R.string.text_lock_time_transfer_period_hours))) {
            return 360;
        } else if (unit.equals(getResources().getString(R.string.text_lock_time_transfer_period_days))) {
            return 21600;
        }
        return 1;
    }

    private void showAddAddressDialog() {
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
     *
     * @param items
     */
    private void removeZeroBalance(List<AccountBalanceObjectItem> items) {
        if (items == null || items.size() == 0) {
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
            mAssetWhiteList = mWebSocketService.getAssetWhiteList();
            loadAccountBalanceObjectItems(fullAccountObject);
            mFromAccountObject = fullAccountObject == null ? null : fullAccountObject.account;
            checkIsLockAndLoadTransferFee(ASSET_ID_CYB, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void loadAccountBalanceObjectItems(FullAccountObject fullAccountObject) {
        /**
         * fix online bug
         * java.lang.NullPointerException: Attempt to read from field
         * 'java.util.List com.cybex.provider.graphene.chain.FullAccountObject.balances' on a null object reference
         */
        if (mAccountBalanceObjectItems != null || fullAccountObject == null) {
            return;
        }
        if (NetworkUtils.getConnectivityStatus(this) == TYPE_NOT_CONNECTED) {
            return;
        }
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        if (accountBalanceObjects != null && accountBalanceObjects.size() > 0) {
            mAccountBalanceObjectItems = new ArrayList<>();
            for (AccountBalanceObject balance : accountBalanceObjects) {
                if (!mAssetWhiteList.contains(balance.asset_type.toString())) {
                    continue;
                }
                if (balance.balance == 0) {
                    continue;
                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                mAccountBalanceObjectItems.add(item);
            }
        }
    }

    private void loadAddress() {
        if (TextUtils.isEmpty(mUserName)) {
            return;
        }
        mLoadAddressDisposable = DBManager.getDbProvider(this).getAddress(mUserName, Address.TYPE_TRANSFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        mAddresses = addresses;
                        if (mAddresses == null || mAddresses.size() == 0) {
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
    private void clearTransferData() {
        mIsCybEnough = false;
        mIsBalanceEnough = false;
        mTransferOperationFee = null;
        mSelectedAccountBalanceObjectItem = null;
        mToAccountObject = null;
        mTotalLockTime = 0;
        mSelectedLockTimePublicKey = null;
        mLockTimePublicKeys = null;
        mSwLockTime.setChecked(false);
        mEtTransferLockTime.setText("");
        mTvPublicKey.setText("");
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
    private void checkIsLockAndLoadTransferFee(String feeAssetId, boolean isLoadFeeToTransfer) {
        if (mFromAccountObject == null) {
            return;
        }
        if (BitsharesWalletWraper.getInstance().is_locked() && !isLoginFromENotes()) {
            unlockDialog = CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject,
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
     * 卡状态读取成功后回调
     *
     * @param card
     */
    @Override
    protected void readCardOnSuccess(Card card) {
        mCard = card;
        if (unlockDialog != null) { unlockDialog.dismiss(); }
        if (unlockDialogWithEnotes != null) {
            if (!TextUtils.isEmpty(mEtRemark.getText().toString())) {
                //如果带了memo，更新UI
                unlockDialogWithEnotes.showNoSupportMemoText();
            } else {
                //没带memo，执行转账
                unlockDialogWithEnotes.dismiss();
                toTransfer();
            }
        } else {
            super.readCardOnSuccess(card);
        }
    }

    @Override
    protected void readCardError(int code, String message) {
        super.readCardError(code, message);
        if (unlockDialogWithEnotes != null) {
            unlockDialogWithEnotes.hideProgress();
        }
    }

    @Override
    protected void nfcStartReadCard() {
        if(unlockDialogWithEnotes != null) {
            unlockDialogWithEnotes.showProgress();
            unlockDialogWithEnotes.showNormalText();
        }
    }

    /**
     * 检查钱包锁定状态 -> 加载转账手续费 -> 转账
     */
    private void checkIsLockAndTransfer() {
        if (BitsharesWalletWraper.getInstance().is_locked() && !isLoginFromENotes()) {
            unlockDialog = CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mFromAccountObject,
                    mFromAccountObject.name, new UnlockDialog.UnLockDialogClickListener() {
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
    private void showTransferConfirmationDialog() {
        /**
         * fix bug:CYM-800
         * 修复手续费未获取到转账crash
         */
        if (mTransferOperationFee == null || (mCybFeeAmountObject == null && mCurrAssetFeeAmountObject == null)) {
            loadTransferFee(ASSET_ID_CYB, true);
            return;
        }
        CybexDialog.showTransferConfirmationDialog(
                this,
                mEtAccountName.getText().toString().trim(),
                String.format("%s %s", mEtQuantity.getText().toString().trim(),
                        AssetUtil.parseSymbol(mSelectedAccountBalanceObjectItem.assetObject.symbol)),
                mTvFee.getText().toString().trim(),
                mEtRemark.getText().toString().trim(),
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        //确认转账弹框确认按钮点击事件
                        if (isLoginFromENotes()) {
                            //eNotes登陆
                            Log.d("status", "eNotes : true");
                            mIsUsedCloudPassword = false;
                            showEnotesWaitingDialog();
                        } else {
                            //其他方式登陆
                            Log.d("status", "eNotes : false");
                            toTransfer();
                        }
                    }
                });
    }

    /**
     * eNotes等待弹窗
     */
    private void showEnotesWaitingDialog() {
        unlockDialogWithEnotes = CybexDialog.showUnlockWithEnotesWalletDialog(
                getSupportFragmentManager(),
                mFromAccountObject,
                mFromAccountObject.name,
                new UnlockDialogWithEnotes.UnLockDialogClickListener() {
                    @Override
                    public void onUnLocked(String password) {
                        if(password != null) {
                            mIsUsedCloudPassword = true;
                            toTransfer();
                        }
                     }
                },
                new UnlockDialogWithEnotes.OnDismissListener() {
                    @Override
                    public void onDismiss(int result) {

                    }
                }
        );
    }

    /**
     * 转账
     */
    private void toTransfer() {
        if (mFromAccountObject == null || mToAccountObject == null || mSelectedAccountBalanceObjectItem == null) {
            return;
        }

        if (isLoginFromENotes() && !mIsUsedCloudPassword) {
            if (!cardManager.isConnected()) {
                if (ReaderUtils.supportNfc(this)) {
                    showToast(this, getResources().getString(R.string.error_connect_card));
                } else {
                    showToast(this, getString(R.string.error_connect_card_ble));
                }
                return;
            }
            if (!cardManager.isPresent()) {
                if (ReaderUtils.supportNfc(this)) { showToast(this, getString(R.string.error_connect_card)); } else {
                    showToast(this, getString(R.string.error_connect_card_ble));
                }
                return;
            }
            if (mCard != null && !mCard.getCurrencyPubKey().equals(getLoginPublicKey())) {
                showToast(this, getString(R.string.please_right_card));
                return;
            }
            if (BaseActivity.cardApp != null) {
                mCard = BaseActivity.cardApp;
            }
        }
        showLoadDialog();
        Operations.base_operation transferOperation;
        if (mSwLockTime.isChecked() && mSelectedLockTimePublicKey != null && mTotalLockTime != 0) {
            transferOperation = BitsharesWalletWraper.getInstance().getTransferOperationWithLockTime(
                    mFromAccountObject.id,
                    mToAccountObject.id,
                    mSelectedAccountBalanceObjectItem.assetObject.id,
                    mIsCybEnough ? mCybFeeAmountObject.amount : mCurrAssetFeeAmountObject.amount,
                    ObjectId.create_from_string(
                            mIsCybEnough ? mCybFeeAmountObject.asset_id : mCurrAssetFeeAmountObject.asset_id),
                    (long) (Double.parseDouble(mEtQuantity.getText().toString().trim()) * Math.pow(10,
                            mSelectedAccountBalanceObjectItem.assetObject.precision)),
                    mEtRemark.getText().toString().trim(),
                    mFromAccountObject.options.memo_key,
                    mToAccountObject.options.memo_key,
                    mSelectedLockTimePublicKey,
                    mTotalLockTime,
                    1);

        } else {
            transferOperation = BitsharesWalletWraper.getInstance().getTransferOperation(
                    mFromAccountObject.id,
                    mToAccountObject.id,
                    mSelectedAccountBalanceObjectItem.assetObject.id,
                    mIsCybEnough ? mCybFeeAmountObject.amount : mCurrAssetFeeAmountObject.amount,
                    ObjectId.create_from_string(
                            mIsCybEnough ? mCybFeeAmountObject.asset_id : mCurrAssetFeeAmountObject.asset_id),
                    (long) (Double.parseDouble(mEtQuantity.getText().toString().trim()) * Math.pow(10,
                            mSelectedAccountBalanceObjectItem.assetObject.precision)),
                    mEtRemark.getText().toString().trim(),
                    mFromAccountObject.options.memo_key,
                    mToAccountObject.options.memo_key);
        }
        try {
            BitsharesWalletWraper.getInstance()
                    .get_dynamic_global_properties(new MessageCallback<Reply<DynamicGlobalPropertyObject>>() {
                        @Override
                        public void onMessage(Reply<DynamicGlobalPropertyObject> reply) {
                            SignedTransaction signedTransaction;
                            if (mCard == null || mIsUsedCloudPassword) {
                                signedTransaction = BitsharesWalletWraper.getInstance().getSignedTransaction(
                                        mFromAccountObject, transferOperation, ID_TRANSER_OPERATION, reply.result);
                            } else {
                                signedTransaction = BitsharesWalletWraper.getInstance()
                                        .getSignedTransactionByENotes(cardManager, mCard,
                                                mFromAccountObject, transferOperation, ID_TRANSER_OPERATION,
                                                reply.result);
                            }
                            try {
                                BitsharesWalletWraper.getInstance()
                                        .broadcast_transaction_with_callback(signedTransaction, mTransferCallback);
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
     *
     * @param feeAssetId
     * @param isLoadFeeToTransfer 是否加载完手续费之后 自动转账
     */
    private void loadTransferFee(String feeAssetId, boolean isLoadFeeToTransfer) {
        if (mFromAccountObject == null) {
            return;
        }
        mTransferOperationFee = BitsharesWalletWraper.getInstance().getTransferOperation(
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
            BitsharesWalletWraper.getInstance()
                    .get_required_fees(feeAssetId, ID_TRANSER_OPERATION, mTransferOperationFee,
                            new MessageCallback<Reply<List<FeeAmountObject>>>() {
                                @Override
                                public void onMessage(Reply<List<FeeAmountObject>> reply) {
                                    EventBus.getDefault()
                                            .post(new Event.LoadTransferFee(reply.result.get(0), isLoadFeeToTransfer));
                                }

                                @Override
                                public void onFailure() {

                                }
                            });
        } catch (NetworkStatusException e) {
            e.printStackTrace();
        }
    }

    private AccountBalanceObjectItem findAccountBalanceObjectItem(
            String assetId,
            List<AccountBalanceObjectItem> items) {
        if (TextUtils.isEmpty(assetId) || items == null || items.size() == 0) {
            return null;
        }
        for (AccountBalanceObjectItem item : items) {
            if (assetId.equals(item.accountBalanceObject.asset_type.toString())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 检查资产是否足够
     *
     * @param amountStr
     */
    private void checkBalanceEnough(String amountStr) {
        if (mSelectedAccountBalanceObjectItem == null ||
                TextUtils.isEmpty(amountStr) || amountStr.endsWith(".")) {
            return;
        }
        if (!mIsCybEnough && mCurrAssetFeeAmountObject == null) {
            mBtnTransfer.setEnabled(true);
        }
        double fee = mIsCybEnough || mCurrAssetFeeAmountObject == null ? 0 : mCurrAssetFeeAmountObject.amount /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        double balanceAmount = mSelectedAccountBalanceObjectItem.accountBalanceObject.balance /
                Math.pow(10, mSelectedAccountBalanceObjectItem.assetObject.precision);
        BigDecimal balance = new BigDecimal(Double.toString(balanceAmount)).subtract(new BigDecimal(amountStr))
                .subtract(new BigDecimal(Double.toString(fee)));
        if (balance.doubleValue() < 0) {
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
    private void resetTransferButtonState() {
        /**
         * fix bug:CYM-507
         * 转账金额必须大于0
         */
        try {
            if (mSwLockTime.isChecked()) {
                mBtnTransfer.setEnabled(
                        mToAccountObject != null &&
                                mSelectedAccountBalanceObjectItem != null &&
                                Double.parseDouble(mEtQuantity.getText().toString()) > 0 &&
                                mIsBalanceEnough &&
                                mSelectedLockTimePublicKey != null &&
                                mTotalLockTime > 0
                );
            } else {
                mBtnTransfer.setEnabled(
                        mToAccountObject != null &&
                                mSelectedAccountBalanceObjectItem != null &&
                                Double.parseDouble(mEtQuantity.getText().toString()) > 0 &&
                                mIsBalanceEnough);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mBtnTransfer.setEnabled(false);
        }
    }

    /**
     * 转账callback
     */
    private MessageCallback<Reply<String>> mTransferCallback = new MessageCallback<Reply<String>>() {

        @Override
        public void onMessage(Reply<String> reply) {
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
            if (dest.length() == 0 && source.equals(".")) {
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
