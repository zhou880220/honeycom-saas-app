package com.honeycom.saas.mobile.util;

//常量类
public class Constant {
    /**
     * 测试环境前缀
     * 页面前缀 ：https://njtestyyzxpad.zhizaoyun.com/
     * 接口前缀 ：https://njtesthoneycomb.zhizaoyun.com/gateway/
     */

    /**
     * 生产环境前缀
     * 页面前缀 ：https://padclient.zhizaoyun.com/
     * 接口前缀 ：https://ulogin.zhizaoyun.com/gateway/
     */

    /**
     * 调试环境前缀
     * 页面前缀 ：https://mobileclientthird.zhizaoyun.com/
     * 接口前缀 ：https://mobileclientthird.zhizaoyun.com/gateway/
     */
    public static final String DEMONSTRAION_PAGE_URL = "https://mobileclientthird.zhizaoyun.com/";
    public static final String DEMONSTRAION_INTERFACE_URL = "https://mobileclientthird.zhizaoyun.com/gateway/";



    public static String profile = "prod";//dev prod test

//    public static final String PAGE_URL = "http://172.16.23.59:3002/";//"https://njtestyyzx.zhizaoyun.com/";//"https://mobileclientthird.zhizaoyun.com/";
//    public static final String INTERFACE_URL = "http://172.16.14.231:18080/";//"https://mobileclientthird.zhizaoyun.com/gateway/";

    public static final String PAGE_URL = String.format("https://%s.zhizaoyun.com/", getCurrentDomain()[0]); // 172.16.23.253:3001/";//172.16.23.138:3003/
    public static final String INTERFACE_URL =  String.format("https://%s.zhizaoyun.com/gateway/", getCurrentDomain()[1]);
    public static final String equipmentId = "2";


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
    public static final String text_url = PAGE_URL + "home/desk"; //用户首页
    public static final String login_url = PAGE_URL + "login"; //登录页
    public static final String apply_url = PAGE_URL + "apply"; //用户中心
    public static final String register_url = PAGE_URL + "register"; //用户注册
    public static final String APP_NOTICE_LIST = PAGE_URL + "home/notice"; //消息页
    public static final String MyOrderList = PAGE_URL + "myOrder";//订单列表
    public static final String MyNews = PAGE_URL + "news"; //咨询页面
    public static final String test_shoppingCart = PAGE_URL + "shoppingCart"; //支付页面订单列表
    //以下为接口前缀      TEST_INTERFACE_URL = "https://njtesthoneycomb.zhizaoyun.com/gateway/";
    public static final String Apply_Details = INTERFACE_URL + "api-apps/client/recentlyApps?equipmentId=2&userId="; //获取悬浮窗应用
    public static final String Apply_Details_POP = INTERFACE_URL + "api-apps/client/recentlyApps"; //获取悬浮窗应用
    public static final String upload_multifile = INTERFACE_URL + "api-f/upload/multifile"; //上传图片
    public static final String headPic = INTERFACE_URL + "api-u/headPic"; //获取头像是否修改成功
    public static final String TAKE_PHOTO = INTERFACE_URL + "api-f/download/getFileUrl";//获取头像URL
    public static final String appOrderInfo = INTERFACE_URL + "api-pay/aliPay/appOrderInfo/"; //获取支付宝订单详情
    public static final String wxPay_appOrderInfo = INTERFACE_URL + "api-pay/wxPay/appOrderInfo/"; //获取微信订单详情
    public static final String payType = INTERFACE_URL + "api-apps/client/order/user/payType";  //获取用户支付类型，订单号，用户id
    public static final String NOTICE_OPEN_SWITCH = INTERFACE_URL + "api-n/notification-anon/client/notice/status"; //开启或关闭用户通知接口
    public static final String TOKEN_IS_OK = INTERFACE_URL + "api-u/users/current?access_token=";//token是否有效
    public static final String DELETE_QUEUE = INTERFACE_URL + "api-n/notification-anon/queue/delete?userId=";//用户登录删除队列
    public static final String GETAPPLY_URL = INTERFACE_URL + "api-apps/operation/apps-anon/appName?appId="; //获取当前三方应用首页链接
    public static final String GETRabbitMQAddress = INTERFACE_URL+"api-apps/menu/apps-anon/rabbitMqInfo";//获取RabbitMq推送服务地址
    public static final String userPushRelation = INTERFACE_URL+"api-msg/userPushRelation";//保存用户推送关系
    public static final String userPushRelationUpdate = INTERFACE_URL+"api-msg/userPushRelation/updateInfo";//保存用户推送关系
    public static final String userFirstUpdate = INTERFACE_URL+"api-msg/userPushRelation/firstUpdate";//用户第一次登录
    public static final String GET_H5_VERSION = INTERFACE_URL+"api-apps/apps-anon/client/h5Url";//获取h5版本号
    public static final String WEBVERSION = INTERFACE_URL+"api-apps/apps-anon/client/version/details?equipmentId=2&updateVersion=";//apk升级功能
    public static final String APP_AUTH_CHECK = INTERFACE_URL+"api-apps/apps-anon/client/platformPermissionAndPutaway";//http://172.16.14.231:18089/


    public static final String NO_AUTH_TIP = "您的企业暂未开通此应用，请联系企业管理页开通后再试。";

    public static final String ERROR_SERVER_TIP = "平台服务器出现未知异常。";

    public static final String HAS_UDATE = "has_update";


    public static String[] getCurrentDomain() {
        String page_head = "";
        String interface_head = "";
        switch (profile) {
            case "test" :
                page_head = "njtestyyzxpad";
                interface_head = "njtesthoneycomb";
                break;
            case "prod" :
                page_head = "padclient";
                interface_head = "ulogin";
                break;
            case "dev":
//                page_head = "njtestyyzx";
                interface_head = "mobileclientthird";
        }
        String[] str = {page_head, interface_head};
        return  str;
    }


}
