package com.honeycom.saas.mobile;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.http.RemoteRepository;
import com.honeycom.saas.mobile.http.bean.AdMessageBean;
import com.honeycom.saas.mobile.http.bean.AdMessagePackage;
import com.honeycom.saas.mobile.push.PushHelper;
import com.honeycom.saas.mobile.ui.activity.MainActivity;
import com.honeycom.saas.mobile.ui.activity.ReminderActivity;
import com.honeycom.saas.mobile.util.NetworkUtils;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.widget.AgreementDialog;
import com.honeycom.saas.mobile.widget.QMUITouchableSpan;
import com.umeng.message.PushAgent;
import com.umeng.message.api.UPushRegisterCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
* author : zhoujr
* date : 2021/9/9 16:46
* desc : 启动页
*/
public class StartPageActivity extends BaseActivity {
    private final String TAG = "StartPageActivity_TAG";

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
            String advUrl = (String) SPUtils.getInstance().get("oldAdvUrl","1");;
            loadAdvImg(advUrl);
        }
        initTimeCount = 7;
        isFirstUse = (boolean) SPUtils.getInstance().get("isFirstUse", true);
        if (isFirstUse == true) {
            showAlterpPolicy();
//            initPush();
        }else {
            layoutSkip.setVisibility(View.INVISIBLE);
            djs = initTimeCount;
            handler.sendMessageDelayed(handler.obtainMessage(-1),100);
        }

        //获取友盟推送deviceToken
        String deviceToken = PushAgent.getInstance(this).getRegistrationId();
        Log.e(TAG, "deviceToken: "+deviceToken);
        if (TextUtils.isEmpty(deviceToken)) {
            initPush();
        }else {
            SPUtils.getInstance().put("deviceToken", deviceToken);
        }
    }

    //初始化推送
    private void initPush() {
        Log.e(TAG, "init push: ");
        PushHelper.init(getApplicationContext());
        PushAgent.getInstance(getApplicationContext()).register(new UPushRegisterCallback() {
            @Override
            public void onSuccess(final String deviceToken) {
                Log.e(TAG, "init deviceToken: "+deviceToken);
                SPUtils.getInstance().put("deviceToken", deviceToken);
            }

            @Override
            public void onFailure(String code, String msg) {
                Log.e(TAG, "code:" + code + " msg:" + msg);
            }
        });
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
            new AgreementDialog(this, generateSp("亲爱的用户，欢迎您信任并使用蜂巢美云！\n" +
                    "您在使用蜂巢美云产品或服务前，请认真阅读并充分理解相关用户条款、平台规则及隐私政策。当您点击同意相关条款" +
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

    private void loadAdvImg(String fileName) {

        RemoteRepository
                .getInstance()
                .getAdMessage(fileName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<AdMessagePackage>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(AdMessagePackage data){
                        String url = "";
                        Log.e(TAG, "onSuccess: "+ data);
                        if (data !=null) {
                            AdMessageBean adMessageBean = data.getData();
                            Log.i(TAG, "广告图片地址："+adMessageBean.getAdPictureUrl());
                            Log.i(TAG, "广告图片是否更新："+adMessageBean.getUpdate());
                            String localUrl = (String) SPUtils.getInstance().get("adPictureAddress", "");
                            if (localUrl !=null && !localUrl.equals("") && adMessageBean.getUpdate().equals("false")) {
                                //本地加载
                                Log.e(TAG, "load old image ");
                                getLocalPicture((String) SPUtils.getInstance().get("adPictureAddress",""));
                            }else {

                                Log.e(TAG, "load new image ");
                                url = adMessageBean.getAdPictureUrl();
                                String oldAdvUrl  =  adMessageBean.getAdUrl();
                                SPUtils.getInstance().put("oldAdvUrl",oldAdvUrl);
                                getAdPicture(url, "adv.jpg");//如果没有下载，直接下载
                            }
                        }else {
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
//                        view.showError();
                    }
                });
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
            String localUrl = (String) SPUtils.getInstance().get("adPictureAddress","");
            Log.i(TAG, "从本地获取图片:"+localUrl);
            getLocalPicture(localUrl);
        } else {
            Log.i(TAG, "从网络中获取图片");
            RemoteRepository
                    .getInstance()
                    .downLoadFile(fileUrl)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Function<ResponseBody, Bitmap>() {
                        @Override
                        public Bitmap apply(ResponseBody responseBody){
                            if (responseBody != null) {
                                Log.i(TAG,"收到的responseBody不为空！");
                            }
                            if (writeResponseBodyToDisk(responseBody, fileName, fileUrl)) {
                                Bitmap bitmap = BitmapFactory.decodeFile(mContext.getExternalFilesDir(null) + File.separator + fileName);
                                return bitmap;
                            }
                            return null;
                        }
                    }).subscribe(new SingleObserver<Bitmap>() {
                @Override
                public void onSubscribe(Disposable d) {
                    mDisposable.add(d);
                }

                @Override
                public void onSuccess(Bitmap bitmap){
                    if (bitmap != null) {
                        ivAdvertising.setImageBitmap(bitmap);
                    } else {//加强用户体验，如果是获取到的bitmap为null，则直接跳过
                        continueCount = false;
                        startHome();
                        finish();
                    }
                }

                @Override
                public void onError(Throwable e) {
//                        view.showError();
                }
            });

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