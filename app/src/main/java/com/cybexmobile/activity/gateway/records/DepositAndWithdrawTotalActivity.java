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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.http.gateway.entity.GatewayNewDepositWithdrawRecordItem;
import com.cybex.basemodule.BitsharesWalletWraper;
import com.cybexmobile.R;
import com.cybexmobile.activity.setting.enotes.SetCloudPasswordActivity;
import com.cybexmobile.adapter.DepositWithdrawRecordAdapter;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;
import com.cybexmobile.injection.base.AppBaseActivity;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybexmobile.activity.gateway.records.DepositAndWithdrawTotalPresenter.LOAD_MORE;
import static com.cybexmobile.activity.gateway.records.DepositAndWithdrawTotalPresenter.LOAD_REFRESH;

public class DepositAndWithdrawTotalActivity extends AppBaseActivity implements DepositAndWithdrawTotalView, OnRefreshListener, OnLoadMoreListener {
    public static String TAG = DepositAndWithdrawTotalActivity.class.getSimpleName();
    private static final int LOAD_COUNT = 20;

    @Inject
    DepositAndWithdrawTotalPresenter<DepositAndWithdrawTotalView> mDepositAndWithdrawTotalPresenter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private AccountObject mAccountObject;
    private List<GatewayNewDepositWithdrawRecordItem> mRecordsItems = new ArrayList<>();
    private DepositWithdrawRecordAdapter mDepositWithdrawRecordAdapter;

