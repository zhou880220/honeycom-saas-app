package com.honeycom.saas.mobile.ws.bean;

/**
 * 打印参数
 */
public class PrintBean {

    private String ip;

    private String port;

    private String zplString;


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getZplString() {
        return zplString;
    }

    public void setZplString(String zplString) {
        this.zplString = zplString;
    }
}
