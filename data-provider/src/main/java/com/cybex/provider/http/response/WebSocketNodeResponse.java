package com.cybex.provider.http.response;

import java.util.List;

public class WebSocketNodeResponse {
    private String mdp;
    private List<String> nodes;
    private String limitOrder;
    private String eto;


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
}
