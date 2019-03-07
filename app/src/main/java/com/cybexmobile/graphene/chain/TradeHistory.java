package com.cybexmobile.graphene.chain;

/**
 * 历史记录（买入，卖出）
 */
public class TradeHistory {

    public Fee fee;
    public String order_id;
    public String account_id;
    public FillPrice fill_price;
    public Pay pays;
    public Receive receives;

    public class Fee {
        public int amount;
        public String asset_id;
    }

    public class FillPrice {
        public Pay base;
        public Pay quote;
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