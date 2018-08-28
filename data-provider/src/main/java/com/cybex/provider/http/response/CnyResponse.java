package com.cybex.provider.http.response;

import com.cybex.provider.http.entity.AssetRmbPrice;

import java.util.List;

/**
 * 人名币Response
 */
public class CnyResponse {
    private int code;
    private List<AssetRmbPrice> prices;

    public CnyResponse(){}

    public CnyResponse(int code, List<AssetRmbPrice> prices) {
        this.code = code;
        this.prices = prices;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<AssetRmbPrice> getPrices() {
        return prices;
    }

    public void setPrices(List<AssetRmbPrice> prices) {
        this.prices = prices;
    }
}
