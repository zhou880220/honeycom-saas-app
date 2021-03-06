package com.honeycom.saas.mobile.ui.activity;

import android.Manifest;
import android.bld.ScanManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.text.TextUtils;
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

import com.android.scanner.impl.ReaderManager;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.honeycom.saas.mobile.App;
import com.honeycom.saas.mobile.BuildConfig;
import com.honeycom.saas.mobile.R;
import com.honeycom.saas.mobile.base.BaseActivity;
import com.honeycom.saas.mobile.http.CallBackUtil;
import com.honeycom.saas.mobile.http.OkhttpUtil;
import com.honeycom.saas.mobile.http.UpdateAppHttpUtil;
import com.honeycom.saas.mobile.http.bean.BrowserBean;
import com.honeycom.saas.mobile.http.bean.UserInfoBean;
import com.honeycom.saas.mobile.http.bean.VersionInfo;
import com.honeycom.saas.mobile.push.PushHelper;
import com.honeycom.saas.mobile.util.CleanDataUtils;
import com.honeycom.saas.mobile.util.Constant;
import com.honeycom.saas.mobile.util.SPUtils;
import com.honeycom.saas.mobile.util.StatusBarCompat;
import com.honeycom.saas.mobile.util.VersionUtils;
import com.honeycom.saas.mobile.web.MWebChromeClient;
import com.honeycom.saas.mobile.web.MyHandlerCallBack;
import com.honeycom.saas.mobile.web.MyWebViewClient;
import com.honeycom.saas.mobile.web.WebViewSetting;
import com.umeng.message.PushAgent;
import com.umeng.message.api.UPushRegisterCallback;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.listener.ExceptionHandler;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import okhttp3.Call;

/**
* author : zhoujr
* date : 2021/9/18 15:54
* desc : ????????????
*/
public class MainActivity  extends BaseActivity {
    private static final String TAG = "MainActivity_TAG";
    private static final int VIDEO_PERMISSIONS_CODE = 1;
    //????????????
    private static final int REQUEST_CAPTURE = 100;
    //????????????
    private static final int REQUEST_PICK = 101;
    //????????? ?????????
    private static final int REQUEST_CODE_SCAN = 1;
    //????????????
    private static final int NOT_NOTICE = 2;//???????????????????????????

    private static final int ADDRESS_PERMISSIONS_CODE = 200;
    private static final String[] APPLY_PERMISSIONS_APPLICATION = { //??????????????????
            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
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
    //?????????????????????????????????
    private File tempFile;

    private String userToken;

    //m8 - ???????????????
    private ReaderManager readerManager;
    private boolean isActive;
    private int outPutMode;
    private boolean enableScankey;
    private int endCharMode;
    //??????-??????
//    private ScanManager scanManager;
//    private String barcodeStr;


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
        Log.e(TAG, "initWidget: start" );
        //?????????????????????
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.status_text));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //????????????
        webView(Constant.text_url);

        //??????????????????deviceToken
        String deviceToken = PushAgent.getInstance(this).getRegistrationId();
        Log.e(TAG, "deviceToken: "+deviceToken);
        if (TextUtils.isEmpty(deviceToken)) {
            initPush();
        }else {
            SPUtils.getInstance().put("deviceToken", deviceToken);
        }

        // m8 Register receiver
        IntentFilter intentFilter = new IntentFilter(Constant.SCN_CUST_ACTION_SCODE);
        registerReceiver(scanDataReceiver, intentFilter);

        readerManager = ReaderManager.getInstance();

        Log.d(TAG, "-------ScannerService----------onCreate----enableScankey-------" + readerManager);
        //Initialize scanner configuration
        initScanner();

        //??????????????????
