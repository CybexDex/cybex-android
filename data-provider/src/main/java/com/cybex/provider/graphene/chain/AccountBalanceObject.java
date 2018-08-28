package com.cybex.provider.graphene.chain;

import java.io.Serializable;

public class AccountBalanceObject implements Serializable {
        public ObjectId id;
        public ObjectId<AccountObject> owner;
        public ObjectId<AssetObject> asset_type;
        public long balance;
}
