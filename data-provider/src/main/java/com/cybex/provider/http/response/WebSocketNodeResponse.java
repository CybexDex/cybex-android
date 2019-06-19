package com.cybex.provider.http.response;

import java.util.List;

public class WebSocketNodeResponse {
    private String mdp;
    private List<String> nodes;
    private String limitOrder;
    private String eto;
    private String gateway1;
    private String gateway1_query;
    private String gateway2;


    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getLimitOrder() {
        return limitOrder;
    }

    public void setLimitOrder(String limitOrder) {
        this.limitOrder = limitOrder;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getEto() {
        return eto;
    }

    public void setEto(String eto) {
        this.eto = eto;
    }

    public String getGateway1() {
        return gateway1;
    }

    public void setGateway1(String gateway1) {
        this.gateway1 = gateway1;
    }

    public String getGateway1_query() {
        return gateway1_query;
    }

    public void setGateway1_query(String gateway1_query) {
        this.gateway1_query = gateway1_query;
    }

    public String getGateway2() {
        return gateway2;
    }

    public void setGateway2(String gateway2) {
        this.gateway2 = gateway2;
    }
}
