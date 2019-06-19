package com.cybex.provider;

public class SettingConfig {

    private double ageRate;
    private boolean isGateway2;


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

    public void setGateway2(boolean gateway2) {
        isGateway2 = gateway2;
    }

    public boolean isGateway2() {
        return isGateway2;
    }
}
