package io.enotes.sdk.repository.api.entity.response.btc.omniexplorer;

import java.util.List;

public class OmniBalance {
    private List<Balance> balance;

    public List<Balance> getBalance() {
        return balance;
    }

    public void setBalance(List<Balance> balance) {
        this.balance = balance;
    }

    public static class Balance{
        private String id;
        private boolean divisible;
        private String value;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isDivisible() {
            return divisible;
        }

        public void setDivisible(boolean divisible) {
            this.divisible = divisible;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
