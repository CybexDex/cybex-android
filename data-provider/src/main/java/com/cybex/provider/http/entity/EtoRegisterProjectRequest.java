package com.cybex.provider.http.entity;

public class EtoRegisterProjectRequest {
    public String  project;
    public String user;
    public msg msg;

    public static class msg {
        public String code;
    }

}
