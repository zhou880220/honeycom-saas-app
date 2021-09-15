package com.honeycom.saas.mobile;

import android.app.Application;
import android.content.Context;

import com.honeycom.umeng.UmengClient;


//import com.alibaba.android.arouter.launcher.ARouter;

/**
 * Created by zhoujr on 20-4-5.
 */

public class App extends Application {

    private static Context sInstance;

    private static String token = "";

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // TODO:暂时没空适配高版本
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//        }

        // 初始化内存分析工具
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
//        }

//        ARouter.init(this);
//        ToastUtils.init(this);
        // 友盟统计、登录、分享 SDK
        UmengClient.init(this);

    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        App.token = token;
    }

    public static Context getContext() {
        return sInstance;
    }
}