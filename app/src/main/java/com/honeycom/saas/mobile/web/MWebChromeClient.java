package com.honeycom.saas.mobile.web;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class MWebChromeClient extends WebChromeClient {
    private Context context;
    private ProgressBar progressBar;
    private OnCloseListener onCloseListener;
    private View mWebError;
    private String mFileName;
    private View mLoadingPage;

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public MWebChromeClient(Context context) {
        this.context = context;
    }

    public MWebChromeClient(Context context, ProgressBar progressBar, View mWebError, View mLoadingPage) {
        this.context = context;
        this.progressBar = progressBar;
        this.mWebError = mWebError;
        this.mLoadingPage = mLoadingPage;
    }

    public MWebChromeClient(Context context, ProgressBar progressBar, View mWebError) {
        this.context = context;
        this.progressBar = progressBar;
        this.mWebError = mWebError;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);

        if (title.contains("404") || title.contains("500") || title.contains("Error") || title.contains("找不到网页") || title.contains("网页无法打开")) {
            mWebError.setVisibility(View.VISIBLE);
        }
        // android 6.0 以下通过title获取判断
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//
//        }
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
            //进度条消失
            if (mLoadingPage != null){
                mLoadingPage.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }else{
                progressBar.setVisibility(View.GONE);
            }
        } else {
            //进度跳显示
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(newProgress);
            //Log.e("wangpan", newProgress + "");
        }

        if (onCloseListener != null) {
            onCloseListener.onCloseClick(newProgress);
        }
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    public interface OnCloseListener {
        void onCloseClick(int progress);
    }


    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }
}
