package com.cybex.provider.http.response;

import java.util.List;

/**
 * 交易对Response
 */
public class AssetsPairResponse {

    private String code;
    private List<String> data;

    public AssetsPairResponse(String code, List<String> data) {
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
