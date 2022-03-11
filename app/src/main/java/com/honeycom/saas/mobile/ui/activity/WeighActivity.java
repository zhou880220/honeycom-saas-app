package com.honeycom.saas.mobile.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.honeycom.saas.mobile.BuildConfig;
import com.honeycom.saas.mobile.R;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.http.bean.BrowserBean;
import com.honeycom.saas.mobile.util.BaseUtils;
import com.honeycom.saas.mobile.util.Constant;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.util.StatusBarCompat;
import com.honeycom.saas.mobile.util.SystemUtil;
import com.honeycom.saas.mobile.web.MyWebViewClient;
import com.honeycom.saas.mobile.web.WebViewSetting;
import com.honeycom.saas.mobile.ws.DoorOfESSocket;
import com.honeycom.saas.mobile.ws.DoorOfBlueTooth;
import com.honeycom.saas.mobile.ws.DoorOfPrinterDirect;
import com.honeycom.saas.mobile.ws.bean.PrintBean;
import com.honeycom.saas.mobile.ws.DoorOfPrinterBySocket;
import com.honeycom.saas.mobile.ws.server.WSServer;
import com.honeycom.saas.mobile.ws.bean.WeighBean;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
* author : zhoujr
* date : 2021/9/28 17:43
* desc : 称重领料系统
*/
public class WeighActivity extends BaseActivity {

    /********************common prams********************/
    private static final String TAG = "WeighActivity_TAG";
    private Context mContext;
    private String token;
    private String url;
    private String userid;
    private String appId;
    private String fromDetail = "0";
    private String accessToken;

    //二维码 返回码
    private static final int REQUEST_CODE_SCAN = 1;
    //请求相机 返回码
    private static final int REQUEST_CAPTURE = 100;
    //请求相册 返回码
    private static final int REQUEST_PICK = 101;
    private static final String[] APPLY_PERMISSIONS_APPLICATION = { //第三方应用授权
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int ADDRESS_PERMISSIONS_CODE = 1;
    //如果权限勾选了不再询问
    private static final int NOT_NOTICE = 2;
    //修改头像回调handler
    private static final int OPLOAD_IMAGE = 2;
    /******************view**********************/
    @BindView(R.id.NewWebProgressbar)
    ProgressBar mNewWebProgressbar;
    @BindView(R.id.eq_Web)
    BridgeWebView mNewWeb;
    @BindView(R.id.web_error)
    View mWebError;
    @BindView(R.id.glide_gif)
    View mLoadingPage;
    @BindView(R.id.apply_menu_image1)
    ImageView mApplyMenuImage1;
    @BindView(R.id.apply_menu_close)
    ImageView mApplyMenuHome1;

    /******************object**********************/
    //调用照相机返回图片文件
    private File tempFile;
    private HashMap<String, String> hashMap = new HashMap<String, String>();
//    private RecentlyApps recentlyApps;
//    private IWXAPI wxApi;
//    public static Tencent mTencent;
//    private ShareSdkBean shareSdkBean;
    private String goBackUrl;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private Uri imageUriThreeApply;
    private RecyclerView mGridPopup;
//    private MyContactAdapter adapter;
//    private List<RecentlyApps.DataBean> appData;
    private String path;

//    private MessageQueue messageQueue = new MessageQueue(100);
//    private MessageQueue sendQueue = new MessageQueue(100);

//    public Thread listerT = null;
//    public Thread senderT = null;
//    public Thread wsT = null;
//    public BluetoothServer bts = null;

    DoorOfESSocket bp = null;
    DoorOfBlueTooth bpp = null;

    //Handler
    private Handler handler = new Handler(new Handler.Callback()  {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case OPLOAD_IMAGE:
                    Log.e(TAG, "handleMessage: " + msg.obj);
                    String tete = "mytest";
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.evaluateJavascript("window.sdk.double(\"" + tete + "\")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                        }
                    });
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.callHandler("double", "tete", new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                }
                            });
                        }
                    });
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected int getLayoutId() {
        return R.layout.activity_weigh;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mContext = this;

        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        Log.i(TAG, "initData: execute:"+url);
        token = intent.getStringExtra("token");
        userid = intent.getStringExtra("userid");
        appId = intent.getStringExtra("appId");
        fromDetail = intent.getStringExtra("fromDetail");
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        //更改状态栏颜色
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.status_text));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //修改为深色，因为我们把状态栏的背景色修改为主题色白色，默认的文字及图标颜色为白色，导致看不到了。
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        try {
            if (!url.isEmpty()) {
                webView(url);
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mLodingTime();
    }


    /**
     * 初始化点击事件
     */
    @Override
    protected void initClick() {

        //关闭
        mApplyMenuHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewWeb.evaluateJavascript("window.sdk.notification()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    }
                });
                finish();
            }
        });


    }

    /**
     * 初始页加载--进度条加载
     */
    private void mLodingTime() {
        ImageView imageView = findViewById(R.id.image_view);
        int res = R.drawable.loding_gif;
        Glide.with(this).
                load(res).placeholder(res).
                error(res).
                diskCacheStrategy(DiskCacheStrategy.NONE).
                into(imageView);
    }


    /**
     * webview初始化
     *
     * @param url
     */
    @SuppressLint("JavascriptInterface")
    private void webView(String url) {
        if (Build.VERSION.SDK_INT >= 19) {
            mNewWeb.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mNewWeb.getSettings().setLoadsImagesAutomatically(false);
        }
        WebSettings webSettings = mNewWeb.getSettings();
        String userAgentString = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgentString + "; application-center");
        if (webSettings != null) {
            Log.e(TAG, "webView: init");
            WebViewSetting.initweb(webSettings);
        }

        mNewWeb.loadUrl(url);

        //js交互接口定义
