package com.cybex.provider.websocket.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cybex.provider.graphene.chat.ChatSocketBase;
import com.cybex.provider.graphene.chat.ChatSocketClosed;
import com.cybex.provider.graphene.chat.ChatSocketClosing;
import com.cybex.provider.graphene.chat.ChatSocketFailure;
import com.cybex.provider.graphene.chat.ChatSocketMessage;
import com.cybex.provider.graphene.chat.ChatSocketOpen;
import com.google.gson.Gson;

import org.reactivestreams.Publisher;

import java.util.concurrent.Callable;

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

public class RxChatWebSocket {

    public static final String TAG = RxChatWebSocket.class.getSimpleName();

    //事件分发
    private PublishProcessor<ChatSocketBase> publishProcessor = PublishProcessor.create();
    private ChatSocketOnSubscribe chatSocketOnSubscribe;
    private WebSocket webSocket;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public RxChatWebSocket(@NonNull String url) {
        this.chatSocketOnSubscribe = new ChatSocketOnSubscribe(url);
    }

    private Flowable<ChatSocketBase> getErrorHandler() {
        return publishProcessor.onErrorResumeNext(new Function<Throwable, Publisher<ChatSocketBase>>() {
            @Override
            public Publisher<ChatSocketBase> apply(Throwable throwable) throws Exception {
                Log.e(TAG, "RxChatWebSocket EventSubject internal error occured.");
                Log.e(TAG, throwable.getMessage());
                throwable.printStackTrace();
                publishProcessor = PublishProcessor.create();
                return publishProcessor;
            }
        });
    }

    /**
     * onOpen回调
     * @return
     */
    public Flowable<ChatSocketOpen> onOpen() {
        return getErrorHandler()
                .ofType(ChatSocketOpen.class)
                .doOnNext(new Consumer<ChatSocketOpen>() {
                    @Override
                    public void accept(ChatSocketOpen chatSocketOpen) throws Exception {
                        if(webSocket == null){
                            webSocket = chatSocketOpen.getWebSocket();
                        }
                    }
                })
                .doOnEach(new RxWebSocketLogger<ChatSocketOpen>("onOpen"));
    }

    /**
     * onSubscribe回调
     * @return
     */
    public Flowable<ChatSocketMessage> onSubscribe() {
        return getErrorHandler()
                .ofType(ChatSocketMessage.class)
                .doOnEach(new RxWebSocketLogger<ChatSocketMessage>("onSubscribe"));

    }

    /**
     * onClosing回调
     * @return
     */
    public Flowable<ChatSocketClosing> onClosing() {
        return getErrorHandler()
                .ofType(ChatSocketClosing.class)
                .doOnEach(new RxWebSocketLogger<ChatSocketClosing>("onClosing"));
    }

    /**
     * onClosed回调
     * @return
     */
    public Flowable<ChatSocketClosed> onClosed() {
        return getErrorHandler()
                .ofType(ChatSocketClosed.class)
                .doOnEach(new RxWebSocketLogger<ChatSocketClosed>("onClosed"));
    }

    /**
     * onFailure回调
     * @return
     */
    public Flowable<ChatSocketFailure> onFailure() {
        return getErrorHandler()
                .ofType(ChatSocketFailure.class)
                .doOnEach(new RxWebSocketLogger<ChatSocketFailure>("onFailure"));
    }

    /**
     * 发送消息
     * @param payload
     * @return
     */
    public synchronized <T>Single<Boolean> sendMessage(@Nullable final T payload) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    String jsonBody = new Gson().toJson(payload);
                    Log.v(TAG, jsonBody);
                    return webSocket.send(jsonBody);
                } else {
                    throw new RuntimeException("WebSocket not connected!");
                }
            }
        });
    }

    /**
     * 发送消息
     * @param content
     * @return
     */
    public synchronized Single<Boolean> sendMessage(@Nullable final String content) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    Log.v(TAG, content);
                    return webSocket.send(content);
                } else {
                    throw new RuntimeException("WebSocket not connected!");
                }
            }
        });
    }

    /**
     * 发送消息
     * @param bytes
     * @return
     */
    public synchronized Single<Boolean> sendMessage(@NonNull final ByteString bytes) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    return webSocket.send(bytes);
                } else {
                    throw new RuntimeException("WebSocket not connected!");
                }
            }
        });
    }

    /**
     * WebSocket连接
     */
    public synchronized void connect() {
        Disposable connectionDisposable = Flowable.create(chatSocketOnSubscribe, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<ChatSocketBase>() {
                    @Override
                    public void accept(ChatSocketBase chatSocketBase) throws Exception {
                        publishProcessor.onNext(chatSocketBase);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
        Disposable closeDisposable = getErrorHandler()
                .ofType(ChatSocketClosed.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<ChatSocketClosed>() {
                    @Override
                    public void accept(ChatSocketClosed chatSocketClosed) throws Exception {
                        Log.d(TAG, "----------close----------");
                        webSocket = null;
                        compositeDisposable.dispose();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
        compositeDisposable.add(connectionDisposable);
        compositeDisposable.add(closeDisposable);
    }

    public synchronized Single<Boolean> close(final int code, @Nullable final String reason) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    return webSocket.close(code, reason);
                } else {
                    throw new RuntimeException("WebSocket not connected!");
                }
            }
        });
    }

}
