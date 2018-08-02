package com.cybexmobile.activity;

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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.cybexmobile.R;
import com.cybexmobile.adapter.DepositWithdrawRecordAdapter;
import com.cybexmobile.api.BitsharesWalletWraper;
import com.cybexmobile.api.RetrofitFactory;
import com.cybexmobile.api.WebSocketClient;
import com.cybexmobile.base.BaseActivity;
import com.cybexmobile.data.GateWayRecordsResponse;
import com.cybexmobile.data.GatewayDepositWithdrawRecordsItem;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.data.Record;
import com.cybexmobile.dialog.CybexDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.AccountObject;
import com.cybexmobile.graphene.chain.AssetObject;
import com.cybexmobile.graphene.chain.FullAccountObject;
import com.cybexmobile.graphene.chain.GlobalConfigObject;
import com.cybexmobile.graphene.chain.Operations;
import com.cybexmobile.service.WebSocketService;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybexmobile.utils.Constant.PREF_NAME;

public class DepositWithdrawRecordsActivity extends BaseActivity implements OnRefreshListener, OnLoadMoreListener {
    public static final String TAG = DepositWithdrawRecordsActivity.class.getName();

    private String mAccountName;
    private String mFundType;
    private int mTotalItemAmount = 0;
    private int mOffset = 0;
    private boolean mIsRefresh;

    private Unbinder mUnbinder;
    private AccountObject mAccountObject;
    private AssetObject mAssetObject;
    private WebSocketService mWebSocketService;
    private DepositWithdrawRecordAdapter mDepositWithdrawRecordAdapter;
    private List<GatewayDepositWithdrawRecordsItem> mGatewayDepositWithdrawRecordsItemList = new ArrayList<>();

    @BindView(R.id.deposit_records_rv_deposit_records)
    RecyclerView mDepositRecordsRecyclerView;
    @BindView(R.id.deposit_records_refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
        mDepositRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDepositRecordsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().post(new Runnable() {
            @Override
            public void run() {
                checkIfLocked(true);
                refreshLayout.finishRefresh();
                refreshLayout.setNoMoreData(true);
            }
        });
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        refreshLayout.getLayout().post(new Runnable() {
            @Override
            public void run() {
                if (mDepositWithdrawRecordAdapter.getItemCount() == mTotalItemAmount) {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                } else {
                    checkIfLocked(false);
                    refreshLayout.finishLoadMore();
                }

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(Event.UpdateFullAccount event){
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
        mOffset = mTotalItemAmount - 20 >= 0 ? mTotalItemAmount - 20 : 0;
        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(this, mAccountObject, mAccountName, new CybexDialog.UnLockDialogClickListener(){

                @Override
                public void onUnLocked(String password) {
                    Date expiration = getExpiration();
                    Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(mAccountName, 0, 0, null, null, expiration);
                    String signature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
                    requestLogIn(operation, signature);
                }
            });
        } else {
            //mRefreshLayout.autoRefresh();
            Date expiration = getExpiration();
            Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(mAccountName, 0, 0, null, null, expiration);
            String signature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
            requestLogIn(operation, signature);
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

    private void requestLogIn(Operations.withdraw_deposit_history_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, signature);
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        processLoginCall(gson.toJson(gatewayLogInRecordRequest), signature);
        Log.e("loginrequest", gson.toJson(gatewayLogInRecordRequest));
    }

    private GatewayLogInRecordRequest createLogInRequest(Operations.withdraw_deposit_history_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }

    private void processLoginCall(String request, String signature) {
        RetrofitFactory.getInstance()
                .api()
                .gatewayLogIn(RetrofitFactory.url_deposit_withdraw_log_in, RequestBody.create(MediaType.parse("application/json"), request))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.v(TAG, "onNext");
                        JSONObject jsonObject;
                        try {
                            String responseMessage = responseBody.string();
                            jsonObject = new JSONObject(responseMessage);
                            if (jsonObject.getInt("code") == 200) {
                                requestRecords(signature);
                            } else {

                            }
                            Log.e("depositLoginMessage", responseMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v(TAG, "onError");
                        Log.e(TAG, "onError");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void requestRecords(String signature) {
        Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(mAccountName, mOffset, 20, mFundType, mAssetObject.symbol , new Date());
        Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
        String request = gson.toJson(createLogInRequest(operation, signature));
        RetrofitFactory.getInstance()
                .api()
                .gatewayRecords(RetrofitFactory.url_deposit_withdraw_records, RequestBody.create(MediaType.parse("application/json"), request))
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
                            gatewayDepositWithdrawRecordsItemList.add(gatewayDepositWithdrawRecordsItem);
                        }
                        return gatewayDepositWithdrawRecordsItemList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GatewayDepositWithdrawRecordsItem>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItemList) {
                        Log.e(TAG, "onNext");
                        hideLoadDialog();
                        if (mIsRefresh) {
                            mGatewayDepositWithdrawRecordsItemList.clear();
                            mGatewayDepositWithdrawRecordsItemList.addAll(gatewayDepositWithdrawRecordsItemList);
                        } else {
                            mGatewayDepositWithdrawRecordsItemList.addAll(gatewayDepositWithdrawRecordsItemList);
                        }
                        if(mDepositWithdrawRecordAdapter == null){
                            mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(DepositWithdrawRecordsActivity.this, mGatewayDepositWithdrawRecordsItemList);
                            mDepositRecordsRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
                        } else {
                            mDepositWithdrawRecordAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