//        mNewWeb.addJavascriptInterface(new MJavaScriptInterface(getApplicationContext()), "ApplyFunc");
        wvClientSetting(mNewWeb);



        //回退监听
        mNewWeb.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.e(TAG, "onKey: web back  1");
                    if (mNewWeb != null && mNewWeb.canGoBack()) {
                        Log.e(TAG, "onKey: web back  2"+goBackUrl);
                        if (goBackUrl.contains("/p/home")) { //首页拦截物理返回键  直接关闭应用
                            finish();
                        } else if (goBackUrl.contains("/information")) { //确保从该页面返回的是首页
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

        /**
         * 获取版本号
         */
        mNewWeb.registerHandler("getSystemVersion", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    function.onCallBack("{" + "\"" + "version" + "\"" + ":\"" + "Android" + SystemUtil.getSystemVersion() + "\"" + ",\"" + "model" + "\"" + ":\"" + SystemUtil.getSystemModel() + "\"" + "}");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * 获取手机唯一标识符
         */
        mNewWeb.registerHandler("getIdentifier", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    String imei = SystemUtil.getUniqueIdentificationCode(mContext);
                    function.onCallBack(imei);
                } catch (Exception e) {
                    String id = getId();
                    function.onCallBack(id);
                }
            }
        });
        /**
         * 读取第三方存储信息
         */
        mNewWeb.registerHandler("getStoreData", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    SharedPreferences sb = getSharedPreferences(appId, MODE_PRIVATE);
                    String storeData = sb.getString("storeData", "");
                    Log.e("wangpan", storeData);
                    function.onCallBack(storeData);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        /**
         * 传递用户登录信息
         */
        mNewWeb.registerHandler("getUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    String userInfo = (String) SPUtils.getInstance().get("userInfo", "");
                    if (!userInfo.isEmpty()) {
                        function.onCallBack(userInfo);
                    } else {
                        Toast.makeText(mContext, "获取用户数据异常", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        /**
         * 三方应用拍照
         */
        mNewWeb.registerHandler("setApplyCamera", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
//                        gotoCamera();
                        openImageChooserActivity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 三方应用相册
         */
        mNewWeb.registerHandler("setApplyPhotoAlbum", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        gotoPhoto();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        /**
         * 获取通讯录
         */
        mNewWeb.registerHandler("getMailList", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    String allContancts = "";//getAllContancts(stringBuffer);
                    String substring = allContancts.substring(0, allContancts.length() - 1);//把最后边拼接的逗号去掉
                    function.onCallBack(substring + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 上传文件
         */
        mNewWeb.registerHandler("upLoadFile", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
            }
        });
        /**
         * @param key 用于用户读取临时数据
         */
        mNewWeb.registerHandler("getCookie", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (data != null) {
                        Log.e(TAG, "getCookie: " + data);
                        Map map = new Gson().fromJson(data, Map.class);
                        Set<String> set = map.keySet();
                        Iterator<String> iterator = set.iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            String value = (String) map.get(key);
                            String getCookieValue = (String) hashMap.get(value);
                            Log.e(TAG, "getCookie: " + getCookieValue);
                            function.onCallBack(getCookieValue);
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
                        function.onCallBack("success");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 下载文件
         */
//        mNewWeb.registerHandler("downLoadFile", new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                try {
//                    if (!data.isEmpty()) {
//                        Toast.makeText(mContext, "请稍后...", Toast.LENGTH_SHORT).show();
//                        Map map = new Gson().fromJson(data, Map.class);
//                        String num = (String) map.get("url");
//                        String filename = (String) map.get("filename");
//                        if (filename != null && !filename.equals("")) {
//                            String newReplaceUrl = num.replace(num.substring(num.lastIndexOf("/") + 1), filename);
//                            Log.e(TAG, "新的文件名下载路径: 2" + newReplaceUrl);
//                            List<RecentlyApps.DataBean> Listdata = recentlyApps.getData();
//                            for (int i = 0; i < Listdata.size() - 1; i++) {
//                                String ApplyId = String.valueOf(Listdata.get(i).getAppId());
//                                if (appId.equals(ApplyId)) {
//                                    char[] chars = Listdata.get(i).getAppName().toCharArray();
//                                    String pinYinHeadChar = BaseUtils.getPinYinHeadChar(chars);
//                                    String FileLoad = "zhizaoyun/download/" + pinYinHeadChar + "/";
//                                    downFilePath(FileLoad, newReplaceUrl);
//                                }
//                            }
//                        } else {
//                            Log.e(TAG, "新的文件名下载路径:3 " + num);
//                            List<RecentlyApps.DataBean> Listdata = recentlyApps.getData();
//                            for (int i = 0; i < Listdata.size() - 1; i++) {
//                                String ApplyId = String.valueOf(Listdata.get(i).getAppId());
//                                if (appId.equals(ApplyId)) {
//                                    char[] chars = Listdata.get(i).getAppName().toCharArray();
//                                    String pinYinHeadChar = BaseUtils.getPinYinHeadChar(chars);
//                                    String FileLoad = "zhizaoyun/download/" + pinYinHeadChar + "/";
//                                    downFilePath(FileLoad, num);
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        /**
         * 用户取消授权
         */
        mNewWeb.registerHandler("cancelAuthorization", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 分享更具传递的type类型进行分享的页面
         */
//        mNewWeb.registerHandler("shareInterface", new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                boolean isShareSuc = false;
//                try {
//                    Log.e(TAG, "shareInterface: " + data);
//                    if (!data.isEmpty()) {
//                        //微信初始化
//                        wxApi = WXAPIFactory.createWXAPI(mContext, Constant.APP_ID);
//                        wxApi.registerApp(Constant.APP_ID);
//                        //QQ初始化
//                        mTencent = Tencent.createInstance(Constant.QQ_APP_ID, mContext);
//
//                        Map map = JSONObject.parseObject(data, Map.class);
//                        String num = (String) map.get("obj");
//                        Map mapType = JSONObject.parseObject(num, Map.class);
//                        int type = (int) mapType.get("type");
//                        String value = String.valueOf(mapType.get("data"));
//                        Gson gson = new Gson();
//                        ShareSdkBean shareSdkBean = gson.fromJson(value, ShareSdkBean.class);
//                        if (type == 1) {
//                            boolean wxAppInstalled = isWxAppInstalled(mContext);
//                            if (wxAppInstalled == true) {
//                                isShareSuc = true;
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        wechatShare(0, shareSdkBean); //好友
//                                    }
//                                }).start();
//                            } else {
//                                Toast.makeText(mContext, "手机未安装微信", Toast.LENGTH_SHORT).show();
//                            }
//                        } else if (type == 2) {
//                            boolean wxAppInstalled1 = isWxAppInstalled(mContext);
//                            if (wxAppInstalled1 == true) {
//                                isShareSuc = true;
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        wechatShare(1, shareSdkBean); //朋友圈
//                                    }
//                                }).start();
//                            } else {
//                                Toast.makeText(mContext, "手机未安装微信", Toast.LENGTH_SHORT).show();
//                            }
//                        } else if (type == 3) {
//                            boolean qqClientAvailable = BaseUtils.isQQClientAvailable(mContext);
//                            if (qqClientAvailable == true) {
//                                isShareSuc = true;
//                                qqFriend(shareSdkBean);
//                            } else {
//                                Toast.makeText(mContext, "手机未安装QQ", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                function.onCallBack(isShareSuc+"");
//            }
//        });

        /**
         * 用户登录异常回跳登录页
         */
        mNewWeb.registerHandler("goLogin", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "用户登录异常回跳登录页: ");
                try {
                    SPUtils.getInstance().put("apply_url", Constant.login_url);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 用户登录异常回跳首页
         */
        mNewWeb.registerHandler("backHome", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    SPUtils.getInstance().put("apply_url", Constant.text_url);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 关闭当前页
         */
        mNewWeb.registerHandler("closePage", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler: close page");
                finish();
            }
        });
        /**
         * 用户打开系统浏览器
         */
        mNewWeb.registerHandler("intentBrowser", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Gson gson = new Gson();
                        Map map = gson.fromJson(data, Map.class);
                        String Url = (String) map.get("url");
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
         * 拨打电话
         */
        mNewWeb.registerHandler("openCall", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Log.e(TAG, "openCall: 1" + data);
                        Gson gson = new Gson();
                        Map map = gson.fromJson(data, Map.class);
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
        /**
         * 跳转支付页面，传递商品信息
         */
//        mNewWeb.registerHandler("purchaseOfEntry", new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                try {
//                    if (!data.isEmpty()) {
//                        Map map = JSONObject.parseObject(data, Map.class);
//                        String num = (String) map.get("obj");
//                        if (!num.isEmpty()) {
//                            Intent intent = new Intent(mContext, IntentOpenActivity.class);
//                            intent.putExtra("PurchaseOfEntry", num);
//                            intent.putExtra("appId", appId);
//                            intent.putExtra("token", token);
//                            startActivity(intent);
//                            Log.e(TAG, "商品信息1: " + num);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        /**
         * 存储用户信息
         */
        mNewWeb.registerHandler("setCookie", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "setCookie: " + data);
                try {
                    if (!data.isEmpty()) {
                        Gson gson = new Gson();
                        Map map = gson.fromJson(data, Map.class);
                        String num = (String) map.get("str");
                        String cookieKey = "key";
                        String cookieValue = "value";
                        ArrayList<Object> list = new ArrayList<>();
                        List objects = gson.fromJson(num, List.class);
                        if (objects != null && objects.size() > 0) {
                            for (Object o : objects) {
                                if (o != null) {
                                    Map JsonMap = gson.fromJson(o.toString(), Map.class);
                                    String key = (String) JsonMap.get(cookieKey);
                                    String value = (String) JsonMap.get(cookieValue);
                                    hashMap.put(key, value);
                                }
                            }
                        }
                        Log.e(TAG, "setCookie: " + num);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 打开扫一扫功能
         */
        mNewWeb.registerHandler("startIntentZing", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "startIntentZing: ");
                try {
                    ZxingConfig config = new ZxingConfig();
                    config.setShowAlbum(false);
                    Intent intent = new Intent(mContext, CaptureActivity.class);
                    intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 拨打电话
         */
        mNewWeb.registerHandler("OpenPayIntent", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Log.e(TAG, "打开通讯录: " + data);
                        Gson gson = new Gson();
                        Map map = gson.fromJson(data, Map.class);
                        String tele = (String) map.get("tele");
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tele));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //打开手机系统通知界面
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

        mNewWeb.registerHandler("shareSDKData", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "shareSDKData: " + data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * 获取手机Ip地址
         */
        mNewWeb.registerHandler("getIP", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "getIP: start");
                try {
                    String ip  = wifiIpAddress();
                    Log.e(TAG, "getIP: "+ip);
                    function.onCallBack(wifiIpAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mNewWeb.registerHandler("getCurrESData", (data, function) -> {
            try {
                function.onCallBack(WSServer.currentMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mNewWeb.registerHandler("switchNetwork", new BridgeHandler() {
            @Override
            synchronized public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "switchNetwork: start: "+data);
                try {
                    Gson gson = new Gson();
                    WeighBean weighBean =  gson.fromJson(data, WeighBean.class);
                    if (bp == null) bp = new DoorOfESSocket("6001");
                    if (bp != null) {
                        bp.switchNetwork(weighBean.getIp(), weighBean.getPort());
                    }
                    function.onCallBack("done");
                } catch (Exception e) {
                    Log.e(TAG, "switchNetwork: error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        mNewWeb.registerHandler("sendInstructToES", new BridgeHandler() {
            @Override
            synchronized public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "push instruct by jsbridge "+data);
                try {
                    DoorOfESSocket.pushMsgByCurrConn(data);
                    function.onCallBack("done");
                } catch (Exception e) {
                    Log.e(TAG, "switchNetwork: error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        mNewWeb.registerHandler("sendInstructToES", new BridgeHandler() {
            @Override
            synchronized public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "push instruct by jsbridge "+data);
                try {
                    DoorOfESSocket.pushMsgByCurrConn(data);
                    function.onCallBack("done");
                } catch (Exception e) {
                    Log.e(TAG, "switchNetwork: error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        //打印
        mNewWeb.registerHandler("printDirect", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "printDirect: "+data);
                try {
                    if (DoorOfPrinterDirect.run(data)) {
                        function.onCallBack("success");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                function.onCallBack("failed.");
            }
        });

        //创建蓝牙
        mNewWeb.registerHandler("createBluetooth", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (bpp == null) bpp = new DoorOfBlueTooth();
                    if (bpp.initBT(data)) {
                        function.onCallBack("success.");
                        return;
                    }
                    function.onCallBack("failed.");
                } catch (Exception e){
                    function.onCallBack("failed.");
                }
            }
        });

        //蓝牙打印
        mNewWeb.registerHandler("printByBluetooth", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "printByBluetooth: start" + data);
                    if (bpp == null) {
                        function.onCallBack("print bpp is null.");
                        return;
                    }
                    if (DoorOfBlueTooth.btInitStatus == 0) {
                        function.onCallBack("打印机初始化不成功.");
                        return;
                    }
                    if (bpp.BTPrint(data)) {
                        function.onCallBack("success.");
                        return;
                    }
                    function.onCallBack("failed.");
                } catch (Exception e){
                    function.onCallBack("failed.");
                }
            }
        });

        //关闭蓝牙
        mNewWeb.registerHandler("closeBluetooth", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "closeBluetooth: start" + data);
                    if (bpp == null) return;
                    if (bpp == null) {
                        function.onCallBack("close but bpp is null.");
                        return;
                    }
                    bpp.BTClose();
                    bpp = null;
                    function.onCallBack("bt close success.");
                } catch (Exception e){
                    function.onCallBack("failed.");
                    e.printStackTrace();
                }
            }
        });



    }


    /**
     * 获取wifi地址
     * @return
     */
    public  String wifiIpAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }


    /**
     * webview监听
     *
     * @param ead_web
     */
    private void wvClientSetting(BridgeWebView ead_web) {
        MyWebViewClient myWebViewClient = new MyWebViewClient(ead_web, mWebError);
        ead_web.setWebViewClient(myWebViewClient);
        myWebViewClient.setOnCityClickListener(new MyWebViewClient.OnCityChangeListener() {
            @Override
            public void onCityClick(String name) {
                goBackUrl = name;
                Log.e(TAG, "onCityClick: " + name);
//                WebBackForwardList webBackForwardList = mNewWeb.copyBackForwardList();
//                boolean b = webBackForwardList.getCurrentIndex() != webBackForwardList.getSize() - 1;
                try {
                    if (name.contains("/api-o/oauth")) {  //偶然几率报错  用try
//                        mApplyBackImage1.setVisibility(View.GONE);
                    } else {
//                        mApplyBackImage1.setVisibility(View.VISIBLE);
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED) {
                            //申请READ_EXTERNAL_STORAGE权限
                            Log.e(TAG, "onCityClick: no permission" );
                            ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                                    ADDRESS_PERMISSIONS_CODE);
                        }
                    }
                } catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请READ_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                                ADDRESS_PERMISSIONS_CODE);
                    }
//                    mApplyBackImage1.setVisibility(View.VISIBLE);
                }
            }
        });
//        MWebChromeClient mWebChromeClient = new MWebChromeClient(this, mNewWebProgressbar, mWebError, mLoadingPage);
//        ead_web.setWebChromeClient(mWebChromeClient);

        ead_web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //进度条消失
                    if (mLoadingPage != null) {
                        mLoadingPage.setVisibility(View.GONE);
                        mNewWebProgressbar.setVisibility(View.GONE);
                    } else {
                        mNewWebProgressbar.setVisibility(View.GONE);
                    }
                } else {
                    //进度跳显示
                    if (mNewWebProgressbar !=null) {
                        mNewWebProgressbar.setVisibility(View.VISIBLE);
                        mNewWebProgressbar.setProgress(newProgress);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openFileChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openFileChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openFileChooserActivity();
            }

            // For Android >= 5.0 打开系统文件管理系统
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    Log.e(TAG, "onCityClick: no permission camera" );
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    Log.e(TAG, "onCityClick: no permission record" );
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    Log.e(TAG, "onCityClick: no permission storage" );
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }else {
                    Log.e(TAG, "onCityClick: have permission" );
                    String[] acceptTypes = fileChooserParams.getAcceptTypes();
                    boolean isphoto = fileChooserParams.isCaptureEnabled();
                    int i = fileChooserParams.getMode();
                    Log.i(TAG, "onShowFileChooser: "+isphoto + "  i="+i);
                    uploadMessageAboveL = filePathCallback;
                    Log.e(TAG, "onShowFileChooser:这个是什么鬼 " + acceptTypes[0]);

                    if (acceptTypes[0].equals("image/*") && isphoto && i  == FileChooserParams.MODE_OPEN) {
                        Log.e(TAG, "start capture");
                        openImageCaptureActivity();//打开系统拍照及相册选取
                    }else if (acceptTypes[0].equals("*/*")) {
                        openFileChooserActivity(); //文件系统管理
                    } else if (acceptTypes[0].equals("image/*")) {
                        Log.e(TAG, "onShowFileChooser: 1");
                        openImageChooserActivity();//打开系统拍照及相册选取
                    } else if (acceptTypes[0].equals("video/*")) {
                        openVideoChooserActivity();//打开系统拍摄/选取视频
                    }
                }
                return true;
            }
        });
    }

    //获取手机唯一标识
    private String getId() {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
//        try {
//            //IMEI（imei）
//            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            @SuppressLint("MissingPermission") String imei = tm.getDeviceId();
//            if (!TextUtils.isEmpty(imei)) {
//                deviceId.append("imei");
//                deviceId.append(imei);
//                return deviceId.toString();
//            }
//            //序列号（sn）
//            @SuppressLint("MissingPermission") String sn = tm.getSimSerialNumber();
//            if (!TextUtils.isEmpty(sn)) {
//                deviceId.append("sn");
//                deviceId.append(sn);
//                return deviceId.toString();
//            }
//            //如果上面都没有， 则生成一个id：随机码
//            String uuid = getUUID();
//            if (!TextUtils.isEmpty(uuid)) {
//                deviceId.append(uuid);
//                return deviceId.toString();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
        deviceId.append(getUUID());
//        }
        return deviceId.toString();
    }

    /**
     * 得到全局唯一UUID
     */
    private String uuid;

    public String getUUID() {
        SharedPreferences mShare = getSharedPreferences("uuid", MODE_PRIVATE);
        if (mShare != null) {
            uuid = mShare.getString("uuid", "");
        }
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            mShare.edit().putString("uuid", uuid).commit();
        }
        return uuid;
    }

    /**
     * 跳转到相册
     */
    private void gotoPhoto() {
        //跳转到调用系统图库
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "请选择图片"), REQUEST_PICK);
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
//        tempFile = new File(FileUtil.checkDirPath(Environment.getExternalStorageDirectory().getPath() + "/image/"), System.currentTimeMillis() + ".jpg");
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
        Log.i(TAG, "start gotoCamera: ");
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

