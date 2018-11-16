package com.cybex.provider.graphene.chat;

/**
 * 聊天登录
 */
public class ChatLogin {

    private String channel;// 频道名字，使用币对来表示
    private String messageSize;// 第一次批量推送的消息数量
    private String deviceID;// 设备id

    public ChatLogin(String channel, String messageSize, String deviceID) {
        this.channel = channel;
        this.messageSize = messageSize;
        this.deviceID = deviceID;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
