package com.cybex.provider.graphene.chat;

/**
 * 聊天消息
 */
public class ChatMessage {

    private String UserName;// 用户名
    private long MsgID;// 消息id
    private String Message;// 消息内容
    private String DeviceID;// 设备id
    private long Timestamp;// 时间戳
    private String Sign;// 签名信息，Sign=sign(SHA256({UserName}_{Message}))

    public ChatMessage(String userName, String message) {
        UserName = userName;
        Message = message;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }
}
