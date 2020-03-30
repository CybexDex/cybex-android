package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntNonceEntity;

public class EthNonceForInfura extends BaseEthEntityForInfura implements BaseThirdEntity{
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public EntNonceEntity parseToENotesEntity() {
        EntNonceEntity entNonceEntity=new EntNonceEntity();
        entNonceEntity.setNonce(result);
        return entNonceEntity;
    }
}
