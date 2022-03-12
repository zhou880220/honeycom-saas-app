package com.honeycom.saas.mobile;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.honeycom.saas.mobile.push.PushConstants;
import com.honeycom.saas.mobile.push.PushHelper;
import com.honeycom.umeng.UmengClient;
import com.liulishuo.filedownloader.FileDownloader;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.utils.UMUtils;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.oppo.OppoRegister;
import org.android.agoo.xiaomi.MiPushRegistar;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptor;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;


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
        // 友盟统计、登录、分享 SDK，
        UmengClient.init(this);
        //友盟 推送
        initUmengSDK(this);

        //小米推送集成
        MiPushRegistar.register(this, PushConstants.MI_ID, PushConstants.MI_KEY, false);
        //华为推送集成
        HuaWeiRegister.register(this);
        //oppo推送集成
        OppoRegister.register(this, PushConstants.OPPO_KEY, PushConstants.OPPO_SECRET);


        //下载器
        WebViewCacheInterceptorInst.getInstance().init(new WebViewCacheInterceptor.Builder(this));
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

    public static void initUmengSDK(Application application) {

        //日志开关
        UMConfigure.setLogEnabled(true);
        //预初始化
        PushHelper.preInit(application);

        boolean isMainProcess = UMUtils.isMainProgress(application);

        Log.e("_TAG", "------initUmengSDK: ------"+isMainProcess );
        if (isMainProcess) {
            //启动优化：建议在子线程中执行初始化
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PushHelper.init(application);
                }
            }).start();
        } else {
            //若不是主进程（":channel"结尾的进程），直接初始化sdk，不可在子线程中执行
            PushHelper.init(application);
        }
    }




}