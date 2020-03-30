package io.enotes.sdk.core;

public class EntSignature {
    private String r;
    private String s;
    private int recId;

    public EntSignature(String r, String s, int recId) {
        this.r = r;
        this.s = s;
        this.recId = recId;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public int getRecId() {
        return recId;
    }

    public void setRecId(int recId) {
        this.recId = recId;
    }
}
