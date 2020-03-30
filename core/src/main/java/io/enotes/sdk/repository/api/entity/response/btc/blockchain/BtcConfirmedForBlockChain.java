package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class BtcConfirmedForBlockChain implements BaseThirdEntity {

    /**
     * status : success
     * data : {"txid":"6f47f0b2e1ec762698a9b62fa23b98881b03d052c9d8cb1d16bb0b04eb3b7c5b","network":"DOGE","confirmations":46340,"is_confirmed":true}
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

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity=new EntConfirmedEntity();
        if(data!=null) {
            entConfirmedEntity.setConfirmations(data.getConfirmations() + "");
            entConfirmedEntity.setTxid(data.getTxid());
        }
        return entConfirmedEntity;
    }

    public static class DataBean {
        /**
         * txid : 6f47f0b2e1ec762698a9b62fa23b98881b03d052c9d8cb1d16bb0b04eb3b7c5b
         * network : DOGE
         * confirmations : 46340
         * is_confirmed : true
         */

        private String txid;
        private String network;
        private int confirmations;
        private boolean is_confirmed;

        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network) {
            this.network = network;
        }

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public boolean isIs_confirmed() {
            return is_confirmed;
        }

        public void setIs_confirmed(boolean is_confirmed) {
            this.is_confirmed = is_confirmed;
        }
    }
}
