package com.cybex.provider.graphene.websocket;

import com.cybex.provider.websocket.rx.RxWebSocketStatus;

public class WebSocketBase {

    private RxWebSocketStatus status;

    public WebSocketBase(RxWebSocketStatus status) {
        this.status = status;
    }

    public RxWebSocketStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "WebSocketBase{" +
                "status=" + status +
                '}';
    }
}
