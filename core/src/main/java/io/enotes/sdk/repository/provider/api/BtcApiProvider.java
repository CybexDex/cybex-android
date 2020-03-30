package io.enotes.sdk.repository.provider.api;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.core.RPCApiManager;
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
import io.enotes.sdk.repository.api.entity.request.btc.blockcypher.BtcRequestSendRawTransaction;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcBalanceListForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcTransactionListForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcUtxoForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcTransactionListForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcUtxoForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcTransactionListForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcUtxoForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.chainso.SpendTxForChainSo;
import io.enotes.sdk.repository.api.entity.response.btc.omniexplorer.OmniBalance;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.Utils;

import static io.enotes.sdk.repository.provider.ApiProvider.C_BLOCKCHAIN_BITCOIN;

/**
 * BtcApiProvider
 */
public class BtcApiProvider extends BaseApiProvider {
    private ApiService apiService;
    private ApiService transactionThirdService;
    private static Map<Integer, String> firstNetWork = new HashMap<>();
    private static Map<Integer, String> secondNetWork = new HashMap<>();
    private static Map<Integer, String> chainSoNetWork = new HashMap<>();
    public static Map<Integer, String> eNotesNetWork = new HashMap<>();

    static {
        firstNetWork.put(Constant.Network.BTC_MAINNET, "main");
        firstNetWork.put(Constant.Network.BTC_TESTNET, "test3");

        secondNetWork.put(Constant.Network.BTC_MAINNET, "");
        secondNetWork.put(Constant.Network.BTC_TESTNET, "testnet.");

        chainSoNetWork.put(Constant.Network.BTC_MAINNET, "BTC");
        chainSoNetWork.put(Constant.Network.BTC_TESTNET, "BTCTEST");

        eNotesNetWork.put(Constant.Network.BTC_MAINNET, "mainnet");
        eNotesNetWork.put(Constant.Network.BTC_TESTNET, "testnet");
    }

    public BtcApiProvider(Context context, ApiService apiService, ApiService transactionThirdService) {
        super(context);
        this.apiService = apiService;
        this.transactionThirdService = transactionThirdService;
    }

    public LiveData<Resource<List<EntBalanceEntity>>> getBtcBalanceList(int network, String[] address) {
        return addLiveDataSourceNoENotes(getBtcBalanceListBy1st(network, address));
    }

    /**
     * getBitBalance
     */
    public LiveData<Resource<EntBalanceEntity>> getBtcBalance(int network, String address) {
        List<EntBalanceListRequest> listRequests = new ArrayList<>();
        EntBalanceListRequest request = new EntBalanceListRequest();
        listRequests.add(request);
        request.setBlockchain(C_BLOCKCHAIN_BITCOIN);
        request.setNetwork(eNotesNetWork.get(network));
        request.setAddress(address);
        return addLiveDataSource(getBtcBalanceBy2nd(network, address), getBtcBalanceBy1st(network, address), getBtcBalanceBy3rd(network, address), addSourceForEsList(apiService.getBalanceListByES(listRequests), Constant.BlockChain.BITCOIN));
    }

    public LiveData<Resource<EntBalanceEntity>> getOmniBalance(int network, String address, String id) {
        List<EntBalanceListRequest> listRequests = new ArrayList<>();
        EntBalanceListRequest request = new EntBalanceListRequest();
        listRequests.add(request);
        request.setBlockchain(C_BLOCKCHAIN_BITCOIN);
        request.setNetwork(eNotesNetWork.get(network));
        request.setAddress(address);
        request.setOmniproperty(id);
        return addLiveDataSource(getOmniBalanceBy1st(network, address, id), addSourceForEsList(apiService.getBalanceListByES(listRequests), Constant.BlockChain.BITCOIN));
    }

    /**
     * get bitcoin unSpend list
     */
    public LiveData<Resource<List<EntUtxoEntity>>> getBtcUnSpend(int network, String address) {
        return addLiveDataSource(getUtxoListBy2nd(network, address), getUtxoListBy1st(network, address), getUtxoListBy3rd(network, address), getUtxoListByES(network, address));
    }

