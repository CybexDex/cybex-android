package com.cybex.provider.http.gateway.entity;

public class Blockchain {
    private String name;

    private String confirmation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    @Override
    public String toString() {
        return "ClassPojo [name = " + name + ", confirmation = " + confirmation + "]";
    }
}
