package com.honeycom.saas.mobile.http;

import android.util.Log;


import com.honeycom.saas.mobile.util.Constant;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhoujr on 17-4-20.
 */

public class RemoteHelper {

    private static final String TAG = "RemoteHelper_TAG";
    private static RemoteHelper sInstance;
    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;

    private RemoteHelper() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)//设置读取超时时间
                .addNetworkInterceptor(
                        chain -> {
                            Request request = chain.request();
                            //在这里获取到request后就可以做任何事情了
                            Response response = chain.proceed(request);
                            Log.d(TAG, "intercept: " + request.url().toString());
                            return response;
                        }
                ).build();

        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(Constant.INTERFACE_URL)
                .build();
    }

    public static RemoteHelper getInstance() {
        if (sInstance == null) {
            synchronized (RemoteHelper.class) {
                if (sInstance == null) {
                    sInstance = new RemoteHelper();
                }
            }
        }
        return sInstance;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
