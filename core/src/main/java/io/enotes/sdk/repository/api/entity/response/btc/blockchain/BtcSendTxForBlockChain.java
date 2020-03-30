package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

public class BtcSendTxForBlockChain {

    /**
     * status : success
     * data : {"network":"BTCTEST","txid":"a0b91c2daffb3e817258dc5bd93e36b2fa700e38edd36d603ce64df4af21a542"}
     */

    private String status;
    private DataBean data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * network : BTCTEST
         * txid : a0b91c2daffb3e817258dc5bd93e36b2fa700e38edd36d603ce64df4af21a542
         */

        private String network;
        private String txid;

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network) {
            this.network = network;
        }

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }
    }
}
