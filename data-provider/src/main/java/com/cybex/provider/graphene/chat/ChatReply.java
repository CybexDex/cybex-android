package com.cybex.provider.graphene.chat;

/**
 * 发送消息或者登录响应
 */
public class ChatReply {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;

    private int Status;// 0-success, 1-failed
    private long MsgID; // 消息id

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public long getMsgID() {
        return MsgID;
    }

    public void setMsgID(long msgID) {
        MsgID = msgID;
    }

}
