package com.cybex.provider.http.response;

public class AppConfigResponse {

    private boolean isETOEnabled;
    private boolean isShareEnabled;
    private boolean contestEnabled;
    private double ageRate;

    public AppConfigResponse(boolean isETOEnabled, boolean isShareEnabled, boolean contestEnabled, int ageRate) {
        this.isETOEnabled = isETOEnabled;
        this.isShareEnabled = isShareEnabled;
        this.contestEnabled = contestEnabled;
        this.ageRate = ageRate;
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

    public void setContestEnabled(boolean contestEnabled) {
        this.contestEnabled = contestEnabled;
    }

    public boolean isContestEnabled() {
        return contestEnabled;
    }

    public double getAgeRate() {
        return ageRate;
    }

    public void setAgeRate(double ageRate) {
        this.ageRate = ageRate;
    }
}
