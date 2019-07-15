package com.cybexmobile.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybex.basemodule.base.BaseFragment;
import com.cybex.basemodule.event.Event;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.toastmessage.ToastMessage;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.SettingConfig;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.R;
import com.cybexmobile.activity.gateway.deposit.DepositActivity;
import com.cybexmobile.adapter.DepositAndWithdrawAdapter;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.faucet.DepositAndWithdrawObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DepositItemFragment extends BaseFragment implements DepositAndWithdrawAdapter.OnItemClickListener {
    private static String ARGS_ACCOUNT_BALANCE = "args_account_balance";

    private String TAG = DepositItemFragment.class.getName();
    private List<DepositAndWithdrawObject> mDepositObjectList = new ArrayList<>();
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItemList = new ArrayList<>();
    private DepositAndWithdrawAdapter mDepositAndWithdrawAdapter;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private String mQuery = "";
    private CompositeDisposable mCompositDisposable = new CompositeDisposable();

    @BindView(R.id.deposit_list)
    RecyclerView mRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DepositItemFragment() {
    }

    public static DepositItemFragment newInstance(List<AccountBalanceObjectItem> accountBalanceObjectItemList) {
        DepositItemFragment fragment = new DepositItemFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deposititem_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        Intent intent = new Intent(getContext(), WebSocketService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        // Set the adapter
        Context context = getContext();
        mDepositAndWithdrawAdapter = new DepositAndWithdrawAdapter(context, TAG, mDepositObjectList);
        mDepositAndWithdrawAdapter.setOnItemClickListener(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.deposit_withdraw_divider));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mDepositAndWithdrawAdapter);
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishLoadAssetObjects(Event.LoadAssets event) {
        if (event.getData() != null && event.getData().size() > 0) {
            if (!SettingConfig.getInstance().isGateway2()) {
                requestDepositList();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHideZeroCheckBoxChecked(Event.onHideZeroBalanceAssetCheckBox event) {
        if (mDepositObjectList != null && mDepositObjectList.size() > 0) {
            if (event.isChecked()) {
                List<DepositAndWithdrawObject> listAfterCheck = new ArrayList<>();
                for (DepositAndWithdrawObject depositAndWithdrawObject : mDepositObjectList) {
                    if (depositAndWithdrawObject.getAccountBalanceObject() != null) {
                        listAfterCheck.add(depositAndWithdrawObject);
                    }
                }
                mDepositAndWithdrawAdapter.setDepositAndWithdrawItems(listAfterCheck);
            } else {
                mDepositAndWithdrawAdapter.setDepositAndWithdrawItems(mDepositObjectList);
            }
            mDepositAndWithdrawAdapter.getFilter().filter(mQuery);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearch(Event.onSearchBalanceAsset event) {
        mQuery = event.getQuery();
        mDepositAndWithdrawAdapter.getFilter().filter(event.getQuery());
    }

    private void requestDepositList() {
        RetrofitFactory.getInstance()
                .api()
                .getDepositList()
                .retry()
                .map(new Function<ResponseBody, List<DepositAndWithdrawObject>>() {
                    @Override
                    public List<DepositAndWithdrawObject> apply(ResponseBody responseBody) throws Exception {
                        List<DepositAndWithdrawObject> depositAndWithdrawObjectList = new ArrayList<>();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseBody.string());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            DepositAndWithdrawObject depositAndWithdrawObject = new DepositAndWithdrawObject();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            for (int j = 0; j < mAccountBalanceObjectItemList.size(); j++) {
                                if (mAccountBalanceObjectItemList.get(j).assetObject.id.toString().equals(jsonObject.getString("id"))) {
                                    depositAndWithdrawObject.setAccountBalanceObject(mAccountBalanceObjectItemList.get(j).accountBalanceObject);
                                    break;
                                }
                            }
                            depositAndWithdrawObject.setId(jsonObject.getString("id"));
                            depositAndWithdrawObject.setEnable(jsonObject.getBoolean("enable"));
                            depositAndWithdrawObject.setEnMsg(jsonObject.getString("enMsg"));
                            depositAndWithdrawObject.setCnMsg(jsonObject.getString("cnMsg"));
                            depositAndWithdrawObject.setProjectName(jsonObject.getString("projectName"));
                            depositAndWithdrawObject.setTag(jsonObject.getBoolean("tag"));
                            depositAndWithdrawObject.setAssetObject(mWebSocketService.getAssetObject(jsonObject.getString("id")));
                            depositAndWithdrawObjectList.add(depositAndWithdrawObject);

                        }
                        return depositAndWithdrawObjectList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DepositAndWithdrawObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<DepositAndWithdrawObject> depositAndWithdrawObjects) {
                        mDepositObjectList.clear();
                        mDepositObjectList.addAll(depositAndWithdrawObjects);
                        Collections.sort(mDepositObjectList, new Comparator<DepositAndWithdrawObject>() {
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
                        mDepositAndWithdrawAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadDialog();
                    }

                    @Override
                    public void onComplete() {
                        hideLoadDialog();
                    }
                });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();

            if (!SettingConfig.getInstance().isGateway2()) {
                if (mDepositObjectList.size() == 0) {
                    showLoadDialog(true);
                    if (mWebSocketService != null) {
                        if (mWebSocketService.getAssetObjectsList() != null && mWebSocketService.getAssetObjectsList().size() > 0) {
                            requestDepositList();
                        }
                    }
                }
            }
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
        Objects.requireNonNull(getContext()).unbindService(mConnection);
        if (!mCompositDisposable.isDisposed()) {
            mCompositDisposable.dispose();
        }
    }

    @Override
    public void onItemClick(DepositAndWithdrawObject depositAndWithdrawObject) {
        if (depositAndWithdrawObject.isEnable()) {
            Intent intent = new Intent(getContext(), DepositActivity.class);
            intent.putExtra("assetNameForGatewayRequest", depositAndWithdrawObject.getAssetName());
            intent.putExtra("assetName", AssetUtil.parseSymbol(depositAndWithdrawObject.getAssetObject().symbol));
            intent.putExtra("assetId", depositAndWithdrawObject.getId());
            intent.putExtra("isEnabled", depositAndWithdrawObject.isEnable());
            intent.putExtra("tag", depositAndWithdrawObject.isTag());
            intent.putExtra("assetObject", depositAndWithdrawObject.getAssetObject());
            getContext().startActivity(intent);
        } else {
            if (SettingConfig.getInstance().isGateway2()) {
                if (!depositAndWithdrawObject.getDepositCnMsg().equals("") && !depositAndWithdrawObject.getDepositEnMsg().equals("")) {
                    if (Locale.getDefault().getLanguage().equals("zh")) {
                        ToastMessage.showDepositWithdrawToastMessage(getActivity(), depositAndWithdrawObject.getDepositCnMsg());
                    } else {
                        ToastMessage.showDepositWithdrawToastMessage(getActivity(), depositAndWithdrawObject.getDepositEnMsg());
                    }
                }
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
    }

    public void notifyListDataSetChange(List<DepositAndWithdrawObject> assetList) {
        mDepositObjectList.clear();
        mDepositObjectList.addAll(assetList);
        mDepositAndWithdrawAdapter.notifyDataSetChanged();
    }
    @Override
    public void onNetWorkStateChanged(boolean isAvailable) {

    }
}
