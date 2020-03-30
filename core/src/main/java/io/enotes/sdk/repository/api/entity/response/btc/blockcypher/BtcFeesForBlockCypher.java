package io.enotes.sdk.repository.api.entity.response.btc.blockcypher;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;

public class BtcFeesForBlockCypher implements BaseThirdEntity {
    private int high_fee_per_kb;
    private int medium_fee_per_kb;
    private int low_fee_per_kb;

    public int getHigh_fee_per_kb() {
        return high_fee_per_kb;
    }

    public void setHigh_fee_per_kb(int high_fee_per_kb) {
        this.high_fee_per_kb = high_fee_per_kb;
    }

    public int getMedium_fee_per_kb() {
        return medium_fee_per_kb;
    }

    public void setMedium_fee_per_kb(int medium_fee_per_kb) {
        this.medium_fee_per_kb = medium_fee_per_kb;
    }

    public int getLow_fee_per_kb() {
        return low_fee_per_kb;
    }

    public void setLow_fee_per_kb(int low_fee_per_kb) {
        this.low_fee_per_kb = low_fee_per_kb;
    }

    @Override
    public EntFeesEntity parseToENotesEntity() {
        EntFeesEntity entFeeEntity = new EntFeesEntity();
        entFeeEntity.setLow(low_fee_per_kb + "");
        entFeeEntity.setFast(medium_fee_per_kb + "");
        entFeeEntity.setFastest(high_fee_per_kb + "");
        return entFeeEntity;
    }
}
