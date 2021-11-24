package com.honeycom.saas.mobile.http.bean;

/**
 * Created by zhoujr on 2016/12/30.
 */
public class AdMessageBean {

    String adPictureUrl;//广告图片的地址
    String adUrl;//点击广告图片时，展示详情 webview 的地址
    String update;

    public AdMessageBean(String adPictureUrl, String adUrl) {
        this.adPictureUrl = adPictureUrl;
        this.adUrl = adUrl;
    }

    public String getAdPictureUrl() {
        return adPictureUrl;
    }

    public void setAdPictureUrl(String adPictureUrl) {
        this.adPictureUrl = adPictureUrl;
    }

    public String getAdUrl() {
        return adUrl;
    }

    public void setAdUrl(String adUrl) {
        this.adUrl = adUrl;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    @Override
    public String toString() {
        return "AdMessageBean{" +
                "adPictureUrl='" + adPictureUrl + '\'' +
                ", adUrl='" + adUrl + '\'' +
                ", isUpdate=" + update +
                '}';
    }
}
