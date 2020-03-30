package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseENotesEntity;
import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntCallEntity;

public class EthCallForInfura extends BaseEthEntityForInfura implements BaseThirdEntity {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public BaseENotesEntity parseToENotesEntity() {
        EntCallEntity callEntity = new EntCallEntity();
        callEntity.setResult(result);
        return callEntity;
    }
}
