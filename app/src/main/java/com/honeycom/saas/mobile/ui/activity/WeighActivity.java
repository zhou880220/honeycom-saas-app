package com.honeycom.saas.mobile.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bld.ScanManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.android.scanner.impl.ReaderManager;
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
* desc : ??????????????????
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

    //????????? ?????????
    private static final int REQUEST_CODE_SCAN = 1;
    //???????????? ?????????
    private static final int REQUEST_CAPTURE = 100;
    //???????????? ?????????
    private static final int REQUEST_PICK = 101;
    private static final String[] APPLY_PERMISSIONS_APPLICATION = { //?????????????????????
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int ADDRESS_PERMISSIONS_CODE = 1;
    //?????????????????????????????????
    private static final int NOT_NOTICE = 2;
    //??????????????????handler
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
    //?????????????????????????????????
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

    //m8 - ???????????????
    private ReaderManager readerManager;
    private boolean isActive;
    private int outPutMode;
    private boolean enableScankey;
    private int endCharMode;
    //??????-??????
    private ScanManager scanManager;
    private String barcodeStr;

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

        //?????????????????????
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.status_text));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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

        if (Constant.PDA_TYPE == 0) {
            // Register receiver
            IntentFilter intentFilter = new IntentFilter(Constant.SCN_CUST_ACTION_SCODE);
            registerReceiver(scanDataReceiver, intentFilter);
            readerManager = ReaderManager.getInstance();
            Log.d(TAG, "-------ScannerService----------onCreate----enableScankey-------" + readerManager);
            //Initialize scanner configuration
            initScanner();
        } else if (Constant.PDA_TYPE == 0) {
            //??????????????????
            scanManager = ScanManager.getDefaultInstance(this);
            scanManager.setOPMode(0);
            scanManager.setContinueScan(false);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
        }

    }


    /**
     * ?????????????????????
     */
    @Override
    protected void initClick() {

        //??????
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
     * ???????????????--???????????????
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
     * webview?????????
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

        //js??????????????????
//        mNewWeb.addJavascriptInterface(new MJavaScriptInterface(getApplicationContext()), "ApplyFunc");
        wvClientSetting(mNewWeb);



        //????????????
        mNewWeb.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.e(TAG, "onKey: web back  1");
                    if (mNewWeb != null && mNewWeb.canGoBack()) {
                        Log.e(TAG, "onKey: web back  2"+goBackUrl);
                        if (goBackUrl.contains("/p/home")) { //???????????????????????????  ??????????????????
                            finish();
                        } else if (goBackUrl.contains("/information")) { //????????????????????????????????????
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
         * ???????????????
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
         * ???????????????????????????
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
         * ???????????????????????????
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
         * ????????????????????????
         */
        mNewWeb.registerHandler("getUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    String userInfo = (String) SPUtils.getInstance().get("userInfo", "");
                    if (!userInfo.isEmpty()) {
                        function.onCallBack(userInfo);
                    } else {
                        Toast.makeText(mContext, "????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        /**
         * ??????????????????
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
         * ??????????????????
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
         * ???????????????
         */
        mNewWeb.registerHandler("getMailList", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    StringBuffer stringBuffer = new StringBuffer();
                    String allContancts = "";//getAllContancts(stringBuffer);
                    String substring = allContancts.substring(0, allContancts.length() - 1);//?????????????????????????????????
                    function.onCallBack(substring + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * ????????????
         */
        mNewWeb.registerHandler("upLoadFile", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
            }
        });
        /**
         * @param key ??????????????????????????????
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

        //???????????????????????????????????????
        mNewWeb.registerHandler("setUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    Log.e(TAG, "????????????????????????: " + data);
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
         * ????????????
         */
//        mNewWeb.registerHandler("downLoadFile", new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                try {
//                    if (!data.isEmpty()) {
//                        Toast.makeText(mContext, "?????????...", Toast.LENGTH_SHORT).show();
//                        Map map = new Gson().fromJson(data, Map.class);
//                        String num = (String) map.get("url");
//                        String filename = (String) map.get("filename");
//                        if (filename != null && !filename.equals("")) {
//                            String newReplaceUrl = num.replace(num.substring(num.lastIndexOf("/") + 1), filename);
//                            Log.e(TAG, "???????????????????????????: 2" + newReplaceUrl);
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
//                            Log.e(TAG, "???????????????????????????:3 " + num);
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
         * ??????????????????
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
         * ?????????????????????type???????????????????????????
         */
//        mNewWeb.registerHandler("shareInterface", new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                boolean isShareSuc = false;
//                try {
//                    Log.e(TAG, "shareInterface: " + data);
//                    if (!data.isEmpty()) {
//                        //???????????????
//                        wxApi = WXAPIFactory.createWXAPI(mContext, Constant.APP_ID);
//                        wxApi.registerApp(Constant.APP_ID);
//                        //QQ?????????
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
//                                        wechatShare(0, shareSdkBean); //??????
//                                    }
//                                }).start();
//                            } else {
//                                Toast.makeText(mContext, "?????????????????????", Toast.LENGTH_SHORT).show();
//                            }
//                        } else if (type == 2) {
//                            boolean wxAppInstalled1 = isWxAppInstalled(mContext);
//                            if (wxAppInstalled1 == true) {
//                                isShareSuc = true;
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        wechatShare(1, shareSdkBean); //?????????
//                                    }
//                                }).start();
//                            } else {
//                                Toast.makeText(mContext, "?????????????????????", Toast.LENGTH_SHORT).show();
//                            }
//                        } else if (type == 3) {
//                            boolean qqClientAvailable = BaseUtils.isQQClientAvailable(mContext);
//                            if (qqClientAvailable == true) {
//                                isShareSuc = true;
//                                qqFriend(shareSdkBean);
//                            } else {
//                                Toast.makeText(mContext, "???????????????QQ", Toast.LENGTH_SHORT).show();
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
         * ?????????????????????????????????
         */
        mNewWeb.registerHandler("goLogin", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.e(TAG, "?????????????????????????????????: ");
                try {
                    SPUtils.getInstance().put("apply_url", Constant.login_url);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * ??????????????????????????????
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
         * ???????????????
         */
        mNewWeb.registerHandler("closePage", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler: close page");
                finish();
            }
        });
        /**
         * ???????????????????????????
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
         * ????????????
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
         * ???????????????????????????????????????
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
//                            Log.e(TAG, "????????????1: " + num);
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        /**
         * ??????????????????
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
         * ?????????????????????
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
         * ????????????
         */
        mNewWeb.registerHandler("OpenPayIntent", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    if (!data.isEmpty()) {
                        Log.e(TAG, "???????????????: " + data);
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
        //??????????????????????????????
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
         * ????????????Ip??????
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

        //??????
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

        //????????????
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

        //????????????
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
                        function.onCallBack("???????????????????????????.");
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

        //????????????
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
     * ??????wifi??????
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
     * webview??????
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
                    if (name.contains("/api-o/oauth")) {  //??????????????????  ???try
//                        mApplyBackImage1.setVisibility(View.GONE);
                    } else {
//                        mApplyBackImage1.setVisibility(View.VISIBLE);
                        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            //??????READ_EXTERNAL_STORAGE??????
                            Log.e(TAG, "onCityClick: no permission" );
                            ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                                    ADDRESS_PERMISSIONS_CODE);
                        }
                    }
                } catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        //??????READ_EXTERNAL_STORAGE??????
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
                    //???????????????
                    if (mLoadingPage != null) {
                        mLoadingPage.setVisibility(View.GONE);
                        mNewWebProgressbar.setVisibility(View.GONE);
                    } else {
                        mNewWebProgressbar.setVisibility(View.GONE);
                    }
                } else {
                    //???????????????
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

            // For Android >= 5.0 ??????????????????????????????
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????READ_EXTERNAL_STORAGE??????
                    Log.e(TAG, "onCityClick: no permission camera" );
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????READ_EXTERNAL_STORAGE??????
                    Log.e(TAG, "onCityClick: no permission record" );
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }else if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????READ_EXTERNAL_STORAGE??????
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
                    Log.e(TAG, "onShowFileChooser:?????????????????? " + acceptTypes[0]);

                    if (acceptTypes[0].equals("image/*") && isphoto && i  == FileChooserParams.MODE_OPEN) {
                        Log.e(TAG, "start capture");
                        openImageCaptureActivity();//?????????????????????????????????
                    }else if (acceptTypes[0].equals("*/*")) {
                        openFileChooserActivity(); //??????????????????
                    } else if (acceptTypes[0].equals("image/*")) {
                        Log.e(TAG, "onShowFileChooser: 1");
                        openImageChooserActivity();//?????????????????????????????????
                    } else if (acceptTypes[0].equals("video/*")) {
                        openVideoChooserActivity();//??????????????????/????????????
                    }
                }
                return true;
            }
        });
    }

    //????????????????????????
    private String getId() {
        StringBuilder deviceId = new StringBuilder();
        // ????????????
//        try {
//            //IMEI???imei???
//            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            @SuppressLint("MissingPermission") String imei = tm.getDeviceId();
//            if (!TextUtils.isEmpty(imei)) {
//                deviceId.append("imei");
//                deviceId.append(imei);
//                return deviceId.toString();
//            }
//            //????????????sn???
//            @SuppressLint("MissingPermission") String sn = tm.getSimSerialNumber();
//            if (!TextUtils.isEmpty(sn)) {
//                deviceId.append("sn");
//                deviceId.append(sn);
//                return deviceId.toString();
//            }
//            //???????????????????????? ???????????????id????????????
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
     * ??????????????????UUID
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
     * ???????????????
     */
    private void gotoPhoto() {
        //???????????????????????????
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "???????????????"), REQUEST_PICK);
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
//        tempFile = new File(FileUtil.checkDirPath(Environment.getExternalStorageDirectory().getPath() + "/image/"), System.currentTimeMillis() + ".jpg");
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
        Log.i(TAG, "start gotoCamera: ");
        startActivityForResult(intent, REQUEST_CAPTURE);
    }

