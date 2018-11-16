package com.cybex.provider.graphene.chat;

import java.util.List;

public class ChatMessages {

    private List<ChatMessage> messages;

    public ChatMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public List<ChatMessage> getMessages() {
        return this.messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}