//    /**
//     * 第三方插件下载
//     * @param fileLoad
//     * @param downPath
//     */
//    private void downFilePath(String fileLoad, String downPath) {
//        FileDownloader.setup(mContext);
//        FileDownloader.getImpl().create(downPath)
//                .setPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileLoad + BaseUtils.getNameFromUrl(downPath))
//                .setForceReDownload(true)
//                .setListener(new FileDownloadListener() {
//                    //等待
//                    @Override
//                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//
//                    }
//
//                    //下载进度回调
//                    @Override
//                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
////                            progressBar.setProgress((soFarBytes * 100 / totalBytes));
////                            progressDialog.setProgress((soFarBytes * 100 / totalBytes));
//                        Log.e(TAG, "progress: " + (soFarBytes * 100 / totalBytes));
//                    }
//
//                    //下载完成
//                    @Override
//                    protected void completed(BaseDownloadTask task) {
//                        String[] split1 = task.getPath().split("0/");
//                        Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();
//                        new AlertDialog.Builder(mContext)
//                                .setTitle("保存路径：")
//                                .setMessage(split1[1])
//                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                    }
//                                })
//                                .show();
//                    }
//
//                    //暂停
//                    @Override
//                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//
//                    }
//
//                    //下载出错
//                    @Override
//                    protected void error(BaseDownloadTask task, Throwable e) {
//                        Toast.makeText(mContext, "下载异常", Toast.LENGTH_SHORT).show();
//                    }
//
//                    //已存在相同下载
//                    @Override
//                    protected void warn(BaseDownloadTask task) {
//                        Log.e(TAG, "warn: " + task);
//                    }
//                }).start();

