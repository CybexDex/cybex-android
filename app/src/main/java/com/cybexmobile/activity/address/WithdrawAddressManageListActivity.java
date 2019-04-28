package com.cybexmobile.activity.address;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.adapter.TransferAccountManagerRecyclerViewAdapter;
import com.cybex.basemodule.base.BaseActivity;
import com.cybexmobile.dialog.AddressOperationSelectDialog;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybexmobile.shake.AntiShake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_ID;
import static com.cybex.basemodule.constant.Constant.INTENT_PARAM_CRYPTO_NAME;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class WithdrawAddressManageListActivity extends BaseActivity implements TransferAccountManagerRecyclerViewAdapter.OnItemClickListener,
        AddressOperationSelectDialog.OnAddressOperationSelectedListener {

    private Unbinder mUnbinder;
    private TransferAccountManagerRecyclerViewAdapter mWithdrawAddressManagerAdapter;
    private Disposable mLoadAddressDisposable;
    private Disposable mDeleteAddressDisposable;
    private Address mCurrAddress;
    private int mCurrPosition;

    private String mUserName;
    private String mTokenName;
    private String mTokenId;
    private boolean mIsTag;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.withdraw_address_toolbar_title)
    TextView mTvToolbarTitle;
    @BindView(R.id.withdraw_address_note_address_account)
    TextView mTvNoteAddressAccount;
    @BindView(R.id.withdraw_address_account_rv)
    RecyclerView mWithdrawAddressRecyclerView;
    @BindView(R.id.withdraw_address_subtitle_memo)
    TextView mTvSubtitleMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_address_manage_list);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mTokenName = getIntent().getStringExtra("assetName");
        mTokenId = getIntent().getStringExtra("assetId");
        mIsTag = getIntent().getBooleanExtra("tag", false);
        if (mIsTag) {
            mTvSubtitleMemo.setVisibility(View.VISIBLE);
            mTvSubtitleMemo.setText(getResources().getString(R.string.withdraw_xrp_tag));
            mTvToolbarTitle.setText(String.format("%s %s", mTokenName, getResources().getString(R.string.withdraw_address_title)));
            mTvNoteAddressAccount.setText(getResources().getString(R.string.withdraw_address_note_address));
        } else {
            mTvSubtitleMemo.setVisibility(View.GONE);
            mTvToolbarTitle.setText(String.format("%s %s", mTokenName, getResources().getString(R.string.withdraw_address_title)));
            mTvNoteAddressAccount.setText(getResources().getString(R.string.withdraw_address_note_address));
        }
        mWithdrawAddressRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mWithdrawAddressManagerAdapter = new TransferAccountManagerRecyclerViewAdapter(this, new ArrayList<>(), mTokenName);
        mWithdrawAddressManagerAdapter.setOnItemClickListener(this);
        mWithdrawAddressRecyclerView.setAdapter(mWithdrawAddressManagerAdapter);
        loadAddress();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadAddress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        if (mLoadAddressDisposable != null && !mLoadAddressDisposable.isDisposed()) {
            mLoadAddressDisposable.dispose();
        }
        if (mDeleteAddressDisposable != null && !mDeleteAddressDisposable.isDisposed()) {
            mDeleteAddressDisposable.dispose();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_withdraw_address, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AntiShake.check(item.getItemId())) { return false; }
        switch (item.getItemId()) {
            case R.id.action_add_transfer_account:
                Intent intent = new Intent(this, AddTransferAccountActivity.class);
                intent.putExtra(INTENT_PARAM_CRYPTO_NAME, mTokenName);
                intent.putExtra(INTENT_PARAM_CRYPTO_ID, mTokenId);
                intent.putExtra("tag", mIsTag);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Address address, int position) {
        mCurrAddress = address;
        mCurrPosition = position;
        AddressOperationSelectDialog dialog = new AddressOperationSelectDialog();
        dialog.show(getSupportFragmentManager(), AddressOperationSelectDialog.class.getSimpleName());
        dialog.setOnAddressOperationSelectedListener(this);
    }

    @Override
    public void onAddressOperationSelected(int operation) {
        if (mCurrAddress == null) {
            return;
        }
        mWithdrawAddressManagerAdapter.notifyItemChanged(mCurrPosition);
        switch (operation) {
            case AddressOperationSelectDialog.OPERATION_COPY:
                copyAddress();
                break;
            case AddressOperationSelectDialog.OPERATION_DELETE:
                deleteAddressConfirm();
                break;
            case AddressOperationSelectDialog.OPETATION_CANCEL:
        }

    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void deleteAddressConfirm(){
        CybexDialog.showDeleteConfirmDialog(
                this,
                getResources().getString(R.string.text_delete_confirm),
                getResources().getString(R.string.text_confirm_to_delete_this_account),
                mCurrAddress,
                new CybexDialog.ConfirmationDialogClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        deleteAddress();
                    }
                }, null);

    }


    private void loadAddress() {
        if (TextUtils.isEmpty(mUserName)) {
            return;
        }
        mLoadAddressDisposable = DBManager.getDbProvider(this).getAddress(mUserName, mTokenId, Address.TYPE_WITHDRAW)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        Collections.sort(addresses);
                        mWithdrawAddressManagerAdapter.setAddresses(addresses);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void deleteAddress() {
        mDeleteAddressDisposable = DBManager.getDbProvider(this).deleteAddress(mCurrAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) throws Exception {
                        ToastMessage.showNotEnableDepositToastMessage(WithdrawAddressManageListActivity.this,
                                getResources().getString(R.string.text_deleted),
                                R.drawable.ic_check_circle_green);
                        loadAddress();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ToastMessage.showNotEnableDepositToastMessage(WithdrawAddressManageListActivity.this,
                                getResources().getString(R.string.text_deleted_failed),
                                R.drawable.ic_error_16px);
                    }
                });
    }

    private void copyAddress() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip;
        if (!TextUtils.isEmpty(mCurrAddress.getMemo())) {
            clip = ClipData.newPlainText("address", mCurrAddress.getAddress() + mCurrAddress.getMemo());
        } else if (!TextUtils.isEmpty(mCurrAddress.getTag())) {
            clip = ClipData.newPlainText("address", mCurrAddress.getAddress() + mCurrAddress.getTag());
        } else {
            clip = ClipData.newPlainText("address", mCurrAddress.getAddress());
        }

        clipboard.setPrimaryClip(clip);
        ToastMessage.showNotEnableDepositToastMessage(WithdrawAddressManageListActivity.this,
                getResources().getString(R.string.text_copied),
                R.drawable.ic_check_circle_green);
    }
}
