package com.cybex.provider.websocket;

public class WebSocketNodeConfig {

    private String mdp;
    private String limit_order;

    public static WebSocketNodeConfig getInstance() {
        return WebSocketNodeConfigProvider.factory;
    }

    private static class WebSocketNodeConfigProvider {
        private static final WebSocketNodeConfig factory = new WebSocketNodeConfig();
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getLimit_order() {
        return limit_order;
    }

    public void setLimit_order(String limit_order) {
        this.limit_order = limit_order;
    }
}
