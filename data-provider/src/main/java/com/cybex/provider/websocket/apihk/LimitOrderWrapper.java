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

    /**
     * 获取当前用户的委单
     * @param accountId 用户Id
     * @param callback 结果回调
     */
    public void get_opend_limit_orders(String accountId,
                                       MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_opend_limit_orders(accountId, callback);
    }

    /**
     * 获取当前用户指定交易对委单
     * @param accountId 用户Id
     * @param baseId baseId
     * @param quote quoteId
     * @param callback 结果回调
     */
    public void get_opend_market_limit_orders(String accountId,
                                              String baseId,
                                              String quote,
                                              MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_opend_market_limit_orders(accountId, baseId, quote, callback);
    }

    /**
     * 获取用户历史委单
     * @param accountId 用户名
     * @param lastOrderId 最后订单号
     * @param limit 分页加载数量
     */
    public void get_limit_order_status(String accountId,
                                       String lastOrderId,
                                       int limit,
                                       MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_limit_order_status(accountId, lastOrderId, limit, callback);
    }

    /**
     * 获取用户历史委单
     * @param accountId 用户名
     * @param lastOrderId 最后订单号
     * @param baseId baseId
     * @param quoteId quoteId
     * @param limit 分页加载数量
     */
    public void get_market_limit_order_status(String accountId,
                                       String lastOrderId,
                                       String baseId,
                                       String quoteId,
                                       int limit,
                                       MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_market_limit_order_status(accountId, lastOrderId, baseId, quoteId, limit, callback);
    }

    /**
     * 添加过滤器
     * @param filteredPairs 需要过滤的交易对
     */
    public void add_filtered_market(List<List<String >> filteredPairs,
                                    MessageCallback<Reply<String>> callback) {
        mApihkWebSocketClient.add_filtered_market(filteredPairs, callback);
    }

    /**
     * 获取用户历史委单
     * @param accountId 用户名
     * @param lastOrderId 最后订单号
     * @param limit 分页加载数量
     * @param containFilteredPairs 是否查询添加在过滤其中的交易对
     */
    public void get_filtered_limit_order_status(String accountId,
                                                String lastOrderId,
                                                int limit,
                                                boolean containFilteredPairs,
                                                MessageCallback<Reply<List<LimitOrder>>> callback) {
        mApihkWebSocketClient.get_filtered_limit_order_status(accountId, lastOrderId, limit, containFilteredPairs, callback);
    }


    /**
     * 获取当前时间最后一笔交易订单号
     * @param timestamp 当前时间戳
     * @param callback
     */
    public void get_limit_order_id_by_time(String timestamp, MessageCallback<Reply<String>> callback) {
        mApihkWebSocketClient.get_limit_order_id_by_time(timestamp, callback);
    }
}
