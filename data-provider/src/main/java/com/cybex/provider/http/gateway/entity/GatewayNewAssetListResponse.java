package com.cybex.provider.http.gateway.entity;

import java.util.List;

public class GatewayNewAssetListResponse {
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ClassPojo [data = " + data + "]";
    }
}
