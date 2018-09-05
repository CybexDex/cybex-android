package com.cybex.provider.http.entity;

public class EtoUserStatus {

    //用户kyc状态
    private String kyc_status;

    //用户审核状态
    private String status;

    //原因
    private String reason;

    private String zh;

    private String en;

    public EtoUserStatus(String kyc_status, String status, String reason, String zh, String en) {
        this.kyc_status = kyc_status;
        this.status = status;
        this.reason = reason;
        this.zh = zh;
        this.en = en;
    }

    public String getKyc_status() {
        return kyc_status;
    }

    public void setKyc_status(String kyc_status) {
        this.kyc_status = kyc_status;
    }

    public String getStatus() {
        return status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }
}
