package com.cybex.provider.websocket.rte;

import android.support.annotation.NonNull;

import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.websocket.rx.RxWebSocket;

import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

public class RxRteWebSocket extends RxWebSocket {

    public static final String TAG = RxRteWebSocket.class.getSimpleName();
    //RTE正式服务器
    public static final String RTE_URL = "ws://mdp.cybex.io/";
    //RTE测试服务器
    public static final String RTE_UTL_TEST = "ws://47.244.40.252:18888/";

    public static final String SUBSCRIBE_TICKET = "ticker";
    public static final String SUBSCRIBE_DEPTH = "depth";

    public RxRteWebSocket(@NonNull String url) {
        super(url);
    }

    public Flowable<WebSocketMessage> onSubscribe(final String type) {
        return super.onSubscribe()
                .filter(new Predicate<WebSocketMessage>() {
                    @Override
                    public boolean test(WebSocketMessage webSocketMessage) throws Exception {
                        return webSocketMessage.getText().contains(type);
                    }
                });
    }

}
