package com.cybex.provider.graphene.chain;

public class LockAssetObject {
    public ObjectId<LockAssetObject> id;
    public String owner;
    public balance balance;
    public String last_claim_date;
    public vesting_policy vesting_policy;

    public class balance {
        public double amount;
        public ObjectId<AssetObject> asset_id;
    }

    public class vesting_policy {
        public String begin_timestamp;
        public double vesting_cliff_seconds;
        public long vesting_duration_seconds;
        public double begin_balance;

    }
}
