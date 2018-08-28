package com.cybexmobile.activity.gateway.records;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybexmobile.R;
import com.cybexmobile.adapter.DepositWithdrawRecordAdapter;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.base.BaseActivity;
import com.cybex.provider.http.response.GateWayRecordsResponse;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybex.provider.http.entity.Record;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.dialog.UnlockDialog;
import com.cybexmobile.event.Event;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybexmobile.service.WebSocketService;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybexmobile.utils.Constant.PREF_NAME;

public class DepositWithdrawRecordsActivity extends BaseActivity implements OnRefreshListener, OnLoadMoreListener {

    public static final String TAG = DepositWithdrawRecordsActivity.class.getName();
    private static final int LOAD_COUNT = 20;
    private String mAccountName;
    private String mFundType;
    private int mTotalItemAmount = 0;
    private boolean mIsRefresh;

    private Unbinder mUnbinder;
    private AccountObject mAccountObject;
    private AssetObject mAssetObject;
    private WebSocketService mWebSocketService;
    private DepositWithdrawRecordAdapter mDepositWithdrawRecordAdapter;
    private List<GatewayDepositWithdrawRecordsItem> mRecordsItems = new ArrayList<>();
    private List<Address> mAddressList = new ArrayList<>();

    @BindView(R.id.deposit_records_rv_deposit_records)
    RecyclerView mDepositRecordsRecyclerView;
    @BindView(R.id.deposit_records_refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.deposit_records_tv_title)
    TextView mTvTitle;

    private Disposable mRequestRecordsDisposable;
    private Disposable mLoadAddressDisposable;

