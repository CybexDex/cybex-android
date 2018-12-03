package com.cybex.provider.http.response;

import java.util.ArrayList;

public class DepositAndWithdrawDetailsResponse {
    private String id;
    private Icon_cn Icon_cnObject;
    private Icon_en Icon_enObject;
    ArrayList<Object> msg_cn = new ArrayList<Object>();
    ArrayList<Object> msg_en = new ArrayList<Object>();
    Notice_cn Notice_cnObject;
    Notice_en Notice_enObject;


    // Getter Methods

    public String getId() {
        return id;
    }

    public Icon_cn getIcon_cn() {
        return Icon_cnObject;
    }

    public Icon_en getIcon_en() {
        return Icon_enObject;
    }

    public Notice_cn getNotice_cn() {
        return Notice_cnObject;
    }

    public Notice_en getNotice_en() {
        return Notice_enObject;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setIcon_cn(Icon_cn icon_cnObject) {
        this.Icon_cnObject = icon_cnObject;
    }

    public void setIcon_en(Icon_en icon_enObject) {
        this.Icon_enObject = icon_enObject;
    }

    public void setNotice_cn(Notice_cn notice_cnObject) {
        this.Notice_cnObject = notice_cnObject;
    }

    public void setNotice_en(Notice_en notice_enObject) {
        this.Notice_enObject = notice_enObject;
    }
}

class Notice_en {
    private String title;
    private String main;
    ArrayList<Object> adds = new ArrayList<Object>();


    // Getter Methods

    public String getTitle() {
        return title;
    }

    public String getMain() {
        return main;
    }

    // Setter Methods

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMain(String main) {
        this.main = main;
    }
}

class Notice_cn {
    private String title;
    private String main;
    ArrayList<Object> adds = new ArrayList<Object>();


    // Getter Methods

    public String getTitle() {
        return title;
    }

    public String getMain() {
        return main;
    }

    // Setter Methods

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMain(String main) {
        this.main = main;
    }
}

class Icon_en {
    private String img_url;
    private String link;


    // Getter Methods

    public String getImg_url() {
        return img_url;
    }

    public String getLink() {
        return link;
    }

    // Setter Methods

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

class Icon_cn {
    private String img_url;
    private String link;


    // Getter Methods

    public String getImg_url() {
        return img_url;
    }

    public String getLink() {
        return link;
    }

    // Setter Methods

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
