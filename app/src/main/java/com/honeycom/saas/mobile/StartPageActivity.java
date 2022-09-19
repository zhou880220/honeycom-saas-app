package com.honeycom.saas.mobile;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.http.CallBackUtil;
import com.honeycom.saas.mobile.http.OkhttpUtil;
import com.honeycom.saas.mobile.http.RemoteRepository;
import com.honeycom.saas.mobile.http.bean.AdMessageBean;
import com.honeycom.saas.mobile.http.bean.AdMessagePackage;
import com.honeycom.saas.mobile.http.bean.H5VersionInfo;
import com.honeycom.saas.mobile.http.bean.H5VersionInfoPackage;
import com.honeycom.saas.mobile.push.PushHelper;
import com.honeycom.saas.mobile.ui.activity.HomeActivity;
import com.honeycom.saas.mobile.ui.activity.MainActivity;
import com.honeycom.saas.mobile.ui.activity.ReminderActivity;
import com.honeycom.saas.mobile.util.BaseUtils;
import com.honeycom.saas.mobile.util.Constant;
import com.honeycom.saas.mobile.util.FileDownloadUtils;
import com.honeycom.saas.mobile.util.NetworkUtils;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.util.VersionUtils;
import com.honeycom.saas.mobile.util.ZipUtils;
import com.honeycom.saas.mobile.widget.AgreementDialog;
import com.honeycom.saas.mobile.widget.QMUITouchableSpan;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.umeng.message.PushAgent;
import com.umeng.message.api.UPushRegisterCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
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

