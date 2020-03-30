package io.enotes.sdk.repository.api.entity.response.xrp;

import java.util.List;

public class XRPTransactionList {
    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


    public static class Result {
        private String account;
        private String ledger_index_max;
        private String ledger_index_min;
        private int limit;
        private String status;
        private List<Transaction> transactions;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getLedger_index_max() {
            return ledger_index_max;
        }

        public void setLedger_index_max(String ledger_index_max) {
            this.ledger_index_max = ledger_index_max;
        }

        public String getLedger_index_min() {
            return ledger_index_min;
        }

        public void setLedger_index_min(String ledger_index_min) {
            this.ledger_index_min = ledger_index_min;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }
    }

    public static class Transaction {
        private Tx tx;
        private boolean validated;

        public Tx getTx() {
            return tx;
        }

        public void setTx(Tx tx) {
            this.tx = tx;
        }

        public boolean isValidated() {
            return validated;
        }

        public void setValidated(boolean validated) {
            this.validated = validated;
        }
    }

    public static class Tx {
        private String Account;
        private String Amount;
        private String Destination;
        private String Fee;
        private long date;
        private String hash;

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getAccount() {
            return Account;
        }

        public void setAccount(String account) {
            Account = account;
        }

        public String getAmount() {
            return Amount;
        }

        public void setAmount(String amount) {
            Amount = amount;
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

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }
    }
}
