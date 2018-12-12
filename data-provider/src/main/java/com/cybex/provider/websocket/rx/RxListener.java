package com.cybex.provider.websocket.rx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cybex.provider.graphene.websocket.WebSocketBase;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okio.ByteString;

public interface RxListener {

    /**
     * onOpen回调
     * @return
     */
    Flowable<? extends WebSocketBase> onOpen();

    /**
     * onSubscribe回调
     * @return
     */
    Flowable<? extends WebSocketBase> onSubscribe();

    /**
     * onClosing回调
     * @return
     */
    Flowable<? extends WebSocketBase> onClosing();

    /**
     * onClosed回调
     * @return
     */
    Flowable<? extends WebSocketBase> onClosed();

    /**
     * onFailure回调
     * @return
     */
    Flowable<? extends WebSocketBase> onFailure();

    /**
     * 发送消息
     * @param payload
     * @return
     */
    <T> Single<Boolean> sendMessage(@NonNull final T payload);

    /**
     * 发送消息
     * @param content
     * @return
     */
    Single<Boolean> sendMessage(@NonNull final String content);

    /**
     * 发送消息
     * @param bytes
     * @return
     */
    Single<Boolean> sendMessage(@NonNull final ByteString bytes);

    /**
     * 建立链接
     */
    void connect();

    /**
     * 断线重连
     */
    void reconnect(long delay, TimeUnit unit);

    /**
     * 关闭连接
     * @param code
     * @param reason
     * @return
     */
    Single<Boolean> close(final int code, @Nullable final String reason);

    /**
     * 判断是否连接
     * @return
     */
    boolean isConnected();

}