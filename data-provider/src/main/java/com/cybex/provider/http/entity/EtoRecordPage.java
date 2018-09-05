package com.cybex.provider.http.entity;

import java.util.List;

public class EtoRecordPage {

    private int total_page;
    private int next_page;
    private List<EtoRecord> data;

    public EtoRecordPage(int total_page, int next_page, List<EtoRecord> data) {
        this.total_page = total_page;
        this.next_page = next_page;
        this.data = data;
    }

    public int getTotal_page() {
        return total_page;
    }

    public void setTotal_page(int total_page) {
        this.total_page = total_page;
    }

    public int getNext_page() {
        return next_page;
    }

    public void setNext_page(int next_page) {
        this.next_page = next_page;
    }

    public List<EtoRecord> getData() {
        return data;
    }

    public void setData(List<EtoRecord> data) {
        this.data = data;
    }
}
