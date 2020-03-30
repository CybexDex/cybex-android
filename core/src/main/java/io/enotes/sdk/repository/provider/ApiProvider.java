package io.enotes.sdk.repository.provider;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.ethereum.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.ErrorCode;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.repository.api.ApiService;
import io.enotes.sdk.repository.api.ExchangeRateApiService;
import io.enotes.sdk.repository.api.RetrofitFactory;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.repository.api.entity.EntCallEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;
import io.enotes.sdk.repository.api.entity.EntExchangeRateEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntGasEntity;
import io.enotes.sdk.repository.api.entity.EntGasPriceEntity;
import io.enotes.sdk.repository.api.entity.EntNonceEntity;
import io.enotes.sdk.repository.api.entity.EntNotificationEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;
import io.enotes.sdk.repository.api.entity.EntSpendTxCountEntity;
import io.enotes.sdk.repository.api.entity.EntTransactionEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.EntVersionEntity;
import io.enotes.sdk.repository.api.entity.request.EntNotificationListRequest;
import io.enotes.sdk.repository.base.BaseManager;
import io.enotes.sdk.repository.base.Resource;
import io.enotes.sdk.repository.db.AppDataBase;
import io.enotes.sdk.repository.db.dao.MfrDao;
import io.enotes.sdk.repository.db.entity.Mfr;
import io.enotes.sdk.repository.provider.api.BaseApiProvider;
import io.enotes.sdk.repository.provider.api.BchApiProvider;
import io.enotes.sdk.repository.provider.api.BtcApiProvider;
import io.enotes.sdk.repository.provider.api.EthApiProvider;
import io.enotes.sdk.repository.provider.api.ExchangeRateApiProvider;
import io.enotes.sdk.repository.provider.api.XrpApiProvider;
import io.enotes.sdk.utils.ContractUtils;
import io.enotes.sdk.utils.LogUtils;

public class ApiProvider extends BaseApiProvider implements BaseManager {
    public static final String C_BLOCKCHAIN_BITCOIN = "bitcoin";
    public static final String C_BLOCKCHAIN_ETHER = "ethereum";
    public static final String C_BLOCKCHAIN_BITCOIN_CASH = "bitcoin cash";
    public static final String C_BLOCKCHAIN_RIPPLE = "ripple";
    private ApiService apiService;
    private ApiService transactionThirdService;
    private BtcApiProvider btcApiManager;
    private EthApiProvider ethApiManager;
    private BchApiProvider bchApiProvider;
    private XrpApiProvider xrpApiProvider;
    private ExchangeRateApiProvider exchangeRateApiProvider;
    private ExchangeRateApiService exchangeRateApiService;
    private MfrDao mfrDao;

    public ApiProvider(Context context) {
        super(context);
        if (context != null)
            mfrDao = AppDataBase.init(context).getMfrDao();
        apiService = RetrofitFactory.getTransactionService(context);
        transactionThirdService = RetrofitFactory.getTransactionThirdService(context);
        exchangeRateApiService = RetrofitFactory.getExchangeRateService(context);
        btcApiManager = new BtcApiProvider(context, apiService, transactionThirdService);
        ethApiManager = new EthApiProvider(context, apiService, transactionThirdService);
        bchApiProvider = new BchApiProvider(context, transactionThirdService);
        xrpApiProvider = new XrpApiProvider(context, apiService, transactionThirdService);
        exchangeRateApiProvider = new ExchangeRateApiProvider(context, exchangeRateApiService);
    }

