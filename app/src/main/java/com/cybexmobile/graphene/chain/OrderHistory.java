package com.cybexmobile.graphene.chain;

/**
 * 历史记录（买入，卖出）
 */
public class OrderHistory {

    public Fee fee;
    public String order_id;
    public String account_id;
    public Pay pays;
    public Receive receives;

    public class Fee {
        public int amount;
        public String asset_id;
    }

    public class Pay {
        public int amount;
        public String asset_id;
    }

    public class Receive {
        public int amount;
        public String asset_id;
    }

}