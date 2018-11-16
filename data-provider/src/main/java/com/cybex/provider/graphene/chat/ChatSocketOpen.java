package com.cybex.provider.graphene.chat;

import okhttp3.Response;
import okhttp3.WebSocket;

public class ChatSocketOpen extends ChatSocketBase {

    private WebSocket webSocket;
    private Response response;

    public ChatSocketOpen(WebSocket webSocket, Response response) {
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
