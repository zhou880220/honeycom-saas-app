package com.honeycom.saas.mobile.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.honey_create_cloud_pad.R;


/**
 * Created by wangpan on 2020/7/6
 */
public class AgreementDialog extends Dialog {
    private Context context;
    private TextView tv_tittle;
    private TextView tv_content;
    private TextView tv_dialog_ok;
    private TextView tv_dialog_no;
    private String title;
    private SpannableString str;
    private View.OnClickListener mClickListener;
    private String btnName;
    private String no;
    private String strContent;

    public AgreementDialog(@NonNull Context context) {
        super(context);
    }

    //构造方法
    public AgreementDialog(Context context, SpannableString content, String strContent, String title) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.str = content;
        this.strContent = strContent;
        this.title = title;
    }

    public AgreementDialog setOnClickListener(View.OnClickListener onClick) {
        this.mClickListener = onClick;
        return this;
    }

    public AgreementDialog setBtName(String yes, String no) {
        this.btnName = yes;
        this.no = no;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.widget_sprint_user_dialog);
        initView();
    }

    private void initView() {
        tv_dialog_ok = (TextView) findViewById(R.id.tv_dialog_ok);
        tv_tittle = (TextView) findViewById(R.id.tv_sprint_title);
        tv_content = (TextView) findViewById(R.id.tv_sprint_content);
        tv_dialog_no = (TextView) findViewById(R.id.tv_dialog_no);
        tv_content.setMovementMethod(LinkMovementMethod.getInstance());
        if (!TextUtils.isEmpty(btnName)) {
            tv_dialog_ok.setText("同意并继续");
        }
        if (!TextUtils.isEmpty(no)) {
            tv_dialog_no.setText("不同意");
        }
        //设置点击对话框外部不可取消
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(true);
        tv_dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AgreementDialog.this.dismiss();
                if (mClickListener != null) {
                    mClickListener.onClick(tv_dialog_ok);
                }
            }
        });
        tv_dialog_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AgreementDialog.this.dismiss();
                if (mClickListener != null) {
                    mClickListener.onClick(tv_dialog_no);
                }
            }
        });
        if (TextUtils.isEmpty(strContent)) {
            tv_content.setText(str);
        } else {
            tv_content.setText(strContent);
        }
        tv_tittle.setText(title);
    }

}
