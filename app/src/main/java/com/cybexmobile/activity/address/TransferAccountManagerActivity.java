package com.cybexmobile.activity.address;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.cybex.database.DBManager;
import com.cybex.database.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.adapter.TransferAccountManagerRecyclerViewAdapter;
import com.cybexmobile.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybexmobile.utils.Constant.PREF_NAME;

public class TransferAccountManagerActivity extends BaseActivity implements
        TransferAccountManagerRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_account_manager_rv)
    RecyclerView mRvTransferAccount;

    private String mUserName;
    private TransferAccountManagerRecyclerViewAdapter mTransferAccountAdapter;

    private Unbinder mUnbinder;
    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_account_manager);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mRvTransferAccount.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mTransferAccountAdapter = new TransferAccountManagerRecyclerViewAdapter(TransferAccountManagerActivity.this, new ArrayList<>());
        mTransferAccountAdapter.setOnItemClickListener(this);
        mRvTransferAccount.setAdapter(mTransferAccountAdapter);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transfer_account_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_transfer_account:
                Intent intent = new Intent(this, AddTransferAccountActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private void loadAddress(){
        if(TextUtils.isEmpty(mUserName)){
            return;
        }
        mDisposable = DBManager.getDbProvider(this).getAddress(mUserName, Address.TYPE_TRANSFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        mTransferAccountAdapter.setAddresses(addresses);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    public void onItemClick(Address address) {

    }
}
