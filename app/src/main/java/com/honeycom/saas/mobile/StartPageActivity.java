package com.honeycom.saas.mobile;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.ui.activity.MainActivity;
import com.honeycom.saas.mobile.ui.activity.ReminderActivity;
import com.honeycom.saas.mobile.util.NetworkUtils;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.widget.AgreementDialog;
import com.honeycom.saas.mobile.widget.QMUITouchableSpan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;

/**
* author : zhoujr
* date : 2021/9/9 16:46
* desc : 启动页
*/
public class StartPageActivity extends BaseActivity {
    private final String TAG = "StartPageActivity";

    /*************view*************/
    @BindView(R.id.tv_second)
    TextView tvSecond;
    @BindView(R.id.layout_skip)
    LinearLayout layoutSkip;
    @BindView(R.id.iv_advertising)
    ImageView ivAdvertising;

    /*************object*************/
    private Context mContext;
    private boolean isFirstUse;//是否是第一次使用
    protected CompositeDisposable mDisposable;
    private int initTimeCount;
    private int djs = 0;
    boolean continueCount = true;
    int timeCount = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            countNum();
            if (continueCount) {
                handler.sendMessageDelayed(handler.obtainMessage(-1),1000);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_start_page;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mContext = this;
        if (mDisposable == null) {
            mDisposable = new CompositeDisposable();
        }

        if (NetworkUtils.isConnected()) {
            loadAdvImg();
        }
        initTimeCount = 7;
        isFirstUse = (boolean) SPUtils.getInstance().get("isFirstUse", true);
        if (isFirstUse == true) {
            showAlterpPolicy();
        }else {
            layoutSkip.setVisibility(View.INVISIBLE);
            djs = initTimeCount;
            handler.sendMessageDelayed(handler.obtainMessage(-1),100);
        }

    }

    @Override
    protected void initClick() {
        super.initClick();
        layoutSkip.setOnClickListener(v -> {
            Log.i(TAG,"skip :");
            continueCount = false;
            startHome();
            finish();
        });

    }

    /**
     * 隐私条款协议
     */
    private void showAlterpPolicy() {
        //默认设置为true
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
    }

    /**
     * 倒计时
     * @return
     */
    private int countNum() {//数秒
        timeCount++;
        if (djs >= 0) {
            if (tvSecond!=null) {
                tvSecond.setText(""+ djs);
            }
            djs --;
        }
        if (timeCount == 3) {//数秒，超过3秒后如果没有网络，则进入下一个页面
            if (!NetworkUtils.isConnected()) {
                continueCount = false;
                startHome();
                finish();
            }
            ivAdvertising.setVisibility(View.VISIBLE);
            layoutSkip.setVisibility(View.VISIBLE);
        }
        if (timeCount == initTimeCount) {
            continueCount = false;
            startHome();
            finish();
        }
        return timeCount;
    }

    /**
     * 跳转首页
     */
    private void startHome() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        }, 500);

        Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