    /**
     * bitcoin transaction confirmed
     */
    public LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForBitCoin(int network, String txId) {
        List<EntConfirmedListRequest> listRequests = new ArrayList<>();
        EntConfirmedListRequest request = new EntConfirmedListRequest();
        listRequests.add(request);
        request.setBlockchain(C_BLOCKCHAIN_BITCOIN);
        request.setNetwork(eNotesNetWork.get(network));
        request.setTxid(txId);
        return addLiveDataSource(isConfirmedTxForBitCoinBy2nd(network, txId), isConfirmedTxForBitCoinBy1st(network, txId), addSourceForEsList(apiService.getConfirmedListByES(listRequests), Constant.BlockChain.BITCOIN));
    }


    /**
     * getBitFees
     * because of enotes server result is not completion,so need to request third
     */
    public LiveData<Resource<EntFeesEntity>> getBtcFees(int network) {
        if (network == Constant.Network.BTC_TESTNET)
            return addLiveDataSource(getBtcFeesBy1Xst(network), getBtcFeesBy2nd(network), addSourceForES(apiService.getFeeByES(eNotesNetWork.get(network))));
        else
            return addLiveDataSource(getBtcFeesBy1Xst(network), getBtcFeesBy1st(), getBtcFeesBy2nd(network), addSourceForES(apiService.getFeeByES(eNotesNetWork.get(network))));

    }

    /**
     * sendBitTx
     */
    public LiveData<Resource<EntSendTxEntity>> sendBtcTx(int network, String hex) {
        List<EntSendTxListRequest> listRequests = new ArrayList<>();
        EntSendTxListRequest request = new EntSendTxListRequest();
        listRequests.add(request);
        request.setRawtx(hex);
        request.setBlockchain(C_BLOCKCHAIN_BITCOIN);
        request.setNetwork(eNotesNetWork.get(network));
        return addLiveDataSource(sendBtcTxBy2nd(network, hex), sendBtcTxBy1st(network, hex), addSourceForEsList(apiService.sendRawTransactionByES(listRequests), Constant.BlockChain.BITCOIN));
    }


    public LiveData<Resource<List<EntTransactionEntity>>> getTransactionList(int network, String address) {
        return addLiveDataSourceNoENotes(getTransactionList2nd(network, address), getTransactionList1st(network, address));
    }

    public LiveData<Resource<EntSpendTxCountEntity>> getSpendTransactionCount(int network, String address) {
        return addLiveDataSourceNoENotes(getSpendTransactionCount1st(network, address), getSpendTransactionCount2nd(network, address));
    }

