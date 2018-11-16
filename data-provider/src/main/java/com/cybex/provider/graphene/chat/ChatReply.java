package com.cybex.provider.graphene.chat;

/**
 * 发送消息或者登录响应
 */
public class ChatReply {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;

    private int status;// 0-success, 1-failed
    private long msgID; // 消息id

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        status = status;
    }

    public long getMsgID() {
        return msgID;
    }

    public void setMsgID(long msgID) {
        this.msgID = msgID;
    }

}
