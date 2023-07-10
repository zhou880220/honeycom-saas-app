package com.honeycom.saas.mobile.ws.bean;

import java.io.Serializable;

/**
 * 统一结果返回对象
 */
public class Result implements Serializable {

    private int code;

    private Object data;

    private String msg;

    private Integer status;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        return result;
    }

    public static Result failed() {
        Result result = new Result();
        result.setCode(500);
        return result;
    }
}
