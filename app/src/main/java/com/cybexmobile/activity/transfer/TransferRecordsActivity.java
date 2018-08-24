package com.cybexmobile.activity.transfer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cybex.database.DBManager;
import com.cybex.database.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.adapter.TransferRecordsRecyclerViewAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.event.Event;
import com.cybexmobile.faucet.AssetsPair;
import com.cybexmobile.graphene.chain.AccountHistoryObject;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.BlockHeader;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.GlobalConfigObject;
import com.cybexmobile.graphene.chain.ObjectId;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.service.WebSocketService;
import com.cybexmobile.utils.Constant;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.cybexmobile.utils.Constant.PREF_IS_LOGIN_IN;
import static com.cybexmobile.utils.Constant.PREF_NAME;

public class TransferRecordsActivity extends BaseActivity implements TransferRecordsRecyclerViewAdapter.OnItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.transfer_records_rv_transfer_records)
    RecyclerView mRvTransferRecords;

    private TransferRecordsRecyclerViewAdapter mTransferRecordsAdapter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;

    private boolean mIsLoginIn;
    private String mName;
    private List<TransferHistoryItem> mTransferHistoryItems = new ArrayList<>();
    private Disposable mCheckAddressExistDisposable;

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
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_BLOCK, transferHistoryItem.block);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_FROM_ACCOUNT, transferHistoryItem.fromAccount);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_TO_ACCOUNT, transferHistoryItem.toAccount);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_FEE_ASSET, transferHistoryItem.feeAsset);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_ASSET, transferHistoryItem.transferAsset);
        intent.putExtra(Constant.INTENT_PARAM_TRANSFER_MY_ACCOUNT, mAccountObject);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadBlock(Event.LoadBlock event){
        if(mTransferHistoryItems == null || mTransferHistoryItems.size() == 0){
            return;
        }
        for(int i = 0; i < mTransferHistoryItems.size(); i++){
            if(mTransferHistoryItems.get(i).callId == event.getCallId()){
                mTransferHistoryItems.get(i).block = event.getBlockHeader();
                mTransferRecordsAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountObject(Event.LoadAccountObject event){
        AccountObject accountObject = event.getAccountObject();
        if(accountObject == null || mTransferHistoryItems == null){
            return;
        }
        mCheckAddressExistDisposable = DBManager.getDbProvider(this).checkAddressExist(null,
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
                                mTransferHistoryItems.get(i).address = address;
                                if(mTransferRecordsAdapter != null){
                                    mTransferRecordsAdapter.notifyItemChanged(i);
                                }
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
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadAccountHistory(Event.LoadAccountHistory event){
        List<AccountHistoryObject> accountHistoryObjects = event.getAccountHistoryObjects();
        if(accountHistoryObjects == null || accountHistoryObjects.size() == 0){
            return;
        }
        TransferHistoryItem item = null;
        Iterator<AccountHistoryObject> it = accountHistoryObjects.iterator();
        //过滤非交易记录 op4为交易记录
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        while (it.hasNext()){
            AccountHistoryObject accountHistoryObject = it.next();
            if(accountHistoryObject.op.get(0).getAsInt() == 0){
                item = new TransferHistoryItem();
                item.accountHistoryObject = accountHistoryObject;
                item.transferOperation = gson.fromJson(accountHistoryObject.op.get(1), Operations.transfer_operation.class);
                /**
                 * fix bug:CYM-572
                 * 转账记录列表过滤充值提现记录
                 */
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
                //加载区块信息
                item.callId = BitsharesWalletWraper.getInstance().get_call_id().getAndIncrement();
                mTransferHistoryItems.add(item);
                mWebSocketService.loadBlock(item.callId, item.accountHistoryObject.block_num);
                mWebSocketService.loadAccountObject(item.transferOperation.from.equals(mAccountObject.id) ?
                        item.transferOperation.to.toString() : item.transferOperation.from.toString());
            } else {
                it.remove();
            }
        }
        hideLoadDialog();
        if(mTransferRecordsAdapter == null){
            mTransferRecordsAdapter = new TransferRecordsRecyclerViewAdapter(this, mAccountObject, mTransferHistoryItems);
            mRvTransferRecords.setAdapter(mTransferRecordsAdapter);
            mTransferRecordsAdapter.setOnItemClickListener(this);
        } else {
            mTransferRecordsAdapter.notifyDataSetChanged();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if(mIsLoginIn){
                showLoadDialog(true);
                FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mName);
                if(fullAccountObject != null){
                    mAccountObject = fullAccountObject.account;
                    mWebSocketService.loadAccountHistory(mAccountObject.id, 100);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    public class TransferHistoryItem{
        public int callId;//通过callid对应请求结果
        public AccountHistoryObject accountHistoryObject;
        public Operations.transfer_operation transferOperation;
        public BlockHeader block;
        public AccountObject fromAccount;
        public AccountObject toAccount;
        public AssetObject feeAsset;
        public AssetObject transferAsset;
        public Address address;
    }
}
