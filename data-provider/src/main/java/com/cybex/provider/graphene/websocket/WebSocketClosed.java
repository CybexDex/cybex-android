package com.cybex.provider.graphene.websocket;

import com.cybex.provider.websocket.rx.RxWebSocketStatus;

public class WebSocketClosed extends WebSocketBase {

    private int code;
    private String reason;

    public WebSocketClosed(int code, String reason) {
        super(RxWebSocketStatus.CLOSED);
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
