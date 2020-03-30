package io.enotes.sdk.repository.api.entity.request.eth.infura;

import java.util.ArrayList;
import java.util.List;

public  class EthRequestForInfura {
    public static final String GET_BALANCE="eth_getBalance";
    public static final String GAS_PRICE="eth_gasPrice";
    public static final String TRANSACTION_RECEIPT="eth_getTransactionReceipt";
    public static final String GET_NONCE="eth_getTransactionCount";
    public static final String CALL="eth_call";
    public static final String ESTIMATE_GAS="eth_estimateGas";
    public static final String SEND_RAW_TRANSACTION="eth_sendRawTransaction";
    private String jsonrpc="2.0";
    private String method;
    private List<Object> params;
    private int id=1;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public  void parseParams(Object... params){
        List<Object> paramsList=new ArrayList<>();
        for(Object p:params){
            paramsList.add(p);
        }
        this.params=paramsList;
    }

}