//    }

//    /**
//     * 判断微信是否安装
//     *
//     * @param context
//     * @return true 已安装   false 未安装
//     */
//    public static boolean isWxAppInstalled(Context context) {
//        IWXAPI wxApi = WXAPIFactory.createWXAPI(context, null);
//        wxApi.registerApp(Constant.APP_ID);
//        boolean bIsWXAppInstalled = false;
//        bIsWXAppInstalled = wxApi.isWXAppInstalled();
//        return bIsWXAppInstalled;
//    }

//    /**
//     * @param flag (0:分享到微信好友，1：分享到微信朋友圈)
//     */
//    private void wechatShare(int flag, ShareSdkBean shareSdkBean) {
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = shareSdkBean.getUrl();
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        msg.title = shareSdkBean.getTitle();
//        msg.description = shareSdkBean.getTxt();
//        //这里替换一张自己工程里的图片资源
////        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.wechat);
//        Bitmap thumb = null;
//        try {
//            thumb = BitmapFactory.decodeStream(new URL(shareSdkBean.getIcon()).openStream());
////注意下面的这句压缩，120，150是长宽。
////一定要压缩，不然会分享失败
//            Bitmap thumbBmp = BaseUtils.compressImage(thumb);
////Bitmap回收
////            bitmap1.recycle();
//            msg.thumbData = BaseUtils.bmpToByteArray(thumbBmp, true);
////      msg.setThumbImage(thumb);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        msg.setThumbImage(bitmap1);
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
////        req.transaction = String.valueOf(System.currentTimeMillis());
//        req.message = msg;
//        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
//        wxApi.sendReq(req);
//    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 发送给QQ朋友
     */
    int shareType = 1;
    //IMG
    public static String IMG = "";
    int mExtarFlag = 0x00;

