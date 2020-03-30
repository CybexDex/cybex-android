package io.enotes.sdk.repository.api.entity.response.exchange;

import io.enotes.sdk.constant.Constant;
import io.enotes.sdk.repository.api.entity.BaseENotesEntity;
import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntExchangeRateEntity;
import io.enotes.sdk.repository.provider.api.ExchangeRateApiProvider;

public class BitzEntity implements BaseThirdEntity {
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

    @Override
    public EntExchangeRateEntity parseToENotesEntity() {
        EntExchangeRateEntity rateEntity = new EntExchangeRateEntity();
        rateEntity.setExchange("bitz");
        Symbol symbol =null;
        if(data.btc!=null){
            rateEntity.setDigiccy(Constant.CardType.BTC);
             symbol = data.btc;
        }else if(data.eth!=null){
            rateEntity.setDigiccy(Constant.CardType.ETH);
            symbol = data.eth;
        }else if(data.usdt!=null){
            rateEntity.setDigiccy(Constant.CardType.USDT);
            symbol = data.usdt;
        }
        EntExchangeRateEntity.Data exData = new EntExchangeRateEntity.Data();
        exData.setBtc(symbol.btc);
        exData.setEth(symbol.eth);
        exData.setUsd(symbol.usd);
        exData.setEur(symbol.eur);
        exData.setCny(symbol.cny);
        exData.setJpy(symbol.jpy);
        exData.setUsdt(symbol.usdt);
        rateEntity.setData(exData);
        return rateEntity;
    }

    public static class Data {
        private Symbol btc;
        private Symbol eth;
        private Symbol usdt;

        public Symbol getBtc() {
            return btc;
        }

        public void setBtc(Symbol btc) {
            this.btc = btc;
        }

        public Symbol getEth() {
            return eth;
        }

        public void setEth(Symbol eth) {
            this.eth = eth;
        }

        public Symbol getUsdt() {
            return usdt;
        }

        public void setUsdt(Symbol usdt) {
            this.usdt = usdt;
        }
    }

    public static class Symbol {
        private String btc;
        private String eth;
        private String usd;
        private String cny;
        private String eur;
        private String jpy;
        private String usdt;

        public String getUsdt() {
            return usdt;
        }

        public void setUsdt(String usdt) {
            this.usdt = usdt;
        }

        public String getBtc() {
            return btc;
        }

        public void setBtc(String btc) {
            this.btc = btc;
        }

        public String getEth() {
            return eth;
        }

        public void setEth(String eth) {
            this.eth = eth;
        }

        public String getUsd() {
            return usd;
        }

        public void setUsd(String usd) {
            this.usd = usd;
        }

        public String getCny() {
            return cny;
        }

        public void setCny(String cny) {
            this.cny = cny;
        }

        public String getEur() {
            return eur;
        }

        public void setEur(String eur) {
            this.eur = eur;
        }

        public String getJpy() {
            return jpy;
        }

        public void setJpy(String jpy) {
            this.jpy = jpy;
        }
    }
}