//        Intent intent= new Intent(StartPageActivity.this, ExecuteActivity.class);
//        intent.putExtra("url", Constant.text_url);
//        startActivity(intent);
    }

    private void loadAdvImg() {
//        RemoteRepository
//                .getInstance()
//                .getAdMessage()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SingleObserver<AdMessageBean>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        mDisposable.add(d);
//                    }
//
//                    @Override
//                    public void onSuccess(AdMessageBean adMessageBean){
//                        SPUtils.getInstance().put("adUrl", adMessageBean.getAdUrl());
//                        Log.i("get server info", "广告图片地址："+adMessageBean.getAdPictureUrl());
//                        String url = "";
//                        if (adMessageBean == null){
//                            url = "https://hp-dev.obs.cn-east-2.myhuaweicloud.com/screen/file/loading.png?AccessKeyId=W602NAJW6FUOZTMB8K5Y&Expires=1634276792&response-content-disposition=inline&x-obs-security-token=gQpjbi1ub3J0aC00iFsEs2543ts5mAG5NXBW-iJDfns0CSTak0TqnHBjlffUzagNTYfYuCxcc_t7q56WwtqPssJEWQlph_x6j2Yxl-jYLIzgIq4FcrgbVi44ZB0qLklJKebx3oNay1sOZs-bVzzzaM2sJB-9olcpnlEPb-t6ENwD24ntrS1TvS_T_RR6Kpld_sAHSe-k7avY3j86UGfE2CjPljaaEi-geQf0rtBZBFWInB4_4T-UT0v1FKBlmnetzHhBeR3HqVs5-d0H3iyfSRBTBQGGRXPNONbG30p97pOXfABKySgXQNpMnqI3UARje5QOYmM2rpj0JdZGNh2LyTaCBGGEjStFpmDF00--IOkHo2EbKVK2xT2huP8U-K82VPa_iyeu3_T_uorMYabDgrfvUGLOvlfbJYznN8n4aaNb65p-tezYRNB0kHUWW3JyYe9dcGGCsZT6YGh6mpMQ5BlGlk7HryQ1XL44ubWZnYi-RF8TQwB0D9t3e9tbmN3CqJxLlX4j4U1R3BQ-wsa9GHMykmi6m0RwILDyDnX6feYG2BrbDk6b0-x5qHfPw-ao8liRA1jdysRPv9_LJX8OtcqUp_hmYITeopHZC9wEcA8Ik6ZzbkTehPsGkhm10R7aY0EjKkUxrUf9CDu45O4ZpLdjGcUcWn7KLEg3Z63xY4Vz46pKmRNVy0hrk0IHcngQ6qrfqD_O8gVBDaJEwnT6n_k40PTKEO1XSTbe0VLqWT3nmsLkQKv7N5j6JpAsvN9eWipxiUr3RiogFo7pWDYklH0ad5io_A49jKCpxeg%3D&Signature=E7JEcb/TIrg6ms0ok2KREt46wD0%3D";
//                        }else {
//                            url = adMessageBean.getAdPictureUrl();
//                        }
//                        getAdPicture(url, "adv.jpg");//如果没有下载，直接下载
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
////                        view.showError();
//                    }
//                });

        getAdPicture("https://hp-dev.obs.cn-east-2.myhuaweicloud.com/screen/file/loading.png?AccessKeyId=W602NAJW6FUOZTMB8K5Y&Expires=1634276792&response-content-disposition=inline&x-obs-security-token=gQpjbi1ub3J0aC00iFsEs2543ts5mAG5NXBW-iJDfns0CSTak0TqnHBjlffUzagNTYfYuCxcc_t7q56WwtqPssJEWQlph_x6j2Yxl-jYLIzgIq4FcrgbVi44ZB0qLklJKebx3oNay1sOZs-bVzzzaM2sJB-9olcpnlEPb-t6ENwD24ntrS1TvS_T_RR6Kpld_sAHSe-k7avY3j86UGfE2CjPljaaEi-geQf0rtBZBFWInB4_4T-UT0v1FKBlmnetzHhBeR3HqVs5-d0H3iyfSRBTBQGGRXPNONbG30p97pOXfABKySgXQNpMnqI3UARje5QOYmM2rpj0JdZGNh2LyTaCBGGEjStFpmDF00--IOkHo2EbKVK2xT2huP8U-K82VPa_iyeu3_T_uorMYabDgrfvUGLOvlfbJYznN8n4aaNb65p-tezYRNB0kHUWW3JyYe9dcGGCsZT6YGh6mpMQ5BlGlk7HryQ1XL44ubWZnYi-RF8TQwB0D9t3e9tbmN3CqJxLlX4j4U1R3BQ-wsa9GHMykmi6m0RwILDyDnX6feYG2BrbDk6b0-x5qHfPw-ao8liRA1jdysRPv9_LJX8OtcqUp_hmYITeopHZC9wEcA8Ik6ZzbkTehPsGkhm10R7aY0EjKkUxrUf9CDu45O4ZpLdjGcUcWn7KLEg3Z63xY4Vz46pKmRNVy0hrk0IHcngQ6qrfqD_O8gVBDaJEwnT6n_k40PTKEO1XSTbe0VLqWT3nmsLkQKv7N5j6JpAsvN9eWipxiUr3RiogFo7pWDYklH0ad5io_A49jKCpxeg%3D&Signature=E7JEcb/TIrg6ms0ok2KREt46wD0%3D", "adv.jpg");//如果没有下载，直接下载
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


    private void getAdPicture(final String fileUrl, final String fileName) {//获取要展示的广告图片
        if (SPUtils.getInstance().get( "adPictureUrl", "").equals(fileUrl)) {
            Log.i("server info ", "从本地获取图片");
            getLocalPicture((String) SPUtils.getInstance().get("adPictureAddress",""));
        } else {
            Log.i("server info ", "从网络中获取图片");

//            RemoteRepository
//                    .getInstance()
//                    .downLoadFile(fileUrl)
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .map(new Function<ResponseBody, Bitmap>() {
//                        @Override
//                        public Bitmap apply(ResponseBody responseBody){
//                            if (responseBody != null) {
//                                Log.i(TAG,"收到的responseBody不为空！");
//                            }
//                            if (writeResponseBodyToDisk(responseBody, fileName, fileUrl)) {
//                                Bitmap bitmap = BitmapFactory.decodeFile(mContext.getExternalFilesDir(null) + File.separator + fileName);
//                                return bitmap;
//                            }
//                            return null;
//                        }
//                    }).subscribe(new SingleObserver<Bitmap>() {
//                @Override
//                public void onSubscribe(Disposable d) {
//                    mDisposable.add(d);
//                }
//
//                @Override
//                public void onSuccess(Bitmap bitmap){
//                    if (bitmap != null) {
//                        ivAdvertising.setImageBitmap(bitmap);
//                    } else {//加强用户体验，如果是获取到的bitmap为null，则直接跳过
//                        continueCount = false;
//                        startHome();
//                        finish();
//                    }
//                }
//
//                @Override
//                public void onError(Throwable e) {
////                        view.showError();
//                }
//            });

        }

    }

    private void getLocalPicture(String localUrl) {
        Bitmap bitmap = BitmapFactory.decodeFile(localUrl);
        if (bitmap != null) {
            ivAdvertising.setImageBitmap(bitmap);
        } else {//加强用户体验，如果是获取到的bitmap为null，则直接跳过
            continueCount = false;
            startHome();
            finish();
        }
    }


    private boolean writeResponseBodyToDisk(ResponseBody body, String fileName, String fileUrl) {//保存图片到本地
        try {
            // todo change the file location/name according to your needs

            File futureStudioIconFile = new File(mContext.getExternalFilesDir(null) + File.separator + fileName);
            Log.i(TAG,"文件的保存地址为：" + mContext.getExternalFilesDir(null) + File.separator + fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;

                    Log.i(TAG,"file download: " + fileSizeDownloaded / fileSize * 100);
                    Log.i(TAG,"file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();

                SPUtils.getInstance().put("adPictureAddress", mContext.getExternalFilesDir(null) + File.separator + fileName);
                SPUtils.getInstance().put("adPictureUrl", fileUrl);
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        super.onDestroy();
    }
}