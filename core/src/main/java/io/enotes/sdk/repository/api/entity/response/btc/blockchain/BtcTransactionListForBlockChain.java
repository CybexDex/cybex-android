package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

import java.util.List;

public class BtcTransactionListForBlockChain {
    private String address;
    private int n_tx;
    private List<Tx> txs;

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

    public List<Tx> getTxs() {
        return txs;
    }

    public void setTxs(List<Tx> txs) {
        this.txs = txs;
    }

    public static class Tx {
        private long time;
        private String hash;
        private int ver;
        private List<Input> inputs;
        private List<Out> out;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public int getVer() {
            return ver;
        }

        public void setVer(int ver) {
            this.ver = ver;
        }

        public List<Input> getInputs() {
            return inputs;
        }

        public void setInputs(List<Input> inputs) {
            this.inputs = inputs;
        }

        public List<Out> getOut() {
            return out;
        }

        public void setOut(List<Out> out) {
            this.out = out;
        }
    }

    public static class Input {
        private Out prev_out;

        public Out getPrev_out() {
            return prev_out;
        }

        public void setPrev_out(Out prev_out) {
            this.prev_out = prev_out;
        }
    }

    public static class Out {
        private boolean spent;
        private int type;
        private String addr;
        private String value;

        public boolean isSpent() {
            return spent;
        }

        public void setSpent(boolean spent) {
            this.spent = spent;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
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
}
