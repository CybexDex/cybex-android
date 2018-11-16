package com.cybex.provider.websocket.chat;

import com.cybex.provider.graphene.chat.ChatSocketBase;

import java.util.concurrent.TimeUnit;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * WebSocket数据源
 */
public class ChatSocketOnSubscribe implements FlowableOnSubscribe<ChatSocketBase> {

    private final OkHttpClient mHttpClient;
    private final Request mRequest;

    public ChatSocketOnSubscribe(String url) {
        mHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        mRequest = new Request.Builder()
                .url(url)
                .build();
    }

    @Override
    public void subscribe(FlowableEmitter<ChatSocketBase> e) throws Exception {
        mHttpClient.newWebSocket(mRequest, new ChatSocketClient(e));
    }
}