//    private void qqFriend(ShareSdkBean shareSdkBean) {
//        final Bundle params = new Bundle();
//        //
//        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareSdkBean.getTitle()); //分享的标题
//        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareSdkBean.getUrl());//分享的链接
//        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareSdkBean.getTxt());//分享的摘要
//
//        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareSdkBean.getIcon());//分享的图片
////        params.putString(shareType == QQShare.SHARE_TO_QQ_TYPE_IMAGE ? QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL
////                : QQShare.SHARE_TO_QQ_IMAGE_URL, IMG);
//        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, getPackageName());
//        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
//        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
//
//        doShareToQQ(params);
//        return;
//    }

//    private void doShareToQQ(final Bundle params) {
//
//        // QQ分享要在主线程做
//        handler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (null != mTencent) {
//                    mTencent.shareToQQ(EquipmentActivity.this, params, qqShareListener);
//                }
//            }
//        });
//    }

//    IUiListener qqShareListener = new IUiListener() {
//        @Override
//        public void onCancel() {
//            if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
//            }
//        }
//
//        @Override
//        public void onComplete(Object response) {
//        }
//
//        @Override
//        public void onError(UiError e) {
//        }
//    };

    /**
     * 跳转到用户拍照/选取相册
     */
    public void openImageCaptureActivity() {
        //	获取图片沙盒文件夹
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //图片名称
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //图片路径
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //创建拍照存储的图片文件
        tempFile = new File(mFilePath);
        //相册相机选择窗
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
            captureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        } else {
            imageUriThreeApply =  Uri.fromFile(tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        }
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);

