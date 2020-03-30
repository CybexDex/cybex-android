package io.enotes.sdk.repository.api.entity.response.btc.blockchain;

import java.util.List;

public class BtcBalanceListForBlockChain {
    private List<Address> addresses;

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public static class Address {
        private String address;
        private int n_tx;
        private String final_balance;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getN_tx() {
            return n_tx;
        }

        public void setN_tx(int n_tx) {
            this.n_tx = n_tx;
        }

        public String getFinal_balance() {
            return final_balance;
        }

        public void setFinal_balance(String final_balance) {
            this.final_balance = final_balance;
        }
    }
}
