package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.EntNonceEntity;

public class EthNonceForEtherScan extends BaseEthEntity {

    /**
     * jsonrpc : 2.0
     * id : 1
     * result : 0x0
     */

    private String jsonrpc;
    private int id;
    private String result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "EthNonceForEtherScan:\nEthNonceForEtherScan="+result;
    }

    @Override
    public EntNonceEntity parseToENotesEntity() {
        EntNonceEntity entNonceEntity=new EntNonceEntity();
        entNonceEntity.setNonce(result);
        return entNonceEntity;
    }
}
