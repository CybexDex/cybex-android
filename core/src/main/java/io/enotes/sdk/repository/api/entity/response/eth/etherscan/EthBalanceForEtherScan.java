package io.enotes.sdk.repository.api.entity.response.eth.etherscan;

import io.enotes.sdk.repository.api.entity.BaseEthEntity;
import io.enotes.sdk.repository.api.entity.EntBalanceEntity;
import io.enotes.sdk.utils.Utils;

public class EthBalanceForEtherScan extends BaseEthEntity {

    /**
     * status : 1
     * message : OK
     * result : 190352000000000
     */

    private String status;
    private String message;
    private String result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "EthBalanceForEtherScan:\nmessage="+message+"\nresult="+result;
    }

    @Override
    public EntBalanceEntity parseToENotesEntity() {
        EntBalanceEntity entBalanceEntity=new EntBalanceEntity();
        entBalanceEntity.setBalance(Utils.intToHexString(result));
        return entBalanceEntity;
    }
}