//    /**
//     * ?????????????????????
//     * @param fileLoad
//     * @param downPath
//     */
//    private void downFilePath(String fileLoad, String downPath) {
//        FileDownloader.setup(mContext);
//        FileDownloader.getImpl().create(downPath)
//                .setPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileLoad + BaseUtils.getNameFromUrl(downPath))
//                .setForceReDownload(true)
//                .setListener(new FileDownloadListener() {
//                    //??????
//                    @Override
//                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//
//                    }
//
//                    //??????????????????
//                    @Override
//                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
////                            progressBar.setProgress((soFarBytes * 100 / totalBytes));
////                            progressDialog.setProgress((soFarBytes * 100 / totalBytes));
//                        Log.e(TAG, "progress: " + (soFarBytes * 100 / totalBytes));
//                    }
//
//                    //????????????
//                    @Override
//                    protected void completed(BaseDownloadTask task) {
//                        String[] split1 = task.getPath().split("0/");
//                        Toast.makeText(mContext, "????????????", Toast.LENGTH_SHORT).show();
//                        new AlertDialog.Builder(mContext)
//                                .setTitle("???????????????")
//                                .setMessage(split1[1])
//                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                    }
//                                })
//                                .show();
//                    }
//
//                    //??????
//                    @Override
//                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//
//                    }
//
//                    //????????????
//                    @Override
//                    protected void error(BaseDownloadTask task, Throwable e) {
//                        Toast.makeText(mContext, "????????????", Toast.LENGTH_SHORT).show();
//                    }
//
//                    //?????????????????????
//                    @Override
//                    protected void warn(BaseDownloadTask task) {
//                        Log.e(TAG, "warn: " + task);
//                    }
//                }).start();

