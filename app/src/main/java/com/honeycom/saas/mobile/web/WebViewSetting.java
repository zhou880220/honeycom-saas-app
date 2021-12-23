package com.honeycom.saas.mobile.web;

import android.content.Context;
import android.webkit.WebSettings;

import com.honeycom.saas.mobile.App;

/**
* author : zhoujr
* date : 2021/9/18 15:46
* desc : WebViewSetting
*/
public class WebViewSetting {
    private WebSettings webSettings;

    public WebViewSetting(WebSettings webSettings) {
        this.webSettings = webSettings;
    }

    public static void initweb(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);



        webSettings.setSupportZoom(false);
        webSettings.setTextZoom(100);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAllowFileAccess(true); //设置可以访问文件

        //LOAD_DEFAULT：默认的缓存使用模式。在进行页面前进或后退的操作时，如果缓存可用并未过期就优先加载缓存，否则从网络上加载数据。这样可以减少页面的网络请求次数。
        //LOAD_CACHE_ELSE_NETWORK：只要缓存可用就加载缓存，哪怕它们已经过期失效。如果缓存不可用就从网络上加载数据。
        //LOAD_NO_CACHE：不加载缓存，只从网络加载数据。
        //LOAD_CACHE_ONLY：不从网络加载数据，只从缓存加载数据。
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");

//        String path = App.getContext().getDir("cache", Context.MODE_PRIVATE).getPath();
//        //设置缓存路径
//        webSettings.setAppCachePath(path);
//        //设置缓存大小
//        webSettings.setAppCacheMaxSize(10*1024*1024);
//        //开启缓存
//        webSettings.setAppCacheEnabled(true);

    }
}
