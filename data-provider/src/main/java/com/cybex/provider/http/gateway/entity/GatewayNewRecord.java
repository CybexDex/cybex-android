package com.cybex.provider.http.gateway.entity;

public class GatewayNewRecord {

    private String amount;

    private String outAddr;

    private String outHash;

    private String fee;

    private String link;

    private String cybexName;

    private String type;

    private String cybHash;

    private String totalAmount;

    private String createdAt;

    private String confirms;

    private String asset;

    private String status;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOutAddr() {
        return outAddr;
    }

    public void setOutAddr(String outAddr) {
        this.outAddr = outAddr;
    }

    public String getOutHash() {
        return outHash;
    }

    public void setOutHash(String outHash) {
        this.outHash = outHash;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCybexName() {
        return cybexName;
    }

    public void setCybexName(String cybexName) {
        this.cybexName = cybexName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCybHash() {
        return cybHash;
    }

    public void setCybHash(String cybHash) {
        this.cybHash = cybHash;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getConfirms() {
        return confirms;
    }

    public void setConfirms(String confirms) {
        this.confirms = confirms;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
