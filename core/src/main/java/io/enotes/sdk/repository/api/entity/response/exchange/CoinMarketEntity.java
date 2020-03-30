package io.enotes.sdk.repository.api.entity.response.exchange;

public class CoinMarketEntity {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data{
        private Symbol BCH;
        private Symbol BTC;
        private Symbol XRP;
        private Symbol ETH;
        private Symbol USDT;
        private Symbol GUSD;

        public Symbol getBCH() {
            return BCH;
        }

        public void setBCH(Symbol BCH) {
            this.BCH = BCH;
        }

        public Symbol getBTC() {
            return BTC;
        }

        public void setBTC(Symbol BTC) {
            this.BTC = BTC;
        }

        public Symbol getXRP() {
            return XRP;
        }

        public void setXRP(Symbol XRP) {
            this.XRP = XRP;
        }

        public Symbol getETH() {
            return ETH;
        }

        public void setETH(Symbol ETH) {
            this.ETH = ETH;
        }

        public Symbol getUSDT() {
            return USDT;
        }

        public void setUSDT(Symbol USDT) {
            this.USDT = USDT;
        }

        public Symbol getGUSD() {
            return GUSD;
        }

        public void setGUSD(Symbol GUSD) {
            this.GUSD = GUSD;
        }
    }

    public static class Symbol{
        private String id;
        private String name;
        private String symbol;
        private Quote quote;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Quote getQuote() {
            return quote;
        }

        public void setQuote(Quote quote) {
            this.quote = quote;
        }
    }
    public static class Quote{
        private USD USD;

        public CoinMarketEntity.USD getUSD() {
            return USD;
        }

        public void setUSD(CoinMarketEntity.USD USD) {
            this.USD = USD;
        }
    }

    public static class USD{
        private String price;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
