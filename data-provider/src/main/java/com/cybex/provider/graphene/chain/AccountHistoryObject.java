package com.cybex.provider.graphene.chain;

import com.google.gson.JsonArray;

/**
 * 账户历史记录（交易，转账等）
 */
public class AccountHistoryObject {

    public String id;
    public String obj_id;
    public JsonArray op;
    public int block_num;
    public String timestamp;
    public String pair;
}



