package com.cybex.provider.websocket.rx;

import android.util.Log;

import com.cybex.provider.graphene.websocket.WebSocketBase;
import com.cybex.provider.graphene.websocket.WebSocketClosed;
import com.cybex.provider.graphene.websocket.WebSocketClosing;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
import com.cybex.provider.websocket.chat.RxChatWebSocket;

import javax.annotation.Nullable;

import io.reactivex.FlowableEmitter;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 回调事件转发
 */
public class RxWebSocketClient extends WebSocketListener {

    private final FlowableEmitter<WebSocketBase> emitter;

    public RxWebSocketClient(FlowableEmitter<WebSocketBase> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "RxWebSocket已经连接");
            emitter.onNext(new WebSocketOpen(webSocket, response));
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, text);
            emitter.onNext(new WebSocketMessage(text));
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, bytes.toString());
            emitter.onNext(new WebSocketMessage(bytes));
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "RxWebSocket已经关闭 code=" + code + " reason=" + reason);
            emitter.onNext(new WebSocketClosed(code, reason));
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "RxWebSocket正在关闭 code=" + code + " reason=" + reason);
            emitter.onNext(new WebSocketClosing(code, reason));
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "RxWebSocket发生异常" + t.getMessage());
            emitter.onNext(new WebSocketFailure(t, response));
        }
    }
}
