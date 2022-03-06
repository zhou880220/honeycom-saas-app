package com.honeycom.saas.mobile.ws.bean;

/**
 * 称对象
 */
public class WeighBean {

    private String ip;
    private String port;
    private String baudrate;
    private String bytesize;
    private String stopbits;
    private String parity;

    private String selfServerPort;


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

    public String getSelfServerPort() {
        return selfServerPort;
    }

    public void setSelfServerPort(String selfServerPort) {
        this.selfServerPort = selfServerPort;
    }


    public String getBaudrate() { return baudrate; }
    public String getBytesize() { return bytesize; }
    public String getStopbits() { return stopbits; }
    public String getParity() { return parity; }

    public void setBaudrate(String baudrate) { this.baudrate = baudrate; }
    public void setBytesize(String bytesize) { this.bytesize = bytesize; }
    public void setStopbits(String stopbits) { this.stopbits = stopbits; }
    public void setParity(String parity) { this.parity = parity; }
}
