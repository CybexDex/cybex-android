package io.enotes.sdk.repository.api.entity.response.exchange;

import java.util.Map;

public class BitzUSDEntity {
    private int status;
    private String msg;
    private Data data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Symbol usd_cny;
        private Symbol usd_eur;
        private Symbol usd_jpy;

        public Symbol getUsd_cny() {
            return usd_cny;
        }

        public void setUsd_cny(Symbol usd_cny) {
            this.usd_cny = usd_cny;
        }

        public Symbol getUsd_eur() {
            return usd_eur;
        }

        public void setUsd_eur(Symbol usd_eur) {
            this.usd_eur = usd_eur;
        }

        public Symbol getUsd_jpy() {
            return usd_jpy;
        }

        public void setUsd_jpy(Symbol usd_jpy) {
            this.usd_jpy = usd_jpy;
        }
    }

    public static class Symbol {
        private String coin;
        private String currencyCoin;
        private String rate;

        public String getCoin() {
            return coin;
        }

        public void setCoin(String coin) {
            this.coin = coin;
        }

        public String getCurrencyCoin() {
            return currencyCoin;
        }

        public void setCurrencyCoin(String currencyCoin) {
            this.currencyCoin = currencyCoin;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }
    }
}
