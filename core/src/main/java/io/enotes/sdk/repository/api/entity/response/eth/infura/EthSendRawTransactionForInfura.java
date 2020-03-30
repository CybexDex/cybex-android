package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntSendTxEntity;

public class EthSendRawTransactionForInfura extends BaseEthEntityForInfura implements BaseThirdEntity{
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public EntSendTxEntity parseToENotesEntity() {
        EntSendTxEntity entNonceEntity=new EntSendTxEntity();
        entNonceEntity.setTxid(result);
        return entNonceEntity;
    }
}
