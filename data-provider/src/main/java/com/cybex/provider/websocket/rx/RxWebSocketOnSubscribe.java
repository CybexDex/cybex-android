package com.cybex.provider.websocket.rx;

import com.cybex.provider.graphene.websocket.WebSocketBase;

import java.util.concurrent.TimeUnit;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * WebSocket数据源
 */
public class RxWebSocketOnSubscribe implements FlowableOnSubscribe<WebSocketBase> {

    private final OkHttpClient mHttpClient;
    private final Request mRequest;

    public RxWebSocketOnSubscribe(String url) {
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        mRequest = new Request.Builder()
                .url(url)
                .build();
    }

    @Override
    public void subscribe(FlowableEmitter<WebSocketBase> e) throws Exception {
        mHttpClient.newWebSocket(mRequest, new RxWebSocketClient(e));
    }
}
