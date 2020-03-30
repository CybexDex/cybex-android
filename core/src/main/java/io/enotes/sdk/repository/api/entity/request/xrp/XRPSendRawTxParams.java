package io.enotes.sdk.repository.api.entity.request.xrp;

public class XRPSendRawTxParams {
    private String tx_blob;

    public String getTx_blob() {
        return tx_blob;
    }

    public void setTx_blob(String tx_blob) {
        this.tx_blob = tx_blob;
    }
}