//    }

//    /**
//     * ????????????????????????
//     *
//     * @param context
//     * @return true ?????????   false ?????????
//     */
//    public static boolean isWxAppInstalled(Context context) {
//        IWXAPI wxApi = WXAPIFactory.createWXAPI(context, null);
//        wxApi.registerApp(Constant.APP_ID);
//        boolean bIsWXAppInstalled = false;
//        bIsWXAppInstalled = wxApi.isWXAppInstalled();
//        return bIsWXAppInstalled;
//    }

//    /**
//     * @param flag (0:????????????????????????1???????????????????????????)
//     */
//    private void wechatShare(int flag, ShareSdkBean shareSdkBean) {
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = shareSdkBean.getUrl();
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        msg.title = shareSdkBean.getTitle();
//        msg.description = shareSdkBean.getTxt();
//        //????????????????????????????????????????????????
////        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.wechat);
//        Bitmap thumb = null;
//        try {
//            thumb = BitmapFactory.decodeStream(new URL(shareSdkBean.getIcon()).openStream());
////??????????????????????????????120???150????????????
////???????????????????????????????????????
//            Bitmap thumbBmp = BaseUtils.compressImage(thumb);
////Bitmap??????
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
     * ?????????QQ??????
     */
    int shareType = 1;
    //IMG
    public static String IMG = "";
    int mExtarFlag = 0x00;

