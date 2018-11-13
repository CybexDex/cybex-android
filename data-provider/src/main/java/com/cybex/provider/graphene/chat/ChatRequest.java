package com.cybex.provider.graphene.chat;

/**
 * 聊天发送消息或者登录
 * @param <T>
 */
public class ChatRequest<T> {

    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_MESSAGE = 2;

    private int Type;// 1-login, 2-message
    private T Data;// 数据json对象

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
