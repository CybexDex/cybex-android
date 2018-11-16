package com.cybex.provider.graphene.chat;

/**
 * 聊天消息
 */
public class ChatMessageRequest {

    private String userName;// 用户名
    private String message;// 消息内容
    private String sign;// 签名信息，Sign=sign(SHA256({UserName}_{Message}))

    public ChatMessageRequest() {}

    public ChatMessageRequest(String userName, String message, String sign) {
        this.userName = userName;
        this.message = message;
        this.sign = sign;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
