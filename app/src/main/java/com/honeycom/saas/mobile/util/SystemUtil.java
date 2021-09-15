package com.honeycom.saas.mobile.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * Created by wangpan on 2020/4/14
 */
public class SystemUtil {
    /**
     * 获取当前手机系统语言。
     * 例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context ctx) {
        String imei;

        TelephonyManager telephonyManager = null;
        try {
            telephonyManager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            imei = "";
        }
        return  imei;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return tm.getImei();
//        } else {
//            return tm.getDeviceId();
//        }
    }

    /**
     *   ANDROID_ID(恢复出厂+刷机会变) + 序列号(android 10会unknown/android 9需要设备权限)+品牌    +机型
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getUniqueIdentificationCode(Context context){
        String androidId =  Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String uniqueCode ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            /** 需要权限 且仅适用9.0。 10.0后又不能获取了*/
            uniqueCode = androidId + Build.getSerial();
        }else{
            uniqueCode = androidId + Build.SERIAL;
        }
        return uniqueCode;
    }
}
