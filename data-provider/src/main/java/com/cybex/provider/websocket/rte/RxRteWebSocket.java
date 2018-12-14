package com.cybex.provider.websocket.rte;

import android.support.annotation.NonNull;

import com.cybex.provider.websocket.rx.RxWebSocket;

public class RxRteWebSocket extends RxWebSocket {

    public static final String TAG = RxRteWebSocket.class.getSimpleName();
    //RTE正式服务器
    public static final String CHAT_URL = "ws://mdp.cybex.io/";
    //RTE测试服务器
    public static final String CHAT_UTL_TEST = "ws://47.244.40.252:18888/";

    public RxRteWebSocket(@NonNull String url) {
        super(url);
    }
}
