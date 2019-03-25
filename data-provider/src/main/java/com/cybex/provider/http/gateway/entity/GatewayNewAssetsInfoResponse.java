package com.cybex.provider.http.gateway.entity;

import java.util.List;

public class GatewayNewAssetsInfoResponse {
    private int total;
    private List<GatewayNewAssetRecord> records;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<GatewayNewAssetRecord> getRecords() {
        return records;
    }

    public void setRecords(List<GatewayNewAssetRecord> records) {
        this.records = records;
    }

    public class GatewayNewAssetRecord {
        String asset;
        int total;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public String getAsset() {
            return asset;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }
    }
}
