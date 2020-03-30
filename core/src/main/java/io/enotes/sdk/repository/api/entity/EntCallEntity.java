package io.enotes.sdk.repository.api.entity;

import android.support.annotation.Nullable;

import io.enotes.sdk.utils.ContractUtils;

public class EntCallEntity extends BaseENotesEntity {
    private String jsonrpc;
    private int id;
    private String result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Nullable
    public String getPubKey() {
        return ContractUtils.decodeAbiFunctionKeyOfResult(result);
    }

    @Override
    public String toString() {
        return "EntCallEntity:\nEntCallEntity=" + result;
    }
}
