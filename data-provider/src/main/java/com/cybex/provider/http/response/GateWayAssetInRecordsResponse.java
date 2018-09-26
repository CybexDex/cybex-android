package com.cybex.provider.http.response;

import com.cybex.provider.http.entity.AssetRecord;

import java.util.List;

public class GateWayAssetInRecordsResponse {

    private Data data;

    public class Data {
        int total;
        int size;
        int offset;
        List<AssetRecord> records;

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

        public List<AssetRecord> getRecords() {
            return records;
        }

        public void setRecords(List<AssetRecord> records) {
            this.records = records;
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
