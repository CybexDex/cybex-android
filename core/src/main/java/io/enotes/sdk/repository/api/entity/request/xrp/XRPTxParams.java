package io.enotes.sdk.repository.api.entity.request.xrp;

public class XRPTxParams {
    private String transaction;
    private boolean binary;


    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }
}
