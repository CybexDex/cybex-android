package com.cybexmobile.activity.transfer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybex.provider.graphene.chain.AccountHistoryObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.R;
import com.cybexmobile.adapter.TransferRecordsRecyclerViewAdapter;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybex.basemodule.constant.Constant.PREF_IS_LOGIN_IN;
import static com.cybex.basemodule.constant.Constant.PREF_NAME;

public class TransferRecordsActivity extends BaseActivity implements TransferRecordsRecyclerViewAdapter.OnItemClickListener, OnRefreshListener, OnLoadMoreListener {

    private static final int MAX_PAGE_COUNT = 20;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_records_rv_transfer_records)
    RecyclerView mRvTransferRecords;
    @BindView(R.id.layout_refresh_transfer_records)
    SmartRefreshLayout mRefreshLayout;

    private TransferRecordsRecyclerViewAdapter mTransferRecordsAdapter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;

    private int mCurrPage;

    private boolean mIsLoginIn;
    private String mName;
    private List<TransferHistoryItem> mTransferHistoryItems = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_transfer_records);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsLoginIn = preferences.getBoolean(PREF_IS_LOGIN_IN, false);
        mName = preferences.getString(PREF_NAME, "");
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mRvTransferRecords.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mTransferRecordsAdapter = new TransferRecordsRecyclerViewAdapter(this, mAccountObject, mTransferHistoryItems);
        mRvTransferRecords.setAdapter(mTransferRecordsAdapter);
        mTransferRecordsAdapter.setOnItemClickListener(this);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onItemClick(TransferHistoryItem transferHistoryItem) {
        Intent intent = new Intent(this, TransferDetailsActivity.class);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_OPERATION, transferHistoryItem.transferOperation);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_FROM_ACCOUNT, transferHistoryItem.fromAccount);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_TO_ACCOUNT, transferHistoryItem.toAccount);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_FEE_ASSET, transferHistoryItem.feeAsset);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_ASSET, transferHistoryItem.transferAsset);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT, mAccountObject);
        intent.putExtra(Constant.INTENT_PARAM_TIMESTAMP, transferHistoryItem.accountHistoryObject.timestamp);
        startActivity(intent);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        loadTransferRecords(++mCurrPage, MAX_PAGE_COUNT, false);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        loadTransferRecords(mCurrPage = 0, MAX_PAGE_COUNT, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        AccountObject accountObject = event.getAccountObject();
        if(accountObject == null){
            return;
        }
        mCompositeDisposable.add(DBManager.getDbProvider(this).checkAddressExist(null,
                accountObject.name, Address.TYPE_TRANSFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Address>() {
                    @Override
                    public void accept(Address address) throws Exception {
                        for(int i = 0; i < mTransferHistoryItems.size(); i++){
                            if(mTransferHistoryItems.get(i).transferOperation.from.equals(accountObject.id)){
                                if(mTransferHistoryItems.get(i).fromAccount != null){
                                    continue;
                                }
                                mTransferHistoryItems.get(i).fromAccount = accountObject;
                                mTransferHistoryItems.get(i).address = address;
                                mTransferRecordsAdapter.notifyItemChanged(i);
                                break;
                            }
                            if(mTransferHistoryItems.get(i).transferOperation.to.equals(accountObject.id)){
                                if(mTransferHistoryItems.get(i).toAccount != null){
                                    continue;
                                }
                                mTransferHistoryItems.get(i).toAccount = accountObject;
                                mTransferHistoryItems.get(i).address = address;
                                mTransferRecordsAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        for(int i = 0; i < mTransferHistoryItems.size(); i++){
                            if(mTransferHistoryItems.get(i).transferOperation.from.equals(accountObject.id)){
                                if(mTransferHistoryItems.get(i).fromAccount != null){
                                    continue;
                                }
                                mTransferHistoryItems.get(i).fromAccount = accountObject;
                                if(mTransferRecordsAdapter != null){
                                    mTransferRecordsAdapter.notifyItemChanged(i);
                                }
                                break;
                            }
                            if(mTransferHistoryItems.get(i).transferOperation.to.equals(accountObject.id)){
                                if(mTransferHistoryItems.get(i).toAccount != null){
                                    continue;
                                }
                                mTransferHistoryItems.get(i).toAccount = accountObject;
                                if(mTransferRecordsAdapter != null){
                                    mTransferRecordsAdapter.notifyItemChanged(i);
                                }
                                break;
                            }
                        }
                    }
                }));
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mIsLoginIn){
                FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
                if(fullAccountObject != null){
                    mAccountObject = fullAccountObject.account;
                    mTransferRecordsAdapter.setAccountObject(mAccountObject);
                    loadTransferRecords(mCurrPage, MAX_PAGE_COUNT, true);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    private void loadTransferRecords(int page, int limit, boolean isRefresh) {
        if (mAccountObject == null) {
            mRefreshLayout.finishRefresh();
            mRefreshLayout.finishLoadMore();
            return;
        }
        mCompositeDisposable.add(RetrofitFactory.getInstance().apiCybexLive()
                .getTransferRecords("null", page, limit, "or", mAccountObject.id.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AccountHistoryObject>>() {
                    @Override
                    public void accept(List<AccountHistoryObject> accountHistoryObjects) throws Exception {
                        mRefreshLayout.finishRefresh();
                        mRefreshLayout.finishLoadMore();
                        if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
                            return;
                        }
                        if (isRefresh) {
                            mTransferHistoryItems.clear();
                        }
                        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                        TransferHistoryItem item = null;
                        for (AccountHistoryObject accountHistoryObject : accountHistoryObjects) {
                            item = new TransferHistoryItem();
                            item.accountHistoryObject = accountHistoryObject;
                            item.transferOperation = gson.fromJson(accountHistoryObject.op.get(1), Operations.transfer_operation.class);
                            if(!mAccountObject.id.toString().equals("1.2.4733") &&
                                    (item.transferOperation.to.toString().equals("1.2.4733") ||
                                            item.transferOperation.from.toString().equals("1.2.4733"))){
                                continue;
                            }
                            item.transferAsset = mWebSocketService.getAssetObject(item.transferOperation.amount.asset_id.toString());
                            item.feeAsset = mWebSocketService.getAssetObject(item.transferOperation.fee.asset_id.toString());
                            if(item.transferOperation.from.equals(mAccountObject.id)){
                                item.fromAccount = mAccountObject;
                            }
                            if(item.transferOperation.to.equals(mAccountObject.id)){
                                item.toAccount = mAccountObject;
                            }
                            mTransferHistoryItems.add(item);
                            mWebSocketService.loadAccountObject(item.transferOperation.from.equals(mAccountObject.id) ?
                                    item.transferOperation.to.toString() : item.transferOperation.from.toString());
                        }
                        hideLoadDialog();
                        mTransferRecordsAdapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mRefreshLayout.finishRefresh();
                        mRefreshLayout.finishLoadMore();
                    }
                }));
    }

    public class TransferHistoryItem{
        public AccountHistoryObject accountHistoryObject;
        public Operations.transfer_operation transferOperation;
        public AccountObject fromAccount;
        public AccountObject toAccount;
        public AssetObject feeAsset;
        public AssetObject transferAsset;
        public Address address;
    }
}
