package io.enotes.sdk.repository.api.entity.response.xrp;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntConfirmedEntity;

public class XRPIsConfirmed implements BaseThirdEntity {
    private Result result;
    private String status;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public EntConfirmedEntity parseToENotesEntity() {
        EntConfirmedEntity entConfirmedEntity = new EntConfirmedEntity();
        entConfirmedEntity.setConfirmations(result.validated ? "6" : "0");
        entConfirmedEntity.setTxid(result.hash);
        return entConfirmedEntity;
    }

    public static class Result {
        private String Account;
        private String Destination;
        private String Fee;
        private String hash;
        private boolean validated;

        public String getAccount() {
            return Account;
        }

        public void setAccount(String account) {
            Account = account;
        }

        public String getDestination() {
            return Destination;
        }

        public void setDestination(String destination) {
            Destination = destination;
        }

        public String getFee() {
            return Fee;
        }

        public void setFee(String fee) {
            Fee = fee;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public boolean isValidated() {
            return validated;
        }

        public void setValidated(boolean validated) {
            this.validated = validated;
        }
    }


}
