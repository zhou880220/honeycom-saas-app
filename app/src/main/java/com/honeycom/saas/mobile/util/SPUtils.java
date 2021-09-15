package com.honeycom.saas.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;


import com.honeycom.saas.mobile.App;

import java.util.Map;

/**
* author : zhoujr
* date : 2020/10/27 9:39
* desc : sp公共类
*/
public class SPUtils {

    private static SPUtils sInstance;
    private SharedPreferences sharedReadable;
    private SharedPreferences.Editor sharedWritable;

    /**
     * 保存在手机里的SP文件名
     */
    public static final String FILE_NAME = "honeycomb_app_sp";

    private SPUtils(){
        sharedReadable = App.getContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        sharedWritable = sharedReadable.edit();
    }

    public static SPUtils getInstance(){
        if(sInstance == null){
            synchronized (SPUtils.class){
                if (sInstance == null){
                    sInstance = new SPUtils();
                }
            }
        }
        return sInstance;
    }



    /**
     * 保存数据
     */
    public void put(String key, Object obj) {
        if (obj instanceof Boolean) {
            sharedWritable.putBoolean(key, (Boolean) obj);
        } else if (obj instanceof Float) {
            sharedWritable.putFloat(key, (Float) obj);
        } else if (obj instanceof Integer) {
            sharedWritable.putInt(key, (Integer) obj);
        } else if (obj instanceof Long) {
            sharedWritable.putLong(key, (Long) obj);
        } else {
            sharedWritable.putString(key, (String) obj);
        }
        sharedWritable.commit();
    }


    /**
     * 获取指定数据
     */
    public Object get(String key, Object defaultObj) {
        if (defaultObj instanceof Boolean) {
            return sharedReadable.getBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            return sharedReadable.getFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Integer) {
            return sharedReadable.getInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Long) {
            return sharedReadable.getLong(key, (Long) defaultObj);
        } else if (defaultObj instanceof String) {
            return sharedReadable.getString(key, (String) defaultObj);
        }
        return "";
    }

    /**
     * 删除指定数据
     */
    public void remove(Context context, String key) {
        sharedWritable.remove(key);
        sharedWritable.commit();
    }


    /**
     * 返回所有键值对
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        return map;
    }

    /**
     * 删除所有数据
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 检查key对应的数据是否存在
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, context.MODE_PRIVATE);
        return sp.contains(key);
    }
}
