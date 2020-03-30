package io.enotes.sdk.repository.api.entity.response.bch.bitpay;

import java.util.List;

public class BchTransactionListForBitpay {
    private List<Tx> txs;

    public List<Tx> getTxs() {
        return txs;
    }

    public void setTxs(List<Tx> txs) {
        this.txs = txs;
    }

    public static class Tx {
        private String txid;
        private String blockhash;
        private long time;
        private int confirmations;
        private String valueOut;
        private String valueIn;
        private String fees;
        private List<Input> vin;
        private List<Out> vout;

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public String getBlockhash() {
            return blockhash;
        }

        public void setBlockhash(String blockhash) {
            this.blockhash = blockhash;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public String getValueOut() {
            return valueOut;
        }

        public void setValueOut(String valueOut) {
            this.valueOut = valueOut;
        }

        public String getValueIn() {
            return valueIn;
        }

        public void setValueIn(String valueIn) {
            this.valueIn = valueIn;
        }

        public String getFees() {
            return fees;
        }

        public void setFees(String fees) {
            this.fees = fees;
        }

        public List<Input> getVin() {
            return vin;
        }

        public void setVin(List<Input> vin) {
            this.vin = vin;
        }

        public List<Out> getVout() {
            return vout;
        }

        public void setVout(List<Out> vout) {
            this.vout = vout;
        }
    }

    public static class Input {
        private String txid;
        private String sequence;
        private String addr;
        private String value;

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Out {
        private String value;
        private String[] addresses;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String[] getAddresses() {
            return addresses;
        }

        public void setAddresses(String[] addresses) {
            this.addresses = addresses;
        }
    }
}
