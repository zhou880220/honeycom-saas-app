package com.honeycom.saas.mobile.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.honeycom.saas.mobile.R;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.util.StatusBarCompat;
import com.honeycom.saas.mobile.web.WebViewSetting;

import butterknife.BindView;

/**
* author : zhoujr
* date : 2021/9/18 15:43
* desc : 用户协议与隐私条款
*/
public class ReminderActivity extends BaseActivity {
    private final String TAG = "ReminderActivity_TAG";
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
        Log.e(TAG, "load in ReminderActivity: ");
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        Log.e(TAG, "load in type: "+type);
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        //更改状态栏颜色
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorAccent));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //修改为深色，因为我们把状态栏的背景色修改为主题色白色，默认的文字及图标颜色为白色，导致看不到了。
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (type.equals("1")) {
//            webview("http://mestestweb-product.zhizaoyun.com/app1", "用户协议");
            webview("file:///android_asset/reminder.html", "用户协议");
        } else  {
//            webview("http://mestestweb-product.zhizaoyun.com/app", "隐私政策");
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
