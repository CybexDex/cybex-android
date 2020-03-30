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
import io.enotes.sdk.repository.api.entity.EntTransactionEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.request.EntBalanceListRequest;
import io.enotes.sdk.repository.api.entity.request.EntConfirmedListRequest;
import io.enotes.sdk.repository.api.entity.request.EntSendTxListRequest;
import io.enotes.sdk.repository.api.entity.request.btc.blockcypher.BtcRequestSendRawTransaction;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchTransactionListForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchUtxoForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcBalanceListForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcUtxoForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcTransactionListForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcUtxoForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcTransactionListForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcUtxoForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.omniexplorer.OmniBalance;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.utils.Utils;

import static io.enotes.sdk.repository.provider.ApiProvider.C_BLOCKCHAIN_BITCOIN;

/**
 * BchApiProvider
 */
public class BchApiProvider extends BaseApiProvider {
    private ApiService transactionThirdService;
    private static Map<Integer, String> firstNetWork = new HashMap<>();
    private static Map<Integer, String> secondNetWork = new HashMap<>();

    static {
        firstNetWork.put(Constant.Network.BTC_MAINNET, "");
        firstNetWork.put(Constant.Network.BTC_TESTNET, "tbch.");

        secondNetWork.put(Constant.Network.BTC_MAINNET, "");
        secondNetWork.put(Constant.Network.BTC_TESTNET, "test-");

    }

    public BchApiProvider(Context context, ApiService transactionThirdService) {
        super(context);
        this.transactionThirdService = transactionThirdService;
    }


    /**
     * getBitBalance
     */
    public LiveData<Resource<EntBalanceEntity>> getBchBalance(int network, String address) {
        return addLiveDataSourceNoENotes(getBchBalanceBy1st(network, address), getBtcBalanceBy2nd(network, address));
    }


    /**
     * get bitcoin unSpend list
     */
    public LiveData<Resource<List<EntUtxoEntity>>> getBchUnSpend(int network, String address) {
        return addLiveDataSourceNoENotes(getUtxoListBy1st(network, address), getUtxoListBy2nd(network, address));
    }

    /**
     * bitcoin transaction confirmed
     */
    public LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForBch(int network, String txId) {
        return addLiveDataSourceNoENotes(isConfirmedTxForBchBy1st(network, txId), isConfirmedTxForBitCoinBy2nd(network, txId));
    }


    /**
     * getBitFees
     * because of enotes server result is not completion,so need to request third
     */
    public LiveData<Resource<EntFeesEntity>> getBchFees(int network) {
        return addLiveDataSourceNoENotes(getBchFeesBy1st(network), getBtcFeesBy2nd(network));
    }

    /**
     * sendBitTx
     */
    public LiveData<Resource<EntSendTxEntity>> sendBchTx(int network, String hex) {
        return addLiveDataSourceNoENotes(sendBchTxBy1st(network, hex), sendBtcTxBy2nd(network, hex));
    }


    public LiveData<Resource<List<EntTransactionEntity>>> getTransactionList(int network, String address) {
        return addLiveDataSourceNoENotes(getTransactionList1st(network, address), getTransactionList2nd(network, address));
    }

