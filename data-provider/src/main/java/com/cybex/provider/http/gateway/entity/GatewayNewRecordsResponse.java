package com.cybex.provider.http.gateway.entity;

import java.util.List;

public class GatewayNewRecordsResponse {
    private int total;
    private int size;
    private int offset;
    private List<GatewayNewRecord> records;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<GatewayNewRecord> getRecords() {
        return records;
    }

    public void setRecords(List<GatewayNewRecord> records) {
        this.records = records;
    }
}
