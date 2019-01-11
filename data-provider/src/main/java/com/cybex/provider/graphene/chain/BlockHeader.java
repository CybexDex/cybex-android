package com.cybex.provider.graphene.chain;

import com.cybex.provider.crypto.Ripemd160Object;

import java.io.Serializable;

public class BlockHeader implements Serializable{
    public Ripemd160Object previous;
    public String timestamp;
    public String witness;
    public String transaction_merkle_root;
    public Object[] extensions;
}
