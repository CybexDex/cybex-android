package com.cybex.provider.websocket;

public enum  WebSocketStatus {

    DEFAULT,  // 默认状态
    OPENING,  //正在建立连接
    OPENED,   //完成建立连接
    LOGIN,    //登录完成
    CLOSING,  //正在关闭连接
    CLOSED,   //已经关闭连接
    FAILURE   //发生未知错误

}