    private String mUserName;
    private String mCurrentFundType;
    private String mCurrentCurrency;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.deposit_and_withdraw_records_refresh_layout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.deposit_and_withdraw_records_rv_deposit_records)
    RecyclerView mRecyclerView;
    @BindView(R.id.deposit_and_withdraw_records_currency_spinner)
    MaterialSpinner mCurrencySpinner;
    @BindView(R.id.deposit_and_withdraw_records_types_spinner)
    MaterialSpinner mTypesSpinner;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            FullAccountObject fullAccountObject = mWebSocketService.getFullAccount(mUserName);
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
        setContentView(R.layout.activity_deposit_and_withdraw_total);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        appActivityComponent().inject(this);
        mDepositAndWithdrawTotalPresenter.attachView(this);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        mTypesSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                mRefreshLayout.setNoMoreData(false);
                showLoadDialog(true);
                if (!item.equals(getResources().getString(R.string.withdraw_all))) {
                    mCurrentFundType = mapFundTypes(item).toUpperCase();
                } else {
                    mCurrentFundType = null;
                }
                view.setTextColor(getResources().getColor(R.color.btn_orange_end));
                view.setArrowColor(getResources().getColor(R.color.btn_orange_end));

                if (mAccountObject == null) {
                    return;
                }

                if (BitsharesWalletWraper.getInstance().is_locked()) {
                    CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                        @Override
                        public void onUnLocked(String password) {
                            mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_REFRESH, DepositAndWithdrawTotalActivity.this, mWebSocketService,
                                    mAccountObject, mUserName, LOAD_COUNT, null, mCurrentCurrency, mCurrentFundType, false, false);

                        }
                    });
                } else {
                    mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_REFRESH, DepositAndWithdrawTotalActivity.this, mWebSocketService,
                            mAccountObject, mUserName, LOAD_COUNT, null, mCurrentCurrency, mCurrentFundType, false, false);

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        mDepositAndWithdrawTotalPresenter.detachView();
        unbindService(mConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_UPDATE_ACCOUNT && resultCode == Constant.RESULT_CODE_UPDATE_ACCOUNT) {
            mAccountObject = mWebSocketService.getFullAccount(mUserName).account;
            mRefreshLayout.autoRefresh();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onLoadAsset(List<String> assetList) {
        String[] Types = {getResources().getString(R.string.withdraw_all), getResources().getString(R.string.gate_way_withdraw), getResources().getString(R.string.gate_way_deposit)};
        mTypesSpinner.setItems(Types);
        mCurrencySpinner.setItems(assetList);
        mCurrencySpinner.setDropdownMaxHeight(getResources().getDimensionPixelSize(R.dimen.height_200));
        mCurrencySpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                mRefreshLayout.setNoMoreData(false);
                showLoadDialog(true);
                view.setTextColor(getResources().getColor(R.color.btn_orange_end));
                view.setArrowColor(getResources().getColor(R.color.btn_orange_end));
                if (!item.equals(getResources().getString(R.string.withdraw_all))) {
                    mCurrentCurrency = "JADE." + item;
                } else {
                    mCurrentCurrency = null;
                }

                if (mAccountObject == null) {
                    return;
                }

                if (BitsharesWalletWraper.getInstance().is_locked()) {
                    CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                        @Override
                        public void onUnLocked(String password) {
                            mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_REFRESH, DepositAndWithdrawTotalActivity.this, mWebSocketService,
                                    mAccountObject, mUserName, LOAD_COUNT, null, mCurrentCurrency, mCurrentFundType, false, false);

                        }
                    });
                } else {
                    mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_REFRESH, DepositAndWithdrawTotalActivity.this, mWebSocketService,
                            mAccountObject, mUserName, LOAD_COUNT, null, mCurrentCurrency, mCurrentFundType, false, false);

                }
            }
        });
    }

    @Override
    public void onLoadRecordsData(int loadMode, List<GatewayNewDepositWithdrawRecordItem> gatewayDepositWithdrawRecordsItems) {
        hideLoadDialog();
        if (loadMode == LOAD_REFRESH) {
            mRecordsItems = gatewayDepositWithdrawRecordsItems;
            mRefreshLayout.finishRefresh();
        } else {
            mRecordsItems.addAll(gatewayDepositWithdrawRecordsItems);
            mRefreshLayout.finishLoadMore();
        }

        if (mDepositWithdrawRecordAdapter == null) {
            mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(this, mRecordsItems);
            mRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
        } else {
            mDepositWithdrawRecordAdapter.setData(mRecordsItems);
        }

    }

    @Override
    public void onError() {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        if (mAccountObject == null) {
            return;
        }

        if (isLoginFromENotes() && mAccountObject.active.key_auths.size() < 2) {
            CybexDialog.showLimitOrderCancelConfirmationDialog(
                    DepositAndWithdrawTotalActivity.this,
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_content),
                    getResources().getString(R.string.nfc_dialog_add_cloud_password_button),
                    new CybexDialog.ConfirmationDialogClickListener() {
                        @Override
                        public void onClick(Dialog dialog) {
                            mRefreshLayout.finishRefresh();
                            Intent intent = new Intent(DepositAndWithdrawTotalActivity.this, SetCloudPasswordActivity.class);
                            startActivityForResult(intent, Constant.REQUEST_CODE_UPDATE_ACCOUNT);
                        }
                    });
        } else {
            if (BitsharesWalletWraper.getInstance().is_locked()) {
                CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                    @Override
                    public void onUnLocked(String password) {
                        refreshRecords();
                    }
                });
            } else {
                refreshRecords();
            }
        }
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        if (mAccountObject == null) {
            return;
        }

        if (BitsharesWalletWraper.getInstance().is_locked()) {
            CybexDialog.showUnlockWalletDialog(getSupportFragmentManager(), mAccountObject, mUserName, new UnlockDialog.UnLockDialogClickListener() {
                @Override
                public void onUnLocked(String password) {
                    loadMoreData();
                }
            });
        } else {
            loadMoreData();
        }
    }

    private void refreshRecords() {
        if (TextUtils.isEmpty(mUserName)) {
            mRefreshLayout.finishRefresh();
            mDepositWithdrawRecordAdapter = new DepositWithdrawRecordAdapter(this, mRecordsItems);
            mRecyclerView.setAdapter(mDepositWithdrawRecordAdapter);
            return;
        }
        int size = mRecordsItems.size() > LOAD_COUNT ? mRecordsItems.size() : LOAD_COUNT;
        mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_REFRESH, this, mWebSocketService, mAccountObject, mUserName,
                size, null, mCurrentCurrency, mCurrentFundType, false, false);
    }

    private void loadMoreData() {
        if (TextUtils.isEmpty(mUserName)) {
            mRefreshLayout.finishLoadMore();
            return;
        }

        if (mRecordsItems == null || mRecordsItems.size() == 0 || mRecordsItems.size() % LOAD_COUNT != 0) {
            mRefreshLayout.finishLoadMore();
            mRefreshLayout.setNoMoreData(true);
            return;
        }

        mDepositAndWithdrawTotalPresenter.loadRecords(LOAD_MORE, this, mWebSocketService, mAccountObject, mUserName,
                LOAD_COUNT, mRecordsItems.get(mRecordsItems.size() - 1).getRecord().getId(), mCurrentCurrency, mCurrentFundType, false, false);

    }

    private String mapFundTypes(String fundType) {
        switch (fundType) {
            case "全部":
                return "All";
            case "充值":
                return "DEPOSIT";
            case "提现":
                return "WITHDRAW";
            default:
                return fundType;
        }
    }


}
