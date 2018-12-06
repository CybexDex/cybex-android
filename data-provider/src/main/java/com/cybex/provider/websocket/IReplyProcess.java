package com.cybex.provider.websocket;

public interface IReplyProcess<T> {

    void processTextToObject(String strText);

    T getReply();

    String getError();

    void notifyFailure(Throwable t);

    Throwable getException();

    String getResponse();

    WebSocketClient.MessageCallback<T> getCallback();

    void release();
}