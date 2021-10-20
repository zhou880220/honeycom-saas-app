package com.honeycom.saas.mobile.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.honeycom.saas.mobile.App;
import com.honeycom.saas.mobile.BuildConfig;
import com.honeycom.saas.mobile.R;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.http.UpdateAppHttpUtil;
import com.honeycom.saas.mobile.http.bean.BrowserBean;
import com.honeycom.saas.mobile.http.bean.VersionInfo;
import com.honeycom.saas.mobile.util.CleanDataUtils;
import com.honeycom.saas.mobile.util.Constant;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.util.StatusBarCompat;
import com.honeycom.saas.mobile.util.VersionUtils;
import com.honeycom.saas.mobile.web.MWebChromeClient;
import com.honeycom.saas.mobile.web.MyHandlerCallBack;
import com.honeycom.saas.mobile.web.MyWebViewClient;
import com.honeycom.saas.mobile.web.WebViewSetting;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;

/**
* author : zhoujr
* date : 2021/9/18 15:54
* desc : 系统主页
*/
public class MainActivity  extends BaseActivity {
    private static final String TAG = "MainActivity_TAG";
    private static final int VIDEO_PERMISSIONS_CODE = 1;
    //请求相机
    private static final int REQUEST_CAPTURE = 100;
    //请求相册
    private static final int REQUEST_PICK = 101;
    //二维码 返回码
    private static final int REQUEST_CODE_SCAN = 1;
    //申请权限
    private static final int NOT_NOTICE = 2;//如果勾选了不再询问

