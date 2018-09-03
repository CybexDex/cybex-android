package com.cybex.provider.http.response;

public class EtoBaseResponse<T> {

    private int code;

    private T result;

    public EtoBaseResponse(int code, T result) {
        this.code = code;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
