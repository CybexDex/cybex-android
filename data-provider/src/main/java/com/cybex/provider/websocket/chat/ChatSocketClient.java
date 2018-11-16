package com.cybex.provider.websocket.chat;

import android.util.Log;

import com.cybex.provider.graphene.chat.ChatSocketBase;
import com.cybex.provider.graphene.chat.ChatSocketClosed;
import com.cybex.provider.graphene.chat.ChatSocketClosing;
import com.cybex.provider.graphene.chat.ChatSocketFailure;
import com.cybex.provider.graphene.chat.ChatSocketMessage;
import com.cybex.provider.graphene.chat.ChatSocketOpen;

import javax.annotation.Nullable;

import io.reactivex.FlowableEmitter;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 回调事件转发
 */
public class ChatSocketClient extends WebSocketListener {

    private final FlowableEmitter<ChatSocketBase> emitter;

    public ChatSocketClient(FlowableEmitter<ChatSocketBase> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "ChatWebSocket已经连接");
            emitter.onNext(new ChatSocketOpen(webSocket, response));
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, text);
            emitter.onNext(new ChatSocketMessage(text));
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, bytes.toString());
            emitter.onNext(new ChatSocketMessage(bytes));
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "ChatWebSocket已经关闭 code=" + code + " reason=" + reason);
            emitter.onNext(new ChatSocketClosed(code, reason));
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "ChatWebSocket正在关闭 code=" + code + " reason=" + reason);
            emitter.onNext(new ChatSocketClosing(code, reason));
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        if(!emitter.isCancelled()){
            Log.d(RxChatWebSocket.TAG, "ChatWebSocket发生异常" + t.getMessage());
            emitter.onNext(new ChatSocketFailure(t, response));
        }
    }
}
