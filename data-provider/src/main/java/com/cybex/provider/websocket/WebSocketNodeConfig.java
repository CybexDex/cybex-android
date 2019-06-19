package com.cybex.provider.websocket;

public class WebSocketNodeConfig {

    private String mdp;
    private String limit_order;
    private String eto;
    private String gateway1;
    private String gateway1_query;
    private String gateway2;

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

    public String getEto() {
        return eto + "/";
    }

    public void setEto(String eto) {
        this.eto = eto + "/";
    }

    public String getGateway2() {
        return gateway2;
    }

    public void setGateway2(String gateway2) {
        this.gateway2 = gateway2;
    }

    public String getGateway1_query() {
        return gateway1_query;
    }

    public void setGateway1_query(String gateway1_query) {
        this.gateway1_query = gateway1_query;
    }

    public String getGateway1() {
        return gateway1;
    }

    public void setGateway1(String gateway1) {
        this.gateway1 = gateway1;
    }
}
