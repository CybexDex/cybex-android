package com.cybex.provider.graphene.websocket;

import com.cybex.provider.websocket.rx.RxWebSocketStatus;

import okhttp3.Response;

public class WebSocketFailure extends WebSocketBase {

    private Throwable throwable;
    private Response response;

    public WebSocketFailure(Throwable throwable, Response response) {
        super(RxWebSocketStatus.FAILURE);
        this.throwable = throwable;
        this.response = response;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
