package com.cybex.provider.websocket.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cybex.provider.graphene.websocket.WebSocketBase;
import com.cybex.provider.graphene.websocket.WebSocketClosed;
import com.cybex.provider.graphene.websocket.WebSocketClosing;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
import com.cybex.provider.websocket.rx.RxWebSocket;
import com.cybex.provider.websocket.rx.RxWebSocketOnSubscribe;
import com.cybex.provider.websocket.rx.RxWebSocketLogger;
import com.cybex.provider.websocket.rx.RxWebSocketStatus;
import com.google.gson.Gson;

import org.reactivestreams.Publisher;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import okhttp3.WebSocket;
import okio.ByteString;

public class RxChatWebSocket extends RxWebSocket {

    public static final String TAG = RxChatWebSocket.class.getSimpleName();
    //聊天正式服务器
    public static final String CHAT_URL = "wss://chat.cybex.io/ws";
    //聊天测试服务器
    public static final String CHAT_UTL_TEST = "wss://47.91.242.71:9099/ws";

    public RxChatWebSocket(@NonNull String url) {
        super(url);
    }

    @Override
    public boolean isConnected() {
        return status != null && status == RxWebSocketStatus.MESSAGE;
    }

}
