package com.ldnet.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.Browser;
import com.ldnet.activity.ImageCycleView;
import com.ldnet.activity.home.CommunityShops;
import com.ldnet.activity.mall.GoodsList;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lee on 2017/7/30.
 */
public class GoodsService extends BaseService {


    private String tag = GoodsService.class.getSimpleName();

    public GoodsService(Context context) {
        this.mContext = context;
    }


    //获取商品列表
    public void getGoodsData(final String lastId,final  int PAGE_SIZE,final  Handler handlerGetGoodsData) {
        String url = Services.mHost + "BGoods/App_GetHomeGoodsList_2?CityID=%s&LastID=%s&PageCnt=%s";
        url = String.format(url, UserInformation.getUserInfo().CommuntiyCityId, lastId, PAGE_SIZE);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(mContext, handlerGetGoodsData) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d(tag, "获取商品列表getGoodsData:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s, handlerGetGoodsData)) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<Goods>>() {
                                    }.getType();
                                    List<Goods> goodslist;
                                    goodslist = gson.fromJson(jsonObject.getString("Obj"), listType);

                                    Message msg = handlerGetGoodsData.obtainMessage(DATA_SUCCESS, goodslist);
                                    handlerGetGoodsData.sendMessage(msg);

                                } else {
                                    sendErrorMessage(handlerGetGoodsData, jsonObject);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void getHomePageArea(final Handler handlerGetHomePageArea) {
        String url = Services.mHost + "APPHomePageSet/App_GetList_Two?CID=%s";
        url = String.format(url, UserInformation.getUserInfo().CommunityId);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(mContext, handlerGetHomePageArea) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.e(tag, "getHomePageArea:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s, handlerGetHomePageArea)) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<APPHomePage_Area>>() {
                                    }.getType();

                                    List<APPHomePage_Area> mAppHomePageArea = gson.fromJson(jsonObject.getString("Obj"), listType);
                                    Message msg = handlerGetHomePageArea.obtainMessage(DATA_SUCCESS, mAppHomePageArea);
                                    handlerGetHomePageArea.sendMessage(msg);
                                } else {
                                    sendErrorMessage(handlerGetHomePageArea, jsonObject);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取周边小店
    public void communityshops(final Handler handlerCommunityShop) {
        // 请求的URL
        String url = Services.mHost + "GoodsShop/GetInfo_BYCID?CID=%s";
        url = String.format(url, UserInformation.getUserInfo().CommunityId);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone()
                + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(mContext,handlerCommunityShop) {
                    @Override
                    public void onError(Call call, Exception e, int i) {

                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e(tag, "communityshops:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s, handlerCommunityShop)) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    CommunityShopId communityShopId = gson.fromJson(jsonObject.getString("Obj"), CommunityShopId.class);
                                    Message msg=handlerCommunityShop.obtainMessage(DATA_SUCCESS,communityShopId);
                                    handlerCommunityShop.sendMessage(msg);

                                }else{
                                    sendErrorMessage(handlerCommunityShop,jsonObject);

                                }                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }


}
