package io.enotes.sdk.repository.api.entity.response.btc.chainso;

import java.util.List;

public class SpendTxForChainSo {
    private String status;
    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        private String network;
        private String address;
        private List<Tx> txs;

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network) {
            this.network = network;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public List<Tx> getTxs() {
            return txs;
        }

        public void setTxs(List<Tx> txs) {
            this.txs = txs;
        }
    }

    public static class Tx {
        private String txid;
        private String value;
        private String time;

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
