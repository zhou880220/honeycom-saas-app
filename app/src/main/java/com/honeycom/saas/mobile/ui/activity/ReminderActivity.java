package com.honeycom.saas.mobile.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycom.saas.mobile.R;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.web.WebViewSetting;

import butterknife.BindView;

/**
* author : zhoujr
* date : 2021/9/18 15:43
* desc : 用户协议与隐私条款
*/
public class ReminderActivity extends BaseActivity {

    /******************view*******************/
    @BindView(R.id.Reminder_web)
    WebView mWebView;
    @BindView(R.id.back_image)
    ImageView mBackImage;
    @BindView(R.id.title_text)
    TextView mTitleText;

    /******************prams******************/
    private String  type;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_reminder;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if (type.equals("1")) {
            webview("file:///android_asset/reminder.html", "用户协议");
        } else if (type.equals("2")) {
            webview("file:///android_asset/policy.html", "隐私政策");
        }
    }

    @Override
    protected void initClick() {
        super.initClick();

        mBackImage.setOnClickListener( (V) -> {
            finish();
            }
        );
    }

    private void webview(String fileurl, String titletext) {
        mTitleText.setText(titletext);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        }
        WebSettings settings = mWebView.getSettings();
        if (settings != null) {
            WebViewSetting.initweb(settings);
        }
        mWebView.loadUrl(fileurl);
    }
}
