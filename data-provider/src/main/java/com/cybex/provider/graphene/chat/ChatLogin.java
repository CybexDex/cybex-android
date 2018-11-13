package com.cybex.provider.graphene.chat;

/**
 * 聊天登录
 */
public class ChatLogin {

    private String Channel;// 频道名字，使用币对来表示
    private String MessageSize;// 第一次批量推送的消息数量
    private String DeviceID;// 设备id

    public String getChannel() {
        return Channel;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public String getMessageSize() {
        return MessageSize;
    }

    public void setMessageSize(String messageSize) {
        MessageSize = messageSize;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }
}
