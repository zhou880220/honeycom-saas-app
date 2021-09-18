package com.honeycom.saas.mobile;


import android.content.Intent;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;

import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.ui.activity.MainActivity;
import com.honeycom.saas.mobile.ui.activity.ReminderActivity;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.widget.AgreementDialog;
import com.honeycom.saas.mobile.widget.QMUITouchableSpan;

/**
* author : zhoujr
* date : 2021/9/9 16:46
* desc : 启动页
*/
public class StartPageActivity extends BaseActivity {
    private Handler handler = new Handler();
    private boolean isFirstUse;//是否是第一次使用

    @Override
    protected int getLayoutId() {
        return R.layout.activity_start_page;
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        showAlterpPolicy();
    }

    private void showAlterpPolicy() {
        //默认设置为true
        isFirstUse = (boolean) SPUtils.getInstance().get("isFirstUse", true);
        if (isFirstUse == true) {
            new AgreementDialog(this, generateSp("亲爱的用户，欢迎您信任并使用蜂巢制造云！\n" +
                    "您在使用蜂巢制造云产品或服务前，请认真阅读并充分理解相关用户条款、平台规则及隐私政策。当您点击同意相关条款" +
                    "，并开始使用产品或服务，即表示您已经理解并同意该条款，该条款将构成对您具有法律约束力的文件。" +
                    "用户隐私政策主要包含以下内容：个人信息及设备权限（手机号、用户名、邮箱、设备属性信息、设备位置信息、设备连接信息等）" +
                    "的收集、使用与调用等。您可以通过阅读完整版的《用户协议》和《隐私政策》了解详细信息。如您同意，" +
                    "请点击“同意并继续”开始接受我们的服务"), null, "用户协议")
                    .setBtName("同意", "暂不使用")
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.tv_dialog_ok:
                                    SPUtils.getInstance().put("isFirstUse", false);
                                    //这里是一开始的申请权限，不懂可以看我之前的博客
                                    startHome();
                                    break;
                                case R.id.tv_dialog_no:
                                    finish();
                                    break;
                            }
                        }
                    }).show();
        } else {
            startHome();
        }
    }

    private void startHome() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2500);
    }

    private SpannableString generateSp(String text) {
        //定义需要操作的内容
        String high_light_1 = "《用户协议》";
        String high_light_2 = "《隐私政策》";

        SpannableString spannableString = new SpannableString(text);
        //初始位置
        int start = 0;
        //结束位置
        int end;
        int index;
        //indexOf(String str, int fromIndex): 返回从 fromIndex 位置开始查找指定字符在字符串中第一次出现处的索引，如果此字符串中没有这样的字符，则返回 -1。
        //简单来说，(index = text.indexOf(high_light_1, start)) > -1这部分代码就是为了查找你的内容里面有没有high_light_1这个值的内容，并确定它的起始位置
        while ((index = text.indexOf(high_light_1, start)) > -1) {
            //结束的位置
            end = index + high_light_1.length();
            spannableString.setSpan(new QMUITouchableSpan(this.getResources().getColor(R.color.blue), this.getResources().getColor(R.color.blue),
                    this.getResources().getColor(R.color.white), this.getResources().getColor(R.color.white)) {
                @Override
                public void onSpanClick(View widget) {
                    Intent intent = new Intent(StartPageActivity.this, ReminderActivity.class);
                    intent.putExtra("type", "1");
                    startActivity(intent);
                }
            }, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            start = end;
        }

        start = 0;
        while ((index = text.indexOf(high_light_2, start)) > -1) {
            end = index + high_light_2.length();
            spannableString.setSpan(new QMUITouchableSpan(this.getResources().getColor(R.color.blue), this.getResources().getColor(R.color.blue),
                    this.getResources().getColor(R.color.white), this.getResources().getColor(R.color.white)) {
                @Override
                public void onSpanClick(View widget) {
                    // 点击隐私政策的相关操作，可以使用WebView来加载一个网页
                    Intent intent = new Intent(StartPageActivity.this, ReminderActivity.class);
                    intent.putExtra("type", "2");
                    startActivity(intent);
                }
            }, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            start = end;
        }
        //最后返回SpannableString
        return spannableString;
    }
}