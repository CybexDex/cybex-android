package com.cybexmobile.graphene.chain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class account_balance_object implements Serializable {
        public object_id id;
        public object_id<account_object> owner;
        public object_id<asset_object> asset_type;
        public long balance;
}
