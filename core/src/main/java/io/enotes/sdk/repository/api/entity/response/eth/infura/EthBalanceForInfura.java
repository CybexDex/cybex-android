package io.enotes.sdk.repository.api.entity.response.eth.infura;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;

public class EthBalanceForInfura extends BaseEthEntityForInfura implements BaseThirdEntity {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity=new EntBalanceEntity();
        entBalanceEntity.setBalance(result);
        return entBalanceEntity;
    }
}
