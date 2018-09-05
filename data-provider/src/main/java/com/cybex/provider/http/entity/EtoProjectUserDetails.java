package com.cybex.provider.http.entity;

public class EtoProjectUserDetails {
    private EtoUserStatus etoUserStatus;
    private EtoProject etoProject;

    public EtoProject getEtoProject() {
        return etoProject;
    }

    public void setEtoProject(EtoProject etoProject) {
        this.etoProject = etoProject;
    }

    public EtoUserStatus getEtoUserStatus() {
        return etoUserStatus;
    }

    public void setEtoUserStatus(EtoUserStatus etoUserStatus) {
        this.etoUserStatus = etoUserStatus;
    }
}