//        Intent Photo = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
//        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
        Log.e(TAG, "openImageCaptureActivity: ");
        startActivityForResult(captureIntent, FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * 跳转到用户拍照/选取相册
     */
    public void openImageChooserActivity() {
//        String filePath = Environment.getExternalStorageDirectory() + File.separator
//                + Environment.DIRECTORY_PICTURES + File.separator;
//        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
//        String _file = filePath + fileName;
//        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
//            captureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File( _file));
//            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
//            Log.e(TAG, "openImageChooserActivity: fileprovider");
//        } else {
//            imageUriThreeApply =  Uri.fromFile(new File( _file));
//            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
//            Log.e(TAG, "openImageChooserActivity: urI");
//        }

        //	获取图片沙盒文件夹
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //图片名称
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //图片路径
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //创建拍照存储的图片文件
        tempFile = new File(mFilePath);
        //跳转到调用系统相机
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
            captureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        } else {
            imageUriThreeApply =  Uri.fromFile(tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        }
        Log.i(TAG, "start gotoCamera: ");

//        imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File( _file));//Uri.fromFile(new File(filePath + fileName));
        //相册相机选择窗

//        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);

        Intent Photo = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        Log.e(TAG, "openImageChooserActivity: ");
        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

    }

    /**
     * 跳转到系统文件选择
     */
    public void openFileChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");//文件上传
        startActivityForResult(i, FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * 跳转到用户拍摄/选取视频
     */
    public void openVideoChooserActivity() {
        backgroundAlpha(this, 0.5f);//0.0-welcome1.0
        View centerView = LayoutInflater.from(mContext).inflate(R.layout.video_chooser_popup, null);
        PopupWindow videoPopupWindow = new PopupWindow(centerView, ViewGroup.LayoutParams.MATCH_PARENT,
                465);
        videoPopupWindow.setTouchable(true);
        videoPopupWindow.setFocusable(false);
        videoPopupWindow.setOutsideTouchable(false);
        videoPopupWindow.setAnimationStyle(R.style.pop_animation);
        videoPopupWindow.showAtLocation(centerView, Gravity.BOTTOM, 0, 0);
//        mGridPopup = centerView.findViewById(R.id.grid_popup);
//        Photograph_popup
//                Photo_album_popup
        Button mPhotoGraphPopupButton = centerView.findViewById(R.id.Photo_graph_popup); //用户点击拍摄按钮
        Button mPhotoAlbumPopup = centerView.findViewById(R.id.Photo_album_popup);      //用户点击视频选取按钮
        Button mDismissPopupButton = centerView.findViewById(R.id.video_dismiss_popup_button);  //用户点击取消按钮

        mPhotoGraphPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(WeighActivity.this, 1f);//0.0-welcome1.0
                videoPopupWindow.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                //限制时长
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                //开启摄像机
                startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
            }
        });

        mPhotoAlbumPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(WeighActivity.this, 1f);//0.0-welcome1.0
                videoPopupWindow.dismiss();

                if (Build.BRAND.equals("Huawei")) {
                    Intent intentPic = new Intent(Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentPic, FILE_CHOOSER_RESULT_CODE);
                }
                if (Build.BRAND.equals("Xiaomi")) {//是否是小米设备,是的话用到弹窗选取入口的方法去选取视频
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
                    startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), FILE_CHOOSER_RESULT_CODE);
                } else {//直接跳到系统相册去选取视频
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/*");
                    }
                    startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), FILE_CHOOSER_RESULT_CODE);
                }
            }
        });

        mDismissPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(WeighActivity.this, 1f);//0.0-welcome1.0
                videoPopupWindow.dismiss();
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                } else if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
            }
        });


    }

    /**
     * papawin设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        //回退操作
//        Log.e(TAG, "onClick: 可以返回"+mNewWeb.canGoBack());
//        if (mNewWeb != null && mNewWeb.canGoBack()) {
//            if (mWebError.getVisibility() == View.VISIBLE) {
//                Log.e(TAG, "back: finish");
//                finish();
//            } else {
//                Log.e(TAG, "back: last page");
//                mNewWeb.goBack();
//            }
//        } else {
//            finish();
//        }
//    }

    /**
     * 系统回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Log.e(TAG, "onActivityResult: choose image back"+data);
            if (data != null) {
                if (null == uploadMessage && null == uploadMessageAboveL) return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                // Uri result = (((data == null) || (resultCode != RESULT_OK)) ? null : data.getData());
                if (result == null) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    } else if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    }
                }
                if (uploadMessageAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data, result);
                } else if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(result);
                    uploadMessage = null;
                }
            } else if (imageUriThreeApply != null) {
                Log.e(TAG, "onActivityResult: choose image 3 "+imageUriThreeApply);
                uploadMessageAboveL.onReceiveValue(new Uri[]{imageUriThreeApply});
            } else {
                //这里uploadMessage跟uploadMessageAboveL在不同系统版本下分别持有了
                //WebView对象，在用户取消文件选择器的情况下，需给onReceiveValue传null返回值
                //否则WebView在未收到返回值的情况下，无法进行任何操作，文件选择器会失效
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                } else if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
            }
        } else {
            //这里uploadMessage跟uploadMessageAboveL在不同系统版本下分别持有了
            //WebView对象，在用户取消文件选择器的情况下，需给onReceiveValue传null返回值
            //否则WebView在未收到返回值的情况下，无法进行任何操作，文件选择器会失效
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            } else if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
        }

//        if (requestCode == Constants.REQUEST_QQ_SHARE) {
//            Tencent.onActivityResultData(requestCode, resultCode, data, qqShareListener);
//        }

        switch (requestCode) {
            case NOT_NOTICE:
                if (ContextCompat.checkSelfPermission(WeighActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }//由于不知道是否选择了允许所以需要再次判断
                break;
            case REQUEST_CODE_SCAN: //二维码扫描
            {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String stringExtra = data.getStringExtra(Constant.CODED_CONTENT);
                        Log.e(TAG, "stringExtra length: "+ stringExtra.length());
                        Log.e(TAG, "getCodeUrl: "+ stringExtra);
//                        mNewWeb.evaluateJavascript("window.sdk.getCodeUrl(\"" + stringExtra + "\")", new ValueCallback<String>() {
//                            @Override
//                            public void onReceiveValue(String value) {
//
//                            }
//                        });
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
            case REQUEST_CAPTURE://调用系统相机返回
            {
                if (resultCode == RESULT_OK) {
                    gotoClipActivity(Uri.fromFile(tempFile));
                } else if (resultCode == RESULT_CANCELED) {
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "取消" + "\")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                            mNewWeb.callHandler("AlreadyPhoto", "取消", new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                }
                            });
                        }
                    });
                }
            }
            break;
            case REQUEST_PICK://调用系统相册返回
            {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String realPathFromUri = BaseUtils.getRealPathFromURI(this, uri);
                    if (realPathFromUri.endsWith(".jpg") || realPathFromUri.endsWith(".png") || realPathFromUri.endsWith(".jpeg")) {
                        gotoClipActivity(uri);
                    } else {
                        mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "取消" + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                            }
                        });
                        mNewWeb.callHandler("AlreadyPhoto", "取消", new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                            }
                        });
                        Toast.makeText(this, "选择的格式不对,请重新选择", Toast.LENGTH_SHORT).show();
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "取消" + "\")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                            mNewWeb.callHandler("AlreadyPhoto", "取消", new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                }
                            });
                        }
                    });
                }
            }
            break;

//            case REQ_CLIP_AVATAR: //图片裁剪
//            {
//                if (resultCode == RESULT_OK) {
//                    final Uri uri = data.getData();
//                    if (uri == null) {
//                        return;
//                    }
//                    String cropImagePath = FileUtil.getRealFilePathFromUri(getApplicationContext(), uri);
//                    Log.e(TAG, "onActivityResult: " + cropImagePath);
//                    takePhoneUrl(cropImagePath);
//                } else {
//                    mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "取消" + "\")", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String value) {
//                        }
//                    });
//                    mNewWeb.callHandler("AlreadyPhoto", "取消", new CallBackFunction() {
//                        @Override
//                        public void onCallBack(String data) {
//                        }
//                    });
//                }
//            }
//            break;
            default:
                break;
        }
    }

    //TODO 手机收到推送消息 调用页面 deviceTaskNotice

    /**
     * 打开截图界面
     */
    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
//        ClipImageActivity.goToClipActivity(this, uri);
    }

    // 选择内容回调到Html页面
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent, Uri uri) {
        Log.e(TAG, "onActivityResultAboveL: requestCode:"+requestCode);
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        if ("file".equalsIgnoreCase(intent.getScheme())) {//使用第三方应用打开
            path = intent.getDataString();
            return;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
            path = getPath(this, uri);
        } else {//4.4以下下系统调用方法
            path = BaseUtils.getRealPathFromURI(this, uri);
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                    if (path == null) {

                        String nameFromUrl = BaseUtils.getNameFromUrl(uri.toString());
                        Log.e(TAG, "onActivityResultAboveL: getFileInfo:"+nameFromUrl);
                        mNewWeb.evaluateJavascript("window.sdk.getFileInfo(\"" + nameFromUrl + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {

                            }
                        });
                        /**
                         * 一下注释掉的功能延期开放
                         */
                        mNewWeb.callHandler("getFileInfo", nameFromUrl, new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {

                            }
                        });
                    } else {
                        String nameFromUrl = BaseUtils.getNameFromUrl(path);
                        Log.e(TAG, "onActivityResultAboveL: getFileInfo2:"+nameFromUrl);
                        mNewWeb.evaluateJavascript("window.sdk.getFileInfo(\"" + nameFromUrl + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {

                            }
                        });
                        /**
                         * 一下注释掉的功能延期开放
                         */
                        mNewWeb.callHandler("getFileInfo", nameFromUrl, new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {

                            }
                        });
                    }
                }
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    //上传头像
    private void takePhoneUrl(String cropImagePath) {

        accessToken = "Bearer" + " " + token;
        OkHttpClient client = new OkHttpClient();//创建okhttpClient
        //创建body类型用于传值
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        File file = new File(cropImagePath);
        if (file == null) {
            return;
        }
        final MediaType mediaType = MediaType.parse("image/jpeg");//创建媒房类型
        builder.addFormDataPart("fileObjs", file.getName(), RequestBody.create(mediaType, file));
        builder.addFormDataPart("fileNames", "");
        builder.addFormDataPart("bucketName", Constant.bucket_Name);
        builder.addFormDataPart("folderName", "menu");
        MultipartBody requestBody = builder.build();
        final Request request = new Request.Builder()
                .url(Constant.upload_multifile)
                .addHeader("Authorization", accessToken)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                String s = response.body().string();
//                Log.e(TAG, "onResponse: " + s);
//                Gson gson = new Gson();
//                PictureUpload pictureUpload = gson.fromJson(s, PictureUpload.class);
//                if (pictureUpload.getCode() == 200) {
//                    List<PictureUpload.DataBean> data = pictureUpload.getData();
//                    Message message = new Message();
//                    message.what = OPLOAD_IMAGE;
//                    message.obj = data.get(0).getNewName();
//                    handler.sendMessage(message);
//                } else {
//
//                }
            }
        });
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return BaseUtils.getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return BaseUtils.getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return BaseUtils.getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }





}
