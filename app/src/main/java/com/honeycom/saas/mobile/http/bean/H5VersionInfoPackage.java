package com.honeycom.saas.mobile.http.bean;

import java.util.List;

public class H5VersionInfoPackage {

    private int code;
    private String msg;
    private int count;
    private int status;
    private List<H5VersionInfo> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<H5VersionInfo> getData() {
        return data;
    }

    public void setData(List<H5VersionInfo> data) {
        this.data = data;
    }
}
