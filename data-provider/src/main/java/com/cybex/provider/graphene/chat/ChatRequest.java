package com.cybex.provider.graphene.chat;

/**
 * 聊天发送消息或者登录
 * @param <T>
 */
public class ChatRequest<T> {

    //登录发送
    public static final int TYPE_LOGIN = 1;
    //消息发送
    public static final int TYPE_MESSAGE = 2;

    private int type;// 1-login, 2-message
    private T data;// 数据json对象

    public ChatRequest() {}

    public ChatRequest(int type, T data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
