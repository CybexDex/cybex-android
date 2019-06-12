package com.cybex.provider.http.entity;

import com.cybex.provider.graphene.chain.AssetObject;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewEtoRecord {
    @JsonProperty("id")
    private String id;
    @JsonProperty("exchange_to_record")
    private String exchange_to_record;
    @JsonProperty("exchange_name")
    private String exchange_name;
    @JsonProperty("exchange_description")
    private String exchange_description;
    @JsonProperty("rate")
    private Rate rate;
    @JsonProperty("participator")
    private String participator;
    @JsonProperty("pay_asset_id")
    private String pay_asset_id;
    @JsonProperty("pay_amount")
    private long pay_amount;
    @JsonProperty("receive_asset_id")
    private String receive_asset_id;
    @JsonProperty("receive_amount")
    private long receive_amount;
    @JsonProperty("occurence")
    private String occurence;
    private AssetObject payAssetObject;
    private AssetObject receiveAssetObject;

    public String getID() {
        return id;
    }

    @JsonProperty("id")
    public void setID(String value) {
        this.id = value;
    }


    public String getExchangeToRecord() {
        return exchange_to_record;
    }

    @JsonProperty("exchange_to_record")
    public void setExchangeToRecord(String value) {
        this.exchange_to_record = value;
    }


    public String getExchangeName() {
        return exchange_name;
    }

    @JsonProperty("exchange_name")
    public void setExchangeName(String value) {
        this.exchange_name = value;
    }


    public String getExchangeDescription() {
        return exchange_description;
    }

    @JsonProperty("exchange_description")
    public void setExchangeDescription(String value) {
        this.exchange_description = value;
    }


    public Rate getRate() {
        return rate;
    }

    @JsonProperty("rate")
    public void setRate(Rate value) {
        this.rate = value;
    }


    public String getParticipator() {
        return participator;
    }

    @JsonProperty("participator")
    public void setParticipator(String value) {
        this.participator = value;
    }


    public String getPayAssetID() {
        return pay_asset_id;
    }

    @JsonProperty("pay_asset_id")
    public void setPayAssetID(String value) {
        this.pay_asset_id = value;
    }


    public long getPayAmount() {
        return pay_amount;
    }

    @JsonProperty("pay_amount")
    public void setPayAmount(long value) {
        this.pay_amount = value;
    }


    public String getReceiveAssetID() {
        return receive_asset_id;
    }

    @JsonProperty("receive_asset_id")
    public void setReceiveAssetID(String value) {
        this.receive_asset_id = value;
    }


    public long getReceiveAmount() {
        return receive_amount;
    }

    @JsonProperty("receive_amount")
    public void setReceiveAmount(long value) {
        this.receive_amount = value;
    }


    public String getOccurence() {
        return occurence;
    }

    @JsonProperty("occurence")
    public void setOccurence(String value) {
        this.occurence = value;
    }

    public AssetObject getPayAssetObject() {
        return payAssetObject;
    }

    public void setPayAssetObject(AssetObject payAssetObject) {
        this.payAssetObject = payAssetObject;
    }

    public AssetObject getReceiveAssetObject() {
        return receiveAssetObject;
    }

    public void setReceiveAssetObject(AssetObject receiveAssetObject) {
        this.receiveAssetObject = receiveAssetObject;
    }

    public class Rate {
        private Base base;
        private Base quote;

        @JsonProperty("base")
        public Base getBase() {
            return base;
        }

        @JsonProperty("base")
        public void setBase(Base value) {
            this.base = value;
        }

        @JsonProperty("quote")
        public Base getQuote() {
            return quote;
        }

        @JsonProperty("quote")
        public void setQuote(Base value) {
            this.quote = value;
        }
    }

    public class Base {
        private long amount;
        private String assetID;

        @JsonProperty("amount")
        public long getAmount() {
            return amount;
        }

        @JsonProperty("amount")
        public void setAmount(long value) {
            this.amount = value;
        }

        @JsonProperty("asset_id")
        public String getAssetID() {
            return assetID;
        }

        @JsonProperty("asset_id")
        public void setAssetID(String value) {
            this.assetID = value;
        }
    }

}

