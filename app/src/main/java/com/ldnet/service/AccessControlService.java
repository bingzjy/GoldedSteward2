package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Request;

import static com.ldnet.goldensteward.R.id.et_complain_content;
import static com.unionpay.mobile.android.global.a.s;

/**
 * Created by lee on 2017/9/30
 */

public class AccessControlService extends BaseService {

    private String tag = AccessControlService.class.getSimpleName();
    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AccessControlService(Context context) {
        this.mContext = context;
    }


    //获取访客记录
    public void getAccessRecord(String startDate, String endDate, String lastID, final Handler handler) {

//        String url = Services.mHost + "API/InOut/Men/GetBySponsorId/%s?startDate=%s&endDate=%s&lastId=%s&pageSize=%s";
//        url = String.format(url,"a5e1953b166c44bfbece8550f01327e8","2017-07-09 09:00:00", "2017-09-09 09:00:00", "", Services.PAGE_SIZE);
        String url = Services.mHost + "API/InOut/Men/Del/%s";
        url = String.format(url, "a5e1953b166c44bfbece8550f01327e8");
        Log.e(tag, "getAccessRecord:---url:" + url);

        OkHttpService.get(url).execute(new DataCallBack(mContext, handler) {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
            }

            @Override
            public void onResponse(String s, int i) {
                super.onResponse(s, i);
                Log.e(tag, "getAccessRecord:" + s);
                handler.sendEmptyMessage(BaseService.DATA_SUCCESS);

            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
            }
        });
    }


    //添加访客邀请
    public void addAccessInvite(final String id, final String IdentityType, final String inviteName,
                                final String inviteTel, final String accessDate, final String reason, final String isDriving,
                                final String carNo, final Handler handler) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String url = Services.mHost + "API/InOut/Men/Add";
        Log.e(tag, "addAccessInvite---url:  " + url);
//        HashMap<String, String> extras = new HashMap<>();
//        extras.put("Id", id);
//        extras.put("Created",  mFormatter.format(new Date());  //创建时间
//        extras.put("RoomId", UserInformation.getUserInfo().getHouseId());   //房间ID
//        extras.put("RoomNo", UserInformation.getUserInfo().getHouseName());     //房间房号
//        extras.put("SponsorId", UserInformation.getUserInfo().getUserId());  //发起人
//        extras.put("SponsorName", UserInformation.getUserInfo().getUserName());  //发起人姓名
//        extras.put("SponsorTel", UserInformation.getUserInfo().getUserPhone());       //发起人电话
//        extras.put("IdentityType", IdentityType);     //发起人身份(业主0、家属1、租户2)、
//        extras.put("InviterName", inviteName);      //邀请人姓名
//        extras.put("InviterTel", inviteTel);
//        extras.put("Date", accessDate);  //来访日期
//        extras.put("Reasons", reason); //来访事由
//        extras.put("IsDriving", "true"); //是否开车
//        extras.put("CarNo", "陕A12345");
//        extras.put("CommunityId", UserInformation.getUserInfo().getCommunityId());
//        Services.json(extras);
//        Log.e(tag, "addAccessInvite---params:  " + Services.json(extras).toString());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Id", id);
            jsonObject.put("Created", mFormatter.format(new Date()));  //创建时间
            jsonObject.put("RoomId", UserInformation.getUserInfo().getHouseId());   //房间ID
            jsonObject.put("RoomNo", UserInformation.getUserInfo().getHouseName());     //房间房号
            jsonObject.put("SponsorId", UserInformation.getUserInfo().getUserId());  //发起人
            jsonObject.put("SponsorName", UserInformation.getUserInfo().getUserName());  //发起人姓名
            jsonObject.put("SponsorTel", UserInformation.getUserInfo().getUserPhone());       //发起人电话
            jsonObject.put("IdentityType", IdentityType);     //发起人身份(业主0、家属1、租户2)、
            jsonObject.put("InviterName", inviteName);      //邀请人姓名
            jsonObject.put("InviterTel", inviteTel);
            jsonObject.put("Date", accessDate);  //来访日期
            jsonObject.put("Reasons", reason); //来访事由
            jsonObject.put("IsDriving", isDriving); //是否开车
            jsonObject.put("CarNo", carNo);
            jsonObject.put("CommunityId", UserInformation.getUserInfo().getCommunityId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String dd = "{" + "\"str\"" + ":" + "\"" + jsonObject.toString() + "\"}";
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + dd + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
//                .addParams("Id", extras.get("Id"))
//                .addParams("Created", extras.get("Created"))
//                .addParams("RoomId", extras.get("RoomId"))
//                .addParams("RoomNo", extras.get("RoomNo"))
//                .addParams("SponsorId", extras.get("SponsorId"))
//                .addParams("SponsorName", extras.get("SponsorName"))
//                .addParams("SponsorTel", extras.get("SponsorTel"))
//                .addParams("IdentityType", extras.get("IdentityType"))
//                .addParams("InviterName", extras.get("InviterName"))
//                .addParams("InviterTel", extras.get("InviterTel"))
//                .addParams("Date", extras.get("Date"))
//                .addParams("Reasons", extras.get("Reasons"))
//                .addParams("IsDriving", extras.get("IsDriving"))
//                .addParams("CarNo", extras.get("CarNo"))
//                .addParams("CommunityId", extras.get("CommunityId"))
                .addParams("str", jsonObject.toString())
                .build()
                .execute(new DataCallBack(mContext, handler) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        Log.e(tag, "addAccessInvite---onBefore");
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        Log.e(tag, "addAccessInvite---onError" + e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e(tag, "AddAccessInvite---onResponse:" + s);
                        try {
                            if (checkJsonData(s, handler)) {
                                {
                                    JSONObject json = new JSONObject(s);
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && jsonObject.getString("Obj") != null) {
                                        Message msg = new Message();
                                        msg.what = DATA_SUCCESS;
                                        msg.obj = jsonObject.getString("Message");
                                        handler.sendMessage(msg);
                                    } else {
                                        sendErrorMessage(handler, jsonObject);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //添加物品出入


}
