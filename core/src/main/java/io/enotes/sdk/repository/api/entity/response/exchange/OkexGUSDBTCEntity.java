package io.enotes.sdk.repository.api.entity.response.exchange;

public class OkexGUSDBTCEntity {
    public static final String GUSD_BTC = "GUSD-BTC";
    public static final String ETH_BTC = "ETH-BTC";
    public static final String USDT_BTC = "USDT-BTC";
    private String last;
    private String instrument_id;

    public String getInstrument_id() {
        return instrument_id;
    }

    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
}
