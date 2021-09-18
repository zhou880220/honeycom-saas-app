package com.honeycom.saas.mobile.web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;

/**
 * Created by wangpan on 2020/4/29
 */
public class MyWebViewClient extends BridgeWebViewClient {
    private BridgeWebView webView;
    private Context context;
    private View web_error;
    private String TAG = "TAG";



    private OnCityChangeListener onCityChangeListener;//定义对象

    public void setOnCityClickListener(OnCityChangeListener listener) {
        this.onCityChangeListener = listener;
    }

    public MyWebViewClient(BridgeWebView webView, View web_error) {
        super(webView);
        this.web_error = web_error;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url == null) return false;
        if (url.startsWith("http:") || url.startsWith("https:")) {
            view.loadUrl(url);
            return false;
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } catch (Exception e) {
                // ToastUtils.showShort("暂无应用打开此链接");
            }
        }
        return super.shouldOverrideUrlLoading(view, url); //return 不可更改 否则页面接口会失效 母鸡啊(;≡(▔﹏▔)≡;)
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
//        handler.proceed();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame()) {
            ChangErrorView();
        }
    }

    private void ChangErrorView() {
//        webView.setVisibility(View.GONE);
        web_error.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (onCityChangeListener != null) {
            onCityChangeListener.onCityClick(view.getUrl());
        }
        super.onLoadResource(view, url);
    }

    public interface OnCityChangeListener {
        void onCityClick(String name);
    }
}
