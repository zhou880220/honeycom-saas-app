package com.honeycom.saas.mobile.util;

import android.os.Environment;

import com.honeycom.saas.mobile.App;

import java.io.File;

//常量类
public class Constant {

    /**
     * 测试环境前缀
     * 页面前缀 ：http://mestestwebk8s.zhizaoyun.com:31810/
     * 接口前缀 ：http://mestestapik8s.zhizaoyun.com:31008  "https://h5-prod.zhizaoyun.com/app-miota-v1/"; //
     */
    public static final String PAGE_URL = "http://172.16.41.125:9542";//"https://fmtest-app.zhizaoyun.com:30443";//"http://mestestwebk8s.zhizaoyun.com:31811";//String.format("https://%s.zhizaoyun.com/", getCurrentDomain()[0]);
    public static final String INTERFACE_URL =  "http://mestestapik8s.zhizaoyun.com:31008";//"https://mestestapik8s.zhizaoyun.com:30443";//

    /**
     * 生产环境前缀
     * 页面前缀 ：https://fm-app.zhizaoyun.com/
     * 接口前缀 ：http://indapp-api.zhizaoyun.com
     */

//    public static final String PAGE_URL = "https://fm-app.zhizaoyun.com";//String.format("https://%s.zhizaoyun.com/", getCurrentDomain()[0]); // 172.16.23.253:3001/";//172.16.23.138:3003/
//    public static final String INTERFACE_URL =  "https://indapp-api.zhizaoyun.com";
//    public static final String LOCAL_PAGE_URL = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"fengchaomeiyun" + File.separator +"saas-app"; //FileUtils.getCachePath()+ File.separator + "h5_cache";
      public static final String LOCAL_PAGE_URL = App.getContext().getExternalFilesDir(null) + File.separator +"fengchaomeiyun" + File.separator +"saas-app";



    public static String profile = "prod";//dev prod test

    public static final String equipmentId = "2";
    public static final String platform_type = "saas";
    public static final String equipment_type = "android";


    // 测试及调试环境桶名
//    public static final String bucket_Name = "njdeveloptest";
    // 生产环境桶名
    public static final String bucket_Name = "honeycom-service";

    ///接口调用
    // APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wx5b3f59728cb6aa71"; //微信支付ID
    // QQ
    public static final String QQ_APP_ID = "1110555495";
    //以下为页面前缀
    public static final String locahost_url = PAGE_URL + "cashierDesk"; //路径前缀  "http://172.16.23.116:3001/"
    public static final String text_url1 = PAGE_URL+"/index.html#/login"; //用户首页
    public static final String text_url = "file:///" + LOCAL_PAGE_URL + "/index.html"; //用户首页
    public static final String login_url = PAGE_URL + "login"; //登录页
//    public static final String MyNews = PAGE_URL + "news"; //咨询页面
    //以下为接口前缀      TEST_INTERFACE_URL = "https://njtesthoneycomb.zhizaoyun.com/gateway/";
    public static final String upload_multifile = INTERFACE_URL + "api-f/upload/multifile"; //上传图片
    public static final String WEBVERSION = INTERFACE_URL+"/api-p/tClientVersion/newVersion?equipmentType=1&platformType=saas&updateVersion=";//apk升级功能
    public static final String APP_BG_CHECK = INTERFACE_URL+"/api-p/tAdvSet/phoneOne?equipmentType=1&compare=";//http://172.16.14.231:18089/
    public static final String userPushRelation = INTERFACE_URL+"/api-n/userDevice/register";//保存用户推送关系
    public static final String userUnbindRelation = INTERFACE_URL+"/api-n/userDevice/logout";//保存用户推送关系
    public static final String webVersionCheck = INTERFACE_URL+"/api-p/tClientVersion/selectH5";//获取H5最新版本


    public static final String NO_AUTH_TIP = "您的企业暂未开通此应用，请联系企业管理页开通后再试。";
    public static final String ERROR_SERVER_TIP = "平台服务器出现未知异常。";

    public static final String HAS_UDATE = "has_update";
    public static final String H5_VERSION = "h5_version";

    public static final  String CODED_CONTENT = "codedContent";

    public static final String NOTICE_LIST = "noticeList";

//    public static final String saveH5FilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"fengchaomeiyun" + File.separator +"h5_zip";//FileUtils.getCachePath()+ File.separator + "h5_zip"+ File.separator ;
//    public static final String unH5ZipPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"fengchaomeiyun" + File.separator +"saas-app";

    public static final String saveH5FilePath = App.getContext().getExternalFilesDir(null) + File.separator +"fengchaomeiyun" + File.separator +"h5_zip";//FileUtils.getCachePath()+ File.separator + "h5_zip"+ File.separator ;
    public static final String unH5ZipPath = App.getContext().getExternalFilesDir(null) + File.separator +"fengchaomeiyun" + File.separator +"saas-app";

    //扫描条码服务广播
    //Scanning barcode service broadcast.
    public static final String SCN_CUST_ACTION_SCODE = "com.scanner.broadcast";
    //条码扫描数据广播
    //Barcode scanning data broadcast.
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    //扫描条码结果
    public static final String SCN_CUST_EX_RESULT = "scanner_data";
    //移动扫描条码服务广播
    public static final String SCAN_ACTION = "scan.rcv.message";

    public static final int PDA_TYPE = 1; // 0:肖邦； 1：移动PDA


}
