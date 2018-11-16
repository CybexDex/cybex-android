package com.cybex.provider.graphene.chat;

import okhttp3.Response;

public class ChatSocketFailure extends ChatSocketBase {

    private Throwable throwable;
    private Response response;

    public ChatSocketFailure(Throwable throwable, Response response) {
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
