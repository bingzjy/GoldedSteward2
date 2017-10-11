package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.google.gson.Gson;
import com.ldnet.activity.me.Message;
import com.ldnet.entities.MessageCallBack;
import com.ldnet.entities.Msg;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lee on 2017/7/28.
 */
public class HomeService extends BaseService {

    private String tag = HomeService.class.getSimpleName();

    public HomeService(Context context) {
        this.mContext = context;
    }


    public void APPGetJpushNotification(final int id,final  Handler handlerGetJpushNotification) {
        String url = Services.mHost + "API/Property/APPGetJpushNotification/%s?communityId=%s&resultId=%s";
        url = String.format(url, UserInformation.getUserInfo().UserId, UserInformation.getUserInfo().CommunityId, id);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        // 请求的URL
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(mContext) {

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
                        Log.e(tag, "APPGetJpushNotification" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s, handlerGetJpushNotification)) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    MessageCallBack messageCallBack = gson.fromJson(jsonObject.getString("Obj"), MessageCallBack.class);
                                    android.os.Message msg = handlerGetJpushNotification.obtainMessage(DATA_SUCCESS,messageCallBack);
                                    handlerGetJpushNotification.sendMessage(msg);
                                } else {
                                    sendErrorMessage(handlerGetJpushNotification, jsonObject);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}
