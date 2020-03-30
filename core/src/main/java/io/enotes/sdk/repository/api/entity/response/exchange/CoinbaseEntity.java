package io.enotes.sdk.repository.api.entity.response.exchange;

import io.enotes.sdk.repository.api.entity.BaseENotesEntity;
import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntExchangeRateEntity;

public class CoinbaseEntity implements BaseThirdEntity{
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public EntExchangeRateEntity parseToENotesEntity() {
        EntExchangeRateEntity rateEntity = new EntExchangeRateEntity();
        rateEntity.setDigiccy(data.currency.toUpperCase());
        rateEntity.setExchange("coinbase");
        EntExchangeRateEntity.Data exData = new EntExchangeRateEntity.Data();
        exData.setBtc(data.rates.BTC);
        exData.setEth(data.rates.ETH);
        exData.setUsd(data.rates.USD);
        exData.setEur(data.rates.EUR);
        exData.setCny(data.rates.CNY);
        exData.setJpy(data.rates.JPY);
        rateEntity.setData(exData);
        return rateEntity;
    }

    public static class Data{
        private String currency;
        private Rate rates;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public Rate getRates() {
            return rates;
        }

        public void setRates(Rate rates) {
            this.rates = rates;
        }
    }

    public static class Rate{
        private String BTC;
        private String ETH;
        private String USD;
        private String EUR;
        private String CNY;
        private String JPY;

        public String getBTC() {
            return BTC;
        }

        public void setBTC(String BTC) {
            this.BTC = BTC;
        }

        public String getETH() {
            return ETH;
        }

        public void setETH(String ETH) {
            this.ETH = ETH;
        }

        public String getUSD() {
            return USD;
        }

        public void setUSD(String USD) {
            this.USD = USD;
        }

        public String getEUR() {
            return EUR;
        }

        public void setEUR(String EUR) {
            this.EUR = EUR;
        }

        public String getCNY() {
            return CNY;
        }

        public void setCNY(String CNY) {
            this.CNY = CNY;
        }

        public String getJPY() {
            return JPY;
        }

        public void setJPY(String JPY) {
            this.JPY = JPY;
        }
    }
}
