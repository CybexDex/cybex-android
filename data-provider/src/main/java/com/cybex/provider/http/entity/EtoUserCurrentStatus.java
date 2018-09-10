package com.cybex.provider.http.entity;

public class EtoUserCurrentStatus {
    private boolean real;
    private float current_base_token_count;

    public EtoUserCurrentStatus(boolean real, int current_base_token_count) {
        this.real = real;
        this.current_base_token_count = current_base_token_count;
    }

    public float getCurrent_base_token_count() {
        return current_base_token_count;
    }

    public void setCurrent_base_token_count(float current_base_token_count) {
        this.current_base_token_count = current_base_token_count;
    }

    public boolean isReal() {
        return real;
    }

    public void setReal(boolean real) {
        this.real = real;
    }
}
