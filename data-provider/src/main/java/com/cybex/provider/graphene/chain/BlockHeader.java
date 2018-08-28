package com.cybex.provider.graphene.chain;

import java.io.Serializable;

public class BlockHeader implements Serializable{
    public String previous;
    public String timestamp;
    public String witness;
    public String transaction_merkle_root;
    public Object[] extensions;
}
