package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.BindingHouse;
import com.ldnet.activity.me.Community;
import com.ldnet.entities.MyProperties;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.CustomListView;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static com.ldnet.goldensteward.R.id.lv_me_properties;

/**
 * Created by lee on 2017/10/10
 */

public class CommunityService extends BaseService {

    private String tag = CommunityService.class.getSimpleName();

    public CommunityService(Context context) {
        this.mContext = context;
    }

    //获取我的小区
    public void getMyCommunity(final Handler handler) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetResidentBindInfo/%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId());
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo()).build()
                .execute(new DataCallBack(mContext) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.e(tag, "我的小区----Community" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));

                            if (json.optBoolean("Status")) {
                                if (jsonObject.optBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<MyProperties>>() {
                                    }.getType();
                                    List<MyProperties> propertiesList = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (propertiesList != null && propertiesList.size() > 0) {
                                        Message msg = handler.obtainMessage();
                                        msg.what = BaseService.DATA_SUCCESS;
                                        msg.obj = propertiesList;
                                        handler.sendMessage(msg);
                                    } else {
                                        handler.sendEmptyMessage(DATA_SUCCESS_OTHER);
                                    }
                                } else {
                                    sendErrorMessage(handler, jsonObject);
                                }
                            } else {
                                sendErrorMessage(handler, "");
                            }
                        } catch (JSONException e) {
                            sendErrorMessage(handler, "");
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取该小区下用户的房屋
    private void getMyRoomByCommunityID(final String communityID, final Handler handler) {


    }

    //获取当前用户身份，业主，租户，亲属
    private void getIdentityType() {

    }


}
