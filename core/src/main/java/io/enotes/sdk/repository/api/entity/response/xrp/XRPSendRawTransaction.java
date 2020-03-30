package io.enotes.sdk.repository.api.entity.response.xrp;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;

public class XRPSendRawTransaction implements BaseThirdEntity {
    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public EntSendTxEntity parseToENotesEntity() {
        if (result == null) return null;
        EntSendTxEntity entSendTxEntity = new EntSendTxEntity();
        entSendTxEntity.setTxid(result.tx_json.hash);
        return entSendTxEntity;
    }


    public static class Result {
        private String status;
        private String engine_result;
        private int engine_result_code;
        private String engine_result_message;
        private Tx tx_json;

        public Tx getTx_json() {
            return tx_json;
        }

        public void setTx_json(Tx tx_json) {
            this.tx_json = tx_json;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEngine_result() {
            return engine_result;
        }

        public void setEngine_result(String engine_result) {
            this.engine_result = engine_result;
        }

        public int getEngine_result_code() {
            return engine_result_code;
        }

        public void setEngine_result_code(int engine_result_code) {
            this.engine_result_code = engine_result_code;
        }

        public String getEngine_result_message() {
            return engine_result_message;
        }

        public void setEngine_result_message(String engine_result_message) {
            this.engine_result_message = engine_result_message;
        }
    }

    public static class Tx {
        private String Account;
        private String hash;
    }
}
