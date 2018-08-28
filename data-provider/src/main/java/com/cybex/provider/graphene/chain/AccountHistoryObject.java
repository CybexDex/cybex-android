package com.cybex.provider.graphene.chain;

import com.google.gson.JsonArray;

/**
 * 账户历史记录（交易，转账等）
 */
public class AccountHistoryObject {

    public String id;
    public JsonArray op;
    public int block_num;
    public int trx_in_block;
    public int op_in_trx;
    public int virtual_op;
}