//    private void qqFriend(ShareSdkBean shareSdkBean) {
//        final Bundle params = new Bundle();
//        //
//        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareSdkBean.getTitle()); //???????????????
//        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareSdkBean.getUrl());//???????????????
//        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareSdkBean.getTxt());//???????????????
//
//        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareSdkBean.getIcon());//???????????????
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
//        // QQ????????????????????????
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
     * ?????????????????????/????????????
     */
    public void openImageCaptureActivity() {
        //	???????????????????????????
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //????????????
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //????????????
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //?????????????????????????????????
        tempFile = new File(mFilePath);
        //?????????????????????
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //??????7.0???????????????????????????????????????xml/file_paths.xml
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
     * ?????????????????????/????????????
     */
    public void openImageChooserActivity() {
//        String filePath = Environment.getExternalStorageDirectory() + File.separator
//                + Environment.DIRECTORY_PICTURES + File.separator;
//        String fileName = "IMG_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
//        String _file = filePath + fileName;
//        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            //??????7.0???????????????????????????????????????xml/file_paths.xml
//            captureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File( _file));
//            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
//            Log.e(TAG, "openImageChooserActivity: fileprovider");
//        } else {
//            imageUriThreeApply =  Uri.fromFile(new File( _file));
//            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
//            Log.e(TAG, "openImageChooserActivity: urI");
//        }

        //	???????????????????????????
        File dPictures = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //????????????
        String mFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        //????????????
        String mFilePath = dPictures.getAbsolutePath() + "/" + mFileName;
        //?????????????????????????????????
        tempFile = new File(mFilePath);
        //???????????????????????????
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //??????7.0???????????????????????????????????????xml/file_paths.xml
            captureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        } else {
            imageUriThreeApply =  Uri.fromFile(tempFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);
        }
        Log.i(TAG, "start gotoCamera: ");

//        imageUriThreeApply = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File( _file));//Uri.fromFile(new File(filePath + fileName));
        //?????????????????????

//        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriThreeApply);

        Intent Photo = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(Photo, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        Log.e(TAG, "openImageChooserActivity: ");
        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

    }

    /**
     * ???????????????????????????
     */
    public void openFileChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");//????????????
        startActivityForResult(i, FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * ?????????????????????/????????????
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
        Button mPhotoGraphPopupButton = centerView.findViewById(R.id.Photo_graph_popup); //????????????????????????
        Button mPhotoAlbumPopup = centerView.findViewById(R.id.Photo_album_popup);      //??????????????????????????????
        Button mDismissPopupButton = centerView.findViewById(R.id.video_dismiss_popup_button);  //????????????????????????

        mPhotoGraphPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(WeighActivity.this, 1f);//0.0-welcome1.0
                videoPopupWindow.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                //????????????
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                //???????????????
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
                if (Build.BRAND.equals("Xiaomi")) {//?????????????????????,?????????????????????????????????????????????????????????
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
                    startActivityForResult(Intent.createChooser(intent, "????????????????????????"), FILE_CHOOSER_RESULT_CODE);
                } else {//???????????????????????????????????????
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/*");
                    }
                    startActivityForResult(Intent.createChooser(intent, "????????????????????????"), FILE_CHOOSER_RESULT_CODE);
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
     * papawin????????????????????????????????????
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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        //????????????
//        Log.e(TAG, "onClick: ????????????"+mNewWeb.canGoBack());
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
     * ????????????
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
                //??????uploadMessage???uploadMessageAboveL???????????????????????????????????????
                //WebView????????????????????????????????????????????????????????????onReceiveValue???null?????????
                //??????WebView???????????????????????????????????????????????????????????????????????????????????????
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                } else if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
            }
        } else {
            //??????uploadMessage???uploadMessageAboveL???????????????????????????????????????
            //WebView????????????????????????????????????????????????????????????onReceiveValue???null?????????
            //??????WebView???????????????????????????????????????????????????????????????????????????????????????
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
                    //??????READ_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(WeighActivity.this, APPLY_PERMISSIONS_APPLICATION,
                            ADDRESS_PERMISSIONS_CODE);
                }//????????????????????????????????????????????????????????????
                break;
            case REQUEST_CODE_SCAN: //???????????????
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
            case REQUEST_CAPTURE://????????????????????????
            {
                if (resultCode == RESULT_OK) {
                    gotoClipActivity(Uri.fromFile(tempFile));
                } else if (resultCode == RESULT_CANCELED) {
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "??????" + "\")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                            mNewWeb.callHandler("AlreadyPhoto", "??????", new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                }
                            });
                        }
                    });
                }
            }
            break;
            case REQUEST_PICK://????????????????????????
            {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String realPathFromUri = BaseUtils.getRealPathFromURI(this, uri);
                    if (realPathFromUri.endsWith(".jpg") || realPathFromUri.endsWith(".png") || realPathFromUri.endsWith(".jpeg")) {
                        gotoClipActivity(uri);
                    } else {
                        mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "??????" + "\")", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                            }
                        });
                        mNewWeb.callHandler("AlreadyPhoto", "??????", new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                            }
                        });
                        Toast.makeText(this, "?????????????????????,???????????????", Toast.LENGTH_SHORT).show();
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    mNewWeb.post(new Runnable() {
                        @Override
                        public void run() {
                            mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "??????" + "\")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                            mNewWeb.callHandler("AlreadyPhoto", "??????", new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                }
                            });
                        }
                    });
                }
            }
            break;

//            case REQ_CLIP_AVATAR: //????????????
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
//                    mNewWeb.evaluateJavascript("window.sdk.AlreadyPhoto(\"" + "??????" + "\")", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String value) {
//                        }
//                    });
//                    mNewWeb.callHandler("AlreadyPhoto", "??????", new CallBackFunction() {
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

    //TODO ???????????????????????? ???????????? deviceTaskNotice

    /**
     * ??????????????????
     */
    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
//        ClipImageActivity.goToClipActivity(this, uri);
    }

    // ?????????????????????Html??????
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent, Uri uri) {
        Log.e(TAG, "onActivityResultAboveL: requestCode:"+requestCode);
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        if ("file".equalsIgnoreCase(intent.getScheme())) {//???????????????????????????
            path = intent.getDataString();
            return;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4??????
            path = getPath(this, uri);
        } else {//4.4???????????????????????????
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
                         * ????????????????????????????????????
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
                         * ????????????????????????????????????
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

    //????????????
    private void takePhoneUrl(String cropImagePath) {

        accessToken = "Bearer" + " " + token;
        OkHttpClient client = new OkHttpClient();//??????okhttpClient
        //??????body??????????????????
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        File file = new File(cropImagePath);
        if (file == null) {
            return;
        }
        final MediaType mediaType = MediaType.parse("image/jpeg");//??????????????????
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
     * ??????Android4.4????????????Uri??????????????????????????????????????????????????????
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

    @Override
    protected void onPause() {
        super.onPause();
//        if (Constant.PDA_TYPE == 1) {
//            unregisterReceiver(mScanReceiver);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Initialize scanner's configurations
        if (Constant.PDA_TYPE == 0) {
            initScanner();
        }else if (Constant.PDA_TYPE == 1) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.SCAN_ACTION);
            registerReceiver(mScanReceiver, filter);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "-------ScannerService----------onDestroy");
        if (Constant.PDA_TYPE == 0) {
            readerManager.Release();
            readerManager = null;
        }else if (Constant.PDA_TYPE == 1) {
            unregisterReceiver(mScanReceiver);
        }
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
//                    arrayAdapter.add(message);
                    readerManager.stopScanAndDecode();
//                    isScan = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        }
    };

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            barcodeStr = intent.getStringExtra("barcodeData");
            Log.d(TAG, "-------ScannerService----------message2 = " + barcodeStr);
//            int barocodelen = intent.getIntExtra("length", 0);
//            byte temp = intent.getByteExtra("barcodeType", (byte) 0);
//            byte[] aimid = intent.getByteArrayExtra("aimid");

            SPUtils.getInstance().put(Constant.SCN_CUST_EX_RESULT, barcodeStr);
            //?????????h5
            mNewWeb.callHandler("getInfraredScanResult", barcodeStr, new CallBackFunction() {
                @Override
                public void onCallBack(String data) {

                }
            });
//            scanManager.stopDecode();

        }
    };




}
