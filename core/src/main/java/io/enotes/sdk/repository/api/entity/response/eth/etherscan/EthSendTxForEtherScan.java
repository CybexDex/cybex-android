package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import io.enotes.sdk.repository.api.entity.BaseENotesEntity;
import io.enotes.sdk.repository.api.entity.BaseEthEntity;

public class EthSendTxForEtherScan extends BaseEthEntity {

    /**
     * jsonrpc : 2.0
     * id : 1
     * result : 0x0
     */
    //{"jsonrpc":"2.0","id":1,"error":{"code":-32000,"message":"nonce too low"}}
    //{"jsonrpc":"2.0","id":1,"result":"0xca40374e9ffa7a4e52c03191dc4f56c876f1019c586dffdeee3d708e2501463b"}
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
    public BaseENotesEntity parseToENotesEntity() {
        return null;
    }
}
