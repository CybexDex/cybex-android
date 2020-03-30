package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class EthConfirmedForInfua extends BaseEthEntityForInfura implements BaseThirdEntity {
    private TransactionReceipt result;

    public TransactionReceipt getResult() {
        return result;
    }

    public void setResult(TransactionReceipt result) {
        this.result = result;
    }

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity confirmedEntity=new EntConfirmedEntity();
        if(result==null) {
            confirmedEntity.setConfirmations("0x0");
        }else{
            confirmedEntity.setTxid(result.getTransactionHash());
            confirmedEntity.setConfirmations(result.getStatus());
        }
        return confirmedEntity;
    }

    public static class TransactionReceipt{
        private String transactionHash;
        private String transactionIndex;
        private String blockNumber;
        private String status;

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
