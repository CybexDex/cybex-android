package com.cybex.provider.websocket.apihk;

import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;

import java.util.List;

public class LimitOrderWrapper {

    private ApihkWebSocketClient mApihkWebSocketClient = new ApihkWebSocketClient();

    private static class Factory {
        private static LimitOrderWrapper wrapper = new LimitOrderWrapper();
    }

    public static LimitOrderWrapper getInstance() {
        return Factory.wrapper;
    }

    public void connect() {
        mApihkWebSocketClient.connect();
    }

    public void disconnect() {
        mApihkWebSocketClient.disconnect();
    }

    public void get_opend_limit_orders(String accountId,
                                       MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_opend_limit_orders(accountId, callback);
    }

    public void get_opend_market_limit_orders(String accountId,
                                              String baseId,
                                              String quote,
                                              MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_opend_market_limit_orders(accountId, baseId, quote, callback);
    }
}
