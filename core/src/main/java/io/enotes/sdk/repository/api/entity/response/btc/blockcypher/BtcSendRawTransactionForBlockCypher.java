package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;

public class BtcSendRawTransactionForBlockCypher implements BaseThirdEntity {
    private Tx tx;

    public Tx getTx() {
        return tx;
    }

    public void setTx(Tx tx) {
        this.tx = tx;
    }

    @Override
    public EntSendTxEntity parseToENotesEntity() {
        if (tx == null) return null;
        EntSendTxEntity entSendTxEntity = new EntSendTxEntity();
        entSendTxEntity.setTxid(tx.getHash());
        return entSendTxEntity;
    }

    public static class Tx {
        private String hash;
        private int block_height;
        private int block_index;
        private int total;
        private int size;

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public int getBlock_height() {
            return block_height;
        }

        public void setBlock_height(int block_height) {
            this.block_height = block_height;
        }

        public int getBlock_index() {
            return block_index;
        }

        public void setBlock_index(int block_index) {
            this.block_index = block_index;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
