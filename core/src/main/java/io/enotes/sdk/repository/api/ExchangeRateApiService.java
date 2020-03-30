package io.enotes.sdk.repository.api;

import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import io.enotes.sdk.repository.api.entity.response.exchange.BitzEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.BitzUSDEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.CoinMarketEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.CoinbaseEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.CryptoCompareEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.OkexBTCUSDEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.OkexGUSDBTCEntity;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ExchangeRateApiService {
    String URI_CRYPTOCOMPARE = "min-api.cryptocompare.com";
    String URI_BITZ = "apiv2.bitz.com";
    String URI_COINBASE = "api.coinbase.com";
    String URI_OKEX = "www.okex.com";
    String URI_COINMARKET = "pro-api.coinmarketcap.com";

    @GET("https://" + URI_CRYPTOCOMPARE + "/data/pricemulti?tsyms=USD,CNY,EUR,JPY,BTC,ETH,USDT,BCH,XRP,GUSD")
    LiveData<ApiResponse<Map<String, CryptoCompareEntity>>> getExchangeRateForCryptocompare(@Query("fsyms") String fsyms);

    @GET("https://" + URI_CRYPTOCOMPARE + "/data/pricemulti?fsyms=USD&tsyms=CNY,EUR,JPY")
    LiveData<ApiResponse<Map<String, CryptoCompareEntity>>> getExchangeRateUSDForCryptocompare();

    @GET("https://" + URI_BITZ + "/Market/currencyRate?symbols=usd_cny,usd_eur,usd_jpy")
    LiveData<ApiResponse<BitzUSDEntity>> getExchangeRateUSDForBitz();

    @GET("https://" + URI_BITZ + "/Market/currencyCoinRate")
    LiveData<ApiResponse<BitzEntity>> getExchangeRateForBitz(@Query("coins") String coins);

    @GET("https://" + URI_COINBASE + "/v2/exchange-rates")
    LiveData<ApiResponse<CoinbaseEntity>> getExchangeRateForCoinbase(@Query("currency") String coin);

    @GET("https://" + URI_OKEX + "/api/spot/v3/instruments/ticker")
    LiveData<ApiResponse<List<OkexGUSDBTCEntity>>> getExchangeRateAllForOkex();

    @GET("https://" + URI_OKEX + "/api/v1/future_index.do")
    LiveData<ApiResponse<OkexBTCUSDEntity>> getExchangeRateOkex(@Query("symbol") String symbol);

    @Headers({"X-CMC_PRO_API_KEY:9080d4c4-1f32-442d-a5da-2e191fb427b4"})
    @GET("https://" + URI_COINMARKET + "/v1/cryptocurrency/quotes/latest?symbol=BTC,ETH,XRP,BCH,USDT,GUSD&convert=USD")
    LiveData<ApiResponse<CoinMarketEntity>> getExchangeRateCoinMarket();
}
