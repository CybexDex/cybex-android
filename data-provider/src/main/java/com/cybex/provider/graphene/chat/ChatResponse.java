package com.cybex.provider.graphene.chat;

/**
 * 聊天推送响应
 */
public class ChatResponse<T> {

    private int Type;// 数据类型，0-消息回应，2-消息
    private int Online;// 在线人数
    private T Data;// 数据内容

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getOnline() {
        return Online;
    }

    public void setOnline(int online) {
        Online = online;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