    private LiveData<Resource<EntSpendTxCountEntity>> getSpendTransactionCount1st(int network, String address) {
        MediatorLiveData<Resource<EntSpendTxCountEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getSpendTransactionCountByBlockChain(secondNetWork.get(network), address), (resource) -> {
            if (resource.isSuccessful()) {
                BtcTransactionListForBlockChain body = resource.body;
                EntSpendTxCountEntity spendTxCountEntity = new EntSpendTxCountEntity();
                spendTxCountEntity.setCount(body.getTxs().size());
                mediatorLiveData.postValue(Resource.success(spendTxCountEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, resource.errorMessage));
            }
        });
        return mediatorLiveData;
    }

    private LiveData<Resource<EntSpendTxCountEntity>> getSpendTransactionCount2nd(int network, String address) {
        MediatorLiveData<Resource<EntSpendTxCountEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getSpendTransactionCountByChainSo(chainSoNetWork.get(network), address), (resource) -> {
            if (resource.isSuccessful() && resource.body.getStatus().equals("success")) {
                SpendTxForChainSo body = resource.body;
                EntSpendTxCountEntity spendTxCountEntity = new EntSpendTxCountEntity();
                spendTxCountEntity.setCount(body.getData().getTxs().size());
                mediatorLiveData.postValue(Resource.success(spendTxCountEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, resource.errorMessage));
            }
        });
        return mediatorLiveData;
    }


    private LiveData<Resource<List<EntTransactionEntity>>> getTransactionList1st(int network, String address) {
        MediatorLiveData<Resource<List<EntTransactionEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getTransactionListByBlockExplorer(secondNetWork.get(network), address), (resource -> {
            if (resource.isSuccessful()) {
                List<EntTransactionEntity> list = new ArrayList<>();
                List<BtcTransactionListForBlockExplorer.Tx> txs = resource.body.getItems();
                if (txs != null) {
                    for (BtcTransactionListForBlockExplorer.Tx tx : txs) {
                        EntTransactionEntity entTransactionEntity = new EntTransactionEntity();
                        entTransactionEntity.setConfirmations(tx.getConfirmations());
                        entTransactionEntity.setTime(tx.getTime() + "");
                        entTransactionEntity.setTxId(tx.getTxid());
                        for (BtcTransactionListForBlockExplorer.Input input : tx.getVin()) {
                            if (input.getAddr().equals(address)) {
                                entTransactionEntity.setSent(true);
                                break;
                            }
                        }
                        if (entTransactionEntity.isSent()) {
                            entTransactionEntity.setAmount(tx.getValueOut());
                        } else {
                            entTransactionEntity.setAmount("0");
                            for (BtcTransactionListForBlockExplorer.Out out : tx.getVout()) {
                                if (out.getScriptPubKey().getAddresses() != null && out.getScriptPubKey().getAddresses().length > 0) {
                                    if (out.getScriptPubKey().getAddresses()[0].equals(address)) {
                                        entTransactionEntity.setAmount(new BigDecimal(entTransactionEntity.getAmount()).add(new BigDecimal(out.getValue())).toString());
                                    }
                                }
                            }
                        }

                        entTransactionEntity.setAmount(new BigDecimal(entTransactionEntity.getAmount()).multiply(new BigDecimal("100000000")).toBigInteger().toString());
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

    private LiveData<Resource<List<EntTransactionEntity>>> getTransactionList2nd(int network, String address) {
        MediatorLiveData<Resource<List<EntTransactionEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getTransactionListByBlockCypher(firstNetWork.get(network), address), (resource -> {
            if (resource.isSuccessful()) {
                BtcTransactionListForBlockCypher cypher = resource.body;
                List<EntTransactionEntity> list = new ArrayList<>();
                List<BtcTransactionListForBlockCypher.Tx> txrefs = cypher.getTxrefs();
                if (txrefs != null) {
                    for (BtcTransactionListForBlockCypher.Tx tx : txrefs) {
                        EntTransactionEntity entTransactionEntity = new EntTransactionEntity();
                        entTransactionEntity.setAmount(tx.getValue());
                        entTransactionEntity.setSent(tx.getSpent() == null);
                        entTransactionEntity.setTxId(tx.getTx_hash());
                        String UTCString = tx.getConfirmed();
                        UTCString = UTCString.replace("T", " ").replace("Z", "");
                        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date date = utcFormat.parse(UTCString);
                            entTransactionEntity.setTime(date.getTime() / 1000l + "");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        entTransactionEntity.setConfirmations(tx.getConfirmations());
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
     * getBitBalanceList By first network
     */
    private LiveData<Resource<List<EntBalanceEntity>>> getBtcBalanceListBy1st(int network, String[] addresses) {
        MediatorLiveData<Resource<List<EntBalanceEntity>>> mediatorLiveData = new MediatorLiveData<>();
        StringBuffer addressArr = new StringBuffer();
        for (int i = 0; i < addresses.length; i++) {
            addressArr.append(addresses[i]);
            if (i != addresses.length - 1) {
                addressArr.append("|");
            }
        }
        mediatorLiveData.addSource(transactionThirdService.getBalanceListForBtcByBlockChain(secondNetWork.get(network), addressArr.toString()), (resource -> {
            if (resource.isSuccessful()) {
                BtcBalanceListForBlockChain body = resource.body;
                List<EntBalanceEntity> balanceEntityList = new ArrayList<>();
                if (body.getAddresses() != null) {
                    for (BtcBalanceListForBlockChain.Address address : body.getAddresses()) {
                        EntBalanceEntity balanceEntity = new EntBalanceEntity();
                        balanceEntity.setAddress(address.getAddress());
                        balanceEntity.setBalance(Utils.intToHexString(address.getFinal_balance()));
                        balanceEntityList.add(balanceEntity);
                    }
                }
                mediatorLiveData.postValue(Resource.success(balanceEntityList));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private LiveData<Resource<EntBalanceEntity>> getOmniBalanceBy1st(int network, String address, String id) {
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getOmniBalance(address), (api -> {
            if (api.isSuccessful()) {
                OmniBalance body = api.body;
                for (OmniBalance.Balance b : body.getBalance()) {
                    if (b.getId().equals(id)) {
                        EntBalanceEntity balanceEntity = new EntBalanceEntity();
                        balanceEntity.setBalance(new BigInteger(b.getValue()).toString(16));
                        balanceEntity.setAddress(address);
                        mediatorLiveData.postValue(Resource.success(balanceEntity));
                        break;
                    }
                }
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    /**
     * getBitBalance By first network
     */
    private LiveData<Resource<EntBalanceEntity>> getBtcBalanceBy1st(int network, String address) {
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getBalanceForBtcByBlockChain(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = api.body.parseToENotesEntity();
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.BITCOIN);
                mediatorLiveData.postValue(Resource.success(entBalanceEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getBitBalance By second network
     */
    private LiveData<Resource<EntBalanceEntity>> getBtcBalanceBy2nd(int network, String address) {
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getBalanceForBtcByBlockCypher(firstNetWork.get(network), address, get1stRandomApiKey()), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = api.body.parseToENotesEntity();
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.BITCOIN);
                mediatorLiveData.postValue(Resource.success(entBalanceEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getBitBalance By second network
     */
    private LiveData<Resource<EntBalanceEntity>> getBtcBalanceBy3rd(int network, String address) {
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getBalanceForBtcByBlockExplorer(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = api.body.parseToENotesEntity();
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.BITCOIN);
                mediatorLiveData.postValue(Resource.success(entBalanceEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getBitFees by first X network
     */
    private LiveData<Resource<EntFeesEntity>> getBtcFeesBy1Xst(int network) {
        MediatorLiveData<Resource<EntFeesEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getFeesForBtcByBlockCypher(firstNetWork.get(network)), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getBitFees by first network
     */
    private LiveData<Resource<EntFeesEntity>> getBtcFeesBy1st() {
        MediatorLiveData<Resource<EntFeesEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getFeesForBtcByBitcoinFees(), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getBitFees by second network
     */
    private LiveData<Resource<EntFeesEntity>> getBtcFeesBy2nd(int network) {
        MediatorLiveData<Resource<EntFeesEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getFeesForBtcByBlockExplorer(secondNetWork.get(network)), (api -> {
            if (api.isSuccessful() && api.body != null) {
                try {
                    if (api.body.size() > 0) {
                        Map.Entry<String, String> next = api.body.entrySet().iterator().next();
                        String fee = api.body.get(next.getKey());
                        EntFeesEntity entFeeEntity = new EntFeesEntity();
                        entFeeEntity.setFast(new BigDecimal(fee).multiply(new BigDecimal("100000000")).intValue() + "");
                        mediatorLiveData.postValue(Resource.success(entFeeEntity));
                    } else {
                        mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));

                }
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * isConfirmedTxForBitCoin by first network
     */
    private LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForBitCoinBy1st(int network, String txId) {
        MediatorLiveData<Resource<EntConfirmedEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.isConfirmedTxForBitCoinByBlockCypher(firstNetWork.get(network), txId, get1stRandomApiKey()), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * isConfirmedTxForBitCoin by second network
     */
    private LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForBitCoinBy2nd(int network, String txId) {
        MediatorLiveData<Resource<EntConfirmedEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.isConfirmedTxForBitCoinByBlockExplorer(secondNetWork.get(network), txId), (api -> {
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
    private LiveData<Resource<EntSendTxEntity>> sendBtcTxBy1st(int network, String hex) {
        MediatorLiveData<Resource<EntSendTxEntity>> mediatorLiveData = new MediatorLiveData<>();
        BtcRequestSendRawTransaction requestSendRawTransaction = new BtcRequestSendRawTransaction();
        requestSendRawTransaction.setTx(hex);
        mediatorLiveData.addSource(transactionThirdService.sendRawTransactionForBitCoinByBlockCypher(firstNetWork.get(network), requestSendRawTransaction, get1stRandomApiKey()), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * sendBitTx by second network
     */
    private LiveData<Resource<EntSendTxEntity>> sendBtcTxBy2nd(int network, String hex) {
        MediatorLiveData<Resource<EntSendTxEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.sendRawTransactionForBitCoinByBlockExplorer(secondNetWork.get(network), hex), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getUtxoList by first network
     */
    private LiveData<Resource<List<EntUtxoEntity>>> getUtxoListBy1st(int network, String address) {
        MediatorLiveData<Resource<List<EntUtxoEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getUTXOForBitCoinByBlockChain(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                List<EntUtxoEntity> list = new ArrayList<>();
                if (api.body.getUnspent_outputs() != null) {
                    for (BtcUtxoForBlockChain.UnspentOutputsBean txsBean : api.body.getUnspent_outputs()) {
                        list.add(txsBean.parseToENotesEntity());
                    }
                }
                mediatorLiveData.postValue(Resource.success(list));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getUtxoList by second network
     */
    private LiveData<Resource<List<EntUtxoEntity>>> getUtxoListBy2nd(int network, String address) {
        MediatorLiveData<Resource<List<EntUtxoEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getUTXOForBitCoinByBlockCypher(firstNetWork.get(network), address, get1stRandomApiKey()), (api -> {
            if (api.isSuccessful()) {
                List<EntUtxoEntity> list = new ArrayList<>();
                if (api.body.getTxrefs() != null) {
                    for (BtcUtxoForBlockCypher.BtcUtxo txsBean : api.body.getTxrefs()) {
                        list.add(txsBean.parseToENotesEntity());
                    }
                }
                if (api.body.getUnconfirmed_txrefs() != null) {
                    for (BtcUtxoForBlockCypher.BtcUtxo txsBean : api.body.getUnconfirmed_txrefs()) {
                        list.add(txsBean.parseToENotesEntity());
                    }
                }
                mediatorLiveData.postValue(Resource.success(list));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getUtxoList by third network
     */
    private LiveData<Resource<List<EntUtxoEntity>>> getUtxoListBy3rd(int network, String address) {
        MediatorLiveData<Resource<List<EntUtxoEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getUTXOForBitCoinByBlockExplorer(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                List<EntUtxoEntity> list = new ArrayList<>();
                if (api.body != null) {
                    for (BtcUtxoForBlockExplorer txsBean : api.body) {
                        list.add(txsBean.parseToENotesEntity());
                    }
                }
                mediatorLiveData.postValue(Resource.success(list));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * getUtxoList by eNotes network
     */
    private LiveData<Resource<List<EntUtxoEntity>>> getUtxoListByES(int network, String address) {
        MediatorLiveData<Resource<List<EntUtxoEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(apiService.getUtxoListByES(eNotesNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                if (api.body.getCode() == 0 && api.body != null && api.body.getData() != null) {
                    List<EntUtxoEntity> entUtxoEntityList = api.body.getData();
                    //remove invalid utxo,remove utxo which positive utxo (txid and index) and negative utxo (pretxid nad index) matches is true
                    if (entUtxoEntityList.size() > 0) {
                        List<EntUtxoEntity> positiveList = new ArrayList<>();
                        List<EntUtxoEntity> negativeList = new ArrayList<>();
                        List<EntUtxoEntity> matchList = new ArrayList<>();
                        for (EntUtxoEntity entity : api.body.getData()) {
                            if (entity.isPositive()) {
                                positiveList.add(entity);
                            } else {
                                negativeList.add(entity);
                            }
                        }
                        for (EntUtxoEntity n : negativeList) {
                            for (EntUtxoEntity p : positiveList) {
                                if (n.getPrevtxid() != null && p.getTxid() != null) {
                                    if (n.getPrevtxid().equals(p.getTxid()) && n.getIndex().equals(p.getIndex())) {
                                        matchList.add(p);
                                    }
                                }
                            }
                        }
                        if (matchList.size() > 0) {
                            positiveList.removeAll(matchList);
                        }
                        mediatorLiveData.postValue(Resource.success(positiveList));
                    }

                } else
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.body.getMessage()));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private String get1stRandomApiKey() {
        String[] apiKeys = {"e967dba1620441c8ab57d48e88150d87", "ce05502a8ab447db8f3e7dbf10e830cd", "db5dad2ee7d5496184d78a2a0012246a", "21a6d79adca247808a06b6f899a99577", "ab673cc2aeae4a1b81bc6fa38363b2b6"};
        if (RPCApiManager.networkConfig != null && RPCApiManager.networkConfig.blockchypherKeys != null && RPCApiManager.networkConfig.blockchypherKeys.length > 1) {
            String[] keys = RPCApiManager.networkConfig.blockchypherKeys;
            return keys[new Random().nextInt(100) % keys.length];
        } else {
            return apiKeys[new Random().nextInt(100) % 5];
        }
    }
}
