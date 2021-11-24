package com.honeycom.saas.mobile.http.bean;

/**
* author : zhoujr
* date : 2021/10/20 16:44
* desc : 首页欢迎图片
*/
public class AdMessagePackage {

    private int code;
    private String msg;
    private int count;
    private int status;
    private AdMessageBean data;

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

    public AdMessageBean getData() {
        return data;
    }

    public void setData(AdMessageBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AdMessagePackage{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", count=" + count +
                ", status=" + status +
                ", data=" + data +
                '}';
    }
}