    /**
     * get diff coin balance list
     *
     * @param network
     * @param addresses
     * @param blockChain
     * @return
     */
    public LiveData<Resource<List<EntBalanceEntity>>> getBalanceList(String blockChain, int network, String[] addresses) {
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            LogUtils.i(TAG, "get BitCoin Balance");
            return btcApiManager.getBtcBalanceList(network, addresses);

        } else if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            LogUtils.i(TAG, "get Eth Balance");
            return ethApiManager.getEthBalanceList(network, addresses);
        }

        return new MediatorLiveData<>();
    }

    /**
     * get diff coin balance
     *
     * @param network
     * @param address
     * @param blockChain
     * @return
     */
    public LiveData<Resource<EntBalanceEntity>> getBalance(String blockChain, int network, String address) {
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            LogUtils.i(TAG, "get BitCoin Balance");
            return btcApiManager.getBtcBalance(network, address);
        } else if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            LogUtils.i(TAG, "get Eth Balance");
            return ethApiManager.getEthBalance(network, address);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            LogUtils.i(TAG, "get BitCoin Cash Balance");
            return bchApiProvider.getBchBalance(network, address);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            LogUtils.i(TAG, "get Ripple Balance");
            return xrpApiProvider.getXrpBalance(network, address);
        }

        return new MediatorLiveData<>();
    }

    /**
     * estimateFees
     *
     * @param network
     * @return
     */
    public LiveData<Resource<EntFeesEntity>> estimateFee(String blockChain, int network) {
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            LogUtils.i(TAG, "get BitCoin Fees");
            return btcApiManager.getBtcFees(network);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            LogUtils.i(TAG, "get BitCoin Cash Fees");
            return bchApiProvider.getBchFees(network);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            LogUtils.i(TAG, "get Ripple Fees");
            return xrpApiProvider.getXrpFees(network);
        }
        return new MediatorLiveData<>();
    }


    /**
     * getGasPrice
     *
     * @param network
     * @return
     */
    public LiveData<Resource<EntGasPriceEntity>> getGasPrice(int network) {
        MediatorLiveData<Resource<EntGasPriceEntity>> mediatorLiveData = new MediatorLiveData<>();
        LogUtils.i(TAG, "get Eth Gas");
        mediatorLiveData.addSource(ethApiManager.getEthGasPrice(network), (ethGasPriceEntityResource -> {
            mediatorLiveData.postValue(ethGasPriceEntityResource);
        }));
        return mediatorLiveData;
    }

    /**
     * send diff coin raw transaction
     *
     * @param network
     * @param hexString
     * @param blockChain
     * @return
     */
    public LiveData<Resource<EntSendTxEntity>> sendRawTransaction(String blockChain, int network, String hexString) {
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            LogUtils.i(TAG, "BitCoin Send Tx");
            return btcApiManager.sendBtcTx(network, hexString);

        } else if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            LogUtils.i(TAG, "Eth Send Tx");
            return ethApiManager.sendEthTx(network, hexString);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            LogUtils.i(TAG, "BitCoin Cash Send Tx");
            return bchApiProvider.sendBchTx(network, hexString);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            LogUtils.i(TAG, "Ripple Send Tx");
            return xrpApiProvider.sendXrpTx(network, hexString);
        }
        return null;
    }

    /**
     * diff coin transaction confirmed status;if txId is null , tx status is NoTransaction
     *
     * @param blockChain
     * @param network
     * @param txId
     * @param blockChain
     * @return
     */
    public LiveData<Resource<EntConfirmedEntity>> getTransactionReceipt(String blockChain, int network, String txId) {
        MediatorLiveData<Resource<EntConfirmedEntity>> mediatorLiveData = new MediatorLiveData<>();
        EntConfirmedEntity confirmedEntity = new EntConfirmedEntity();
        confirmedEntity.setTxid(txId);
        if (TextUtils.isEmpty(txId)) {
            confirmedEntity.setStatus(EntConfirmedEntity.STATUS_NO_TRANSACTION);
            mediatorLiveData.postValue(Resource.success(confirmedEntity));
            return mediatorLiveData;
        }
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            LogUtils.i(TAG, "BitCoin confirmed Tx");
            return btcApiManager.isConfirmedTxForBitCoin(network, txId);
        } else if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            LogUtils.i(TAG, "ETH confirmed Tx");
            return ethApiManager.isConfirmedTxForEth(network, txId);

        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            LogUtils.i(TAG, "BitCoin Cash confirmed Tx");
            return bchApiProvider.isConfirmedTxForBch(network, txId);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            LogUtils.i(TAG, "Ripple confirmed Tx");
            return xrpApiProvider.isConfirmedTxForXrp(network, txId);
        }
        return mediatorLiveData;
    }

    /**
     * get bitcoin unSpend list
     *
     * @param network
     * @param address
     * @return
     */
    public LiveData<Resource<List<EntUtxoEntity>>> getUnSpend(String blockChain, int network, String address) {
        if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            return btcApiManager.getBtcUnSpend(network, address);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            return bchApiProvider.getBchUnSpend(network, address);
        }
        return new MediatorLiveData<>();
    }


    /**
     * get eth GasLimit
     *
     * @param toAddress
     * @param value
     * @param gasPrice
     * @return
     */
    public LiveData<Resource<EntGasEntity>> estimateGas(int network, String from, String toAddress, String value, String gasPrice, String data) {
        if (TextUtils.isEmpty(gasPrice)) gasPrice = "0";
        return ethApiManager.estimateGas(network, from, toAddress, value, gasPrice, data);
    }

    /**
     * submit notification Cid after withdraw successful
     *
     * @param txId
     * @param cId
     * @param blockChain
     * @param network
     * @return
     */
    public LiveData<Resource<EntNotificationEntity>> subscribeNotification(String blockChain, int network, String txId, String cId) {
        List<EntNotificationListRequest> listRequests = new ArrayList<>();
        EntNotificationListRequest request = new EntNotificationListRequest();
        listRequests.add(request);
        request.setCid(cId);
        if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            request.setBlockchain(C_BLOCKCHAIN_ETHER);
            request.setNetwork(EthApiProvider.eNotesNetWork.get(network));
            request.setTxid(txId);
        } else if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            request.setBlockchain(C_BLOCKCHAIN_BITCOIN);
            request.setNetwork(BtcApiProvider.eNotesNetWork.get(network));
            request.setTxid(txId);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            request.setBlockchain(C_BLOCKCHAIN_BITCOIN_CASH);
            request.setNetwork(BtcApiProvider.eNotesNetWork.get(network));
            request.setTxid(txId);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            request.setBlockchain(C_BLOCKCHAIN_RIPPLE);
            request.setNetwork(BtcApiProvider.eNotesNetWork.get(network));
            request.setTxid(txId);
        }
        return addSourceForEsList(apiService.subscribeNotificationByES(listRequests), blockChain);
    }

    /**
     * get eth nonce
     *
     * @param network
     * @param address
     * @return
     */
    public LiveData<Resource<EntNonceEntity>> getNonce(int network, String address) {
        return ethApiManager.getEthNonce(network, address);
    }

    public LiveData<Resource<List<EntTransactionEntity>>> getTransactionList(@NonNull String blockChain, int network, @NonNull String address, String tokenAddress) {
        if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            return ethApiManager.getTransactionList(network, address, tokenAddress);
        } else if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            return btcApiManager.getTransactionList(network, address);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            return bchApiProvider.getTransactionList(network, address);
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            return xrpApiProvider.getTransactionList(network, address);
        }
        return new MediatorLiveData<>();
    }

    public LiveData<Resource<EntSpendTxCountEntity>> getSpendTransactionCount(@NonNull String blockChain, int network, String address) {
        if (Constant.BlockChain.ETHEREUM.equals(blockChain)) {
            return ethApiManager.getSpendTransactionCount(network, address);
        } else if (Constant.BlockChain.BITCOIN.equals(blockChain)) {
            return btcApiManager.getSpendTransactionCount(network, address);
        } else if (Constant.BlockChain.BITCOIN_CASH.equals(blockChain)) {
            return new MediatorLiveData<>();
        } else if (Constant.BlockChain.RIPPLE.equals(blockChain)) {
            return xrpApiProvider.getSpendTransactionCount(network, address);
        }
        return new MediatorLiveData<>();
    }

    /**
     * call
     *
     * @param network
     * @param toAddress
     * @param data
     * @return
     */
    public Resource<EntCallEntity> callEth(int network, String toAddress, String data) {
        try {
            return getValue(ethApiManager.callEth(network, toAddress, data));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * call
     *
     * @param network
     * @param toAddress
     * @param data
     * @return
     */
    public LiveData<Resource<EntCallEntity>> call(int network, String toAddress, String data) {
        return ethApiManager.callEth(network, toAddress, data);
    }


    /**
     * callCertPubKey
     *
     * @param vendorName
     * @param batch
     * @return
     */
    public Mfr callCertPubKey(String abiAddress, String vendorName, String batch, boolean testCard) {
        try {
            Mfr mfr = getValue(mfrDao.getMfr(vendorName, batch));
            if (mfr != null) {
                return mfr;
            }
            String data = "0x" + ByteUtil.toHexString(ContractUtils.getAbiFunctionKeyOf().encode(getSha3BigInteger(vendorName.toUpperCase()), getSha3BigInteger(batch)));

            Resource<EntCallEntity> value = getValue(ethApiManager.callEth(abiAddress, data, testCard));
            if (value.status == Status.SUCCESS && value.data != null) {
                Mfr mfr1 = new Mfr(vendorName, batch, value.data.getPubKey());
                if (mfr1.getPublicKey() != null && mfr1.getPublicKey().length() > 0) {
                    mfrDao.insert(mfr1);
                }
                return mfr1;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * update version
     *
     * @return latest version
     */
    public LiveData<Resource<EntVersionEntity>> updateVersion() {
        MediatorLiveData<Resource<EntVersionEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(apiService.updateVersion(), (api -> {
            if (api.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(api.body));
            } else {
                mediatorLiveData.postValue(Resource.error(ErrorCode.NET_ERROR, api.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    /**
     * get digital currency exchange rate
     *
     * @param digiccy you can see EntExchangeRateEntity
     * @return usd ,eur ,cny ,jpy
     */
    public LiveData<Resource<EntExchangeRateEntity>> getExchangeRate(String digiccy) {
        return exchangeRateApiProvider.getExchangeRate(digiccy);
    }

    public LiveData<Resource<EntBalanceEntity>> getOmniBalance(int network, String address, String id) {
        return btcApiManager.getOmniBalance(network, address, id);
    }

    public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] objects = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer observer = new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                objects[0] = o;
                latch.countDown();
                try {
                    liveData.removeObserver(this);
                } catch (Exception e) {
                }

            }
        };
        liveData.observeForever(observer);
        latch.await();
        return (T) objects[0];
    }
}
