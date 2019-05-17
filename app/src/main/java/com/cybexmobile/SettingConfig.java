package com.cybexmobile;

public class SettingConfig {

    private double ageRate;


    private SettingConfig() {

    }

    public static SettingConfig getInstance() {
        return SettingConfigProvider.factory;
    }

    private static class SettingConfigProvider {
        private static final SettingConfig factory = new SettingConfig();
    }

    public double getAgeRate() {
        return ageRate;
    }

    public void setAgeRate(double ageRate) {
        this.ageRate = ageRate;
    }
}
