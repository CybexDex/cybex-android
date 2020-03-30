package io.enotes.sdk.repository.api.entity;

public class EntTransactionEntity {
    private String txId;
    private String from;
    private String time;
    private String amount;
    private boolean sent;
    private int confirmations;
    private String tokenAddress;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    @Override
    public String toString() {
        return "txId = " + txId + "\ntime = " + time + "\namount = " + amount + "\nsent = " + sent + "\nconfirmations = " + confirmations;
    }
}
