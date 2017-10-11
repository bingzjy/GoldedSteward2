package com.ldnet.service;

import android.content.Context;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.request.RequestCall;
import okhttp3.OkHttpClient;

import java.util.HashMap;

/**
 * Created by zjy on 2017/6/28.
 */
public class OkHttpService {



    public static RequestCall get(String url){
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;

        GetBuilder builder= OkHttpUtils.get().url(url);
        builder.addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo());
        builder.addHeader("phone", UserInformation.getUserInfo().getUserPhone());
        builder.addHeader("timestamp", aa);
        builder.addHeader("nonce", aa1);
        builder.addHeader("signature", Services.textToMD5L32(md5));
        return builder.build();
    }



//      Services.json(extras);
//    String md5 = UserInformation.getUserInfo().getUserPhone() +
//            aa + aa1 + Services.json(extras) + Services.TOKEN;
//        OkHttpUtils.post().url(url)
//                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
//            .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
//            .addHeader("timestamp", aa)
//                .addHeader("nonce", aa1)
//                .addHeader("signature", Services.textToMD5L32
//                        (md5))
//            .addParams("feesIds", feesIds)
//                .addParams("payerId", payerId)
//                .addParams("channel", channel)
//
//


    public static PostFormBuilder post(String url, HashMap params){
        Services.json(params);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + Services.json(params) + Services.TOKEN;

        PostFormBuilder builder=OkHttpUtils.post().url(url);
        builder.addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo());
        builder.addHeader("phone", UserInformation.getUserInfo().getUserPhone());
        builder.addHeader("timestamp", aa);
        builder.addHeader("nonce", aa1);
        builder.addHeader("signature", Services.textToMD5L32(md5));
        return builder;
    }



}
