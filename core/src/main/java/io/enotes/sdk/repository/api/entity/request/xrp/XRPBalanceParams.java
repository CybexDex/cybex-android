package io.enotes.sdk.repository.api.entity.request.xrp;

public class XRPBalanceParams {
    private String account;
    private boolean strict;
    private String ledger_index;
    private boolean queue;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public String getLedger_index() {
        return ledger_index;
    }

    public void setLedger_index(String ledger_index) {
        this.ledger_index = ledger_index;
    }

    public boolean isQueue() {
        return queue;
    }

    public void setQueue(boolean queue) {
        this.queue = queue;
    }
}
