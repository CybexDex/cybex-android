package io.enotes.sdk.repository.api.entity.response.btc.bitcoinfees;

import io.enotes.sdk.repository.api.entity.BaseThirdEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;

public class BtcFeesEntity implements BaseThirdEntity {

    /**
     * fastestFee : 40
     * halfHourFee : 20
     * hourFee : 10
     */

    private int fastestFee;
    private int halfHourFee;
    private int hourFee;

    public int getFastestFee() {
        return fastestFee;
    }

    public void setFastestFee(int fastestFee) {
        this.fastestFee = fastestFee;
    }

    public int getHalfHourFee() {
        return halfHourFee;
    }

    public void setHalfHourFee(int halfHourFee) {
        this.halfHourFee = halfHourFee;
    }

    public int getHourFee() {
        return hourFee;
    }

    public void setHourFee(int hourFee) {
        this.hourFee = hourFee;
    }

    @Override
    public String toString() {
        return "BtcFeesEntity:\nfastestFee="+fastestFee+"\nhalfHourFee="+halfHourFee+"\nhourFee="+hourFee;
    }

    @Override
    public EntFeesEntity parseToENotesEntity() {
        EntFeesEntity entFeeEntity=new EntFeesEntity();
        entFeeEntity.setLow(hourFee*1000+"");
        entFeeEntity.setFast(halfHourFee*1000+"");
        entFeeEntity.setFastest(fastestFee*1000+"");
        return entFeeEntity;
    }
}
