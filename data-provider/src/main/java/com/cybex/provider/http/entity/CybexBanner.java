package com.cybex.provider.http.entity;

public class CybexBanner {

    public static class Status {
        public static final String ONLINE = "online";
        public static final String OFFLINE = "offline";
    }

    private String image;
    private String link;
    private String status;
    private int score;
    private String name;

    public CybexBanner(String image, String link, String status, int score, String name) {
        this.image = image;
        this.link = link;
        this.status = status;
        this.score = score;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
