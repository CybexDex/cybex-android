package com.cybexmobile.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.constant.Constant;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.graphene.chain.AccountBalanceObject;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.http.gateway.entity.Data;
import com.cybex.provider.http.gateway.entity.GatewayNewAssetListResponse;
import com.cybex.provider.utils.MyUtils;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.GatewayActivity;
import com.cybexmobile.activity.gateway.withdraw.WithdrawActivity;
import com.cybexmobile.adapter.DepositAndWithdrawAdapter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybex.basemodule.service.WebSocketService;
import com.cybexmobile.utils.GatewayUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.cybexmobile.utils.GatewayUtils.createLogInRequest;

public class WithdrawItemFragment extends BaseFragment implements DepositAndWithdrawAdapter.OnItemClickListener {
    private static final String TAG = WithdrawItemFragment.class.getName();
    private static String ARGS_ACCOUNT_BALANCE = "args_account_balance";

    private List<DepositAndWithdrawObject> mWithdrawObjectList = new ArrayList<>();
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItemList = new ArrayList<>();
    private DepositAndWithdrawAdapter mDepositAndWithdrawAdapter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private String mQuery = "";
    private AccountObject mAccountObject;
    private String mUserName;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private String mSignature;

    @BindView(R.id.withdraw_list)
    RecyclerView mRecyclerView;

    public WithdrawItemFragment() {
        // Required empty public constructor
    }

