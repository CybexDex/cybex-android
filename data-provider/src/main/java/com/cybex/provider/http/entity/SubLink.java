package com.cybex.provider.http.entity;

public class SubLink {

    public static class Status {
        public static final String ONLINE = "online";
        public static final String OFFLINE = "offline";
    }

    //标题
    private String title;
    //描述
    private String desc;
    //图标
    private String icon;
    //链接 app内链接 cybexapp://  浏览器链接 https://
    private String link;
    //状态
    private String status;
    //是否需要登陆
    private boolean needlogin;
    //是否需要与原生交互
    private int needtalk;
    //排序
    private String score;
    //
    private String name;

    public SubLink(String title, String desc, String icon, String link, String status, boolean needlogin, int needtalk, String score, String name) {
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.link = link;
        this.status = status;
        this.needlogin = needlogin;
        this.needtalk = needtalk;
        this.score = score;
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public boolean isNeedlogin() {
        return needlogin;
    }

    public void setNeedlogin(boolean needlogin) {
        this.needlogin = needlogin;
    }

    public int getNeedtalk() {
        return needtalk;
    }

    public void setNeedtalk(int needtalk) {
        this.needtalk = needtalk;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
