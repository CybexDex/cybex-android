package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class EthConfirmedForEtherScan extends BaseEthEntity {


    /**
     * jsonrpc : 2.0
     * id : 1
     * result : {"blockHash":"0xf64a12502afc36db3d29931a2148e5d6ddaa883a2a3c968ca2fb293fa9258c68","blockNumber":"0x70839","contractAddress":null,"cumulativeGasUsed":"0x75d5","from":"0xc80fb22930b303b55df9b89901889126400add38","gasUsed":"0x75d5","logs":[{"address":"0x03fca6077d38dd99d0ce14ba32078bd2cda72d74","topics":["0x24bcf19562365f6510754002f8d7b818d275886315d29c7aa04785570b97a363"],"data":"0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000a4861636b65726e65777300000000000000000000000000000000000000000000","blockNumber":"0x70839","transactionHash":"0x1e2910a262b1008d0616a0beb24c1a491d78771baa54a33e66065e03b1f46bc1","transactionIndex":"0x0","blockHash":"0xf64a12502afc36db3d29931a2148e5d6ddaa883a2a3c968ca2fb293fa9258c68","logIndex":"0x0","removed":false}],"logsBloom":"0x00000000000000000000000000000400000000020000000000000000400000000000000000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000","root":"0xc659845f1ac4e899ff1b0666dbac5deeda33a4a5d85da71f617f352824146e40","to":"0x03fca6077d38dd99d0ce14ba32078bd2cda72d74","transactionHash":"0x1e2910a262b1008d0616a0beb24c1a491d78771baa54a33e66065e03b1f46bc1","transactionIndex":"0x0"}
     */

    private String jsonrpc;
    private int id;
    private ResultBean result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity=new EntConfirmedEntity();
        if (result!=null){
            entConfirmedEntity.setTxid(result.getTransactionHash());
            entConfirmedEntity.setConfirmations(result.getBlockNumber());
        }
        return entConfirmedEntity;
    }

    public static class ResultBean {
        /**
         * blockHash : 0xf64a12502afc36db3d29931a2148e5d6ddaa883a2a3c968ca2fb293fa9258c68
         * blockNumber : 0x70839
         * contractAddress : null
         * cumulativeGasUsed : 0x75d5
         * from : 0xc80fb22930b303b55df9b89901889126400add38
         * gasUsed : 0x75d5
         * logs : [{"address":"0x03fca6077d38dd99d0ce14ba32078bd2cda72d74","topics":["0x24bcf19562365f6510754002f8d7b818d275886315d29c7aa04785570b97a363"],"data":"0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000a4861636b65726e65777300000000000000000000000000000000000000000000","blockNumber":"0x70839","transactionHash":"0x1e2910a262b1008d0616a0beb24c1a491d78771baa54a33e66065e03b1f46bc1","transactionIndex":"0x0","blockHash":"0xf64a12502afc36db3d29931a2148e5d6ddaa883a2a3c968ca2fb293fa9258c68","logIndex":"0x0","removed":false}]
         * logsBloom : 0x00000000000000000000000000000400000000020000000000000000400000000000000000004000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000020000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
         * root : 0xc659845f1ac4e899ff1b0666dbac5deeda33a4a5d85da71f617f352824146e40
         * to : 0x03fca6077d38dd99d0ce14ba32078bd2cda72d74
         * transactionHash : 0x1e2910a262b1008d0616a0beb24c1a491d78771baa54a33e66065e03b1f46bc1
         * transactionIndex : 0x0
         */

        private String blockHash;
        private String blockNumber;
        private Object contractAddress;
        private String cumulativeGasUsed;
        private String from;
        private String gasUsed;
        private String logsBloom;
        private String root;
        private String to;
        private String transactionHash;
        private String transactionIndex;

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
        }

        public Object getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(Object contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getCumulativeGasUsed() {
            return cumulativeGasUsed;
        }

        public void setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = cumulativeGasUsed;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getGasUsed() {
            return gasUsed;
        }

        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        public String getLogsBloom() {
            return logsBloom;
        }

        public void setLogsBloom(String logsBloom) {
            this.logsBloom = logsBloom;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

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


    }
}
