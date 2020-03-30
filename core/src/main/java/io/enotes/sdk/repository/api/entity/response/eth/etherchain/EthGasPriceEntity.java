package io.enotes.sdk.repository.api.entity.response.eth.etherchain;

import java.math.BigDecimal;

import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.EntFeesEntity;
import io.enotes.sdk.repository.api.entity.EntGasPriceEntity;

public class EthGasPriceEntity extends BaseEthEntity {

    /**
     * jsonrpc : 2.0
     * id : 73
     * result : 0x77359400
     */

    private String safeLow;
    private String standard;
    private String fast;
    private String fastest;

    public String getSafeLow() {
        return safeLow;
    }

    public void setSafeLow(String safeLow) {
        this.safeLow = safeLow;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getFast() {
        return fast;
    }

    public void setFast(String fast) {
        this.fast = fast;
    }

    public String getFastest() {
        return fastest;
    }

    public void setFastest(String fastest) {
        this.fastest = fastest;
    }

    @Override
    public String toString() {
        return "EthGasPriceEntity:\nsafeLow=" + safeLow + "standard=" + standard + "fast=" + fast + "fastest=" + fastest;
    }

    @Override
    public EntGasPriceEntity parseToENotesEntity() {
        EntGasPriceEntity entFeesEntity = new EntGasPriceEntity();
        entFeesEntity.setFastest(getGasPrice(fastest));
        entFeesEntity.setFast(getGasPrice(fast));
        entFeesEntity.setStander(getGasPrice(standard));
        entFeesEntity.setLow(getGasPrice(safeLow));
        return entFeesEntity;
    }

    private String getGasPrice(String count) {
        return new BigDecimal(count).multiply(new BigDecimal("1000000000")).toBigInteger().toString();
    }
}
