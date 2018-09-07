package com.cybex.provider.http.entity;

public class EtoErrorMsgResponse {
    private String zh;
    private String en;


    public EtoErrorMsgResponse(String zh, String en) {
        this.zh = zh;
        this.en = en;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getZh() {
        return zh;
    }

    public void setZh(String zh) {
        this.zh = zh;
    }


}
