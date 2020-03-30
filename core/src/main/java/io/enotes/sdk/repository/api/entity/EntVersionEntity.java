package io.enotes.sdk.repository.api.entity;


import io.enotes.sdk.utils.Utils;

public class EntVersionEntity extends BaseENotesEntity {
    private int version_last;
    private String version_url;
    private String version_description;

    public int getVersion_last() {
        return version_last;
    }

    public void setVersion_last(int version_last) {
        this.version_last = version_last;
    }

    public String getVersion_url() {
        return version_url;
    }

    public void setVersion_url(String version_url) {
        this.version_url = version_url;
    }

    public String getVersion_description() {
        return version_description;
    }

    public void setVersion_description(String version_description) {
        this.version_description = version_description;
    }
}

