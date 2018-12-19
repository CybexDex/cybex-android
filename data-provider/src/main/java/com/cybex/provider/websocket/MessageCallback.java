package com.cybex.provider.websocket;

public interface MessageCallback<T>{

    void onMessage(T reply);

    void onFailure();
}