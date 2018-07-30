package com.cybexmobile.data;

public class GatewayLogInResponse {
    public int code;
    public Data data;

    public class Data {
        String accountName;
        String signer;
    }

}
