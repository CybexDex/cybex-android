package com.cybexmobile.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybex.basemodule.base.BaseActivity;
import com.cybex.basemodule.event.Event;
import com.cybexmobile.R;
import com.cybexmobile.adapter.DepositAndWithdrawAdapter;
import com.cybex.provider.http.RetrofitFactory;
import com.cybexmobile.data.item.AccountBalanceObjectItem;
import com.cybexmobile.faucet.DepositAndWithdrawObject;
import com.cybexmobile.fragment.dummy.DummyContent.DummyItem;
import com.cybex.basemodule.service.WebSocketService;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DepositItemFragment extends Fragment {
    private static String ARGS_ACCOUNT_BALANCE = "args_account_balance";

    private OnListFragmentInteractionListener mListener;
    private String TAG = DepositItemFragment.class.getName();
    private List<DepositAndWithdrawObject> mDepositObjectList = new ArrayList<>();
    private List<AccountBalanceObjectItem> mAccountBalanceObjectItemList = new ArrayList<>();
    private DepositAndWithdrawAdapter mDepositAndWithdrawAdapter;
    private BaseActivity mActivity;

    private Unbinder mUnbinder;
    private WebSocketService mWebSocketService;
    private String mQuery = "";

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
        mActivity = (BaseActivity) getActivity();
        // Set the adapter
        Context context = getContext();
        mDepositAndWithdrawAdapter = new DepositAndWithdrawAdapter(mActivity, TAG, mDepositObjectList);
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
            requestDepositList();
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
                            depositAndWithdrawObject.setEnInfo(jsonObject.getString("enInfo"));
                            depositAndWithdrawObject.setCnInfo(jsonObject.getString("cnInfo"));
                            depositAndWithdrawObject.setProjectName(jsonObject.getString("projectName"));
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
                        if (mActivity != null) {
                            mActivity.hideLoadDialog();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mActivity != null) {
                            mActivity.hideLoadDialog();
                        }
                    }
                });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            mWebSocketService = binder.getService();
            if (mDepositObjectList.size() == 0) {
                if (mActivity != null) {
                    mActivity.showLoadDialog();
                }
                if (mWebSocketService != null) {
                    if (mWebSocketService.getAssetObjectsList() != null && mWebSocketService.getAssetObjectsList().size() > 0) {
                        requestDepositList();
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
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
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
        mListener = null;
        getContext().unbindService(mConnection);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
