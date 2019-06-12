package com.cybex.provider.http.entity;

public class EtoProjectStatus {
    //
    private String id;
    //
    private boolean real;
    //项目当前进度
    private float current_percent;
    //项目当前额度
    private float current_base_token_count;
    //项目当前quote额度
    private float current_remain_quota_count;
    //项目当前参投用户数
    private int current_user_count;
    //项目当前状态
    private String status;
    //结束时间
    private String finish_at;

    public EtoProjectStatus(String id, boolean real, float current_percent, float current_base_token_count,
                            float current_remain_quota_count,
                            int current_user_count, String status, String finish_at) {
        this.id = id;
        this.real = real;
        this.current_percent = current_percent;
        this.current_base_token_count = current_base_token_count;
        this.current_remain_quota_count = current_remain_quota_count;
        this.current_user_count = current_user_count;
        this.status = status;
        this.finish_at = finish_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isReal() {
        return real;
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    public float getCurrent_percent() {
        return current_percent;
    }

    public void setCurrent_percent(float current_percent) {
        this.current_percent = current_percent;
    }

    public float getCurrent_base_token_count() {
        return current_base_token_count;
    }

    public void setCurrent_base_token_count(float current_base_token_count) {
        this.current_base_token_count = current_base_token_count;
    }

    public int getCurrent_user_count() {
        return current_user_count;
    }

    public void setCurrent_user_count(int current_user_count) {
        this.current_user_count = current_user_count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinish_at() {
        return finish_at;
    }

    public void setFinish_at(String finish_at) {
        this.finish_at = finish_at;
    }

    public float getCurrent_remain_quota_count() {
        return current_remain_quota_count;
    }

    public void setCurrent_remain_quota_count(float current_remain_quota_count) {
        this.current_remain_quota_count = current_remain_quota_count;
    }
}
