package io.enotes.sdk.repository.api.entity.response.xrp;

public class XRPBalance {
    private Result result;
    private String status;
    private boolean validated;

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

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public static class Result{
        private AccountData account_data;

        public AccountData getAccount_data() {
            return account_data;
        }

        public void setAccount_data(AccountData account_data) {
            this.account_data = account_data;
        }
    }

    public static class AccountData{
        private String Account;
        private String Balance;
        private String Flags;
        private int Sequence;

        public String getAccount() {
            return Account;
        }

        public void setAccount(String account) {
            Account = account;
        }

        public String getBalance() {
            return Balance;
        }

        public void setBalance(String balance) {
            Balance = balance;
        }

        public String getFlags() {
            return Flags;
        }

        public void setFlags(String flags) {
            Flags = flags;
        }

        public int getSequence() {
            return Sequence;
        }

        public void setSequence(int sequence) {
            Sequence = sequence;
        }
    }
}
