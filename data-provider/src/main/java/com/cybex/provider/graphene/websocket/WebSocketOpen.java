package com.cybex.provider.graphene.websocket;

import com.cybex.provider.websocket.rx.RxWebSocketStatus;

import okhttp3.Response;
import okhttp3.WebSocket;

public class WebSocketOpen extends WebSocketBase {

    private WebSocket webSocket;
    private Response response;

    public WebSocketOpen(WebSocket webSocket, Response response) {
        super(RxWebSocketStatus.OPEN);
        this.webSocket = webSocket;
        this.response = response;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
