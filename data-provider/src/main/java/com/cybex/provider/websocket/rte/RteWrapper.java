package com.cybex.provider.websocket.rte;

import com.cybex.provider.graphene.chain.LimitOrder;
import com.cybex.provider.websocket.MessageCallback;
import com.cybex.provider.websocket.Reply;
import com.cybex.provider.websocket.apihk.ApihkWebSocketClient;

import java.util.List;

public class RteWrapper {

    private ApihkWebSocketClient mApihkWebSocketClient = new ApihkWebSocketClient();

    private static class Factory {
        private static RteWrapper wrapper = new RteWrapper();
    }

    public static RteWrapper getInstance() {
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
