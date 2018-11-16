package com.cybex.provider.graphene.chat;

/**
 * 聊天消息
 */
public class ChatMessage {

    private String userName;// 用户名
    private long msgID;// 消息id
    private String message;// 消息内容
    private String deviceID;// 设备id
    private long timestamp;// 时间戳
    private int signed;//0-未签名验证过的，1-签名验证过的

    public ChatMessage(String userName, String message) {
        this.userName = userName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getMsgID() {
        return msgID;
    }

    public void setMsgID(long msgID) {
        this.msgID = msgID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSigned() {
        return signed;
    }

    public void setSigned(int signed) {
        this.signed = signed;
    }
}
