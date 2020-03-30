package io.enotes.sdk.repository.api.entity.request.xrp;

public class XRPTransactionListParams {
    private String account;
    private boolean binary;
    private boolean forward;
    private int ledger_index_max;
    private int ledger_index_min;
    private int limit;


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public int getLedger_index_max() {
        return ledger_index_max;
    }

    public void setLedger_index_max(int ledger_index_max) {
        this.ledger_index_max = ledger_index_max;
    }

    public int getLedger_index_min() {
        return ledger_index_min;
    }

    public void setLedger_index_min(int ledger_index_min) {
        this.ledger_index_min = ledger_index_min;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
