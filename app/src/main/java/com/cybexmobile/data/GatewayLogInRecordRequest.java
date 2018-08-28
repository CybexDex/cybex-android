package com.cybexmobile.data;

import com.cybex.provider.graphene.chain.Operations;

public class GatewayLogInRecordRequest {
    private Operations.base_operation op;
    private String signer;

    public void setOp(Operations.base_operation op) {
        this.op = op;
    }

    public Operations.base_operation getOp() {
        return op;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getSigner() {
        return signer;
    }
}