    public static WithdrawItemFragment newInstance(List<AccountBalanceObjectItem> accountBalanceObjectItemList) {
        WithdrawItemFragment fragment = new WithdrawItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_ACCOUNT_BALANCE, (Serializable) accountBalanceObjectItemList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccountBalanceObjectItemList = (List<AccountBalanceObjectItem>) getArguments().getSerializable(ARGS_ACCOUNT_BALANCE);
        }
        mUserName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constant.PREF_NAME, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_withdraw_item, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        Context context = view.getContext();
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mDepositAndWithdrawAdapter = new DepositAndWithdrawAdapter(context, TAG, mWithdrawObjectList);
        mDepositAndWithdrawAdapter.setOnItemClickListener(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.deposit_withdraw_divider));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mDepositAndWithdrawAdapter);
        mRecyclerView.addItemDecoration(itemDecoration);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishLoadAssetObjects(Event.LoadAssets event) {
        if (event.getData() != null && event.getData().size() > 0) {
//            requestWithdrawList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHideZeroCheckBoxChecked(Event.onHideZeroBalanceAssetCheckBox event) {
        if (mWithdrawObjectList != null && mWithdrawObjectList.size() > 0) {
            if (event.isChecked()) {
                List<DepositAndWithdrawObject> listAfterCheck = new ArrayList<>();
                for (DepositAndWithdrawObject depositAndWithdrawObject : mWithdrawObjectList) {
                    if (depositAndWithdrawObject.getAccountBalanceObject() != null) {
                        listAfterCheck.add(depositAndWithdrawObject);
                    }
                }
                mDepositAndWithdrawAdapter.setDepositAndWithdrawItems(listAfterCheck);
            } else {
                mDepositAndWithdrawAdapter.setDepositAndWithdrawItems(mWithdrawObjectList);
            }
            mDepositAndWithdrawAdapter.getFilter().filter(mQuery);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearch(Event.onSearchBalanceAsset event) {
        mQuery = event.getQuery();
        mDepositAndWithdrawAdapter.getFilter().filter(event.getQuery());
    }

    //    private void requestWithdrawList() {
//        mCompositeDisposable.add(
//                Observable.create((ObservableOnSubscribe<Operations.gateway_login_operation>) e -> {
//                    Date expiration = GatewayUtils.getExpiration();
//                    Operations.gateway_login_operation operation = BitsharesWalletWraper.getInstance().getGatewayLoginOperation(mUserName, expiration);
//                    mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(mAccountObject, operation);
//                    if (!e.isDisposed()) {
//                        e.onNext(operation);
//                        e.onComplete();
//                    }
//                })
//                        .concatMap((Function<Operations.gateway_login_operation, ObservableSource<ResponseBody>>) operation -> {
//                            GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, mSignature);
//                            Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
//                            Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
//                            return RetrofitFactory.getInstance()
//                                    .apiGateway()
//                                    .gatewayLogIn(RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));
//
//                        })
//                        .concatMap((Function<ResponseBody, ObservableSource<GatewayNewAssetListResponse>>) responseBody -> {
//                            return RetrofitFactory.getInstance()
//                                    .apiGateway()
//                                    .getAssetList(
//                                            "application/json",
//                                            "bearer " + mSignature
//                                    );
//                        })
//                        .map((Function<GatewayNewAssetListResponse, List<DepositAndWithdrawObject>>) gatewayNewAssetListResponse -> {
//                            List<DepositAndWithdrawObject> depositAndWithdrawObjectList = new ArrayList<>();
//                            for (Data data : gatewayNewAssetListResponse.getData()) {
//                                DepositAndWithdrawObject depositAndWithdrawObject = new DepositAndWithdrawObject();
//                                for (int j = 0; j < mAccountBalanceObjectItemList.size(); j++) {
//                                    if (mAccountBalanceObjectItemList.get(j).assetObject.id.toString().equals(data.getCybid())) {
//                                        depositAndWithdrawObject.setAccountBalanceObject(mAccountBalanceObjectItemList.get(j).accountBalanceObject);
//                                        break;
//                                    }
//                                }
//                                depositAndWithdrawObject.setId(data.getCybid());
//                                depositAndWithdrawObject.setEnable(data.getWithdrawSwith());
////                        depositAndWithdrawObject.setEnMsg(jsonObject.getString("enMsg"));
////                        depositAndWithdrawObject.setCnMsg(jsonObject.getString("cnMsg"));
//                                depositAndWithdrawObject.setProjectName(data.getBlockchain().getName());
//                                depositAndWithdrawObject.setAssetObject(mWebSocketService.getAssetObject(data.getCybid()));
//                                depositAndWithdrawObjectList.add(depositAndWithdrawObject);
//                            }
//                            return depositAndWithdrawObjectList;
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(
//                                depositAndWithdrawObjects -> {
//                                    mWithdrawObjectList.clear();
//                                    mWithdrawObjectList.addAll(depositAndWithdrawObjects);
//                                    Collections.sort(mWithdrawObjectList, new Comparator<DepositAndWithdrawObject>() {
//                                        @Override
//                                        public int compare(DepositAndWithdrawObject o1, DepositAndWithdrawObject o2) {
//                                            if (o1.getAccountBalanceObject() == null && o2.getAccountBalanceObject() != null) {
//                                                return 1;
//                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() == null) {
//                                                return -1;
//                                            } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() != null) {
//                                                return o1.getAccountBalanceObject().balance > o2.getAccountBalanceObject().balance ? -1 : 1;
//                                            } else {
//                                                return 0;
//                                            }
//                                        }
//
//                                    });
//                                    mDepositAndWithdrawAdapter.notifyDataSetChanged();
//                                    hideLoadDialog();
//                                },
//                                throwable -> {
//                                    hideLoadDialog();
//                                }
//                        )
//        );
//    }

//    private void requestWithdrawList() {
//        RetrofitFactory.getInstance()
//                .api()
//                .getWithdrawList()
//                .retry()
//                .map(new Function<ResponseBody, List<DepositAndWithdrawObject>>() {
//                    @Override
//                    public List<DepositAndWithdrawObject> apply(ResponseBody responseBody) throws Exception {
//                        List<DepositAndWithdrawObject> depositAndWithdrawObjectList = new ArrayList<>();
//                        JSONArray jsonArray = null;
//                        jsonArray = new JSONArray(responseBody.string());
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            DepositAndWithdrawObject depositAndWithdrawObject = new DepositAndWithdrawObject();
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            for (int j = 0; j < mAccountBalanceObjectItemList.size(); j++) {
//                                if (mAccountBalanceObjectItemList.get(j).assetObject.id.toString().equals(jsonObject.getString("id"))) {
//                                    depositAndWithdrawObject.setAccountBalanceObject(mAccountBalanceObjectItemList.get(j).accountBalanceObject);
//                                    break;
//                                }
//                            }
//                            depositAndWithdrawObject.setId(jsonObject.getString("id"));
//                            depositAndWithdrawObject.setEnable(jsonObject.getBoolean("enable"));
//                            depositAndWithdrawObject.setEnMsg(jsonObject.getString("enMsg"));
//                            depositAndWithdrawObject.setCnMsg(jsonObject.getString("cnMsg"));
//                            depositAndWithdrawObject.setProjectName(jsonObject.getString("projectName"));
//                            depositAndWithdrawObject.setTag(jsonObject.getBoolean("tag"));
//                            depositAndWithdrawObject.setAssetObject(mWebSocketService.getAssetObject(jsonObject.getString("id")));
//                            depositAndWithdrawObjectList.add(depositAndWithdrawObject);
//
//                        }
//                        return depositAndWithdrawObjectList;
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<List<DepositAndWithdrawObject>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(List<DepositAndWithdrawObject> depositAndWithdrawObjects) {
//                        mWithdrawObjectList.clear();
//                        mWithdrawObjectList.addAll(depositAndWithdrawObjects);
//                        Collections.sort(mWithdrawObjectList, new Comparator<DepositAndWithdrawObject>() {
//                            @Override
//                            public int compare(DepositAndWithdrawObject o1, DepositAndWithdrawObject o2) {
//                                if (o1.getAccountBalanceObject() == null && o2.getAccountBalanceObject() != null) {
//                                    return 1;
//                                } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() == null) {
//                                    return -1;
//                                } else if (o1.getAccountBalanceObject() != null && o2.getAccountBalanceObject() != null) {
//                                    return o1.getAccountBalanceObject().balance > o2.getAccountBalanceObject().balance ? -1 : 1;
//                                } else {
//                                    return 0;
//                                }
//                            }
//
//                        });
//                        mDepositAndWithdrawAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        hideLoadDialog();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        hideLoadDialog();
//                    }
//                });
//    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            mAccountObject = mWebSocketService.getFullAccount(mUserName).account;
//            if (mWithdrawObjectList.size() == 0) {
//                if (mWebSocketService != null) {
//                    showLoadDialog(true);
//                    if (mWebSocketService.getAssetObjectsList() != null && mWebSocketService.getAssetObjectsList().size() > 0) {
////                        requestWithdrawList();
//                    }
//                }
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWebSocketService = null;
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getContext().unbindService(mConnection);
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }

    @Override
    public void onItemClick(DepositAndWithdrawObject depositAndWithdrawObject) {
        if (depositAndWithdrawObject.isEnable()) {
            Intent intent = new Intent(getContext(), WithdrawActivity.class);
            intent.putExtra("assetName", AssetUtil.parseSymbol(depositAndWithdrawObject.getAssetObject().symbol));
            intent.putExtra("assetId", depositAndWithdrawObject.getId());
            intent.putExtra("isEnabled", depositAndWithdrawObject.isEnable());
//            intent.putExtra("enMsg", depositAndWithdrawObject.getEnMsg());
//            intent.putExtra("cnMsg", depositAndWithdrawObject.getCnMsg());
            intent.putExtra("tag", depositAndWithdrawObject.isTag());
            intent.putExtra("assetObject", depositAndWithdrawObject.getAssetObject());
            AccountBalanceObject accountBalanceObject = depositAndWithdrawObject.getAccountBalanceObject();
            if (accountBalanceObject != null) {
                intent.putExtra("availableAmount", accountBalanceObject.balance / Math.pow(10, depositAndWithdrawObject.getAssetObject().precision));
            }
            getContext().startActivity(intent);
        } else {
            if (!depositAndWithdrawObject.getCnMsg().equals("") && !depositAndWithdrawObject.getEnMsg().equals("")) {
                if (Locale.getDefault().getLanguage().equals("zh")) {
                    ToastMessage.showDepositWithdrawToastMessage(getActivity(), depositAndWithdrawObject.getCnMsg());
                } else {
                    ToastMessage.showDepositWithdrawToastMessage(getActivity(), depositAndWithdrawObject.getEnMsg());
                }
            }
        }
    }

    public void notifyListDataSetChange(List<DepositAndWithdrawObject> assetList) {
        mWithdrawObjectList.clear();
        mWithdrawObjectList.addAll(assetList);
        mDepositAndWithdrawAdapter.notifyDataSetChanged();
    }
}
