package com.honeycom.saas.mobile.web;

import android.content.Context;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * Created by wangpan on 2020/1/7
 */
public class MWebChromeClient2 extends WebChromeClient {
    private Context context;
    private ProgressBar progressBar;
    private OnCityChangeListener onCityChangeListener;//定义对象

    public void setOnCityClickListener(OnCityChangeListener listener) {
        this.onCityChangeListener = listener;
    }

    public MWebChromeClient2(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100){
            if (onCityChangeListener != null) {
//                onCityChangeListener.onCityClick(newProgress);
                progressBar.setVisibility(View.GONE);
            }
        }else{
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(newProgress);
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    public interface OnCityChangeListener {
        void onCityClick(int newProgress);
    }

}