//        scanManager = ScanManager.getDefaultInstance(this);
//        scanManager.setContinueScan(true);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constant.SCAN_ACTION);
//        registerReceiver(mScanReceiver, filter);

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateApp();
            }
        }, 5000);


    }

    //???????????????
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

    //????????????
    public void updateApp() {
        int sysVersion = VersionUtils.getVersion(this);
        Log.e(TAG, "updateApp: "+sysVersion);
        new UpdateAppManager
                .Builder()
                //??????Activity
                .setActivity(this)
                //????????????
                .setUpdateUrl(Constant.WEBVERSION + sysVersion)
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(Exception e) {
                        Log.e(TAG, "updateApp Exception: "+e.getMessage());
                        e.printStackTrace();
                    }
                })
                //??????httpManager???????????????
                .setHttpManager(new UpdateAppHttpUtil())
                .setTopPic(R.mipmap.top_3)
                //????????????????????????????????????
                .setThemeColor(0xff47bbf1)
                .build()
                //????????????????????????????????????
                .update();
    }

    /**
     * ?????????webview js??????
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
        //??????Webview???????????????
//        setSettings();
        //Handler????????????????????????????????????????????????H5???????????????Native?????????????????????h5??????send()??????????????????????????????MyHandlerCallBack
        mNewWeb.setDefaultHandler(new MyHandlerCallBack(mOnSendDataListener));


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
        myWebViewClient.setOnCityClickListener(new MyWebViewClient.OnCityChangeListener() {
            @Override
            public void onCityClick(String name) {  //??????????????????????????????
                myOrder = name;
                Log.e(TAG, "onCityClick: "+name);

                try {
                    if (name.contains("/api-o/oauth")) {  //??????????????????  ???try
//                        mApplyBackImage1.setVisibility(View.GONE);
                    } else {
//                        mApplyBackImage1.setVisibility(View.VISIBLE);
//                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
//                                != PackageManager.PERMISSION_GRANTED) {
//                            //??????READ_EXTERNAL_STORAGE??????
//                            ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
//                                    ADDRESS_PERMISSIONS_CODE);
//                        }
                    }
                } catch (Exception e) {
//                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
//                            != PackageManager.PERMISSION_GRANTED) {
//                        //??????READ_EXTERNAL_STORAGE??????
//                        ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
//                                ADDRESS_PERMISSIONS_CODE);
//                    }
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

        Log.e(TAG, "load url: "+url);
        mNewWeb.loadUrl(url);


        //????????????
        mNewWeb.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.e(TAG, "onKey: web back  1");
                    if (mNewWeb != null && mNewWeb.canGoBack()) {
                        Log.e(TAG, "onKey: web back  2"+myOrder);
//                        SharedPreferences sb = getSharedPreferences("userInfoSafe", MODE_PRIVATE);
//                        String userInfo = sb.getString("userInfo", "");
                        if (myOrder.contains("/home")) { //???????????????????????????  ??????????????????
                            exit();
                        } else if (myOrder.contains("/information")) { //????????????????????????????????????
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

        //?????????????????????????????????????????? ????????????
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

        //??????????????????????????????Handler?????????  ???????????????
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
         * ?????????????????????h5?????????
         */
        mNewWeb.registerHandler("isInSurface", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "isInSurface: " + data);
                    function.onCallBack("true");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * ??????????????????
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

        //???????????? ????????????????????? ????????????
        mNewWeb.registerHandler("getCache", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (ChaceSize == true) {
                            function.onCallBack(CleanDataUtils.getTotalCacheSize(mContext));
                    } else {
                        function.onCallBack("0.00MB");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //??????????????????????????????
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

        //?????????????????????
        mNewWeb.registerHandler("getTakeCamera", new BridgeHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    //????????????
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //??????READ_EXTERNAL_STORAGE??????
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

        //???????????????????????????
        mNewWeb.registerHandler("getPhotoAlbum", new BridgeHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    //????????????
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        //??????READ_EXTERNAL_STORAGE??????
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

        //???????????????????????????????????????
        mNewWeb.registerHandler("setUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "????????????????????????: " + data);
                    if (!data.isEmpty()) {
                        SPUtils.getInstance().put("userInfo", data);
                        //????????????deviceToken
                        UserInfoBean userInfoBean = new Gson().fromJson(data, UserInfoBean.class);
                        if (userInfoBean !=null && !TextUtils.isEmpty(userInfoBean.getCompanyId())) {
                            userToken = userInfoBean.getAccessToken();
                            String deviceToken = (String) SPUtils.getInstance().get("deviceToken","");
                            sendDeviceToken(deviceToken);
                        }
                        function.onCallBack("success");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //??????????????????(??????????????????)
        mNewWeb.registerHandler("saveLoginInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "handler = saveLoginInfo, data from web = " + data);
                if (!TextUtils.isEmpty(data)) {
                    SPUtils.getInstance().put("loginData", data);
                    function.onCallBack("success");
                }
            }
        });


        //??????????????????
        mNewWeb.registerHandler("getLoginInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String _data = (String)SPUtils.getInstance().get("loginData", "");
                Log.e(TAG, "_loginData : "+_data);
                function.onCallBack(_data);
            }
        });

        //??????????????????
        mNewWeb.registerHandler("clearLoginInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "handler = clearLoginInfo" + data);
                SPUtils.getInstance().remove("loginData");
                SPUtils.getInstance().remove("userInfo");
                String deviceToken = (String) SPUtils.getInstance().get("deviceToken","");
//                unBindDeviceToken(deviceToken);
                Map<String, String> headerMap =  new HashMap<>();
                headerMap.put("authorization", "Bearer "+userToken);
                Map<String, String> paramsMap =  new HashMap<>();
                paramsMap.put("deviceToken", deviceToken);
                paramsMap.put("deviceType", Constant.equipment_type);
                paramsMap.put("platformType", Constant.platform_type);
                String jsonStr = new Gson().toJson(paramsMap);
                Log.e(TAG, "request params: "+jsonStr);
                Log.e(TAG, "request header: "+headerMap);
                Log.e(TAG, "request api: "+Constant.userUnbindRelation);
                OkhttpUtil.okHttpPostJson(Constant.userUnbindRelation, jsonStr, headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.e(TAG, "onFailure: "+e.getMessage());
                        function.onCallBack("fail");
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "-----onResponse: " + response);
                        function.onCallBack("success");
                    }
                });
            }
        });

        //???????????????????????????????????????
        mNewWeb.registerHandler("getUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    String userInfo = (String) SPUtils.getInstance().get("userInfo", "");
                    Log.e(TAG, userInfo);
                    if (!userInfo.isEmpty()) {
                        function.onCallBack(userInfo);
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //???????????????????????????????????????
        mNewWeb.registerHandler("showApplyParams", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "???????????????:1 " + data);
                    if (!data.isEmpty()) {
                        Map map = new Gson().fromJson(data, Map.class);
//                        String _redirectUrl = (String) map.get("redirectUrl");//"https://mobileclientthird.zhizaoyun.com/jsapi/view/api.html";//
//                        Map<String,String> reqMap = BaseUtils.urlSplit(_redirectUrl);
                        String redirectUrl = (String) map.get("redirectUrl");//"http://172.16.41.239:3001/equipment/app/home?access_token="+reqMap.get("access_token");//
//                        String currentUrl = mNewWeb.getUrl();
                        Log.e(TAG, "redirectUrl: "+redirectUrl );
                        if (!redirectUrl.isEmpty()) {
                            Intent intent;
                            //??????app????????????????????????
                            if (redirectUrl.contains("/p/")) {
                                intent = new Intent(MainActivity.this, WeighActivity.class);
                                if (redirectUrl.contains("?")) {
                                    redirectUrl = redirectUrl +"&r="+new Date().getTime();
                                }else {
                                    redirectUrl = redirectUrl +"?r="+new Date().getTime();
                                }
                                intent.putExtra("url", redirectUrl);
                                startActivity(intent);
                            }else if (redirectUrl.contains("/ws")) {
                                intent = new Intent(MainActivity.this, WeighActivity.class);
                                if (redirectUrl.contains("?")) {
                                    redirectUrl = redirectUrl +"&r="+new Date().getTime();
                                }else {
                                    redirectUrl = redirectUrl +"?r="+new Date().getTime();
                                }
                                intent.putExtra("url", redirectUrl);
                                startActivity(intent);
                            }else {
                                //??????????????????
                                intent = new Intent(MainActivity.this, ExecuteActivity.class);
                                intent.putExtra("url", redirectUrl);
                                startActivity(intent);
                            }
                        }
                        function.onCallBack("success");
                    }else {
                        function.onCallBack("fail");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //????????????
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

        //????????????????????????????????????????????????  ????????????????????????????????????
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

        //?????????????????? ????????????????????????
        mNewWeb.registerHandler("ClearUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    SPUtils.getInstance().put("userInfoSafe","");
                    function.onCallBack("success");
                } catch (Exception e) {
                    e.printStackTrace();
                    function.onCallBack("fail");
                }
            }
        });

        //?????????????????????????????????????????????
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
         * ?????????????????????type???????????????????????????
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
         * ?????????????????????
         */
        mNewWeb.registerHandler("startIntentZing", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "startIntentZing: start" );
                try {
                    // ????????????
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "handler: no permission");
                        //???????????????????????????????????????????????????????????????
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
                    Log.e(TAG, "startIntentZing: fail" );
                    function.onCallBack("fail");
                }
            }
        });

        /**
         * ?????????????????????????????????
         */
        mNewWeb.registerHandler("toPolicy", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Map map = new Gson().fromJson(data, Map.class);
                        String type = (String) map.get("type");
                        Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
                        intent.putExtra("type", type);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * ??????????????????????????????
         */
        mNewWeb.registerHandler("getScanData", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "startScanData: ");
                try {
                    String result = (String) SPUtils.getInstance().get(Constant.SCN_CUST_EX_RESULT,"");
                    function.onCallBack(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendDeviceToken(String deviceToken){
        Map<String, String> headerMap =  new HashMap<>();
        headerMap.put("authorization", "Bearer "+userToken);
        Map<String, String> paramsMap =  new HashMap<>();
        paramsMap.put("deviceToken", deviceToken);
        paramsMap.put("deviceType", Constant.equipment_type);
        paramsMap.put("platformType", Constant.platform_type);
        String jsonStr = new Gson().toJson(paramsMap);
        Log.e(TAG, "request api: "+Constant.userPushRelation);
        Log.e(TAG, "request params: "+jsonStr);
        OkhttpUtil.okHttpPostJson(Constant.userPushRelation, jsonStr, headerMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Log.e(TAG, "onFailure: "+e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "-----onResponse: " + response);
//                Result result = new Gson().fromJson(response, Result.class);
//                if (result.getCode() == 200) {
//                    SPUtils.getInstance().put(Constant.HAS_INSTALL, "1");
//                } else {
//                    Log.e("StartPageActivity", "?????????????????????");
//                }
            }
        });
    }

    /**
     * ??????deviceToken
     * @param deviceToken
     */
    private void unBindDeviceToken(String deviceToken){
        Map<String, String> headerMap =  new HashMap<>();
        headerMap.put("authorization", "Bearer "+userToken);
        Map<String, String> paramsMap =  new HashMap<>();
        paramsMap.put("deviceToken", deviceToken);
        paramsMap.put("deviceType", Constant.equipment_type);
        paramsMap.put("platformType", Constant.platform_type);
        String jsonStr = new Gson().toJson(paramsMap);
        Log.e(TAG, "request params: "+jsonStr);
        Log.e(TAG, "request header: "+headerMap);
        Log.e(TAG, "request api: "+Constant.userUnbindRelation);
        OkhttpUtil.okHttpPostJson(Constant.userUnbindRelation, jsonStr, headerMap, new CallBackUtil.CallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {
                Log.e(TAG, "onFailure: "+e.getMessage());
            }

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "-----onResponse: " + response);

            }
        });
    }


    /**
     * ??????????????????
     */
    private void gotoCamera() {
        //	???????????????????????????
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //????????????
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //????????????
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //?????????????????????????????????
        tempFile = new File(mFilePath);
        //???????????????????????????
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //??????7.0???????????????????????????????????????xml/file_paths.xml
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

    /**
     * ???????????????
     */
    private void gotoPhoto() {
        //???????????????????????????
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "???????????????"), REQUEST_PICK);
    }

