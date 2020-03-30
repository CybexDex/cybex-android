package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntGasEntity;

public class EthEstimateGasForInfura extends BaseEthEntityForInfura implements BaseThirdEntity {
    private String result;

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
