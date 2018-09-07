package com.cybex.provider.http.response;

public class AppConfigResponse {

    private boolean isETOEnabled;
    private boolean isShareEnabled;

    public AppConfigResponse(boolean isETOEnabled, boolean isShareEnabled) {
        this.isETOEnabled = isETOEnabled;
        this.isShareEnabled = isShareEnabled;
    }

    public boolean isETOEnabled() {
        return isETOEnabled;
    }

    public void setETOEnabled(boolean ETOEnabled) {
        isETOEnabled = ETOEnabled;
    }

    public boolean isShareEnabled() {
        return isShareEnabled;
    }

    public void setShareEnabled(boolean shareEnabled) {
        isShareEnabled = shareEnabled;
    }
}
