package com.cybexmobile.activity.gateway.records;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cybex.basemodule.base.BasePresenter;
import com.cybex.basemodule.service.WebSocketService;
import com.cybex.basemodule.utils.AssetUtil;
import com.cybex.provider.db.DBManager;
import com.cybex.provider.db.entity.Address;
import com.cybex.provider.graphene.chain.AccountObject;
import com.cybex.provider.graphene.chain.GlobalConfigObject;
import com.cybex.provider.graphene.chain.Operations;
import com.cybex.provider.http.RetrofitFactory;
import com.cybex.provider.http.entity.AssetRecord;
import com.cybex.provider.http.entity.BlockerExplorer;
import com.cybex.provider.http.entity.Record;
import com.cybex.provider.http.response.GateWayAssetInRecordsResponse;
import com.cybex.provider.http.response.GateWayRecordsResponse;
import com.cybex.provider.websocket.BitsharesWalletWraper;
import com.cybexmobile.data.GatewayLogInRecordRequest;
import com.cybexmobile.data.item.GatewayDepositWithdrawRecordsItem;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class DepositAndWithdrawTotalPresenter<V extends DepositAndWithdrawTotalView> extends BasePresenter<V> {

    public static final int LOAD_REFRESH = 1;
    public static final int LOAD_MORE = 2;

    private String mSignature;
    private List<Address> mAddressList = new ArrayList<>();
    private List<BlockerExplorer> mBlockerExplorerList = new ArrayList<>();
    private List<String> mAssetList = new ArrayList<>();


    @Inject
    DepositAndWithdrawTotalPresenter() {
    }

    public void loadAssetList(String userName) {
        mCompositeDisposable.add(
                RetrofitFactory.getInstance()
                        .apiGateway()
                        .getDepositWithdrawAsset(
                                "application/json",
                                "bearer " + mSignature,
                                userName,
                                null,
                                null
                        )
                        .map(new Function<GateWayAssetInRecordsResponse, List<String>>() {
                            @Override
                            public List<String> apply(GateWayAssetInRecordsResponse gateWayAssetInRecordsResponse) throws Exception {
                                for (AssetRecord assetRecord : gateWayAssetInRecordsResponse.getData().getRecords()) {
                                    mAssetList.add(AssetUtil.parseSymbol(assetRecord.getGroupInfo().getAsset()));
                                }
                                return mAssetList;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<String>>() {
                            @Override
                            public void accept(List<String> strings) throws Exception {
                                strings.add(0, "ALL");
                                getMvpView().onLoadAsset(strings);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().onError();
                            }
                        })

        );
    }

    public void loadRecords(int loadMode, Context context, WebSocketService webSocketService, AccountObject accountObject, String userName, int size, int offset, String assetName, String fundType, boolean isGroupByAsset, boolean isGroupByFundType) {
        mCompositeDisposable.add(
                RetrofitFactory.getInstance()
                        .api()
                        .getBlockExplorerLink()
                        .concatMap(new Function<ResponseBody, ObservableSource<List<Address>>>() {
                            @Override
                            public ObservableSource<List<Address>> apply(ResponseBody responseBody) throws Exception {
                                mBlockerExplorerList.clear();
                                JSONArray jsonArray = new JSONArray(responseBody.string());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    BlockerExplorer blockerExplorer = new BlockerExplorer();
                                    blockerExplorer.setAsset(jsonObject.getString("asset"));
                                    blockerExplorer.setExpolorerLink(jsonObject.getString("explorer"));
                                    mBlockerExplorerList.add(blockerExplorer);
                                }

                                return DBManager.getDbProvider(context)
                                        .getAddress(userName, Address.TYPE_WITHDRAW);
                            }
                        })
                        .concatMap(new Function<List<Address>, ObservableSource<Operations.withdraw_deposit_history_operation>>() {
                            @Override
                            public ObservableSource<Operations.withdraw_deposit_history_operation> apply(List<Address> addresses) throws Exception {
                                mAddressList.addAll(addresses);
                                return Observable.create(new ObservableOnSubscribe<Operations.withdraw_deposit_history_operation>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<Operations.withdraw_deposit_history_operation> emitter) throws Exception {
                                        Date expiration = getExpiration();
                                        Operations.withdraw_deposit_history_operation operation = BitsharesWalletWraper.getInstance().getWithdrawDepositOperation(userName, 0, 0, null, null, expiration);
                                        mSignature = BitsharesWalletWraper.getInstance().getWithdrawDepositSignature(accountObject, operation);
                                        if (!emitter.isDisposed()) {
                                            emitter.onNext(operation);
                                            emitter.onComplete();
                                        }
                                    }
                                });
                            }
                        })
                        .concatMap(new Function<Operations.withdraw_deposit_history_operation, ObservableSource<ResponseBody>>() {
                            @Override
                            public ObservableSource<ResponseBody> apply(Operations.withdraw_deposit_history_operation operation) throws Exception {
                                GatewayLogInRecordRequest gatewayLogInRecordRequest = createLogInRequest(operation, mSignature);
                                Gson gson = GlobalConfigObject.getInstance().getGsonBuilder().create();
                                Log.v("loginRequestBody", gson.toJson(gatewayLogInRecordRequest));
                                return RetrofitFactory.getInstance()
                                        .apiGateway()
                                        .gatewayLogIn(RequestBody.create(MediaType.parse("application/json"), gson.toJson(gatewayLogInRecordRequest)));
                            }
                        })
                        .concatMap(new Function<ResponseBody, ObservableSource<GateWayRecordsResponse>>() {
                            @Override
                            public ObservableSource<GateWayRecordsResponse> apply(ResponseBody responseBody) throws Exception {
                                JSONObject jsonObject = new JSONObject(responseBody.string());
                                if (jsonObject.getInt("code") != 200) {
                                    return Observable.error(new Exception(jsonObject.getString("error")));
                                }
                                return RetrofitFactory.getInstance()
                                        .apiGateway()
                                        .getDepositWithdrawRecords(
                                                "application/json",
                                                "bearer " + mSignature,
                                                userName,
                                                size,
                                                offset,
                                                assetName,
                                                fundType,
                                                isGroupByAsset,
                                                isGroupByFundType);
                            }
                        })
                        .map(new Function<GateWayRecordsResponse, List<GatewayDepositWithdrawRecordsItem>>() {
                            @Override
                            public List<GatewayDepositWithdrawRecordsItem> apply(GateWayRecordsResponse gateWayRecordsResponse) throws Exception {
                                List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItemList = new ArrayList<>();
                                for (Record record : gateWayRecordsResponse.getData().getRecords()) {
                                    GatewayDepositWithdrawRecordsItem gatewayDepositWithdrawRecordsItem = new GatewayDepositWithdrawRecordsItem();
                                    gatewayDepositWithdrawRecordsItem.setItemAsset(webSocketService.getAssetObjectBySymbol(record.getAsset()));
                                    gatewayDepositWithdrawRecordsItem.setRecord(record);
                                    for (Record.Details details : record.getDetails()) {
                                        if (!TextUtils.isEmpty(details.getHash())) {
                                            for (BlockerExplorer blockerExplorer : mBlockerExplorerList) {
                                                if (blockerExplorer.getAsset().equals(record.getCoinType())) {
                                                    gatewayDepositWithdrawRecordsItem.setExplorerLink(blockerExplorer.getExpolorerLink() + details.getHash());
                                                    break;
                                                }
                                            }
                                            if (gatewayDepositWithdrawRecordsItem.getExplorerLink() == null) {
                                                gatewayDepositWithdrawRecordsItem.setExplorerLink("https://etherscan.io/tx/" + details.getHash());
                                            }
                                            break;
                                        }
                                    }

                                    if (gatewayDepositWithdrawRecordsItem.getExplorerLink() == null) {
                                        gatewayDepositWithdrawRecordsItem.setExplorerLink("No Link");
                                    }
                                    for (Address address : mAddressList) {
                                        if (address.getAddress().equals(record.getAddress())) {
                                            gatewayDepositWithdrawRecordsItem.setNote(address.getNote());
                                            break;
                                        }
                                    }

                                    Log.e("ExplorerLink", gatewayDepositWithdrawRecordsItem.getExplorerLink());
                                    gatewayDepositWithdrawRecordsItemList.add(gatewayDepositWithdrawRecordsItem);
                                }
                                return gatewayDepositWithdrawRecordsItemList;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<GatewayDepositWithdrawRecordsItem>>() {
                            @Override
                            public void accept(List<GatewayDepositWithdrawRecordsItem> gatewayDepositWithdrawRecordsItems) throws Exception {
                                getMvpView().onLoadRecordsData(loadMode, gatewayDepositWithdrawRecordsItems);
                                if (mAssetList.isEmpty()) {
                                    loadAssetList(userName);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().onError();
                                throwable.printStackTrace();
                            }
                        })

        );
    }

    private Date getExpiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 5);
        return calendar.getTime();
    }

    private GatewayLogInRecordRequest createLogInRequest(Operations.withdraw_deposit_history_operation operation, String signature) {
        GatewayLogInRecordRequest gatewayLogInRecordRequest = new GatewayLogInRecordRequest();
        gatewayLogInRecordRequest.setOp(operation);
        gatewayLogInRecordRequest.setSigner(signature);
        return gatewayLogInRecordRequest;
    }

}
