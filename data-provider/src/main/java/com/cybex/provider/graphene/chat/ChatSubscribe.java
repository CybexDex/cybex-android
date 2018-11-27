package com.cybex.provider.graphene.chat;

/**
 * 聊天推送响应
 */
public class ChatSubscribe<T> {

    //登录回应
    public static final int TYPE_LOGIN_REPLY = 101;
    //消息回应
    public static final int TYPE_MESSAGE_REPLY = 102;
    //消息推送
    public static final int TYPE_MESSAGE = 2;

    private int type;// 数据类型
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
