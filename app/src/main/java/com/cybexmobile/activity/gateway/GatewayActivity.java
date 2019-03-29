package com.cybexmobile.activity.gateway;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.dialog.CybexDialog;
import com.cybex.basemodule.dialog.UnlockDialog;
import com.cybex.basemodule.dialog.UnlockDialogWithEnotes;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.FullAccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.gateway.entity.Data;
import com.cybex.provider.http.gateway.entity.GatewayNewAssetListResponse;
import com.cybex.provider.utils.NetworkUtils;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.records.DepositAndWithdrawTotalActivity;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybexmobile.fragment.DepositItemFragment;
import com.cybexmobile.fragment.WithdrawItemFragment;
import com.cybexmobile.fragment.dummy.DummyContent;
import com.cybexmobile.shake.AntiShake;
import com.cybexmobile.utils.GatewayUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import info.hoang8f.android.segmented.SegmentedGroup;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybex.basemodule.constant.Constant.PREF_NAME;
import static com.cybex.provider.utils.NetworkUtils.TYPE_NOT_CONNECTED;
import static com.cybexmobile.utils.GatewayUtils.createLogInRequest;

public class GatewayActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    public static String INTENT_ACCOUNT_BALANCE_ITEMS = "intent_account_balance_items";
    public static String INTENT_IS_DEPOSIT = "intent_is_deposit";

    @BindView(R.id.gate_way_segmented_group)
    SegmentedGroup mSegmentedGroup;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.gate_way_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.gate_way_segment_deposit)
    RadioButton mDepositButton;
    @BindView(R.id.gate_way_segment_withdraw)
    RadioButton mWithdrawButton;
    @BindView(R.id.gate_way_checkbox)
    CheckBox mCheckBox;
    @BindView(R.id.gate_way_search)
    SearchView mSearchView;

    private String mAccountName;
    private String mSignature;

    private DepositItemFragment mDepositItemFragment;
    private WithdrawItemFragment mWithdrawItemFragment;
    private Unbinder mUnbinder;
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItemList;
    private List<DepositAndWithdrawObject> mDepositAndWithdrawAssetList = new ArrayList<>();
    private WebSocketService mWebSocketService;
    private ScreenSlidePagerAdapter mScreenSlidePagerAdapter;
    private AccountObject mAccountObject;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mAccountObject = mWebSocketService.getFullAccount(mAccountName).account;
            if (mAccountBalanceObjectItemList == null) {
                mAccountBalanceObjectItemList = new ArrayList<>();
                loadData(mWebSocketService.getFullAccount(mAccountName));
            }
            if (mDepositAndWithdrawAssetList.size() == 0) {
                showLoadDialog(true);
                if (mWebSocketService.getAssetObjectsList() != null && mWebSocketService.getAssetObjectsList().size() > 0) {
                    loadList1();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setViews();
        mAccountBalanceObjectItemList = (List<AccountBalanceObjectItem>) getIntent().getSerializableExtra(INTENT_ACCOUNT_BALANCE_ITEMS);
        mAccountName = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME, "");
        Intent intent = new Intent(this, WebSocketService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        mScreenSlidePagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mScreenSlidePagerAdapter);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        if (getIntent().getBooleanExtra(INTENT_IS_DEPOSIT, true)) {
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishLoadAssetObjects(Event.LoadAssets event) {
        if (event.getData() != null && event.getData().size() > 0) {
            loadData(mWebSocketService.getFullAccount(mAccountName));
            loadList1();
        }
    }

    private void setSearchViewStyle() {
        mSearchView.setIconifiedByDefault(false);
        EditText searchEditText = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ImageView closeButton = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.font_large));
        searchEditText.setTextColor(getResources().getColor(R.color.font_color_white_dark));
        searchEditText.setHintTextColor(getResources().getColor(R.color.primary_color_grey));
        searchEditText.setHint(getResources().getString(R.string.gate_way_search));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setFocusable(false);
                mSearchView.setQuery("", false);
            }
        });
    }

    private void setSearchListener() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                EventBus.getDefault().post(new Event.onSearchBalanceAsset(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                EventBus.getDefault().post(new Event.onSearchBalanceAsset(newText));
                return false;
            }
        });
    }

    @OnCheckedChanged(R.id.gate_way_checkbox)
    public void onCheckBoxCheckedChange(CompoundButton button, boolean checked) {
        EventBus.getDefault().post(new Event.onHideZeroBalanceAssetCheckBox(checked));

    }

    private void setViews() {
        mSegmentedGroup.setOnCheckedChangeListener(this);
        setSearchViewStyle();
        setSearchListener();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.gate_way_segment_deposit:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.gate_way_segment_withdraw:
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                default:
                    mDepositButton.setChecked(true);
                    break;
                case 1:
                    mWithdrawButton.setChecked(true);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        if (mWebSocketService != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deposit_withdraw_total_records, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AntiShake.check(item.getItemId())) { return false; }
        switch (item.getItemId()) {
            case R.id.action_records:
                Intent intent = new Intent(this, DepositAndWithdrawTotalActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                    mDepositItemFragment = DepositItemFragment.newInstance(mAccountBalanceObjectItemList);
                    return mDepositItemFragment;
                case 1:
                    mWithdrawItemFragment = WithdrawItemFragment.newInstance(mAccountBalanceObjectItemList);
                    return mWithdrawItemFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void loadData(FullAccountObject fullAccountObject) {
        if (fullAccountObject == null) {
            return;
        }
        if (NetworkUtils.getConnectivityStatus(this) == TYPE_NOT_CONNECTED) {
            return;
        }
        List<AccountBalanceObject> accountBalanceObjects = fullAccountObject.balances;
        if (accountBalanceObjects != null && accountBalanceObjects.size() > 0) {
            for (AccountBalanceObject balance : accountBalanceObjects) {
                if (mWebSocketService.getAssetObject(balance.asset_type.toString()) == null) {
                    continue;
                }
                if (balance.balance == 0) {
                    continue;
                }
                AccountBalanceObjectItem item = new AccountBalanceObjectItem();
                item.accountBalanceObject = balance;
                item.assetObject = mWebSocketService.getAssetObject(balance.asset_type.toString());
                mAccountBalanceObjectItemList.add(item);
            }

        }
        mScreenSlidePagerAdapter.notifyDataSetChanged();
    }

    private void loadList() {
        mCompositeDisposable.add(
                Observable.create((ObservableOnSubscribe<Operations.gateway_login_operation>) e -> {
                    Date expiration = GatewayUtils.getExpiration();
                    Operations.gateway_login_operation operation = BitsharesWalletWraper.getInstance().getGatewayLoginOperation(mAccountName, expiration);
                    mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
                    if (!e.isDisposed()) {
                        e.onNext(operation);
                        e.onComplete();
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
                        .concatMap((Function<ResponseBody, ObservableSource<GatewayNewAssetListResponse>>) responseBody -> {
                            return RetrofitFactory.getInstance()
                                    .apiGateway()
                                    .getAssetList(
                                            "application/json",
                                            "bearer " + mSignature
                                    );
                        })
                        .map((Function<GatewayNewAssetListResponse, List<DepositAndWithdrawObject>>) gatewayNewAssetListResponse -> {
                            List<DepositAndWithdrawObject> depositAndWithdrawObjectList = new ArrayList<>();
                            for (Data data : gatewayNewAssetListResponse.getData()) {
                                DepositAndWithdrawObject depositAndWithdrawObject = new DepositAndWithdrawObject();
                                for (int j = 0; j < mAccountBalanceObjectItemList.size(); j++) {
                                    if (mAccountBalanceObjectItemList.get(j).assetObject.id.toString().equals(data.getCybid())) {
                                        depositAndWithdrawObject.setAccountBalanceObject(mAccountBalanceObjectItemList.get(j).accountBalanceObject);
                                        break;
                                    }
                                }
                                depositAndWithdrawObject.setId(data.getCybid());
                                depositAndWithdrawObject.setEnable(data.getWithdrawSwith());
//                        depositAndWithdrawObject.setEnMsg(jsonObject.getString("enMsg"));
//                        depositAndWithdrawObject.setCnMsg(jsonObject.getString("cnMsg"));
                                depositAndWithdrawObject.setProjectName(data.getBlockchain().getName());
                                depositAndWithdrawObject.setAssetObject(mWebSocketService.getAssetObject(data.getCybid()));
                                depositAndWithdrawObjectList.add(depositAndWithdrawObject);
                            }
                            return depositAndWithdrawObjectList;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                depositAndWithdrawObjects -> {
                                    mDepositAndWithdrawAssetList.clear();
                                    mDepositAndWithdrawAssetList.addAll(depositAndWithdrawObjects);
                                    Collections.sort(mDepositAndWithdrawAssetList, new Comparator<DepositAndWithdrawObject>() {
                                        @Override
                                        public int compare(DepositAndWithdrawObject o1, DepositAndWithdrawObject o2) {
                                            if (o1.getAccountBalanceObject() == null && o2.getAccountBalanceObject() != null) {
                                                return 1;
                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() == null) {
                                                return -1;
                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() != null) {
                                                return o1.getAccountBalanceObject().balance > o2.getAccountBalanceObject().balance ? -1 : 1;
                                            } else {
                                                return 0;
                                            }
                                        }

                                    });
                                    if (mDepositItemFragment != null && mDepositItemFragment.isResumed()) {
                                        mDepositItemFragment.notifyListDataSetChange(mDepositAndWithdrawAssetList);
                                    }
                                    if (mWithdrawItemFragment != null && mWithdrawItemFragment.isResumed()) {
                                        mWithdrawItemFragment.notifyListDataSetChange(mDepositAndWithdrawAssetList);
                                    }
                                    hideLoadDialog();
                                },
                                throwable -> hideLoadDialog()
                        )
        );
    }

    private void loadList1() {
        mCompositeDisposable.add(
                RetrofitFactory.getInstance()
                        .apiGateway()
                        .getAssetList(
                                "application/json",
                                null
                        )
                        .map(gatewayNewAssetListResponse -> {
                            List<DepositAndWithdrawObject> depositAndWithdrawObjectList = new ArrayList<>();
                            for (Data data : gatewayNewAssetListResponse.getData()) {
                                DepositAndWithdrawObject depositAndWithdrawObject = new DepositAndWithdrawObject();
                                for (int j = 0; j < mAccountBalanceObjectItemList.size(); j++) {
                                    if (mAccountBalanceObjectItemList.get(j).assetObject.id.toString().equals(data.getCybid())) {
                                        depositAndWithdrawObject.setAccountBalanceObject(mAccountBalanceObjectItemList.get(j).accountBalanceObject);
                                        break;
                                    }
                                }
                                depositAndWithdrawObject.setId(data.getCybid());
                                depositAndWithdrawObject.setEnable(data.getWithdrawSwith());
                                depositAndWithdrawObject.setGatewayAccount(data.getGatewayAccount());
                                depositAndWithdrawObject.setMinWithdraw(data.getMinWithdraw());
                                depositAndWithdrawObject.setPrecision(data.getPrecision());
                                depositAndWithdrawObject.setWithdrawFee(data.getWithdrawFee());
//                        depositAndWithdrawObject.setEnMsg(jsonObject.getString("enMsg"));
//                        depositAndWithdrawObject.setCnMsg(jsonObject.getString("cnMsg"));
                                depositAndWithdrawObject.setProjectName(data.getBlockchain().getName());
                                depositAndWithdrawObject.setAssetObject(mWebSocketService.getAssetObject(data.getCybid()));
                                depositAndWithdrawObjectList.add(depositAndWithdrawObject);
                            }
                            return depositAndWithdrawObjectList;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                depositAndWithdrawObjects -> {
                                    mDepositAndWithdrawAssetList.clear();
                                    mDepositAndWithdrawAssetList.addAll(depositAndWithdrawObjects);
                                    Collections.sort(mDepositAndWithdrawAssetList, new Comparator<DepositAndWithdrawObject>() {
                                        @Override
                                        public int compare(DepositAndWithdrawObject o1, DepositAndWithdrawObject o2) {
                                            if (o1.getAccountBalanceObject() == null && o2.getAccountBalanceObject() != null) {
                                                return 1;
                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() == null) {
                                                return -1;
                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() != null) {
                                                return o1.getAccountBalanceObject().balance > o2.getAccountBalanceObject().balance ? -1 : 1;
                                            } else {
                                                return 0;
                                            }
                                        }

                                    });
                                    if (mDepositItemFragment != null && mDepositItemFragment.isResumed()) {
                                        mDepositItemFragment.notifyListDataSetChange(mDepositAndWithdrawAssetList);
                                    }
                                    if (mWithdrawItemFragment != null && mWithdrawItemFragment.isResumed()) {
                                        mWithdrawItemFragment.notifyListDataSetChange(mDepositAndWithdrawAssetList);
                                    }
                                    hideLoadDialog();
                                },
                                throwable -> hideLoadDialog()
                        )
        );
    }
}
