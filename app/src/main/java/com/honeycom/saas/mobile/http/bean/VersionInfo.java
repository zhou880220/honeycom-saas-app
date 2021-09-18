package com.honeycom.saas.mobile.http.bean;

public class VersionInfo {

    private String versionName;

    private int versionCode;

    public VersionInfo(String versionName, int versionCode) {
        this.versionName = versionName;
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
