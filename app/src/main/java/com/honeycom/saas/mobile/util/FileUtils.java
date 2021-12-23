package com.honeycom.saas.mobile.util;

import android.os.Environment;

import com.honeycom.saas.mobile.App;

/**
* author : zhoujr
* date : 2021/12/20 14:22
* desc : 文件工具类
*/
public class FileUtils {

    //获取Cache文件夹
    public static String getCachePath(){
        if (isSdCardExist()){
            return App.getContext()
                    .getExternalCacheDir()
                    .getAbsolutePath();
        }
        else{
            return App.getContext()
                    .getCacheDir()
                    .getAbsolutePath();
        }
    }

    //判断是否挂载了SD卡
    public static boolean isSdCardExist(){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            return true;
        }
        return false;
    }
}
