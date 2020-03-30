package io.enotes.sdk.repository.provider.api;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.List;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.constant.Status;
import io.enotes.sdk.repository.api.ExchangeRateApiService;
import io.enotes.sdk.repository.api.RetrofitFactory;
import io.enotes.sdk.repository.api.entity.EntExchangeRateEntity;
import io.enotes.sdk.repository.api.entity.EntExchangeRateUSDEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.CoinMarketEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.CryptoCompareEntity;
import io.enotes.sdk.repository.api.entity.response.exchange.OkexGUSDBTCEntity;
import io.enotes.sdk.repository.base.Resource;

import static io.enotes.sdk.constant.ErrorCode.NET_ERROR;

public class ExchangeRateApiProvider extends BaseApiProvider {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Constant.CardType.BTC, Constant.CardType.ETH, Constant.CardType.GUSD, Constant.CardType.USDT, Constant.CardType.OTHER_ERC20})
    public @interface RateMode {
    }

    private ExchangeRateApiService exchangeRateApiService;
    private EntExchangeRateUSDEntity entExchangeRateUSDEntity;

    public ExchangeRateApiProvider(Context context, ExchangeRateApiService exchangeRateApiService) {
        super(context);
        this.exchangeRateApiService = exchangeRateApiService;
    }

    public LiveData<Resource<EntExchangeRateEntity>> getExchangeRate(@RateMode String digiccy) {
        if (digiccy.contains(Constant.CardType.BCH) || digiccy.contains(Constant.CardType.XRP)) {
            return addLiveDataSourceNoENotes(getExchangeRate4ur(digiccy), getExchangeRate5ve(digiccy));
        } else if (digiccy.contains(Constant.CardType.GUSD) || digiccy.contains(Constant.CardType.OTHER_ERC20)) {
            return addLiveDataSourceNoENotes(getExchangeRate4ur(digiccy), getExchangeRate5ve(digiccy), getExchangeRateGUSD1st(digiccy));
        } else if (digiccy.contains(Constant.CardType.USDT)) {
            return addLiveDataSourceNoENotes(getExchangeRate3rd(digiccy), getExchangeRate4ur(digiccy), getExchangeRate5ve(digiccy), getExchangeRate2nd(digiccy));
        } else {
            return addLiveDataSourceNoENotes(getExchangeRate1st(digiccy), getExchangeRate3rd(digiccy), getExchangeRate4ur(digiccy), getExchangeRate5ve(digiccy), getExchangeRate2nd(digiccy));
        }
    }

    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRate1st(String digiccy) {
        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateForCoinbase(digiccy.toUpperCase()), (resource -> {
            if (resource.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(resource.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRate2nd(String digiccy) {

        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateAllForOkex(), (resource0 -> {
            if (resource0.isSuccessful()) {
                mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateOkex("btc_usd"), (resource -> {
                    if (resource.isSuccessful()) {
                        mediatorLiveData.addSource(getExchangeRateUSD(), (resource1 -> {
                            if (resource1.status == Status.SUCCESS) {
                                EntExchangeRateEntity rateEntity = new EntExchangeRateEntity();
                                rateEntity.setDigiccy(digiccy);
                                rateEntity.setExchange("okex");
                                EntExchangeRateEntity.Data exData = new EntExchangeRateEntity.Data();

                                List<OkexGUSDBTCEntity> listAll = resource0.body;
                                String eth2btc = "0";
                                String usdt2btc = "0";
                                for (OkexGUSDBTCEntity gusdbtcEntity : listAll) {
                                    if (gusdbtcEntity.getInstrument_id().equals(OkexGUSDBTCEntity.ETH_BTC)) {
                                        eth2btc = gusdbtcEntity.getLast();
                                    } else if (gusdbtcEntity.getInstrument_id().equals(OkexGUSDBTCEntity.USDT_BTC)) {
                                        usdt2btc = gusdbtcEntity.getLast();
                                    }
                                }
                                String scale = "1";
                                if (digiccy.equals(Constant.CardType.BTC)) {
                                    scale = "1";
                                    exData.setBtc(scale);
                                    exData.setEth(new BigDecimal(scale).divide(new BigDecimal(eth2btc), 10, BigDecimal.ROUND_HALF_UP).toString());
                                    exData.setUsdt(new BigDecimal(scale).divide(new BigDecimal(usdt2btc), 10, BigDecimal.ROUND_HALF_UP).toString());

                                } else if (digiccy.equals(Constant.CardType.ETH)) {
                                    scale = eth2btc;
                                    exData.setBtc(eth2btc);
                                    exData.setEth("1");

                                }
                                String digi2usd = new BigDecimal(scale).multiply(new BigDecimal(resource.body.getFuture_index())).toString();
                                exData.setUsd(digi2usd);
                                exData.setEur(new BigDecimal(digi2usd).multiply(new BigDecimal(resource1.data.getEur())).toString());
                                exData.setCny(new BigDecimal(digi2usd).multiply(new BigDecimal(resource1.data.getCny())).toString());
                                exData.setJpy(new BigDecimal(digi2usd).multiply(new BigDecimal(resource1.data.getJpy())).toString());
                                rateEntity.setData(exData);
                                mediatorLiveData.postValue(Resource.success(rateEntity));
                            } else {
                                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
                            }
                        }));
                    } else {
                        mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
                    }
                }));

            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource0.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRate3rd(String digiccy) {
        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateForBitz(digiccy.toLowerCase()), (resource -> {
            if (resource.isSuccessful()) {
                mediatorLiveData.postValue(Resource.success(resource.body.parseToENotesEntity()));
            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRate4ur(String digiccy) {
        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateForCryptocompare(digiccy.toUpperCase()), (resource -> {
            if (resource.isSuccessful()) {
                EntExchangeRateEntity rateEntity = new EntExchangeRateEntity();
                rateEntity.setExchange("cryptocompare");
                rateEntity.setDigiccy(digiccy.toUpperCase());
                CryptoCompareEntity entity = resource.body.get(digiccy.toUpperCase());
                EntExchangeRateEntity.Data data = new EntExchangeRateEntity.Data();
                data.setBtc(entity.getBTC());
                data.setEth(entity.getETH());
                data.setUsd(entity.getUSD());
                data.setEur(entity.getEUR());
                data.setCny(entity.getCNY());
                data.setJpy(entity.getJPY());
                data.setUsdt(entity.getUSDT());
                data.setBch(entity.getBCH());
                data.setXrp(entity.getXRP());
                data.setGusd(entity.getGUSD());
                rateEntity.setData(data);
                mediatorLiveData.postValue(Resource.success(rateEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }


    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRateGUSD1st(String digiccy) {
        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateAllForOkex(), (resource0 -> {
            if (resource0.isSuccessful()) {
                mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateOkex("btc_usd"), (resource -> {
                    if (resource.isSuccessful()) {
                        mediatorLiveData.addSource(getExchangeRateUSD(), (resource1 -> {
                            if (resource1.status == Status.SUCCESS) {
                                EntExchangeRateEntity rateEntity = new EntExchangeRateEntity();
                                rateEntity.setDigiccy(digiccy);
                                rateEntity.setExchange("okex");
                                EntExchangeRateEntity.Data exData = new EntExchangeRateEntity.Data();

                                List<OkexGUSDBTCEntity> listAll = resource0.body;
                                String gusd2btc = "1";
                                String gusd2eth = "1";
                                String eth2btc = "1";
                                String gusd2usdt = "1";
                                String usdt2btc = "1";

                                for (OkexGUSDBTCEntity gusdbtcEntity : listAll) {
                                    if (gusdbtcEntity.getInstrument_id().equals(OkexGUSDBTCEntity.GUSD_BTC)) {
                                        gusd2btc = gusdbtcEntity.getLast();
                                    }
                                    if (gusdbtcEntity.getInstrument_id().equals(OkexGUSDBTCEntity.ETH_BTC)) {
                                        eth2btc = gusdbtcEntity.getLast();
                                    }
                                    if (gusdbtcEntity.getInstrument_id().equals(OkexGUSDBTCEntity.USDT_BTC)) {
                                        usdt2btc = gusdbtcEntity.getLast();
                                    }
                                }
                                gusd2eth = new BigDecimal(gusd2btc).divide(new BigDecimal(eth2btc), 10, BigDecimal.ROUND_HALF_UP).toString();
                                gusd2usdt = new BigDecimal(gusd2btc).divide(new BigDecimal(usdt2btc), 10, BigDecimal.ROUND_HALF_UP).toString();
                                String gusd2usd = new BigDecimal(gusd2btc).multiply(new BigDecimal(resource.body.getFuture_index())).toString();
                                exData.setUsd(gusd2usd);
                                exData.setBtc(gusd2btc);
                                exData.setEth(gusd2eth);
                                exData.setUsdt(gusd2usdt);
                                exData.setGusd("1");
                                exData.setEur(new BigDecimal(gusd2usd).multiply(new BigDecimal(resource1.data.getEur())).toString());
                                exData.setCny(new BigDecimal(gusd2usd).multiply(new BigDecimal(resource1.data.getCny())).toString());
                                exData.setJpy(new BigDecimal(gusd2usd).multiply(new BigDecimal(resource1.data.getJpy())).toString());
                                rateEntity.setData(exData);
                                mediatorLiveData.postValue(Resource.success(rateEntity));
                            } else {
                                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
                            }
                        }));
                    } else {
                        mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
                    }
                }));

            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource0.errorMessage));
            }
        }));
        return mediatorLiveData;
    }

    private LiveData<Resource<EntExchangeRateEntity>> getExchangeRate5ve(String digiccy) {
        MediatorLiveData<Resource<EntExchangeRateEntity>> mediatorLiveData = new MediatorLiveData<>();
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateCoinMarket(), (resource) -> {
            if (resource.isSuccessful()) {
                mediatorLiveData.addSource(getExchangeRateUSD(), (usdResource) -> {
                    if (usdResource.status == Status.SUCCESS) {
                        EntExchangeRateEntity entExchangeRateEntity = new EntExchangeRateEntity();
                        entExchangeRateEntity.setExchange("coinmarketcap");
                        entExchangeRateEntity.setDigiccy(digiccy);
                        EntExchangeRateEntity.Data eData = new EntExchangeRateEntity.Data();
                        CoinMarketEntity.Data data = resource.body.getData();
                        entExchangeRateEntity.setData(eData);
                        if (digiccy.equals(Constant.CardType.XRP)) {
                            eData.setXrp("1");
                            eData.setBch(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsdt(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setEth(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBtc(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setGusd(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        } else if (digiccy.equals(Constant.CardType.BCH)) {
                            eData.setBch("1");
                            eData.setXrp(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsdt(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setEth(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBtc(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setGusd(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        } else if (digiccy.equals(Constant.CardType.USDT)) {
                            eData.setUsdt("1");
                            eData.setXrp(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBch(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setEth(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBtc(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setGusd(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        } else if (digiccy.equals(Constant.CardType.GUSD)) {
                            eData.setGusd("1");
                            eData.setXrp(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsdt(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setEth(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBtc(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBch(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        } else if (digiccy.equals(Constant.CardType.ETH)) {
                            eData.setEth("1");
                            eData.setXrp(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsdt(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setGusd(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBtc(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBch(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        } else if (digiccy.equals(Constant.CardType.BTC)) {
                            eData.setBtc("1");
                            eData.setXrp(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getXRP().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsdt(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getUSDT().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setGusd(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getGUSD().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setEth(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getETH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setBch(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).divide(new BigDecimal(data.getBCH().getQuote().getUSD().getPrice()), 10, BigDecimal.ROUND_HALF_UP).toString());
                            eData.setUsd(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).toString());
                            eData.setEur(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getEur())).toString());
                            eData.setJpy(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getJpy())).toString());
                            eData.setCny(new BigDecimal(data.getBTC().getQuote().getUSD().getPrice()).multiply(new BigDecimal(usdResource.data.getCny())).toString());
                        }
                        mediatorLiveData.postValue(Resource.success(entExchangeRateEntity));
                    } else if (usdResource.status == Status.ERROR) {
                        mediatorLiveData.postValue(Resource.error(NET_ERROR, usdResource.message));
                    }
                });

            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        });
        return mediatorLiveData;
    }


    private LiveData<Resource<EntExchangeRateUSDEntity>> getExchangeRateUSD() {
        return addLiveDataSourceNoENotes(getExchangeRateUSD1st(), getExchangeRateUSD2nd());
    }

    private LiveData<Resource<EntExchangeRateUSDEntity>> getExchangeRateUSD1st() {
        MediatorLiveData<Resource<EntExchangeRateUSDEntity>> mediatorLiveData = new MediatorLiveData<>();
        if (entExchangeRateUSDEntity != null) {
            mediatorLiveData.postValue(Resource.success(entExchangeRateUSDEntity));
        }
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateUSDForCryptocompare(), resource -> {
            if (resource.isSuccessful() && resource.body != null) {
                CryptoCompareEntity entity = resource.body.get("USD");
                EntExchangeRateUSDEntity rateUSDEntity = new EntExchangeRateUSDEntity();
                rateUSDEntity.setEur(entity.getEUR());
                rateUSDEntity.setCny(entity.getCNY());
                rateUSDEntity.setJpy(entity.getJPY());
                entExchangeRateUSDEntity = rateUSDEntity;
                mediatorLiveData.postValue(Resource.success(entExchangeRateUSDEntity));
            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        });
        return mediatorLiveData;
    }

    private LiveData<Resource<EntExchangeRateUSDEntity>> getExchangeRateUSD2nd() {
        MediatorLiveData<Resource<EntExchangeRateUSDEntity>> mediatorLiveData = new MediatorLiveData<>();
        if (entExchangeRateUSDEntity != null) {
            mediatorLiveData.postValue(Resource.success(entExchangeRateUSDEntity));
        }
        mediatorLiveData.addSource(exchangeRateApiService.getExchangeRateUSDForBitz(), (resource -> {
            if (resource.isSuccessful() && resource.body != null && resource.body.getStatus() == 200) {
                EntExchangeRateUSDEntity rateUSDEntity = new EntExchangeRateUSDEntity();
                rateUSDEntity.setCny(resource.body.getData().getUsd_cny().getRate());
                rateUSDEntity.setEur(resource.body.getData().getUsd_eur().getRate());
                rateUSDEntity.setJpy(resource.body.getData().getUsd_jpy().getRate());
                entExchangeRateUSDEntity = rateUSDEntity;
                mediatorLiveData.postValue(Resource.success(entExchangeRateUSDEntity));
            } else if (resource.body != null && resource.body.getStatus() != 200) {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.body.getMsg()));
            } else {
                mediatorLiveData.postValue(Resource.error(NET_ERROR, resource.errorMessage));
            }
        }));
        return mediatorLiveData;
    }
}
