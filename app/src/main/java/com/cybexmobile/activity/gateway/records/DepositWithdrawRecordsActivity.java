package com.cybexmobile.activity.gateway.records;

import android.app.Dialog;
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

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.constant.Constant;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybex.provider.http.entity.BlockerExplorer;
import com.cybex.provider.http.gateway.entity.GatewayNewDepositWithdrawRecordItem;
import com.cybex.provider.http.gateway.entity.GatewayNewRecord;
import com.cybex.provider.http.gateway.entity.GatewayNewRecordsResponse;
import com.cybexmobile.R;
import com.cybexmobile.activity.setting.enotes.SetCloudPasswordActivity;
import com.cybexmobile.adapter.DepositWithdrawRecordAdapter;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.response.GateWayRecordsResponse;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybex.provider.http.entity.Record;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.AssetObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.basemodule.service.WebSocketService;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.basemodule.constant.Constant.REQUEST_CODE_UPDATE_ACCOUNT;

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
    private List<GatewayNewDepositWithdrawRecordItem> mRecordsItems = new ArrayList<>();
    private List<Address> mAddressList = new ArrayList<>();
    private List<BlockerExplorer> mBlockerExplorerList = new ArrayList<>();

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

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private String mSignature;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mAccountName);
            if (fullAccountObject != null) {
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
        if (mFundType.equals("DEPOSIT")) {
            mTvTitle.setText(getResources().getString(R.string.title_deposit_records));
        } else if (mFundType.equals("WITHDRAW")) {
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
        if (isLoginFromENotes() && mAccountObject.active.key_auths.size() < 2) {
            CybexDialog.showLimitOrderCancelConfirmationDialog(
                    DepositWithdrawRecordsActivity.this,
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_content),
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_button),
                    new CybexDialog.ConfirmationDialogClickListener() {
                        @Override
                        public void onClick(Dialog dialog) {
                            mRefreshLayout.finishRefresh();
                            Intent intent = new Intent(DepositWithdrawRecordsActivity.this, SetCloudPasswordActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_UPDATE_ACCOUNT);
                        }
                    });
        } else {
            checkIfLocked(true);
        }
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (mDepositWithdrawRecordAdapter.getItemCount() == mTotalItemAmount) {
            refreshLayout.finishLoadMoreWithNoMoreData();
        } else {
            checkIfLocked(false);
        }
    }

    private void checkIfLocked(boolean isRefresh) {
        if (mAccountObject == null) {
            return;
        }
        mIsRefresh = isRefresh;
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mAccountName, new UnlockDialog.UnLockDialogClickListener() {

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
    protected void onResume() {
        super.onResume();
        Log.e("onResumeCalled", "called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        /**
         * fix bug:CYM-586
         * 退出页面取消网络请求
         */
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
        if (mRequestRecordsDisposable != null && !mRequestRecordsDisposable.isDisposed()) {
            mRequestRecordsDisposable.dispose();
        }
        if (mLoadAddressDisposable != null && !mLoadAddressDisposable.isDisposed()) {
            mLoadAddressDisposable.dispose();
        }
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE_ACCOUNT && resultCode == Constant.RESULT_CODE_UPDATE_ACCOUNT) {
            mAccountObject = mWebSocketService.getFullAccount(mAccountName).account;
            mRefreshLayout.autoRefresh();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    private Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 15);
        return calendar.getTime();
    }

    private void loadAddress() {
        if (TextUtils.isEmpty(mAccountName)) {
            return;
        }

        mCompositeDisposable.add(
                Observable.create((ObservableOnSubscribe<Operations.gateway_login_operation>) emitter -> {
                    Date expiration = getExpiration();
                    Operations.gateway_login_operation operation = BitsharesWalletWraper.getInstance().getGatewayLoginOperation(mAccountName, expiration);
                    mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
                    if (!emitter.isDisposed()) {
                        emitter.onNext(operation);
                        emitter.onComplete();
                    }
                })
                .concatMap((Function<Operations.gateway_login_operation, ObservableSource<ResponseBody>>) operation -> {
                    GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, mSignature);
                    Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                    Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
                    return RetrofitFactory.getInstance()
                            .apiGateway()
                            .gatewayLogIn(RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));

                })
                .concatMap((Function<ResponseBody, ObservableSource<GatewayNewRecordsResponse>>) responseBody -> {
                    return RetrofitFactory.getInstance()
                            .apiGateway()
                            .getDepositWithdrawRecordNewGateway(
                                    "application/json",
                                    "bearer " + mSignature,
                                    mAccountName,
                                    mIsRefresh && mRecordsItems.size() > LOAD_COUNT ? mRecordsItems.size() : LOAD_COUNT,
                                    mIsRefresh ? 0 : mRecordsItems.size(),
                                    mAssetObject.symbol,
                                    mFundType);
                })
                .map((Function<GatewayNewRecordsResponse, List<GatewayNewDepositWithdrawRecordItem>>) gatewayRecordsResponse ->  {
                    mTotalItemAmount = gatewayRecordsResponse.getTotal();
                    List<GatewayNewDepositWithdrawRecordItem> gatewayDepositWithdrawRecordsItemList = new ArrayList<>();
                    if (gatewayRecordsResponse.getRecords() != null && gatewayRecordsResponse.getRecords().size() > 0) {
                        for (GatewayNewRecord record : gatewayRecordsResponse.getRecords()) {
                            GatewayNewDepositWithdrawRecordItem gatewayNewDepositWithdrawRecordItem = new GatewayNewDepositWithdrawRecordItem();
                            gatewayNewDepositWithdrawRecordItem.setItemAsset(mAssetObject);
                            gatewayNewDepositWithdrawRecordItem.setRecord(record);

                            for (Address address : mAddressList) {
                                if (address.getAddress().equals(record.getOutAddr())) {
                                    gatewayNewDepositWithdrawRecordItem.setNote(address.getNote());
                                    break;
                                }
                            }
                            gatewayDepositWithdrawRecordsItemList.add(gatewayNewDepositWithdrawRecordItem);
                        }
                    }
                    return gatewayDepositWithdrawRecordsItemList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        gatewayNewDepositWithdrawRecordItems -> {
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadMore();
                            if (mIsRefresh) {
                                mRecordsItems.clear();
                                mRecordsItems.addAll(gatewayNewDepositWithdrawRecordItems);
                            } else {
                                mRecordsItems.addAll(gatewayNewDepositWithdrawRecordItems);
                            }
                            if (mDepositWithdrawRecordAdapter == null) {
                            mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(DepositWithdrawRecordsActivity.this, mRecordsItems);
                            mDepositRecordsRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
                            } else {
                                mDepositWithdrawRecordAdapter.notifyDataSetChanged();
                            }
                        },
                        throwable -> {
                            mRefreshLayout.finishRefresh();
                            mRefreshLayout.finishLoadMore();
                        }
                )
        );

//        mLoadAddressDisposable =
//                RetrofitFactory.getInstance()
//                        .api()
//                        .getBlockExplorerLink()
//                        .concatMap(new Function<ResponseBody, ObservableSource<List<Address>>>() {
//                            @Override
//                            public ObservableSource<List<Address>> apply(ResponseBody responseBody) throws Exception {
//                                mBlockerExplorerList.clear();
//                                JSONArray jsonArray = new JSONArray(responseBody.string());
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                    BlockerExplorer blockerExplorer = new BlockerExplorer();
//                                    blockerExplorer.setAsset(jsonObject.getString("asset"));
//                                    blockerExplorer.setExpolorerLink(jsonObject.getString("explorer"));
//                                    mBlockerExplorerList.add(blockerExplorer);
//                                }
//                                return DBManager.getDbProvider(DepositWithdrawRecordsActivity.this)
//                                        .getAddress(mAccountName, mAssetObject.id.toString(), Address.TYPE_WITHDRAW);
//                            }
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<List<Address>>() {
//                            @Override
//                            public void accept(List<Address> addresses) throws Exception {
//                                mAddressList.addAll(addresses);
//                                createWithdrawDepositSignature();
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//
//                            }
//                        });
    }

    /**
     * 创建signature -> 网管login -> 获取充值提现记录
     */
//    private void createWithdrawDepositSignature() {
//        mRequestRecordsDisposable = Observable.create(new ObservableOnSubscribe<Operations.withdraw_deposit_history_operation>() {
//            @Override
//            public void subscribe(ObservableEmitter<Operations.withdraw_deposit_history_operation> emitter) throws Exception {
//                Date expiration = getExpiration();
//                Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(mAccountName, 0, 0, null, null, expiration);
//                mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
//                if (!emitter.isDisposed()) {
//                    emitter.onNext(operation);
//                    emitter.onComplete();
//                }
//            }
//        })
//                .concatMap(new Function<Operations.withdraw_deposit_history_operation, ObservableSource<ResponseBody>>() {
//                    @Override
//                    public ObservableSource<ResponseBody> apply(Operations.withdraw_deposit_history_operation operation) throws Exception {
//                        GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, mSignature);
//                        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
//                        Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
//                        return RetrofitFactory.getInstance()
//                                .apiGateway()
//                                .gatewayLogIn(RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));
//                    }
//                })
//                .concatMap(new Function<ResponseBody, ObservableSource<GateWayRecordsResponse>>() {
//                    @Override
//                    public ObservableSource<GateWayRecordsResponse> apply(ResponseBody responseBody) throws Exception {
//                        JSONObject jsonObject = new JSONObject(responseBody.string());
//                        if (jsonObject.getInt("code") != 200) {
//                            return Observable.error(new Exception(jsonObject.getString("error")));
//                        }
////                        Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(
////                                mAccountName,
////                                mIsRefresh ? 0 : mRecordsItems.size(),
////                                mIsRefresh && mRecordsItems.size() > LOAD_COUNT ? mRecordsItems.size() : LOAD_COUNT,
////                                mFundType,
////                                mAssetObject.symbol,
////                                new Date());
////                        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
////                        String request = gson.toJson(createLogInRequest(operation, mSignature));
////                        Log.v("gatewayRequestBody", request);
//                        return RetrofitFactory.getInstance()
//                                .apiGateway()
//                                .getDepositWithdrawRecords(
//                                        "application/json",
//                                        "bearer " + mSignature,
//                                        mAccountName,
//                                        mIsRefresh && mRecordsItems.size() > LOAD_COUNT ? mRecordsItems.size() : LOAD_COUNT,
//                                        mIsRefresh ? 0 : mRecordsItems.size(),
//                                        mAssetObject.symbol,
//                                        mFundType,
//                                        false,
//                                        false
//                                );
//                    }
//                })
//                .map(new Function<GateWayRecordsResponse, List<GatewayDepositWithdrawRecordsItem>>() {
//                    @Override
//                    public List<GatewayDepositWithdrawRecordsItem> apply(GateWayRecordsResponse gateWayRecordsResponse) throws Exception {
//                        Log.e(TAG, String.valueOf(gateWayRecordsResponse.getData().getTotal()));
//                        mTotalItemAmount = gateWayRecordsResponse.getData().getTotal();
//                        List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItemList = new ArrayList<>();
//                        for (Record record : gateWayRecordsResponse.getData().getRecords()) {
//                            GatewayDepositWithdrawRecordsItem gatewayDepositWithdrawRecordsItem = new GatewayDepositWithdrawRecordsItem();
//                            gatewayDepositWithdrawRecordsItem.setItemAsset(mAssetObject);
//                            gatewayDepositWithdrawRecordsItem.setRecord(record);
//                            for (Record.Details details : record.getDetails()) {
//                                if (!TextUtils.isEmpty(details.getHash())) {
//                                    for (BlockerExplorer blockerExplorer : mBlockerExplorerList) {
//                                        if (blockerExplorer.getAsset().equals(record.getCoinType())) {
//                                            gatewayDepositWithdrawRecordsItem.setExplorerLink(blockerExplorer.getExpolorerLink() + details.getHash());
//                                            break;
//                                        }
//                                    }
//                                    if (gatewayDepositWithdrawRecordsItem.getExplorerLink() == null) {
//                                        gatewayDepositWithdrawRecordsItem.setExplorerLink("https://etherscan.io/tx/" + details.getHash());
//                                    }
//                                    break;
//                                }
//                            }
//
//                            if (gatewayDepositWithdrawRecordsItem.getExplorerLink() == null) {
//                                gatewayDepositWithdrawRecordsItem.setExplorerLink("No Link");
//                            }
//
//                            for (Address address : mAddressList) {
//                                if (address.getAddress().equals(record.getAddress())) {
//                                    gatewayDepositWithdrawRecordsItem.setNote(address.getNote());
//                                    break;
//                                }
//                            }
//                            gatewayDepositWithdrawRecordsItemList.add(gatewayDepositWithdrawRecordsItem);
//                        }
//                        return gatewayDepositWithdrawRecordsItemList;
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<GatewayDepositWithdrawRecordsItem>>() {
//                    @Override
//                    public void accept(List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItems) {
//                        mRefreshLayout.finishRefresh();
//                        mRefreshLayout.finishLoadMore();
//                        if (mIsRefresh) {
//                            mRecordsItems.clear();
//                            mRecordsItems.addAll(gatewayDepositWithdrawRecordsItems);
//                        } else {
//                            mRecordsItems.addAll(gatewayDepositWithdrawRecordsItems);
//                        }
//                        if (mDepositWithdrawRecordAdapter == null) {
////                            mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(DepositWithdrawRecordsActivity.this, mRecordsItems);
////                            mDepositRecordsRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
//                        } else {
//                            mDepositWithdrawRecordAdapter.notifyDataSetChanged();
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        mRefreshLayout.finishRefresh();
//                        mRefreshLayout.finishLoadMore();
//                    }
//                });
//    }

    private GatewayLogInRecordRequest createLogInRequest(Operations.base_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }

}
