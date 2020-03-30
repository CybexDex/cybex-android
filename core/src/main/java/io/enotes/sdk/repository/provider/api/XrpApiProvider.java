package io.enotes.sdk.repository.provider.api;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.core.ENotesSDK;
import io.enotes.sdk.repository.api.ApiService;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;
import io.enotes.sdk.repository.api.entity.EntSpendTxCountEntity;
import io.enotes.sdk.repository.api.entity.EntTransactionEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.request.EntBalanceListRequest;
import io.enotes.sdk.repository.api.entity.request.EntConfirmedListRequest;
import io.enotes.sdk.repository.api.entity.request.EntSendTxListRequest;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPBalanceParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPRequest;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPSendRawTxParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPTransactionListParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPTxParams;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchTransactionListForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchUtxoForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPBalance;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPFee;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPTransactionList;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.Utils;

import static io.enotes.sdk.repository.provider.ApiProvider.C_BLOCKCHAIN_BITCOIN;
import static io.enotes.sdk.repository.provider.ApiProvider.C_BLOCKCHAIN_RIPPLE;

/**
 * BchApiProvider
 */
public class XrpApiProvider extends BaseApiProvider {
    private ApiService transactionThirdService;
    private ApiService apiService;
    private static Map<Integer, String> firstNetWork = new HashMap<>();
    private static Map<Integer, String> secondNetWork = new HashMap<>();
    public static Map<Integer, String> eNotesNetWork = new HashMap<>();


    static {
        firstNetWork.put(Constant.Network.BTC_MAINNET, "s2.ripple.com");
        firstNetWork.put(Constant.Network.BTC_TESTNET, "s.altnet.rippletest.net");

        secondNetWork.put(Constant.Network.BTC_MAINNET, "");
        secondNetWork.put(Constant.Network.BTC_TESTNET, "test-");

        eNotesNetWork.put(Constant.Network.BTC_MAINNET, "mainnet");
        eNotesNetWork.put(Constant.Network.BTC_TESTNET, "testnet");
    }

    public XrpApiProvider(Context context, ApiService apiService, ApiService transactionThirdService) {
        super(context);
        this.transactionThirdService = transactionThirdService;
        this.apiService = apiService;
    }


    /**
     * getBitBalance
     */
    public LiveData<Resource<EntBalanceEntity>> getXrpBalance(int network, String address) {
        if (!ENotesSDK.config.isRequestENotesServer || network == Constant.Network.BTC_TESTNET)
            return addLiveDataSourceNoENotes(getXrpBalanceBy1st(network, address));
        return addLiveDataSourceNoENotes(addSourceForES(apiService.getXrpBalanceByES(eNotesNetWork.get(network), address)), getXrpBalanceBy1st(network, address));

    }


    /**
     * bitcoin transaction confirmed
     */
    public LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForXrp(int network, String txId) {
        if (!ENotesSDK.config.isRequestENotesServer || network == Constant.Network.BTC_TESTNET)
            return addLiveDataSourceNoENotes(isConfirmedTxForXrpBy1st(network, txId));
        List<EntConfirmedListRequest> listRequests = new ArrayList<>();
        EntConfirmedListRequest request = new EntConfirmedListRequest();
        listRequests.add(request);
        request.setBlockchain(C_BLOCKCHAIN_RIPPLE);
        request.setNetwork(eNotesNetWork.get(network));
        request.setTxid(txId);
        return addLiveDataSourceNoENotes(addSourceForEsList(apiService.getConfirmedListByES(listRequests), Constant.BlockChain.RIPPLE), isConfirmedTxForXrpBy1st(network, txId));
    }


    /**
     * getBitFees
     * because of enotes server result is not completion,so need to request third
     */
    public LiveData<Resource<EntFeesEntity>> getXrpFees(int network) {
        if (!ENotesSDK.config.isRequestENotesServer || network == Constant.Network.BTC_TESTNET)
            return addLiveDataSourceNoENotes(getXrpFeesBy1st(network));

        return addLiveDataSourceNoENotes(addSourceForES(apiService.getXrpFeeByES(eNotesNetWork.get(network))), getXrpFeesBy1st(network));
    }

    /**
     * sendBitTx
     */
    public LiveData<Resource<EntSendTxEntity>> sendXrpTx(int network, String hex) {
        if (!ENotesSDK.config.isRequestENotesServer || network == Constant.Network.BTC_TESTNET)
            return addLiveDataSourceNoENotes(sendXrpTxBy1st(network, hex));
        List<EntSendTxListRequest> listRequests = new ArrayList<>();
        EntSendTxListRequest request = new EntSendTxListRequest();
        listRequests.add(request);
        request.setRawtx(hex);
        request.setBlockchain(C_BLOCKCHAIN_RIPPLE);
        request.setNetwork(eNotesNetWork.get(network));
        return addLiveDataSourceNoENotes(addSourceForEsList(apiService.sendRawTransactionByES(listRequests), Constant.BlockChain.RIPPLE), sendXrpTxBy1st(network, hex));
    }


    public LiveData<Resource<List<EntTransactionEntity>>> getTransactionList(int network, String address) {
        return addLiveDataSourceNoENotes(getTransactionList1st(network, address));
    }

