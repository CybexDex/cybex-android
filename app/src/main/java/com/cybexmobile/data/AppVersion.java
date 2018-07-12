package com.cybexmobile.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AppVersion {

    private String version;
    private String url;
    private String cnUpdateInfo;
    private String enUpdateInfo;
    private JsonObject force;

    public AppVersion(String version, String url, JsonObject force, String cnUpdateInfo, String enUpdateInfo) {
        this.version = version;
        this.url = url;
        this.force = force;
        this.cnUpdateInfo = cnUpdateInfo;
        this.enUpdateInfo = enUpdateInfo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JsonObject getForce() {
        return force;
    }

    public void setForce(JsonObject force) {
        this.force = force;
    }

    public String getCnUpdateInfo() {
        return cnUpdateInfo;
    }

    public void setCnUpdateInfo(String cnUpdateInfo) {
        this.cnUpdateInfo = cnUpdateInfo;
    }

    public String getEnUpdateInfo() {
        return enUpdateInfo;
    }

    public void setEnUpdateInfo(String enUpdateInfo) {
        this.enUpdateInfo = enUpdateInfo;
    }

    public boolean isForceUpdate(String currVersion){
        JsonElement element = force.get(currVersion);
        return element != null && element.getAsBoolean();
    }

    public boolean compareVersion(String currVersion){
        return Integer.parseInt(version.replace(".", "")) >
                Integer.parseInt(currVersion.replace(".", ""));
    }
}
