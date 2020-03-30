package io.enotes.sdk.repository.api.entity.response.xrp;

public class XRPFee {
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


    public static class Result{
        private Drop drops;
        private String current_ledger_size;
        private String current_queue_size;

        public Drop getDrops() {
            return drops;
        }

        public void setDrops(Drop drops) {
            this.drops = drops;
        }

        public String getCurrent_ledger_size() {
            return current_ledger_size;
        }

        public void setCurrent_ledger_size(String current_ledger_size) {
            this.current_ledger_size = current_ledger_size;
        }

        public String getCurrent_queue_size() {
            return current_queue_size;
        }

        public void setCurrent_queue_size(String current_queue_size) {
            this.current_queue_size = current_queue_size;
        }
    }

    public static class Drop{
        private String base_fee;
        private String median_fee;
        private String minimum_fee;
        private String open_ledger_fee;

        public String getBase_fee() {
            return base_fee;
        }

        public void setBase_fee(String base_fee) {
            this.base_fee = base_fee;
        }

        public String getMedian_fee() {
            return median_fee;
        }

        public void setMedian_fee(String median_fee) {
            this.median_fee = median_fee;
        }

        public String getMinimum_fee() {
            return minimum_fee;
        }

        public void setMinimum_fee(String minimum_fee) {
            this.minimum_fee = minimum_fee;
        }

        public String getOpen_ledger_fee() {
            return open_ledger_fee;
        }

        public void setOpen_ledger_fee(String open_ledger_fee) {
            this.open_ledger_fee = open_ledger_fee;
        }
    }
}
