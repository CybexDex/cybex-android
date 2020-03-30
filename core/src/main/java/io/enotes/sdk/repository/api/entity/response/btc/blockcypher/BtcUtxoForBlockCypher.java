package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import java.util.List;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.utils.Utils;

public class BtcUtxoForBlockCypher {
    private String address;
    private List<BtcUtxo> txrefs;
    private List<BtcUtxo> unconfirmed_txrefs;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<BtcUtxo> getTxrefs() {
        return txrefs;
    }

    public void setTxrefs(List<BtcUtxo> txrefs) {
        this.txrefs = txrefs;
    }

    public List<BtcUtxo> getUnconfirmed_txrefs() {
        return unconfirmed_txrefs;
    }

    public void setUnconfirmed_txrefs(List<BtcUtxo> unconfirmed_txrefs) {
        this.unconfirmed_txrefs = unconfirmed_txrefs;
    }

    public static class BtcUtxo implements BaseThirdEntity {
        private String tx_hash;
        private int block_height;
        private int tx_input_n;
        private int tx_output_n;
        private int value;
        private int confirmations;
        private boolean spent;
        private String script;

        @Override
        public EntUtxoEntity parseToENotesEntity() {
            EntUtxoEntity entUtxoEntity = new EntUtxoEntity();
            entUtxoEntity.setBalance(Utils.intToHexString(value + ""));
            entUtxoEntity.setIndex(Utils.intToHexString(tx_output_n + ""));
            entUtxoEntity.setScript(script);
            entUtxoEntity.setTxid(tx_hash);
            entUtxoEntity.setComfirmed(confirmations>0);
            entUtxoEntity.setPositive(true);
            return entUtxoEntity;
        }

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }

        public int getBlock_height() {
            return block_height;
        }

        public void setBlock_height(int block_height) {
            this.block_height = block_height;
        }

        public int getTx_input_n() {
            return tx_input_n;
        }

        public void setTx_input_n(int tx_input_n) {
            this.tx_input_n = tx_input_n;
        }

        public int getTx_output_n() {
            return tx_output_n;
        }

        public void setTx_output_n(int tx_output_n) {
            this.tx_output_n = tx_output_n;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public boolean isSpent() {
            return spent;
        }

        public void setSpent(boolean spent) {
            this.spent = spent;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }
    }
}
