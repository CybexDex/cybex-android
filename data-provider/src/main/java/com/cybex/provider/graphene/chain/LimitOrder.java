package com.cybex.provider.graphene.chain;

import java.io.Serializable;
import java.util.Date;

public class LimitOrder implements Serializable {

    public String id; //对象id
    public ObjectId<LimitOrder> order_id; //订单号
    public String seller; //用户账号
    public Key key; //交易市场
    public boolean is_sell; //true表示此订单为出售asset1， false表示此订单为出售asset2
    public long amount_to_sell; //下单时请求出售资产的数量
    public long min_to_receive; //下单时希望获取资产的数量
    public long sold; //已经出售的资产数量
    public long received; //已经购得的资产数量
    public long canceled; //撤单时尚未完全出售的资产数量
    public long block_num; //创建订单的交易所在区块号
    public int trx_in_blk; //创建订单的交易在区块中的交易编号
    public int op_in_trx; //创建订单的操作在交易中的操作编号
    public String create_time; //创建订单的交易所在区块的时间戳

    public class Key {
        public String asset1;
        public String asset2;
    }
}
