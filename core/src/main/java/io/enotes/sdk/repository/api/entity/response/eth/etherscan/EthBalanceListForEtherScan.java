package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import java.util.List;

public class EthBalanceListForEtherScan {
    private String status;
    private String message;
    private List<Account> result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Account> getResult() {
        return result;
    }

    public void setResult(List<Account> result) {
        this.result = result;
    }

    public static class Account {
        private String account;
        private String balance;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }
    }
}
