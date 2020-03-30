package io.enotes.sdk.repository.api.entity;

public class EntExchangeRateEntity extends BaseENotesEntity {
    private String exchange;
    private String digiccy;
    private Data data;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getDigiccy() {
        return digiccy;
    }

    public void setDigiccy(String digiccy) {
        this.digiccy = digiccy;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String btc;
        private String eth;
        private String usdt;
        private String bch;
        private String xrp;
        private String usd;
        private String eur;
        private String cny;
        private String jpy;
        private String gusd;

        public String getGusd() {
            return gusd;
        }

        public void setGusd(String gusd) {
            this.gusd = gusd;
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

        public String getUsdt() {
            return usdt;
        }

        public void setUsdt(String usdt) {
            this.usdt = usdt;
        }

        public String getBch() {
            return bch;
        }

        public void setBch(String bch) {
            this.bch = bch;
        }

        public String getUsd() {
            return usd;
        }

        public void setUsd(String usd) {
            this.usd = usd;
        }

        public String getEur() {
            return eur;
        }

        public void setEur(String eur) {
            this.eur = eur;
        }

        public String getCny() {
            return cny;
        }

        public void setCny(String cny) {
            this.cny = cny;
        }

        public String getJpy() {
            return jpy;
        }

        public void setJpy(String jpy) {
            this.jpy = jpy;
        }

        public String getXrp() {
            return xrp;
        }

        public void setXrp(String xrp) {
            this.xrp = xrp;
        }
    }

    @Override
    public String toString() {
        return "exchange = " + exchange + "\ndigiccy = " + digiccy + "\nrate = "+ " # btc : " + data.btc+ " # eth : " + data.eth + " # bch : " + data.bch+ " # gusd : " + data.gusd+ " # usdt : " + data.usdt   +" # xrp : " + data.xrp   + " # usd : " + data.usd + " # eur : " + data.eur + " # cny : " + data.cny + " # jpy : " + data.jpy;
    }
}