    private String mSignature;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mAccountName);
            if(fullAccountObject != null){
                mAccountObject = fullAccountObject.account;
                mRefreshLayout.autoRefresh();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_records);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mAssetObject = (AssetObject) getIntent().getSerializableExtra("assetObject");
        mFundType = getIntent().getStringExtra("fundType");
        if(mFundType.equals("DEPOSIT")){
            mTvTitle.setText(getResources().getString(R.string.title_deposit_records));
        } else if(mFundType.equals("WITHDRAW")){
            mTvTitle.setText(getResources().getString(R.string.title_withdraw_records));
        }
        mDepositRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDepositRecordsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        checkIfLocked(true);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (mDepositWithdrawRecordAdapter.getItemCount() == mTotalItemAmount) {
            refreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            checkIfLocked(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateFullAccount(Event.UpdateFullAccount event){
        if(mAccountObject == null){
            mAccountObject = event.getFullAccount().account;
            checkIfLocked(true);
        }
    }

    private void checkIfLocked(boolean isRefresh) {
        if(mAccountObject == null){
            return;
        }
        mIsRefresh = isRefresh;
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mAccountName, new UnlockDialog.UnLockDialogClickListener(){

                @Override
                public void onUnLocked(String password) {
                    loadAddress();
                }
            });
        } else {
            loadAddress();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        /**
         * fix bug:CYM-586
         * 退出页面取消网络请求
         */
        if(mRequestRecordsDisposable != null && !mRequestRecordsDisposable.isDisposed()){
            mRequestRecordsDisposable.dispose();
        }
        if(mLoadAddressDisposable != null && !mLoadAddressDisposable.isDisposed()){
            mLoadAddressDisposable.dispose();
        }
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 5);
        return calendar.getTime();
    }

    private void loadAddress() {
        if (TextUtils.isEmpty(mAccountName)) {
            return;
        }
        mLoadAddressDisposable = DBManager.getDbProvider(this).getAddress(mAccountName, mAssetObject.id.toString(), Address.TYPE_WITHDRAW)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Address>>() {
                    @Override
                    public void accept(List<Address> addresses) throws Exception {
                        mAddressList.addAll(addresses);
                        createWithdrawDepositSignature();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    /**
     * 创建signature -> 网管login -> 获取充值提现记录
     */
    private void createWithdrawDepositSignature(){
        mRequestRecordsDisposable = Observable.create(new ObservableOnSubscribe<Operations.withdraw_deposit_history_operation>() {
            @Override
            public void subscribe(ObservableEmitter<Operations.withdraw_deposit_history_operation> emitter) throws Exception {
                Date expiration = getExpiration();
                Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(mAccountName, 0, 0, null, null, expiration);
                mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
                if(!emitter.isDisposed()){
                    emitter.onNext(operation);
                    emitter.onComplete();
                }
            }
        })
        .concatMap(new Function<Operations.withdraw_deposit_history_operation, ObservableSource<ResponseBody>>() {
            @Override
            public ObservableSource<ResponseBody> apply(Operations.withdraw_deposit_history_operation operation) throws Exception {
                GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, mSignature);
                Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
                return RetrofitFactory.getInstance()
                        .api()
                        .gatewayLogIn(RetrofitFactory.url_deposit_withdraw_log_in,
                                RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));
            }
        })
        .concatMap(new Function<ResponseBody, ObservableSource<GateWayRecordsResponse>>() {
            @Override
            public ObservableSource<GateWayRecordsResponse> apply(ResponseBody responseBody) throws Exception {
                JSONObject jsonObject = new JSONObject(responseBody.string());
                if (jsonObject.getInt("code") != 200) {
                    return Observable.error(new Exception(jsonObject.getString("error")));
                }
                Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(
                        mAccountName,
                        mIsRefresh ? 0 : mRecordsItems.size(),
                        mIsRefresh && mRecordsItems.size() > LOAD_COUNT ? mRecordsItems.size() : LOAD_COUNT,
                        mFundType,
                        mAssetObject.symbol ,
                        new Date());
                Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                String request = gson.toJson(createLogInRequest(operation, mSignature));
                Log.v("gatewayRequestBody", request);
                return RetrofitFactory.getInstance()
                        .api()
                        .gatewayRecords(RetrofitFactory.url_deposit_withdraw_records,
                                RequestBody.create(MediaType.parse("application/json"), request));
            }
        })
        .map(new Function<GateWayRecordsResponse, List<GatewayDepositWithdrawRecordsItem>>() {
            @Override
            public List<GatewayDepositWithdrawRecordsItem> apply(GateWayRecordsResponse gateWayRecordsResponse) throws Exception {
                Log.e(TAG, String.valueOf(gateWayRecordsResponse.getData().getTotal()));
                mTotalItemAmount = gateWayRecordsResponse.getData().getTotal();
                List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItemList = new ArrayList<>();
                for (Record record : gateWayRecordsResponse.getData().getRecords()) {
                    GatewayDepositWithdrawRecordsItem gatewayDepositWithdrawRecordsItem = new GatewayDepositWithdrawRecordsItem();
                    gatewayDepositWithdrawRecordsItem.setItemAsset(mAssetObject);
                    gatewayDepositWithdrawRecordsItem.setRecord(record);
                    for (Address address : mAddressList) {
                        if (address.getAddress().equals(record.getAddress())) {
                            gatewayDepositWithdrawRecordsItem.setNote(address.getNote());
                            break;
                        }
                    }
                    gatewayDepositWithdrawRecordsItemList.add(gatewayDepositWithdrawRecordsItem);
                }
                return gatewayDepositWithdrawRecordsItemList;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<GatewayDepositWithdrawRecordsItem>>() {
            @Override
            public void accept(List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItems) {
                mRefreshLayout.finishRefresh();
                mRefreshLayout.finishLoadMore();
                if (mIsRefresh) {
                    mRecordsItems.clear();
                    mRecordsItems.addAll(gatewayDepositWithdrawRecordsItems);
                } else {
                    mRecordsItems.addAll(gatewayDepositWithdrawRecordsItems);
                }
                if(mDepositWithdrawRecordAdapter == null){
                    mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(DepositWithdrawRecordsActivity.this, mRecordsItems);
                    mDepositRecordsRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
                } else {
                    mDepositWithdrawRecordAdapter.notifyDataSetChanged();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                mRefreshLayout.finishRefresh();
                mRefreshLayout.finishLoadMore();
            }
        });
    }

    private GatewayLogInRecordRequest createLogInRequest(Operations.withdraw_deposit_history_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }

}
