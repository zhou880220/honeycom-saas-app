package com.honeycom.saas.mobile.http.api;



import com.honeycom.saas.mobile.http.bean.AdMessageBean;
import com.honeycom.saas.mobile.http.bean.AdMessagePackage;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
* author : zhoujr
* date : 2021/10/14 16:57
* desc : 请求接口配置
*/
public interface MesApi {


    /************************************系统******************************************************/

    @GET("/api-p/tAdvSet/phoneOne")
    Single<AdMessagePackage> getAdMessage(@Query("compare") String url, @Query("equipmentType") int eqType, @Query("platformType") String platformType);

    @GET
    Single<ResponseBody> downLoadFile(@Url String fileUrl);




}