//    private String h5_url = "https://hp-prod.obs.cn-east-2.myhuaweicloud.com:443/h5/saas-app.zip?AccessKeyId=F564FOJG9OAAE92CP5NU&Expires=1640203102&response-content-disposition=inline&x-obs-security-token=gQpjbi1ub3J0aC00hkQ1IAOeIE6cCYvzMmfEZio74Gg8wiLJKt1CTFWUhs-3ljQ3UPlPpYOqfDQi4ZvoYuObU9QAFqA2NNwQ4z8RcvX_z_MmRlgRDZ6536W5Ap7FFB1V2S_s6JLwjpXx0EVoxDPyXYWGa-Maiu7KLt3LwS6Vt4u3Xj3oXyeMacK7y0bn6lG2GaWhRF-KVejgp-CvNDWv3jZJkyURInMCOpAOh3ysZfc5an46HMC2Gbzipx16hviSaMdJCmCKum1h1C2qehAqy5pNRp3rq0D0JxwfT4AsR3b6N9fDWV7M9UUca3K51JpTfU4iK1dQ44ccLz5GWZ1NIcMYfp92MLtNpfoZnVnF34Y0vSHZ3M8j015Oui1ccYZQW_SPu18mhVMAV_NgvDtafY-R5M6pKeh9RHxpEJGm15bwDEde5VlD0YTSiSuuRVLY8GfXKYowi9n9as3OhL7GWPBch_vlxHCXcEyr4D216GBsrICetsDiAcSU9LdT1ycvhU1N6COeAZWRYhKs_x7UN5ee_y5Dvk59oDyK1I3jJpjJ4QiR5Imm80IbcqBnrbEuFoVwp-c-rLHcGpFuba0-svn3Sja2tQgSHVlJznUQlyE0vFV21uVNKpNAKOclXOVGsc37gzOBVutUn06zsSeUHpp2d1vJTDSLw5mpwJzNEsnjh1KR7Gom2tUWB2_8C9PITD18voihBYVjT7wH76YBEPqShfrfJPF5f_f4BfZEV2jzrTR-9i4w0z9fzqgyornj9d2n9arkzlP-07wbQEFYhl4HDRpJ9p5c8TonuFE%3D&Signature=yc8TOmex0mDxGokXt2aHN%2BX%2B19M%3D";

    long time = new Date().getTime();

    private static final int ADDRESS_PERMISSIONS_CODE = 200;
    private static final String[] APPLY_PERMISSIONS_APPLICATION = { //相机扫码授权
            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            countNum();
            if (continueCount) {
                handler2.sendMessageDelayed(handler2.obtainMessage(-1),1000);
            }
        }
    };

    //其他句柄
    private  Handler handler = new Handler();

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

        Log.e(TAG, "initWidget: start " + time);
        if (NetworkUtils.isConnected()) {
            String advUrl = (String) SPUtils.getInstance().get("oldAdvUrl","1");;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    loadAdvImg(advUrl);
                }
            });
        }

        initTimeCount = 3;
        isFirstUse = (boolean) SPUtils.getInstance().get("isFirstUse", true);
        if (isFirstUse == true) {
            layoutSkip.setVisibility(View.INVISIBLE);
            showAlterpPolicy();
        }else {
//            layoutSkip.setVisibility(View.INVISIBLE);
            djs = initTimeCount;
            handler2.sendMessage(handler2.obtainMessage(-1));
        }



    }



    @Override
    protected void initClick() {
        super.initClick();
        layoutSkip.setOnClickListener(v -> {
            Log.i(TAG,"skip :");
            continueCount = false;
//            startHome();
            toHome();
//            finish();
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
//                                    startHome();
                                    toHome();
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
        Log.e(TAG, "countNum: "+timeCount );
        timeCount++;
        if (djs >= 0) {
            if (tvSecond!= null) {
                tvSecond.setText(""+ djs);
            }
            djs --;
            Log.e(TAG, "countNum: a " );
        }
        if (timeCount == 3) {//数秒，超过3秒后如果没有网络，则进入下一个页面
            if (!NetworkUtils.isConnected()) {
                continueCount = false;
                toHome();
//                startHome();
            }
            if (ivAdvertising !=null) {
                ivAdvertising.setVisibility(View.VISIBLE);
            }
            if (layoutSkip !=null) {
                layoutSkip.setVisibility(View.VISIBLE);
            }
        }
        if (timeCount == initTimeCount) {
            continueCount = false;
            toHome();
//            startHome();
            Log.e(TAG, "countNum: b " );
        }
        return timeCount;
    }

    /**
     * 跳转首页
     */
    private void startHome() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请READ_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(StartPageActivity.this, APPLY_PERMISSIONS_APPLICATION,
                    ADDRESS_PERMISSIONS_CODE);
        }else {

            Log.e(TAG, "check had Permission ");
            checkH5Version();
        }
    }

    /**
     * 检测h5版本，有新版则下载
     */
    private void checkH5Version() {
        int sysVersion = VersionUtils.getVersion(this);
        Map<String, String> paramsMap =  new HashMap<>();
        paramsMap.put("updateVersion", sysVersion+"");
        paramsMap.put("equipmentType", "1");
        paramsMap.put("platformType", Constant.platform_type);
        String jsonStr = new Gson().toJson(paramsMap);
        Log.e(TAG, "request api: "+Constant.webVersionCheck);
        Log.e(TAG, "request params: "+jsonStr);
        OkhttpUtil.okHttpPostJson(Constant.webVersionCheck, jsonStr, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) { }

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "onResponse2: " + response);
                try{
                    if(!TextUtils.isEmpty(response)) {
                        H5VersionInfoPackage h5VersionInfoPackage = new Gson().fromJson(response, H5VersionInfoPackage.class);
                        String oldVersion = (String) SPUtils.getInstance().get(Constant.H5_VERSION, "");
                        if (h5VersionInfoPackage !=null) {
                            H5VersionInfo h5VersionInfo = h5VersionInfoPackage.getData().get(0);
                            String h5Version = h5VersionInfo.getH5Version();
                            Log.e(TAG, "H5_VERSION: " + h5Version);
                            if (h5Version!=null && !h5Version.equals(oldVersion)) {
                                downH5(h5VersionInfo.getH5UrlAll(), h5Version);
                            }else {
                                toHome();
                            }
                        }else {
                            toHome();
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    toHome();
                    Log.e(TAG, "H5_VERSION check fail !! ");
                }
            }
        });
    }


    /**
     * 获取用户权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ADDRESS_PERMISSIONS_CODE:
                //权限请求失败
                if (grantResults.length == APPLY_PERMISSIONS_APPLICATION.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //弹出对话框引导用户去设置
//                            showDialog();
                            Toast.makeText(StartPageActivity.this, "请求权限被拒绝", Toast.LENGTH_LONG).show();
                            break;
                        } else {

                        }
                    }
                    String isUpdate = (String) SPUtils.getInstance().get("H5VersionName","");
                    if(TextUtils.isEmpty(isUpdate)) {
                        //权限回调
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "down had Permission ");
                                checkH5Version();
                            }
                        });
                    }
                }
                break;
        }
    }

    private void toHome() {

        Log.e(TAG, "initWidget: end use " + (new Date().getTime() - time) +" ms");

        Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
//        intent.setClassName()
        startActivity(intent);
        finish();
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
                        checkH5Version();
//                        finish();
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
            checkH5Version();
//            finish();
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




    /**
     * 第三方插件下载
     */
    private void downH5(String url, String H5VersionName) {
        Log.e(TAG, "initH5Page: start: "+url);
        Log.e(TAG, "initH5Page: H5VersionName: "+H5VersionName);
        FileDownloader.setup(mContext);
        FileDownloader.getImpl().create(url)
                .setPath(mContext.getExternalFilesDir(null)  + File.separator +"fengchaomeiyun" + File.separator +"h5_zip")
                .setForceReDownload(true)
                .setListener(new FileDownloadListener() {
                    //等待
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.e(TAG, "pending: " );
                    }

                    //下载进度回调
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//                            progressBar.setProgress((soFarBytes * 100 / totalBytes));
//                            progressDialog.setProgress((soFarBytes * 100 / totalBytes));
                        Log.e(TAG, "progress: " + (soFarBytes * 100 / totalBytes));
                    }

                    //下载完成
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        String[] split1 = task.getPath().split("0/");
                        Log.e(TAG, "initH5Page: download complete");
                        try {
                            //解压ZIP压缩包
                            ZipUtils.UnZipFolder(Constant.saveH5FilePath, Constant.unH5ZipPath);
                            SPUtils.getInstance().put(Constant.H5_VERSION, H5VersionName);

                            toHome();
                            Log.e(TAG, "initH5Page: load end");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "initH5Page: load error");
                        }
                    }

                    //暂停
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    //下载出错
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        task.getErrorCause().printStackTrace();
                        Toast.makeText(mContext, "系统更新异常", Toast.LENGTH_SHORT).show();

                    }

                    //已存在相同下载
                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.e(TAG, "warn: " + task);
                    }
                }).start();

    }


    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        super.onDestroy();
    }
}