    private static final int ADDRESS_PERMISSIONS_CODE = 200;
    private static final String[] APPLY_PERMISSIONS_APPLICATION = { //相机扫码授权
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**********************view******************************/
    @BindView(R.id.NewWebProgressbar)
    ProgressBar mNewWebProgressbar;
    @BindView(R.id.new_Web)
    BridgeWebView mNewWeb;
    @BindView(R.id.web_error)
    View mWebError;
    @BindView(R.id.closeLoginPage)
    ImageView mCloseLoginPage;
    @BindView(R.id.text_policy_reminder)
    TextView mTextPolicyReminder;
    @BindView(R.id.text_policy_reminder_back)
    RelativeLayout mTextPolicyReminderBack;

    /*************************object***************************/
    private Context mContext;
    private String myOrder;
    private String mVersionName = "";
    private String zxIdTouTiao;
    private boolean ChaceSize = true;
    private String totalCacheSize = "";
    private String clearSize = "";
    //调用照相机返回图片文件
    private File tempFile;

    private MyHandlerCallBack.OnSendDataListener mOnSendDataListener;
    private MWebChromeClient myChromeWebClient;
    private WebSettings webSettings;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        mContext = this;

        //更改状态栏颜色
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.status_text));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //修改为深色，因为我们把状态栏的背景色修改为主题色白色，默认的文字及图标颜色为白色，导致看不到了。
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //加载页面
        webView(Constant.text_url);

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                updateApp();
            }
        }, 5000);
    }

    //版本更新
    public void updateApp() {
        int sysVersion = VersionUtils.getVersion(this);
        Log.e(TAG, "updateApp: "+sysVersion);
        new UpdateAppManager
                .Builder()
                //当前Activity
                .setActivity(this)
                //更新地址
                .setUpdateUrl(Constant.WEBVERSION + sysVersion)
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(Exception e) {
                        Log.e(TAG, "updateApp Exception: "+e.getMessage());
                        e.printStackTrace();
                    }
                })
                //实现httpManager接口的对象
                .setHttpManager(new UpdateAppHttpUtil())
                .setTopPic(R.mipmap.top_3)
                //为按钮，进度条设置颜色。
                .setThemeColor(0xff47bbf1)
                .build()
                //为按钮，进度条设置颜色。
                .update();
    }

    /**
     * 初始化webview js交互
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void webView(String url) {
//        String hasUpdate = (String) SPUtils.getInstance().get(Constant.HAS_UDATE, "0");
//        if (hasUpdate.equals("1")) {
//            Log.e(TAG,"----> h5 page has update");
//            mNewWeb.clearCache(true);
//        }

        mNewWeb.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= 19) {
            mNewWeb.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mNewWeb.getSettings().setLoadsImagesAutomatically(false);
        }
        //设置Webview需要的条件
//        setSettings();
        mNewWeb.setDefaultHandler(new MyHandlerCallBack(mOnSendDataListener));
        Log.i(TAG, "load url: "+url);
        mNewWeb.loadUrl(url);

        WebSettings webSettings = mNewWeb.getSettings();
        String userAgentString = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgentString + ";");
        if (webSettings != null) {
            WebViewSetting.initweb(webSettings);
        }

        MWebChromeClient myChromeWebClient = new MWebChromeClient(this, mNewWebProgressbar, mWebError);
        mNewWeb.setWebChromeClient(myChromeWebClient);
        MyWebViewClient myWebViewClient = new MyWebViewClient(mNewWeb, mWebError);
        mNewWeb.setWebViewClient(myWebViewClient);
//
//        //Handler做为通信桥梁的作用，接收处理来自H5数据及回传Native数据的处理，当h5调用send()发送消息的时候，调用MyHandlerCallBack
        mNewWeb.setDefaultHandler(new MyHandlerCallBack(mOnSendDataListener));
//        myChromeWebClient = new MWebChromeClient(this, mNewWebProgressbar, mWebError);
//        MyWebViewClient myWebViewClient = new MyWebViewClient(mNewWeb, mWebError);
        myWebViewClient.setOnCityClickListener(new MyWebViewClient.OnCityChangeListener() {
            @Override
            public void onCityClick(String name) {  //动态监听页面加载链接
                myOrder = name;
                Log.e(TAG, "onCityClick: "+name);

                try {
                    if (name.contains("/api-o/oauth")) {  //偶然几率报错  用try
//                        mApplyBackImage1.setVisibility(View.GONE);
                    } else {
//                        mApplyBackImage1.setVisibility(View.VISIBLE);
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED) {
                            //申请READ_EXTERNAL_STORAGE权限
                            ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
                                    ADDRESS_PERMISSIONS_CODE);
                        }
                    }
                } catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请READ_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
                                ADDRESS_PERMISSIONS_CODE);
                    }
//                    mApplyBackImage1.setVisibility(View.VISIBLE);
                }

//                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                if (name != null) {
//                    if (name.equals(Constant.login_url)) {
//                        mTextPolicyReminder.setVisibility(View.VISIBLE);
//                        mCloseLoginPage.setVisibility(View.VISIBLE);
//                        mTextPolicyReminderBack.setVisibility(View.VISIBLE);
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                    } else if (name.equals(Constant.register_url)) {
//                        mTextPolicyReminder.setVisibility(View.VISIBLE);
//                        mCloseLoginPage.setVisibility(View.VISIBLE);
//                        mTextPolicyReminderBack.setVisibility(View.GONE);
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                    } else if (name.contains("bindPhone")) {
//                        Log.e(TAG, "onCityClick: bind");
//                        mTextPolicyReminder.setVisibility(View.VISIBLE);
//                        mCloseLoginPage.setVisibility(View.VISIBLE);
//                        mTextPolicyReminderBack.setVisibility(View.GONE);
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//                    }  else if (name.contains("/about")) {
//                        mTextPolicyReminder.setVisibility(View.GONE);
//                        mCloseLoginPage.setVisibility(View.GONE);
//                        mTextPolicyReminderBack.setVisibility(View.GONE);
//                    } else {
//                        mTextPolicyReminder.setVisibility(View.GONE);
//                        mCloseLoginPage.setVisibility(View.GONE);
//                        mTextPolicyReminderBack.setVisibility(View.GONE);
//                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);  //SOFT_INPUT_ADJUST_RESIZE
//                    }
//                }
            }
        });
//        mNewWeb.setWebViewClient(myWebViewClient);
//        mNewWeb.setWebChromeClient(myChromeWebClient);
//        mNewWeb.loadUrl(url);
//
//        MWebChromeClient myChromeWebClient = new MWebChromeClient(this, mNewWebProgressbar);
//        mNewWeb.setWebChromeClient(myChromeWebClient);
//        MyWebViewClient myWebViewClient = new MyWebViewClient(mNewWeb, mWebError);
//        mNewWeb.setWebViewClient(myWebViewClient);
        //提供网页加载过程中提供的数据
//        MWebChromeClient mWebChromeClient = new MWebChromeClient(getApplicationContext(), progress_bar);
//        solgan_wv.setWebChromeClient(mWebChromeClient);


        //回退监听
        mNewWeb.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.e(TAG, "onKey: web back  1");
                    if (mNewWeb != null && mNewWeb.canGoBack()) {
                        Log.e(TAG, "onKey: web back  2"+myOrder);
//                        SharedPreferences sb = getSharedPreferences("userInfoSafe", MODE_PRIVATE);
//                        String userInfo = sb.getString("userInfo", "");
                        if (myOrder.contains("/app/home")) { //首页拦截物理返回键  直接关闭应用
                            exit();
                        } else if (myOrder.contains("/information")) { //确保从该页面返回的是首页
                            webView(Constant.text_url);
                        } else {
                            mNewWeb.goBack();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        //登录页，注册页右上角关闭按钮 返回首页
        mCloseLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mNewWeb.canGoBack()) {
                        webView(Constant.text_url);
                        mCloseLoginPage.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //有方法名的都需要注册Handler后使用  获取版本号
        mNewWeb.registerHandler("getVersionName", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!mVersionName.isEmpty()) {
//                        function.onCallBack("V" + mVersionName);
                        int sysVersion = VersionUtils.getVersion(App.getContext());
                        VersionInfo versionInfo = new VersionInfo(mVersionName, sysVersion);
                        function.onCallBack(new Gson().toJson(versionInfo));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * 是否在壳子忠（h5调用）
         */
        mNewWeb.registerHandler("isInSurface", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "isInSurface: " + data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 存储用户信息
         */
        mNewWeb.registerHandler("setCookie", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        zxIdTouTiao = data;
                        Log.e(TAG, "setCookie: " + data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //初始缓存 需用户关闭应用 再次打开
        mNewWeb.registerHandler("getCache", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (ChaceSize == true) {
                        if (!totalCacheSize.isEmpty()) {
                            function.onCallBack(totalCacheSize);
                        }
                    } else {
                        function.onCallBack("0.00MB");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //用户点击清除后的缓存
        mNewWeb.registerHandler("ClearCache", new BridgeHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    CleanDataUtils.clearAllCache(Objects.requireNonNull(MainActivity.this));
                    clearSize = CleanDataUtils.getTotalCacheSize(Objects.requireNonNull(MainActivity.this));
                    if (!clearSize.isEmpty()) {
                        ChaceSize = false;
                        function.onCallBack(clearSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //跳转到拍照界面
        mNewWeb.registerHandler("getTakeCamera", new BridgeHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    //权限判断
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请READ_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                VIDEO_PERMISSIONS_CODE);
                    } else {
                        if (!data.isEmpty()) {
                            String replace1 = data.replace("\"", "");
                            String replace2 = replace1.replace("token:", "");
                            String replace3 = replace2.replace("{", "");
                            String replace4 = replace3.replace("}", "");
                            String[] s = replace4.split(" ");
//                            token1 = s[0];
//                            userid = s[1];
                            gotoCamera();
                        } else {

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //跳转到系统相册界面
        mNewWeb.registerHandler("getPhotoAlbum", new BridgeHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    //权限判断
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请READ_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                VIDEO_PERMISSIONS_CODE);
                    } else {
                        if (!data.isEmpty()) {
                            String replace1 = data.replace("\"", "");
                            String replace2 = replace1.replace("token:", "");
                            String replace3 = replace2.replace("{", "");
                            String replace4 = replace3.replace("}", "");
                            String[] s = replace4.split(" ");
//                            token1 = s[0];
//                            userid = s[1];
                            gotoPhoto();
                        } else {

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //存储用户登录页面传递的信息
        mNewWeb.registerHandler("setUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "获取用户登录信息: " + data);
                    if (!data.isEmpty()) {
                        SPUtils.getInstance().put("userInfo", data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //向页面传递用户登录基本信息
        mNewWeb.registerHandler("getUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    SharedPreferences sb = getSharedPreferences("userInfoSafe", MODE_PRIVATE);
                    String userInfo = sb.getString("userInfo", "");
                    Log.e("saas", userInfo);
                    if (!userInfo.isEmpty()) {
                        function.onCallBack(sb.getString("userInfo", ""));
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //用户点击跳转打开第三方应用
        mNewWeb.registerHandler("showApplyParams", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "跳转第三方:1 " + data);
                    if (!data.isEmpty()) {
                        Map map = new Gson().fromJson(data, Map.class);
//                        String _redirectUrl = (String) map.get("redirectUrl");//"https://mobileclientthird.zhizaoyun.com/jsapi/view/api.html";//
//                        Map<String,String> reqMap = BaseUtils.urlSplit(_redirectUrl);
                        String redirectUrl = (String) map.get("redirectUrl");//"http://172.16.41.239:3001/equipment/app/home?access_token="+reqMap.get("access_token");//
                        String currentUrl = mNewWeb.getUrl();
                        Log.e(TAG, "currentUrl: "+currentUrl );
                        if (!redirectUrl.isEmpty()) {
                            Intent intent;
                            //设备app单独跳转一个页面
                            if (redirectUrl.contains("execute/app")) {
                                intent = new Intent(MainActivity.this, ExecuteActivity.class);
                                intent.putExtra("url", redirectUrl);
//                                intent.putExtra("token", usertoken1);
//                                intent.putExtra("userid", userid1);
//                                intent.putExtra("appId", appId);
                                intent.putExtra("zxIdTouTiao", zxIdTouTiao);
                                intent.putExtra("isFromHome", currentUrl.contains("apply") ? "0": "1");
                                startActivity(intent);
                            }


                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //可能不用
        mNewWeb.registerHandler("openNotification", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
//                    gotoSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //用户点击跳转系统手机拨打电话界面  该接口用于第三方拨打电话
        mNewWeb.registerHandler("openCall", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Map map = new Gson().fromJson(data, Map.class);
                        String num = (String) map.get("num");
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //用户退出登录 清除基本存储信息
        mNewWeb.registerHandler("ClearUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    SPUtils.getInstance().put("userInfoSafe","");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //用户点击跳转手机系统浏览器界面
        mNewWeb.registerHandler("intentBrowser", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Map map = new Gson().fromJson(data, Map.class);
                        String Url = (String) map.get("url");
                        Gson gson = new Gson();
                        BrowserBean browserBean = gson.fromJson(Url, BrowserBean.class);
                        if (!Url.isEmpty()) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(browserBean.getUrl());
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * 分享更具传递的type类型进行分享的页面
         */
        mNewWeb.registerHandler("shareInterface", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                boolean isShareSuc = false;
                try {
                    Log.e(TAG, "shareInterface: " + data);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                function.onCallBack(isShareSuc+"");
            }
        });

        /**
         * 打开扫一扫功能
         */
        mNewWeb.registerHandler("startIntentZing", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "startIntentZing: start" );
                try {
                    // 权限申请
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "handler: no permission");
                        //权限还没有授予，需要在这里写申请权限的代码
                        ActivityCompat.requestPermissions(MainActivity.this,
                                APPLY_PERMISSIONS_APPLICATION, 200);
                    } else {
                        Log.e(TAG, "handler: has permission");
                        ZxingConfig config = new ZxingConfig();
                        config.setShowAlbum(false);
                        Intent intent = new Intent(mContext, CaptureActivity.class);
                        intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);
                        startActivityForResult(intent, REQUEST_CODE_SCAN);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 跳转到照相机
     */
    private void gotoCamera() {
        //	获取图片沙盒文件夹
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //图片名称
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //图片路径
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //创建拍照存储的图片文件
        tempFile = new File(mFilePath);
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

    /**
     * 跳转到相册
     */
    private void gotoPhoto() {
        //跳转到调用系统图库
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "请选择图片"), REQUEST_PICK);
    }

//    private void setSettings() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mNewWeb.getSettings().setSafeBrowsingEnabled(false);
//        }
//        //声明子类
//        webSettings = mNewWeb.getSettings();
//        //js交互
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);
//        //自适应屏幕
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setTextZoom(100);
//        //缩放操作
//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setDisplayZoomControls(false);
//        //细节操作
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setDefaultTextEncodingName("utf-8");
//    }




    // 用来计算返回键的点击间隔时间
    private long exitTime = 0;
    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            //弹出提示，可以有多种方式
            Toast.makeText(mContext, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
//            CleanDataUtils.clearAllCache(Objects.requireNonNull(MainActivity.this));
//            WebStorage.getInstance().deleteAllData();
//            mNewWeb.clearCache(true);
//            mNewWeb.clearHistory();
//            mNewWeb.clearFormData();
        } else {
            finish();
        }
    }


//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
//            Log.e(TAG, "back: back");
//            if (mNewWeb != null && mNewWeb.canGoBack()) {
//                Log.e(TAG, "onClick: 可以返回");
//                if (mWebError.getVisibility() == View.VISIBLE) {
//                    finish();
//                } else {
//                    mNewWeb.goBack();
//                }
//            }else {
//                exit();
//            }
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.e(TAG, "onClick: 可以返回1");
        //回退操作
        if (mNewWeb != null && mNewWeb.canGoBack()) {
            if (mWebError.getVisibility() == View.VISIBLE) {
                finish();
            } else {
                mNewWeb.goBack();
            }
        } else {
            finish();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "onRequestPermissionsResult: "+grantResults.length + "   ---"+APPLY_PERMISSIONS_APPLICATION.length);
        switch (requestCode) {
            case ADDRESS_PERMISSIONS_CODE:
                //权限请求失败
                if (grantResults.length == APPLY_PERMISSIONS_APPLICATION.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //弹出对话框引导用户去设置
                            showDialog();
                            Toast.makeText(mContext, "请求权限被拒绝", Toast.LENGTH_LONG).show();
                            break;
                        } else {
                        }
                    }
                }
                break;
        }
    }

    //弹出提示框
    private void showDialog() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("若您取消权限可能会导致某些功能无法使用！！！")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToAppSetting();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case NOT_NOTICE:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }//由于不知道是否选择了允许所以需要再次判断
                break;
            case REQUEST_CODE_SCAN: //二维码扫描
            {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String stringExtra = data.getStringExtra(Constant.CODED_CONTENT);
                        Log.e(TAG, "stringExtra length: " + stringExtra.length());
                        Log.e(TAG, "onActivityResult: " + stringExtra);
                        mNewWeb.evaluateJavascript("window.sdk.getCodeUrl(\"" + stringExtra + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {

                            }
                        });
                        /**
                         * 一下注释掉的功能延期开放
                         */
                        mNewWeb.callHandler("getCodeUrl", stringExtra, new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {

                            }
                        });
                    }
                }
            }
            break;
        }
    }


}