//    private void setSettings() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mNewWeb.getSettings().setSafeBrowsingEnabled(false);
//        }
//        //????????????
//        webSettings = mNewWeb.getSettings();
//        //js??????
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);
//        //???????????????
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setTextZoom(100);
//        //????????????
//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setDisplayZoomControls(false);
//        //????????????
//        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setDefaultTextEncodingName("utf-8");
//    }




    // ??????????????????????????????????????????
    private long exitTime = 0;
    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            //????????????????????????????????????
            Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_SHORT).show();
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


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.e(TAG, "keyCode: "+keyCode);
//            if (mNewWeb != null && mNewWeb.canGoBack()) {
//                Log.e(TAG, "onClick: ????????????");
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
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.e(TAG, "onClick: ????????????1");
        //????????????
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
                //??????????????????
                if (grantResults.length == APPLY_PERMISSIONS_APPLICATION.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //????????????????????????????????????
                            showDialog();
                            Toast.makeText(mContext, "?????????????????????", Toast.LENGTH_LONG).show();
                            break;
                        } else {
                        }
                    }
                }
                break;
        }
    }

    //???????????????
    private void showDialog() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("??????")
                .setMessage("??????????????????????????????????????????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToAppSetting();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // ????????????????????????????????????
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
        if (requestCode == 10 && resultCode == 2) {//????????????????????????????????????????????????
            String apply_url = data.getStringExtra("apply_url");//data:???????????????putExtra()??????????????????
            webView(apply_url);
        }
        switch (requestCode) {
            case NOT_NOTICE:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????READ_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(MainActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }//????????????????????????????????????????????????????????????
                break;
            case REQUEST_CODE_SCAN: //???????????????
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
                         * ????????????????????????????????????
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        String apply_url = (String) SPUtils.getInstance().get("apply_url", "");//???????????????????????????????????????????????????
        Log.e(TAG, " onStart: "+ apply_url);
        if (!TextUtils.isEmpty(apply_url)) {
            webView(apply_url);
        }
        //??????????????????
        SPUtils.getInstance().put("apply_url","");
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = getIntent().getData(); //????????????????????????  ?????????????????????  oppo????????????
        if (uri != null) {
            Log.e(TAG, "open notice list: " + uri);


//            String thirdId = uri.getQueryParameter("thirdId");
//            if (thirdId != null) {
//                intent = new Intent(this, NewsActivity.class);
//                intent.putExtra("url", thirdId);
//                startActivity(intent);
//            }
            String open = uri.getQueryParameter("open");
            if (open.equals("message")) {
                Log.e(TAG, "huaweiUrl: " + uri);
                //test://zzy:8080/home?open=message&appid=2&appName=????????????????????????  ????????????????????????
//                String huaWei = uri.getQueryParameter("appid");
//                String appName = uri.getQueryParameter("appName");
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("appid", huaWei);
//                jsonObject.put("appName", appName);
//                String s = jsonObject.toJSONString();
//                Log.e(TAG, "onNewIntent: " + s);
//                webView(Constant.APP_NOTICE_LIST);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //??????????????????
//                        mNewWeb.callHandler("PushMessageIntent", s, new CallBackFunction() {
//                            @Override
//                            public void onCallBack(String data) {
//
//                            }
//                        });
//                    }
//                }, 1000);//2????????????Runnable??????run??????
//            }
            }

            String app_notice_list = intent.getStringExtra("APP_NOTICE_LIST");
//            String xiaomiMessage = intent.getStringExtra("pushContentMessage");
            if (app_notice_list != null) {
//            webView(Constant.APP_NOTICE_LIST);
                if (app_notice_list.equals("??????")) { //?????????????????????
//                    webView(Constant.MyNews);
                } else if (app_notice_list.equals(Constant.NOTICE_LIST)) {
                    webView(Constant.text_url);
//                    Log.e(TAG, "xiaomiMessage: " + xiaomiMessage);
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //??????????????????
//                            mNewWeb.callHandler("PushMessageIntent", xiaomiMessage, new CallBackFunction() {
//                                @Override
//                                public void onCallBack(String data) {
//
//                                }
//                            });
//                        }
//                    }, 1000);//2????????????Runnable??????run??????
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Initialize scanner's configurations
        initScanner();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.SCAN_ACTION);
//        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "-------ScannerService----------onDestroy");
        //Release resource
        readerManager.Release();
        readerManager = null;
//        unregisterReceiver(mScanReceiver);
    }


    private void initScanner() {
        //1.Check whether turn on scan engine
        // Return value
        // false : scan and decode is disable, not work
        //true: scan and decode is enable
        isActive = readerManager.GetActive();
        if (!isActive) {
            readerManager.SetActive(true);
//            powerSwitch.setChecked(true);
        }

        //2.The physical scanning key work or not
        // Return value isOn
        //true Scan key can to start scan
        //false Scan key can not to start scan
        enableScankey = readerManager.isEnableScankey();
        Log.d(TAG, "-------ScannerService----------enableScankey---" + enableScankey);
        if (!enableScankey) {
            readerManager.setEnableScankey(true);
//            pauseSwitch.setChecked(true);
            Log.d(TAG, "-------ScannerService----------isEnableScankey---" + readerManager.isEnableScankey());
        }

        //3.Get current data output mode:0 means Copy and Paste,1 means Key Emulation???2 means API.
        outPutMode = readerManager.getOutPutMode();
        //If the mode is not API, please configure it as mode API???and restore to original mode when destroying activity.
        if (outPutMode != 2) {
            readerManager.setOutPutMode(2);
        }
        //4. Set up End character:
        // 0 the end of code add "\n"
        //1 the end of code add " "
        //2 the end of code add "\t"
        //3 NULL
        if (endCharMode != 3) {
            //null, no character
            readerManager.setEndCharMode(3);
        }
    }


    //The broadcast about transmit scanning data information
    private BroadcastReceiver scanDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.SCN_CUST_ACTION_SCODE)) {
                try {
                    String message = "";
                    message = intent.getStringExtra(Constant.SCN_CUST_EX_SCODE);
                    Log.d(TAG, "-------ScannerService----------message = " + message);

                    SPUtils.getInstance().put(Constant.SCN_CUST_EX_RESULT, message);

                    //?????????h5
                    mNewWeb.callHandler("getInfraredScanResult", message, new CallBackFunction() {
                        @Override
                        public void onCallBack(String data) {

                        }
                    });
                    readerManager.stopScanAndDecode();
//                    isScan = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        }
    };

//    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            barcodeStr = intent.getStringExtra("barcodeData");
//            Log.d(TAG, "-------ScannerService----------message2 = " + barcodeStr);
//
//            SPUtils.getInstance().put(Constant.SCN_CUST_EX_RESULT, barcodeStr);
//            //?????????h5
//            mNewWeb.callHandler("getInfraredScanResult", barcodeStr, new CallBackFunction() {
//                @Override
//                public void onCallBack(String data) {
//
//                }
//            });
//        }
//    };


}
