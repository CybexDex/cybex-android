package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import java.util.List;

public class BtcTransactionListForBlockCypher {
    private String address;
    private int n_tx;
    private List<Tx> txrefs;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getN_tx() {
        return n_tx;
    }

    public void setN_tx(int n_tx) {
        this.n_tx = n_tx;
    }

    public List<Tx> getTxrefs() {
        return txrefs;
    }

    public void setTxrefs(List<Tx> txrefs) {
        this.txrefs = txrefs;
    }

    public static class Tx{
        private String tx_hash;
        private String value;
        private String confirmed;
        private int confirmations;
        private String spent;

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(String confirmed) {
            this.confirmed = confirmed;
        }

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public String getSpent() {
            return spent;
        }

        public void setSpent(String spent) {
            this.spent = spent;
        }
    }
}