    public LiveData<Resource<EntSpendTxCountEntity>> getSpendTransactionCount(int network, String address) {
        MediatorLiveData<Resource<EntSpendTxCountEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(getXrpBalance(network, address),(resource)->{
            if(resource.status == Status.SUCCESS){
                EntSpendTxCountEntity entity = new EntSpendTxCountEntity();
                entity.setCount(Integer.valueOf(resource.data.getSequence()));
                mediatorLiveData.postValue(Resource.success(entity));
            }else{
                mediatorLiveData.postValue(Resource.error(resource.errorCode,resource.message));
            }

        });
        return mediatorLiveData;
    }

    private LiveData<Resource<List<EntTransactionEntity>>> getTransactionList1st(int network, String address) {
        XRPTransactionListParams params = new XRPTransactionListParams();
        params.setAccount(address);
        params.setBinary(false);
        params.setForward(false);
        params.setLedger_index_max(-1);
        params.setLedger_index_min(-1);
        params.setLimit(50);
        XRPRequest<XRPTransactionListParams> request = XRPRequest.getXRPRequest("account_tx", params);

        MediatorLiveData<Resource<List<EntTransactionEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getTransactionListForRipple(firstNetWork.get(network), request), (resource -> {
            if (resource.isSuccessful()) {
                List<EntTransactionEntity> list = new ArrayList<>();
                List<XRPTransactionList.Transaction> txs = resource.body.getResult().getTransactions();
                if (txs != null) {
                    for (XRPTransactionList.Transaction tx : txs) {
                        EntTransactionEntity entTransactionEntity = new EntTransactionEntity();
                        entTransactionEntity.setConfirmations(tx.isValidated() ? 6 : 0);
                        entTransactionEntity.setTime(tx.getTx().getDate() + "");
                        entTransactionEntity.setTxId(tx.getTx().getHash());
                        entTransactionEntity.setSent(tx.getTx().getAccount().equals(address));
                        entTransactionEntity.setAmount(tx.getTx().getAmount());
                        list.add(entTransactionEntity);
                    }
                }
                mediatorLiveData.postValue(Resource.success(list));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    /**
     * getBitBalance By first network
     */
    private LiveData<Resource<EntBalanceEntity>> getXrpBalanceBy1st(int network, String address) {
        XRPBalanceParams xrpBalanceParams = new XRPBalanceParams();
        xrpBalanceParams.setAccount(address);
        xrpBalanceParams.setStrict(true);
        xrpBalanceParams.setLedger_index("current");
        xrpBalanceParams.setQueue(true);
        XRPRequest<XRPBalanceParams> request = XRPRequest.getXRPRequest("account_info", xrpBalanceParams);
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getBalanceForRipple(firstNetWork.get(network), request), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = new EntBalanceEntity();
                XRPBalance balance = api.body;
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.RIPPLE);
                if (balance.getResult().getAccount_data() == null) {
                    entBalanceEntity.setBalance(Utils.intToHexString("0"));
                    entBalanceEntity.setSequence("1");
                } else {
                    entBalanceEntity.setBalance(Utils.intToHexString(balance.getResult().getAccount_data().getBalance()));
                    entBalanceEntity.setSequence(Utils.intToHexString(balance.getResult().getAccount_data().getSequence() + ""));
                }
                mediatorLiveData.postValue(Resource.success(entBalanceEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    /**
     * getBitFees by first network
     */
    private LiveData<Resource<EntFeesEntity>> getXrpFeesBy1st(int network) {
        XRPRequest<Object> request = XRPRequest.getXRPRequest("fee", null);
        MediatorLiveData<Resource<EntFeesEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getFeesForRipple(firstNetWork.get(network), request), (api -> {
            if (api.isSuccessful()) {
                XRPFee.Drop drops = api.body.getResult().getDrops();
                EntFeesEntity feesEntity = new EntFeesEntity();
                feesEntity.setLow(drops.getMinimum_fee());
                feesEntity.setFast(drops.getBase_fee());
                feesEntity.setFastest(drops.getMedian_fee());
                mediatorLiveData.postValue(Resource.success(feesEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * isConfirmedTxForBitCoin by first network
     */
    private LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForXrpBy1st(int network, String txId) {
        XRPTxParams params = new XRPTxParams();
        params.setTransaction(txId);
        params.setBinary(false);

        XRPRequest<XRPTxParams> request = XRPRequest.getXRPRequest("tx", params);
        MediatorLiveData<Resource<EntConfirmedEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.isConfirmedForRipple(firstNetWork.get(network), request), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    /**
     * sendBitTx by first network
     */
    private LiveData<Resource<EntSendTxEntity>> sendXrpTxBy1st(int network, String hex) {
        XRPSendRawTxParams params = new XRPSendRawTxParams();
        params.setTx_blob(hex);
        XRPRequest<XRPSendRawTxParams> request = XRPRequest.getXRPRequest("submit", params);
        MediatorLiveData<Resource<EntSendTxEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.sendRawTransactionForRipple(firstNetWork.get(network), request), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


}
