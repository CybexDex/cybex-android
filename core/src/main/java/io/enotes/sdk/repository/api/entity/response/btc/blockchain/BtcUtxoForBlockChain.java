package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

import java.util.List;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntUtxoEntity;
import io.enotes.sdk.utils.Utils;

public class BtcUtxoForBlockChain {


    private List<UnspentOutputsBean> unspent_outputs;


    public List<UnspentOutputsBean> getUnspent_outputs() {
        return unspent_outputs;
    }

    public void setUnspent_outputs(List<UnspentOutputsBean> unspent_outputs) {
        this.unspent_outputs = unspent_outputs;
    }

    public static class UnspentOutputsBean implements BaseThirdEntity {
        /**
         * tx_age : 1322659106
         * tx_hash : e6452a2cb71aa864aaa959e647e7a4726a22e640560f199f79b56b5502114c37
         * tx_index : 12790219
         * tx_output_n : 0
         * script : 76a914641ad5051edd97029a003fe9efb29359fcee409d88ac
         * value : 5000661330
         */

        private String tx_age;
        private String tx_hash;
        private String tx_hash_big_endian;
        private String tx_index;
        private String tx_output_n;
        private String script;
        private String value;
        private int confirmations;

        @Override
        public EntUtxoEntity parseToENotesEntity() {
            EntUtxoEntity entUtxoEntity = new EntUtxoEntity();
            entUtxoEntity.setBalance(Utils.intToHexString(value));
            entUtxoEntity.setIndex(Utils.intToHexString(tx_output_n + ""));
            entUtxoEntity.setScript(script);
            entUtxoEntity.setTxid(tx_hash_big_endian);
            entUtxoEntity.setComfirmed(confirmations>0);
            entUtxoEntity.setPositive(true);
            return entUtxoEntity;
        }

        public String getTx_age() {
            return tx_age;
        }

        public void setTx_age(String tx_age) {
            this.tx_age = tx_age;
        }

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }

        public String getTx_index() {
            return tx_index;
        }

        public void setTx_index(String tx_index) {
            this.tx_index = tx_index;
        }

        public String getTx_output_n() {
            return tx_output_n;
        }

        public void setTx_output_n(String tx_output_n) {
            this.tx_output_n = tx_output_n;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getTx_hash_big_endian() {
            return tx_hash_big_endian;
        }

        public void setTx_hash_big_endian(String tx_hash_big_endian) {
            this.tx_hash_big_endian = tx_hash_big_endian;
        }

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }
    }
}
