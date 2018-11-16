package com.cybex.provider.graphene.chat;

import okio.ByteString;

public class ChatSocketMessage extends ChatSocketBase {

    private String text;
    private ByteString bytes;

    public ChatSocketMessage() {}

    public ChatSocketMessage(String text) {
        this.text = text;
    }

    public ChatSocketMessage(ByteString bytes) {
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
