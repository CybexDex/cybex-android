package com.cybex.provider.websocket;

public class Reply<T> {
    public String id;
    public String jsonrpc;
    public T result;
    public WebSocketError error;

    class WebSocketError {
        int code;
        String message;
        Object data;
    }
}