    private LiveData<Resource<List<EntTransactionEntity>>> getTransactionList1st(int network, String address) {
        MediatorLiveData<Resource<List<EntTransactionEntity>>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getTransactionListForBchByBlockdozer(firstNetWork.get(network), address), (resource -> {
            if (resource.isSuccessful()) {
                List<EntTransactionEntity> list = new ArrayList<>();
                List<BchTransactionListForBlockdozer.Tx> txs = resource.body.getTxs();
                if (txs != null) {
                    for (BchTransactionListForBlockdozer.Tx tx : txs) {
                        EntTransactionEntity entTransactionEntity = new EntTransactionEntity();
                        entTransactionEntity.setConfirmations(tx.getConfirmations());
                        entTransactionEntity.setTime(tx.getTime() + "");
                        entTransactionEntity.setTxId(tx.getTxid());
                        for (BchTransactionListForBlockdozer.Input input : tx.getVin()) {
                            if (input.getAddr().equals(address)) {
                                entTransactionEntity.setSent(true);
                                break;
                            }
                        }
                        entTransactionEntity.setAmount(!entTransactionEntity.isSent() ? tx.getValueOut() : tx.getValueIn());
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
        mediatorLiveData.addSource(transactionThirdService.getTransactionListForBchByBitpay(secondNetWork.get(network), address), (resource -> {
            if (resource.isSuccessful()) {
                List<EntTransactionEntity> list = new ArrayList<>();
                List<BchTransactionListForBlockdozer.Tx> txs = resource.body.getTxs();
                if (txs != null) {
                    for (BchTransactionListForBlockdozer.Tx tx : txs) {
                        EntTransactionEntity entTransactionEntity = new EntTransactionEntity();
                        entTransactionEntity.setConfirmations(tx.getConfirmations());
                        entTransactionEntity.setTime(tx.getTime() + "");
                        entTransactionEntity.setTxId(tx.getTxid());
                        for (BchTransactionListForBlockdozer.Input input : tx.getVin()) {
                            if (input.getAddr().equals(address)) {
                                entTransactionEntity.setSent(true);
                                break;
                            }
                        }
                        entTransactionEntity.setAmount(!entTransactionEntity.isSent() ? tx.getValueOut() : tx.getValueIn());
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


    /**
     * getBitBalance By first network
     */
    private LiveData<Resource<EntBalanceEntity>> getBchBalanceBy1st(int network, String address) {
        MediatorLiveData<Resource<EntBalanceEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getBalanceForBchByBlockZoder(firstNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = api.body.parseToENotesEntity();
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.BITCOIN_CASH);
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
        mediatorLiveData.addSource(transactionThirdService.getBalanceForBchByBitpay(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                EntBalanceEntity entBalanceEntity = api.body.parseToENotesEntity();
                entBalanceEntity.setAddress(address);
                entBalanceEntity.setCoinType(Constant.BlockChain.BITCOIN_CASH);
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
    private LiveData<Resource<EntFeesEntity>> getBchFeesBy1st(int network) {
        MediatorLiveData<Resource<EntFeesEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.getFeesForBchByBlockdozer(firstNetWork.get(network)), (api -> {
            if (api.isSuccessful()) {
                if (api.body.size() > 0) {
                    Map.Entry<String, String> next = api.body.entrySet().iterator().next();
                    String fee = api.body.get(next.getKey());
                    EntFeesEntity entFeeEntity = new EntFeesEntity();
                    entFeeEntity.setFast(new BigDecimal(fee).multiply(new BigDecimal("100000000")).intValue() + "");
                    mediatorLiveData.postValue(Resource.success(entFeeEntity));
                } else {
                    mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
                }
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
        mediatorLiveData.addSource(transactionThirdService.getFeesForBchByBitpay(secondNetWork.get(network)), (api -> {
            if (api.isSuccessful()) {
                if (api.body.size() > 0) {
                    Map.Entry<String, String> next = api.body.entrySet().iterator().next();
                    String fee = api.body.get(next.getKey());
                    EntFeesEntity entFeeEntity = new EntFeesEntity();
                    entFeeEntity.setFast(new BigDecimal(fee).multiply(new BigDecimal("100000000")).intValue() + "");
                    mediatorLiveData.postValue(Resource.success(entFeeEntity));
                } else {
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
    private LiveData<Resource<EntConfirmedEntity>> isConfirmedTxForBchBy1st(int network, String txId) {
        MediatorLiveData<Resource<EntConfirmedEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.isConfirmedTxForBchByBlockdozer(firstNetWork.get(network), txId), (api -> {
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
        mediatorLiveData.addSource(transactionThirdService.isConfirmedTxForBchByBitpay(secondNetWork.get(network), txId), (api -> {
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
    private LiveData<Resource<EntSendTxEntity>> sendBchTxBy1st(int network, String hex) {
        MediatorLiveData<Resource<EntSendTxEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(transactionThirdService.sendRawTransactionForBchByBlockdozer(firstNetWork.get(network), hex), (api -> {
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
        mediatorLiveData.addSource(transactionThirdService.sendRawTransactionForBchByBitpay(secondNetWork.get(network), hex), (api -> {
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
        mediatorLiveData.addSource(transactionThirdService.getUTXOForBchByBlockdozer(firstNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                List<EntUtxoEntity> list = new ArrayList<>();
                if (api.body != null) {
                    for (BchUtxoForBlockdozer unspend : api.body) {
                        list.add(unspend.parseToENotesEntity());
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
        mediatorLiveData.addSource(transactionThirdService.getUTXOForBchByBitpay(secondNetWork.get(network), address), (api -> {
            if (api.isSuccessful()) {
                List<EntUtxoEntity> list = new ArrayList<>();
                if (api.body != null) {
                    for (BchUtxoForBlockdozer unspend : api.body) {
                        list.add(unspend.parseToENotesEntity());
                    }
                }
                mediatorLiveData.postValue(Resource.success(list));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


}
