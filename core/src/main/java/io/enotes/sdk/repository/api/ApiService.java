package io.enotes.sdk.repository.api;

import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.repository.api.entity.EntCallEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntGasEntity;
import io.enotes.sdk.repository.api.entity.EntGasPriceEntity;
import io.enotes.sdk.repository.api.entity.EntNonceEntity;
import io.enotes.sdk.repository.api.entity.EntNotificationEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.repository.api.entity.EntVersionEntity;
import io.enotes.sdk.repository.api.entity.ResponseEntity;
import io.enotes.sdk.repository.api.entity.request.EntBalanceListRequest;
import io.enotes.sdk.repository.api.entity.request.EntConfirmedListRequest;
import io.enotes.sdk.repository.api.entity.request.EntNotificationListRequest;
import io.enotes.sdk.repository.api.entity.request.EntSendTxListRequest;
import io.enotes.sdk.repository.api.entity.request.btc.blockcypher.BtcRequestSendRawTransaction;
import io.enotes.sdk.repository.api.entity.request.eth.infura.EthRequestForInfura;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPBalanceParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPRequest;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPSendRawTxParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPTransactionListParams;
import io.enotes.sdk.repository.api.entity.request.xrp.XRPTxParams;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchBalanceForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchConfirmedForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchFeesForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchSendRawTransactionForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchTransactionListForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.bch.blockdozer.BchUtxoForBlockdozer;
import io.enotes.sdk.repository.api.entity.response.btc.bitcoinfees.BtcFeesEntity;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcBalanceForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcBalanceListForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcTransactionListForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockchain.BtcUtxoForBlockChain;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcBalanceForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcConfirmedForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcFeesForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcSendRawTransactionForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcTransactionListForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockcypher.BtcUtxoForBlockCypher;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcBalanceForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcConfirmedForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcSendRawTransactionForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcTransactionListForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.blockexplorer.BtcUtxoForBlockExplorer;
import io.enotes.sdk.repository.api.entity.response.btc.chainso.SpendTxForChainSo;
import io.enotes.sdk.repository.api.entity.response.btc.omniexplorer.OmniBalance;
import io.enotes.sdk.repository.api.entity.response.eth.etherchain.EthGasPriceEntity;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthBalanceForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthBalanceListForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthConfirmedForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthEstimateGasForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthGasPriceForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthNonceForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthSendTxForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.etherscan.EthTransactionListForEtherScan;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthBalanceForInfura;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthConfirmedForInfua;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthEstimateGasForInfura;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthGasPriceForInfura;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthNonceForInfura;
import io.enotes.sdk.repository.api.entity.response.eth.infura.EthSendRawTransactionForInfura;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPBalance;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPFee;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPIsConfirmed;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPSendRawTransaction;
import io.enotes.sdk.repository.api.entity.response.xrp.XRPTransactionList;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    /**********************************BTC****************************************/
    String URI_BLOCKCHAIN = "blockchain.info";
    String URI_BLOCKCYPHER = "api.blockcypher.com";
    String URI_BLOCKEXPLORER = "blockexplorer.com";

    /*****Balance List Api***/
    @GET("https://{network}" + URI_BLOCKCHAIN + "/multiaddr?n=1&limit=0&filter=5")
    LiveData<ApiResponse<BtcBalanceListForBlockChain>> getBalanceListForBtcByBlockChain(@Path("network") String network, @Query("active") String addresses);

    /*****Balance Api***/
    @GET("https://{network}" + URI_BLOCKCHAIN + "/rawaddr/{address}?limit=0&filter=5")
    LiveData<ApiResponse<BtcBalanceForBlockChain>> getBalanceForBtcByBlockChain(@Path("network") String network, @Path("address") String address);

    @GET("https://" + URI_BLOCKCYPHER + "/v1/btc/{network}/addrs/{address}/balance")
    LiveData<ApiResponse<BtcBalanceForBlockCypher>> getBalanceForBtcByBlockCypher(@Path("network") String network, @Path("address") String address, @Query("tokens") String token);

    @GET("https://{network}" + URI_BLOCKEXPLORER + "/api/addr/{address}")
    LiveData<ApiResponse<BtcBalanceForBlockExplorer>> getBalanceForBtcByBlockExplorer(@Path("network") String network, @Path("address") String address);

    /*****USDT Balance Api***/
    @FormUrlEncoded
    @Headers({"Content-Type: application/x-www-form-urlencoded", "Content-Type: application/x-www-form-urlencoded"})
    @POST("https://api.omniexplorer.info/v1/address/addr/")
    LiveData<ApiResponse<OmniBalance>> getOmniBalance(@Field("addr") String address);

    /*****Fee Api***/
    @GET("https://bitcoinfees.earn.com/api/v1/fees/recommended")
    LiveData<ApiResponse<BtcFeesEntity>> getFeesForBtcByBitcoinFees();

    @GET("https://{network}" + URI_BLOCKEXPLORER + "/api/utils/estimatefee?nbBlocks=3")
    LiveData<ApiResponse<Map<String, String>>> getFeesForBtcByBlockExplorer(@Path("network") String network);

    @GET("https://api.blockcypher.com/v1/btc/{network}")
    LiveData<ApiResponse<BtcFeesForBlockCypher>> getFeesForBtcByBlockCypher(@Path("network") String network);


    /*****TransactionReceipt Api***/
    @GET("https://" + URI_BLOCKCYPHER + "/v1/btc/{network}/txs/{TXID}")
    LiveData<ApiResponse<BtcConfirmedForBlockCypher>> isConfirmedTxForBitCoinByBlockCypher(@Path("network") String network, @Path("TXID") String TxId, @Query("tokens") String token);

    @GET("https://{network}" + URI_BLOCKEXPLORER + "/api/tx/{TXID}")
    LiveData<ApiResponse<BtcConfirmedForBlockExplorer>> isConfirmedTxForBitCoinByBlockExplorer(@Path("network") String network, @Path("TXID") String TxId);

    /*****SendRawTransaction Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://" + URI_BLOCKCYPHER + "/v1/btc/{network}/txs/push")
    LiveData<ApiResponse<BtcSendRawTransactionForBlockCypher>> sendRawTransactionForBitCoinByBlockCypher(@Path("network") String network, @Body BtcRequestSendRawTransaction hex, @Query("tokens") String token);

    @FormUrlEncoded
    @POST("https://{network}" + URI_BLOCKEXPLORER + "/api/tx/send")
    LiveData<ApiResponse<BtcSendRawTransactionForBlockExplorer>> sendRawTransactionForBitCoinByBlockExplorer(@Path("network") String network, @Field("rawtx") String hex);

    /*****UnSpend Api***/
    @GET("https://{network}" + URI_BLOCKCHAIN + "/unspent")
    LiveData<ApiResponse<BtcUtxoForBlockChain>> getUTXOForBitCoinByBlockChain(@Path("network") String network, @Query("active") String address);

    @GET("https://" + URI_BLOCKCYPHER + "/v1/btc/{network}/addrs/{address}?unspentOnly=true&includeScript=true")
    LiveData<ApiResponse<BtcUtxoForBlockCypher>> getUTXOForBitCoinByBlockCypher(@Path("network") String network, @Path("address") String address, @Query("tokens") String token);

    @GET("https://{network}" + URI_BLOCKEXPLORER + "/api/addrs/{address}/utxo")
    LiveData<ApiResponse<List<BtcUtxoForBlockExplorer>>> getUTXOForBitCoinByBlockExplorer(@Path("network") String network, @Path("address") String address);

    /*****Transaction List Api***/
    @GET("https://{network}" + URI_BLOCKCHAIN + "/rawaddr/{address}")
    LiveData<ApiResponse<BtcTransactionListForBlockChain>> getTransactionListByBlockChain(@Path("network") String network, @Path("address") String address);

    @GET("https://{network}" + URI_BLOCKEXPLORER + "/api/addrs/{address}/txs/?from=0&to=50")
    LiveData<ApiResponse<BtcTransactionListForBlockExplorer>> getTransactionListByBlockExplorer(@Path("network") String network, @Path("address") String address);

    @GET("https://" + URI_BLOCKCYPHER + "/v1/btc/{network}/addrs/{address}")
    LiveData<ApiResponse<BtcTransactionListForBlockCypher>> getTransactionListByBlockCypher(@Path("network") String network, @Path("address") String address);

    /*****spend Transaction count Api***/
    @GET("https://{network}" + URI_BLOCKCHAIN + "/rawaddr/{address}?filter=1")
    LiveData<ApiResponse<BtcTransactionListForBlockChain>> getSpendTransactionCountByBlockChain(@Path("network") String network, @Path("address") String address);

    @GET("https://chain.so/api/v2/get_tx_spent/{network}/{address}")
    LiveData<ApiResponse<SpendTxForChainSo>> getSpendTransactionCountByChainSo(@Path("network") String network, @Path("address") String address);


    /**********************************ETH****************************************/
    String URI_INFURA = "infura.io/v3/11bf57adbfc943228151de57fc441b8b";
    String URI_ETHERSCAN = "etherscan.io";

    /*****Balance List Api***/
    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=account&action=balancemulti&tag=latest")
    LiveData<ApiResponse<EthBalanceListForEtherScan>> getBalanceListForEthByEtherScan(@Path("network") String network, @Query("address") String address, @Query("apikey") String apiKey);

    /*****Balance Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthBalanceForInfura>> getBalanceForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=account&action=balance")
    LiveData<ApiResponse<EthBalanceForEtherScan>> getBalanceForEthByEtherScan(@Path("network") String network, @Query("address") String address, @Query("apikey") String apiKey);

    /*****Nonce Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthNonceForInfura>> getNonceForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_getTransactionCount&tag=latest")
    LiveData<ApiResponse<EthNonceForEtherScan>> getNonceForEthByEtherScan(@Path("network") String network, @Query("address") String address, @Query("apikey") String apiKey);

    /*****EstimateGas Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthEstimateGasForInfura>> estimateGasForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_estimateGas")
    LiveData<ApiResponse<EthEstimateGasForEtherScan>> getGasLimitForEthByEtherScan(@Path("network") String network, @Query("to") String to, @Query("from") String from, @Query("value") String value, @Query("gasPrice") String gasPrice, @Query("data") String data, @Query("apikey") String apiKey);

    /*****TransactionReceipt Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthConfirmedForInfua>> isConfirmedTxForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_getTransactionReceipt")
    LiveData<ApiResponse<EthConfirmedForEtherScan>> isConfirmedTxForEthByEtherScan(@Path("network") String network, @Query("txhash") String TxId, @Query("apikey") String apiKey);

    /*****SendRawTransaction Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthSendRawTransactionForInfura>> sendRawTransactionForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @FormUrlEncoded
    @POST("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_sendRawTransaction")
    LiveData<ApiResponse<EthSendTxForEtherScan>> sendRawTransactionForEthByEtherScan(@Path("network") String network, @Field("hex") String hex, @Query("apikey") String apiKey);

    /*****GasPrice Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EthGasPriceForInfura>> getGasPriceForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_gasPrice")
    LiveData<ApiResponse<EthGasPriceForEtherScan>> getGasPriceForEthByEtherScan(@Path("network") String network, @Query("apikey") String apiKey);


    @GET("https://www.etherchain.org/api/gasPriceOracle")
    LiveData<ApiResponse<EthGasPriceEntity>> getGasPriceForEthByEtherChain();

    /*****Call Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{network}." + URI_INFURA)
    LiveData<ApiResponse<EntCallEntity>> callForEthByInfura(@Path("network") String network, @Body EthRequestForInfura request, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=proxy&action=eth_call&tag=latest")
    LiveData<ApiResponse<EntCallEntity>> callForEthByEtherScan(@Path("network") String network, @Query("to") String contractAddress, @Query("data") String data, @Query("apikey") String apiKey);

    /*****Transaction List Api***/
    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=account&action=txlist&page=1&offset=50&sort=desc")
    LiveData<ApiResponse<EthTransactionListForEtherScan>> getTransactionListByEtherScan(@Path("network") String network, @Query("address") String address, @Query("token") String apiKey);

    @GET("https://{network}." + URI_ETHERSCAN + "/api?module=account&action=tokentx&page=1&offset=50&sort=desc")
    LiveData<ApiResponse<EthTransactionListForEtherScan>> getTokenTransactionListByEtherScan(@Path("network") String network, @Query("contractaddress") String contractAddress, @Query("address") String address, @Query("token") String apiKey);

    /**********************************Bitcoin Cash****************************************/
    String URI_BLOCKDOZER = "blockdozer.com/insight-api";
    String URI_BITPAY = "bch-insight.bitpay.com/api";

    /*****Balance Api***/
    @GET("https://{network}" + URI_BLOCKDOZER + "/addr/{address}")
    LiveData<ApiResponse<BchBalanceForBlockdozer>> getBalanceForBchByBlockZoder(@Path("network") String network, @Path("address") String address);

    @GET("https://{network}" + URI_BITPAY + "/addr/{address}")
    LiveData<ApiResponse<BchBalanceForBlockdozer>> getBalanceForBchByBitpay(@Path("network") String network, @Path("address") String address);

    /*****TransactionReceipt Api***/
    @GET("https://{network}" + URI_BLOCKDOZER + "/tx/{txid}")
    LiveData<ApiResponse<BchConfirmedForBlockdozer>> isConfirmedTxForBchByBlockdozer(@Path("network") String network, @Path("txid") String TxId);

    @GET("https://{network}" + URI_BITPAY + "/tx/{txid}")
    LiveData<ApiResponse<BchConfirmedForBlockdozer>> isConfirmedTxForBchByBitpay(@Path("network") String network, @Path("txid") String TxId);

    /*****Fee Api***/
    @GET("https://{network}" + URI_BLOCKDOZER + "/utils/estimatefee")
    LiveData<ApiResponse<Map<String, String>>> getFeesForBchByBlockdozer(@Path("network") String network);

    @GET("https://{network}" + URI_BITPAY + "/utils/estimatefee")
    LiveData<ApiResponse<Map<String, String>>> getFeesForBchByBitpay(@Path("network") String network);

    /*****SendRawTransaction Api***/
    @FormUrlEncoded
    @POST("https://{network}" + URI_BLOCKDOZER + "/tx/send")
    LiveData<ApiResponse<BchSendRawTransactionForBlockdozer>> sendRawTransactionForBchByBlockdozer(@Path("network") String network, @Field("rawtx") String hex);

    @FormUrlEncoded
    @POST("https://{network}" + URI_BITPAY + "/tx/send")
    LiveData<ApiResponse<BchSendRawTransactionForBlockdozer>> sendRawTransactionForBchByBitpay(@Path("network") String network, @Field("rawtx") String hex);

    /*****Transaction List Api***/
    @GET("https://{network}" + URI_BLOCKDOZER + "/txs")
    LiveData<ApiResponse<BchTransactionListForBlockdozer>> getTransactionListForBchByBlockdozer(@Path("network") String network, @Query("address") String address);

    @GET("https://{network}" + URI_BITPAY + "/txs")
    LiveData<ApiResponse<BchTransactionListForBlockdozer>> getTransactionListForBchByBitpay(@Path("network") String network, @Query("address") String address);

    /*****UnSpend Api***/
    @GET("https://{network}" + URI_BLOCKDOZER + "/addr/{addr}/utxo")
    LiveData<ApiResponse<List<BchUtxoForBlockdozer>>> getUTXOForBchByBlockdozer(@Path("network") String network, @Path("addr") String address);

    @GET("https://{network}" + URI_BITPAY + "/addr/{addr}/utxo")
    LiveData<ApiResponse<List<BchUtxoForBlockdozer>>> getUTXOForBchByBitpay(@Path("network") String network, @Path("addr") String address);

    /**********************************Ripple****************************************/
    String URI_RIPPLE = "s2.ripple.com";
    String URI_RIPPLE_TEST = "s.altnet.rippletest.net";

    /*****Balance Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{url}:51234")
    LiveData<ApiResponse<XRPBalance>> getBalanceForRipple(@Path("url") String url, @Body XRPRequest<XRPBalanceParams> requests);

    /*****TransactionReceipt Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{url}:51234")
    LiveData<ApiResponse<XRPIsConfirmed>> isConfirmedForRipple(@Path("url") String url, @Body XRPRequest<XRPTxParams> requests);

    /*****Fee Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{url}:51234")
    LiveData<ApiResponse<XRPFee>> getFeesForRipple(@Path("url") String url, @Body XRPRequest<Object> requests);

    /*****SendRawTransaction Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{url}:51234")
    LiveData<ApiResponse<XRPSendRawTransaction>> sendRawTransactionForRipple(@Path("url") String url, @Body XRPRequest<XRPSendRawTxParams> requests);

    /*****Transaction List Api***/
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("https://{url}:51234")
    LiveData<ApiResponse<XRPTransactionList>> getTransactionListForRipple(@Path("url") String url, @Body XRPRequest<XRPTransactionListParams> requests);

    /**********************************ENOTES****************************************/

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("v1/get_address_balance")
    LiveData<ApiResponse<List<ResponseEntity<EntBalanceEntity>>>> getBalanceListByES(@Body List<EntBalanceListRequest> requests);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("v1/get_transaction_receipt")
    LiveData<ApiResponse<List<ResponseEntity<EntConfirmedEntity>>>> getConfirmedListByES(@Body List<EntConfirmedListRequest> requests);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("v1/send_raw_transaction")
    LiveData<ApiResponse<List<ResponseEntity<EntSendTxEntity>>>> sendRawTransactionByES(@Body List<EntSendTxListRequest> requests);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("v1/subscribe_notification")
    LiveData<ApiResponse<List<ResponseEntity<EntNotificationEntity>>>> subscribeNotificationByES(@Body List<EntNotificationListRequest> requests);

    @GET("v1/get_address_utxos/bitcoin/{network}/")
    LiveData<ApiResponse<ResponseEntity<List<EntUtxoEntity>>>> getUtxoListByES(@Path("network") String network, @Query("address") String address);

    @GET("v1/estimate_fee/bitcoin/{network}/?blocks=3")
    LiveData<ApiResponse<ResponseEntity<EntFeesEntity>>> getFeeByES(@Path("network") String network);

    @GET("v1/get_address_nonce/ethereum/{network}/")
    LiveData<ApiResponse<ResponseEntity<EntNonceEntity>>> getNonceByES(@Path("network") String network, @Query("address") String address);

    @GET("v1/estimate_gas/ethereum/{network}/")
    LiveData<ApiResponse<ResponseEntity<EntGasEntity>>> getEstimateGasByES(@Path("network") String network, @Query("to") String toAddress, @Query("from") String from, @Query("value") String value, @Query("data") String data);

    @GET("v1/get_gas_price/ethereum/{network}")
    LiveData<ApiResponse<ResponseEntity<EntGasPriceEntity>>> getGasPriceByES(@Path("network") String network);

    @GET("v1/eth_call/ethereum/{network}/")
    LiveData<ApiResponse<ResponseEntity<EntCallEntity>>> callByES(@Path("network") String network, @Query("to") String contractAddress, @Query("data") String data);

    @GET("v1/version/check/")
    LiveData<ApiResponse<EntVersionEntity>> updateVersion();

    @GET("v1/get_fee/ripple/{network}")
    LiveData<ApiResponse<ResponseEntity<EntFeesEntity>>> getXrpFeeByES(@Path("network") String network);

    @GET("v1/get_address_info/ripple/{network}/")
    LiveData<ApiResponse<ResponseEntity<EntBalanceEntity>>> getXrpBalanceByES(@Path("network") String network, @Query("address") String address);
}
