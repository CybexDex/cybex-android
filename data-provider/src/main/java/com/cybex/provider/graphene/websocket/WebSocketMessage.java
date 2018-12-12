package com.cybex.provider.graphene.websocket;

import com.cybex.provider.websocket.rx.RxWebSocketStatus;

import okio.ByteString;

public class WebSocketMessage extends WebSocketBase {

    private String text;
    private ByteString bytes;

    public WebSocketMessage() {
        super(RxWebSocketStatus.MESSAGE);
    }

    public WebSocketMessage(String text) {
        super(RxWebSocketStatus.MESSAGE);
        this.text = text;
    }

    public WebSocketMessage(ByteString bytes) {
        super(RxWebSocketStatus.MESSAGE);
        this.bytes = bytes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ByteString getBytes() {
        return bytes;
    }

    public void setBytes(ByteString bytes) {
        this.bytes = bytes;
    }

    public boolean isText() {
        return bytes == null;
    }
}
