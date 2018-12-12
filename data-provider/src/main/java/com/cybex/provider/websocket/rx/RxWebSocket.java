package com.cybex.provider.websocket.rx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cybex.provider.graphene.websocket.WebSocketBase;
import com.cybex.provider.graphene.websocket.WebSocketClosed;
import com.cybex.provider.graphene.websocket.WebSocketClosing;
import com.cybex.provider.graphene.websocket.WebSocketFailure;
import com.cybex.provider.graphene.websocket.WebSocketMessage;
import com.cybex.provider.graphene.websocket.WebSocketOpen;
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

public class RxWebSocket implements RxListener {

    public static final String TAG = RxWebSocket.class.getSimpleName();

    //事件分发
    private PublishProcessor<WebSocketBase> publishProcessor = PublishProcessor.create();
    private RxWebSocketOnSubscribe rxWebSocketOnSubscribe;
    private WebSocket webSocket;
    protected RxWebSocketStatus status;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public RxWebSocket(@NonNull String url) {
        this.rxWebSocketOnSubscribe = new RxWebSocketOnSubscribe(url);
    }

    protected final Flowable<WebSocketBase> getErrorHandler() {
        return publishProcessor.onErrorResumeNext(new Function<Throwable, Publisher<WebSocketBase>>() {
            @Override
            public Publisher<WebSocketBase> apply(Throwable throwable) throws Exception {
                Log.e(TAG, throwable.getMessage());
                throwable.printStackTrace();
                publishProcessor = PublishProcessor.create();
                return publishProcessor;
            }
        });
    }

    @Override
    public Flowable<WebSocketOpen> onOpen() {
        return getErrorHandler()
                .ofType(WebSocketOpen.class)
                .doOnNext(new Consumer<WebSocketOpen>() {
                    @Override
                    public void accept(WebSocketOpen webSocketOpen) throws Exception {
                        status = webSocketOpen.getStatus();
                        webSocket = webSocketOpen.getWebSocket();
                    }
                })
                .doOnEach(new RxWebSocketLogger<WebSocketOpen>("onOpen"));
    }

    @Override
    public Flowable<WebSocketMessage> onSubscribe() {
        return getErrorHandler()
                .ofType(WebSocketMessage.class)
                .doOnNext(new Consumer<WebSocketMessage>() {
                    @Override
                    public void accept(WebSocketMessage webSocketMessage) throws Exception {
                        status = webSocketMessage.getStatus();
                    }
                })
                .doOnEach(new RxWebSocketLogger<WebSocketMessage>("onSubscribe"));

    }

    @Override
    public Flowable<WebSocketClosing> onClosing() {
        return getErrorHandler()
                .ofType(WebSocketClosing.class)
                .doOnNext(new Consumer<WebSocketClosing>() {
                    @Override
                    public void accept(WebSocketClosing webSocketClosing) throws Exception {
                        status = webSocketClosing.getStatus();
                    }
                })
                .doOnEach(new RxWebSocketLogger<WebSocketClosing>("onClosing"));
    }

    @Override
    public Flowable<WebSocketClosed> onClosed() {
        return getErrorHandler()
                .ofType(WebSocketClosed.class)
                .doOnNext(new Consumer<WebSocketClosed>() {
                    @Override
                    public void accept(WebSocketClosed webSocketClosed) throws Exception {
                        status = webSocketClosed.getStatus();
                    }
                })
                .doOnEach(new RxWebSocketLogger<WebSocketClosed>("onClosed"));
    }

    @Override
    public Flowable<WebSocketFailure> onFailure() {
        return getErrorHandler()
                .ofType(WebSocketFailure.class)
                .doOnNext(new Consumer<WebSocketFailure>() {
                    @Override
                    public void accept(WebSocketFailure webSocketFailure) throws Exception {
                        status = webSocketFailure.getStatus();
                    }
                })
                .doOnEach(new RxWebSocketLogger<WebSocketFailure>("onFailure"));
    }

    @Override
    public synchronized <T>Single<Boolean> sendMessage(@NonNull final T payload) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    String jsonBody = new Gson().toJson(payload);
                    Log.v(TAG, jsonBody);
                    return webSocket.send(jsonBody);
                } else {
                    throw new RuntimeException("RxWebSocket not connected!");
                }
            }
        });
    }

    @Override
    public synchronized Single<Boolean> sendMessage(@NonNull final String content) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    Log.v(TAG, content);
                    return webSocket.send(content);
                } else {
                    throw new RuntimeException("RxWebSocket not connected!");
                }
            }
        });
    }

    @Override
    public synchronized Single<Boolean> sendMessage(@NonNull final ByteString bytes) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    return webSocket.send(bytes);
                } else {
                    throw new RuntimeException("RxWebSocket not connected!");
                }
            }
        });
    }

    @Override
    public synchronized void connect() {
        Disposable connectionDisposable = Flowable.create(rxWebSocketOnSubscribe, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<WebSocketBase>() {
                    @Override
                    public void accept(WebSocketBase webSocketBase) throws Exception {
                        publishProcessor.onNext(webSocketBase);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
        Disposable closeDisposable = getErrorHandler()
                .ofType(WebSocketClosed.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<WebSocketClosed>() {
                    @Override
                    public void accept(WebSocketClosed webSocketClosed) throws Exception {
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

    @Override
    public synchronized void reconnect(long delay, TimeUnit unit) {
        Disposable reconnectionDisposable = Flowable.timer(delay, unit)
                .concatMap(new Function<Long, Publisher<WebSocketBase>>() {
                    @Override
                    public Publisher<WebSocketBase> apply(Long aLong) throws Exception {
                        return Flowable.create(rxWebSocketOnSubscribe, BackpressureStrategy.BUFFER);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<WebSocketBase>() {
                    @Override
                    public void accept(WebSocketBase webSocketBase) throws Exception {
                        publishProcessor.onNext(webSocketBase);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
        compositeDisposable.add(reconnectionDisposable);
    }

    @Override
    public synchronized Single<Boolean> close(final int code, @Nullable final String reason) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (webSocket != null) {
                    return webSocket.close(code, reason);
                } else {
                    throw new RuntimeException("RxWebSocket not connected!");
                }
            }
        });
    }

    @Override
    public boolean isConnected() {
        return status != null && status == RxWebSocketStatus.OPEN;
    }

}
