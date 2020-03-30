package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.EntGasEntity;

public class EthEstimateGasForEtherScan extends BaseEthEntity {
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
    public EntGasEntity parseToENotesEntity() {
        EntGasEntity gasEntity = new EntGasEntity();
        gasEntity.setGas(result);
        return gasEntity;
    }
}
