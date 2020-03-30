package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntGasPriceEntity;
import io.enotes.sdk.utils.Utils;

public class EthGasPriceForInfura extends BaseEthEntityForInfura implements BaseThirdEntity {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public EntGasPriceEntity parseToENotesEntity() {
        EntGasPriceEntity entFeesEntity = new EntGasPriceEntity();
        entFeesEntity.setFast(Utils.hexToBigIntString(result));
        return entFeesEntity;
    }
}
