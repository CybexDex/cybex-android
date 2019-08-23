package com.cybex.provider.graphene.chain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class HtlcObject implements Serializable {
    public ObjectId<HtlcObject> id;
    public Transfer transfer;
    public Condition conditions;


    public class Transfer implements Serializable {
        public ObjectId<AccountObject> from;
        public ObjectId<AccountObject> to;
        public long amount;
        public ObjectId<AssetObject> asset_id;
    }

    public class Condition implements Serializable {
        public HashLock hash_lock;
        public TimeLock time_lock;
    }

    public class HashLock implements Serializable {
        public List<Object> preimage_hash;
        int preimage_size;
    }

    public class TimeLock implements Serializable {
        public String expiration;
    }

}
