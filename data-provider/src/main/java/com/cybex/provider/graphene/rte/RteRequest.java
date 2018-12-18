package com.cybex.provider.graphene.rte;

public class RteRequest {

    public static final String TYPE_SUBSCRIBE = "subscribe";
    public static final String TYPE_UNSUBSCRIBE = "unsubscribe";

    public static final String TOPIC_ORDERBOOK = "ORDERBOOK";

    private String type;
    private String topic;

    public RteRequest(String type, String topic) {
        this.type = type;
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
