package com.cybex.provider.graphene.chat;

/**
 * 聊天推送响应
 */
public class ChatSubscribe<T> {

    public static final int TYPE_REPLY = 0;
    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_MESSAGE = 2;

    private int type;// 数据类型，0-消息回应，2-消息
    private int online;// 在线人数
    private T data;// 数据内容

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
