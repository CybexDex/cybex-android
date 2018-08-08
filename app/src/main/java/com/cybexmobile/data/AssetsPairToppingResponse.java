package com.cybexmobile.data;

import java.util.List;

public class AssetsPairToppingResponse {

    private String base;
    private List<String> quotes;

    public AssetsPairToppingResponse(String base, List<String> quotes) {
        this.base = base;
        this.quotes = quotes;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public List<String> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<String> quotes) {
        this.quotes = quotes;
    }
}
