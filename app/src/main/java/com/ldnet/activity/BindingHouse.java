package com.ldnet.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.base.LoadingDialog;
import com.ldnet.activity.commen.GetData;
import com.ldnet.activity.me.Community;
import com.ldnet.activity.me.VisitorPsd;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.service.AcountService;
import com.ldnet.service.BaseService;
import com.ldnet.service.BindingService;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import okhttp3.Call;
import okhttp3.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ***************************************************
 * 绑定房产，绑定用户的房屋信息
 * **************************************************
 */
public class BindingHouse extends BaseActionBarActivity {

    private Spinner sr_binding_house_build;
    private List<Building> mBuild_datas;
    private ArrayAdapter<Building> adapter_build;
    private Spinner sr_binding_house_unit;
    private List<Building> mUnit_datas;
    private ArrayAdapter<Building> adapter_unit;
    private Spinner sr_binding_house_room;
    private List<Building> mRoom_datas;
    private ArrayAdapter<Building> adapter_room;
    private Services services;
    private String mCommunityId;
    private Boolean IsFromRegister;
    private String mHouseId;
    // 标题
    private TextView tv_page_title;
    // 返回
    private ImageButton btn_back;
    // 确定
    private Button btn_binding_house;
    private String mCommunityId1, mCOMMUNITY_NAME;

    private TextView aaa;
    private List<Building> buildings;
    private List<Building> buildings1;
    private List<Building> buildings2;
    private List<EntranceGuard> entranceGuards;
    // private ListViewAdapter<PPhones> mAdapter;
    private boolean havebind;

    private BindingService service;


    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding_house);

        service = new BindingService(this);
        //得到传递的小区ID
        IsFromRegister = Boolean.valueOf(getIntent().getStringExtra("IsFromRegister"));
        mCommunityId = getIntent().getStringExtra("COMMUNITY_ID");
        mCOMMUNITY_NAME = getIntent().getStringExtra("COMMUNITY_NAME");
        //标题
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.fragment_me_community_btn_plus);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        //确定
        btn_binding_house = (Button) findViewById(R.id.btn_binding_house);

        aaa = (TextView) findViewById(R.id.aaa);
        //楼栋信息
        initBuildings();
        //单元信息
        initUnits();
        //房屋信息
        initHouses();
        //拉去楼栋信息
        services = new Services();
        //  Buildings(mCommunityId);
        service.Buildings(mCommunityId, handlerBuilding);
        showProgressDialog();
        initEvent();


    }


    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    if (!IsFromRegister) {
                        gotoActivityAndFinish(Community.class.getName(), null);
                    } else {
                        gotoActivityAndFinish(MainActivity.class.getName(), null);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_binding_house:
                if (mHouseId != null && mHouseId.equals("0")) {
                    RemoveHouse(mCommunityId, "0");
                }
                //  MyProperties(mCommunityId, mHouseId);
                showProgressDialog();
                service.MyProperties(mCommunityId, mHouseId, handlerMyProperties);
                break;
            default:
                break;
        }
    }


    Handler handlerBuilding = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.e("asd", "handlerBuilding---" + msg.obj);
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if ((List<Building>) msg.obj != null && ((List<Building>) msg.obj).size() > 0) {
                        buildings = (List<Building>) msg.obj;
                        mBuild_datas.addAll(buildings);
                        adapter_build.notifyDataSetChanged();
                    } else {
                        btn_binding_house.setEnabled(false);
                        showToast(R.string.perfect_property_information);
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };


    Handler handlerUnit = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if ((List<Building>) msg.obj != null && ((List<Building>) msg.obj).size() > 0) {
                        mUnit_datas.clear();
                        mUnit_datas.addAll((List<Building>) msg.obj);
                        adapter_unit.notifyDataSetChanged();
                    }
                    buildings.clear();
                    if (mUnit_datas.get(0) != null && !TextUtils.isEmpty(mUnit_datas.get(0).Id)) {
                        //  Houses(mUnit_datas.get(0).Id);
                        service.Houses(mUnit_datas.get(0).getId(), handlerHouse);
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }


        }
    };


    Handler handlerHouse = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if ((List<Building>) msg.obj != null && ((List<Building>) msg.obj).size() > 0) {
                        mRoom_datas.clear();
                        mRoom_datas.addAll((List<Building>) msg.obj);
                        adapter_room.notifyDataSetChanged();
                    }
                    buildings.clear();
                    if (mRoom_datas.get(0) != null && !TextUtils.isEmpty(mRoom_datas.get(0).Id)) {
                        mHouseId = mRoom_datas.get(0).Id;
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };


    //获取我的小区和房产,判断用户是否绑定该房屋
    Handler handlerMyProperties = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null && ((List<MyProperties>) msg.obj).size() > 0) {
                        boolean havebind = false;
                        List<MyProperties> myProperties = (List<MyProperties>) msg.obj;
                        for (int t = 0; t < myProperties.size(); t++) {
                            if (myProperties.get(t).getCommunityId().equals(mCommunityId)) {
                                if (myProperties.get(t).getRooms() != null && myProperties.get(t).getRooms().size() > 0) {
                                    int leg = myProperties.get(t).getRooms().size();
                                    for (int j = 0; j < leg; j++) {
                                        if (myProperties.get(t).getRooms().get(j).getRoomId().equals(mHouseId)) {
                                            havebind = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (!havebind) {
                            //   getEntranceGuard(mHouseId);
                            service.getEntranceGuard(mHouseId, handlerGetEntranceGuard);
                        } else {
                            closeProgressDialog();
                            showToast(getString(R.string.havebind));
                        }
                    } else {
                        closeProgressDialog();
                        showToast(R.string.network_error);
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    closeProgressDialog();
                    showToast(msg.obj.toString());
                    break;
            }

        }
    };


    //获取房屋的业主列表
    Handler handlerGetEntranceGuard = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null && ((List<EntranceGuard>) msg.obj).size() > 0) {
                        boolean isOwner = false;
                        entranceGuards = (List<EntranceGuard>) msg.obj;
                        for (int j = 0; j < entranceGuards.size(); j++) {
                            //如果当前用户属于业主（不含家属和租户）列表中的，那么直接绑定
                            if (UserInformation.getUserInfo().getUserPhone().equals(entranceGuards.get(j).getValue())) {
                                isOwner = true;
                                break;
                            }
                        }
                        if (isOwner) {
                            service.BindingHouse(mCommunityId, mHouseId, handlerBindingHouse);
                            // BindingHouse(mCommunityId, mHouseId);
                        } else {
                            closeProgressDialog();
                            HashMap<String, String> extras = new HashMap<String, String>();
                            extras.put("ROOM_ID", mHouseId);
                            extras.put("CLASS_FROM", "BindingHouse");
                            extras.put("COMMUNITY_ID", mCommunityId);
                            extras.put("COMMUNITY_NAME", mCOMMUNITY_NAME);
                            try {
                                //非业主的话先验证（申请访客密码是也需要验证）
                                gotoActivityAndFinish(VisitorPsd.class.getName(), extras);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case BaseService.DATA_SUCCESS_OTHER:
                    closeProgressDialog();
                    getPropertyTelphone();
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    closeProgressDialog();
                    showToast(msg.obj.toString());
                    break;
            }

        }
    };


    //绑定房子
    Handler handlerBindingHouse = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null) {
                        showToast(msg.obj.toString());
                        postEGBind("", "");
                        try {
                            if (!IsFromRegister) {
                                HashMap<String, String> extras = new HashMap<String, String>();
                                extras.put("LEFT", "LEFT");
                                gotoActivityAndFinish(Community.class.getName(), extras);
                            } else {
                                HashMap<String, String> extras = new HashMap<String, String>();
                                extras.put("LEFT", "LEFT");
                                gotoActivityAndFinish(MainActivity.class.getName(), extras);
                            }
//                            SetCurrentInforamtion(communityId, roomId);
//                            IntegralTip(url);
                            SetCurrentInforamtion(mCommunityId, mHouseId);
                            IntegralTip(Services.mHost + "API/Resident/ResidentBindRoom");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };


//    //获取楼栋信息
//    public void Buildings(String communityId) {
//        // 请求的URL
//        String url = Services.mHost + "API/Property/GetBuildByCommunityId/%s";
//        url = String.format(url, communityId);
//        String aa = Services.timeFormat();
//        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
//        String aa2 = url;
//        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
//        OkHttpUtils.get().url(url)
//                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
//                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
//                .addHeader("timestamp", aa)
//                .addHeader("nonce", aa1)
//                .addHeader("signature", Services.textToMD5L32(md5))
//                .build()
//                .execute(new DataCallBack(this) {
//
//                    @Override
//                    public void onBefore(Request request, int id) {
//                        super.onBefore(request, id);
//                        showProgressDialog();
//                    }
//
//                    @Override
//                    public void onError(Call call, Exception e, int i) {
//                        super.onError(call, e, i);
//                        closeProgressDialog();
//                    }
//
//                    @Override
//                    public void onResponse(String s, int i) {
//                        super.onResponse(s, i);
//                        Log.d("asdsdasd", "111111111" + s);
//                        closeProgressDialog();
//                        try {
//                            JSONObject json = new JSONObject(s);
//                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
//                            if (json.getBoolean("Status")) {
//                                if (jsonObject.getBoolean("Valid")) {
//                                    Gson gson = new Gson();
//                                    Type listType = new TypeToken<List<Building>>() {
//                                    }.getType();
//                                    buildings = gson.fromJson(jsonObject.getString("Obj"), listType);
//                                    if (buildings != null && buildings.size() != 0) {
//                                        mBuild_datas.addAll(buildings);
//                                        adapter_build.notifyDataSetChanged();
//                                    } else {
//                                        btn_binding_house.setEnabled(false);
//                                        showToast(R.string.perfect_property_information);
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    //解除房子绑定
    public void RemoveHouse(String communityId, String roomId) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Resident/RemoveBindRoom";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("CommunityId", communityId);
        extras.put("RoomId", roomId);
        extras.put("ResidentId", UserInformation.getUserInfo().getUserId());
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("ResidentId", UserInformation.getUserInfo().getUserId())
                .addParams("RoomId", roomId)
                .addParams("CommunityId", communityId)
                .build()
                .execute(new DataCallBack(this) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    showToast(jsonObject.getString("Message"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //绑定房子(绑定成功后，EGBind)
    public void BindingHouse(final String communityId, final String roomId) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        final String url = Services.mHost + "API/Resident/ResidentBindRoom";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("CommunityId", communityId);
        extras.put("RoomId", roomId);
        extras.put("ResidentId", UserInformation.getUserInfo().getUserId());
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("CommunityId", communityId)
                .addParams("ResidentId", UserInformation.getUserInfo().getUserId())
                .addParams("RoomId", roomId)
                .build()
                .execute(new DataCallBack(this) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd", "绑定房子结果" + s);
                        closeProgressDialog();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    //业主绑定成功后，也要EGbind
                                    postEGBind("", "");
                                    showToast(jsonObject.getString("Message"));
                                    try {
                                        if (!IsFromRegister) {
                                            HashMap<String, String> extras = new HashMap<String, String>();
                                            extras.put("LEFT", "LEFT");
                                            gotoActivityAndFinish(Community.class.getName(), extras);
                                        } else {
                                            HashMap<String, String> extras = new HashMap<String, String>();
                                            extras.put("LEFT", "LEFT");
                                            gotoActivityAndFinish(MainActivity.class.getName(), extras);
                                        }
                                        SetCurrentInforamtion(communityId, roomId);
                                        IntegralTip(url);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // MyProperties(communityId,roomId);
                                    showToast(jsonObject.getString("Message"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取房屋的业主列表
    public void getEntranceGuard(String roomId) {

        String url = Services.mHost + "API/EntranceGuard/RoomOwners?roomId=" + roomId;
        url = String.format(url);
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
                .execute(new DataCallBack(this) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        closeProgressDialog();
                        Log.e("asdsdasd", "getEntranceGuard=" + s);


                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            Log.e("asdsdasd", "getEntranceGuard=" + jsonObject.getString("Obj"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    if (jsonObject.getString("Obj") != null && !jsonObject.getString("Obj").equals("[]")) {
                                        Gson gson = new Gson();
                                        Type type = new TypeToken<List<EntranceGuard>>() {
                                        }.getType();
                                        entranceGuards = gson.fromJson(jsonObject.getString("Obj"), type);
                                        if (entranceGuards.size() > 0) {
                                            boolean isOwner = false;

                                            for (int j = 0; j < entranceGuards.size(); j++) {

                                                //如果当前用户属于业主（不含家属和租户）列表中的，那么直接绑定
                                                if (UserInformation.getUserInfo().getUserPhone().equals(entranceGuards.get(j).getValue())) {
                                                    isOwner = true;
                                                    break;
                                                }
                                            }
                                            if (isOwner) {
                                                BindingHouse(mCommunityId, mHouseId);
                                            } else {
                                                HashMap<String, String> extras = new HashMap<String, String>();
                                                extras.put("ROOM_ID", mHouseId);
                                                extras.put("CLASS_FROM", "BindingHouse");
                                                extras.put("COMMUNITY_ID", mCommunityId);
                                                extras.put("COMMUNITY_NAME", mCOMMUNITY_NAME);
                                                try {
                                                    //非业主的话先验证（申请访客密码是也需要验证）
                                                    gotoActivityAndFinish(VisitorPsd.class.getName(), extras);
                                                } catch (ClassNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } else {
                                        getPropertyTelphone();
                                    }
                                }
                            } else {
                                showToast(getString(R.string.network_error));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //切换小区\房子
    public void SetCurrentInforamtion(String communityId, String roomId) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Account/SetResidentLogonInfo";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("CommunityId", communityId);
        extras.put("HouseId", roomId);
        extras.put("ResidentId", UserInformation.getUserInfo
                ().getUserId());
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("ResidentId", UserInformation.getUserInfo().getUserId())
                .addParams("HouseId", roomId)
                .addParams("CommunityId", communityId)
                .build()
                .execute(new DataCallBack(this) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    User user = gson.fromJson(jsonObject.getString("Obj"), User.class);
                                    UserInformation.setUserInfo(user);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //查看积分
    public void IntegralTip(String pUrl) {
        String url1 = "";
        try {
            pUrl = new URL(pUrl).getPath();
            // 请求的URL
            String url = Services.mHost + "API/Prints/Add/%s?route=%s";
            url = String.format(url, UserInformation.getUserInfo().UserId, URLEncoder.encode(pUrl, "UTF-8"));
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
                    .execute(new DataCallBack(this) {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            super.onError(call, e, i);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Log.e("asdsdasd", "绑定积分呢" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

//    //获取单元信息
//    public void Units(String buildingId) {
//        // 请求的URL
//        String url = Services.mHost + "API/Property/GetUnitByBuildId/%s";
//        url = String.format(url, buildingId);
//        String aa = Services.timeFormat();
//        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
//        String aa2 = url;
//        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
//        OkHttpUtils.get().url(url)
//                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
//                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
//                .addHeader("timestamp", aa)
//                .addHeader("nonce", aa1)
//                .addHeader("signature", Services.textToMD5L32(md5))
//                .build()
//                .execute(new DataCallBack(this) {
//                    @Override
//                    public void onError(Call call, Exception e, int i) {
//                        super.onError(call, e, i);
//                    }
//
//                    @Override
//                    public void onResponse(String s, int i) {
//                        super.onResponse(s, i);
//                        Log.e("asdsdasd", "单元" + s);
//                        try {
//                            JSONObject json = new JSONObject(s);
//                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
//                            if (json.getBoolean("Status")) {
//                                if (jsonObject.getBoolean("Valid")) {
//                                    showToast(jsonObject.getString("Message"));
//                                    Gson gson = new Gson();
//                                    Type listType = new TypeToken<List<Building>>() {
//                                    }.getType();
//
//                                    buildings = gson.fromJson(jsonObject.getString("Obj"), listType);
//                                    if (buildings != null) {
//                                        mUnit_datas.clear();
//                                        mUnit_datas.addAll(buildings);
//                                        adapter_unit.notifyDataSetChanged();
//                                    }
//                                    //绑定房屋
//                                    buildings.clear();
//                                    if (mUnit_datas.get(0) != null && !TextUtils.isEmpty(mUnit_datas.get(0).Id)) {
//                                        Houses(mUnit_datas.get(0).Id);
//                                    }
//
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }
//
//    //获取房子列表信息
//    public void Houses(String unitId) {
//        // 请求的URL
//        String url = Services.mHost + "API/Property/GetRoomByUnitId/%s";
//        url = String.format(url, unitId);
//        String aa = Services.timeFormat();
//        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
//        String aa2 = url;
//        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
//        OkHttpUtils.get().url(url)
//                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
//                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
//                .addHeader("timestamp", aa)
//                .addHeader("nonce", aa1)
//                .addHeader("signature", Services.textToMD5L32(md5))
//                .build()
//                .execute(new DataCallBack(this) {
//                    @Override
//                    public void onError(Call call, Exception e, int i) {
//                        super.onError(call, e, i);
//                    }
//
//                    @Override
//                    public void onResponse(String s, int i) {
//                        super.onResponse(s, i);
//                        Log.d("asdsdasd", "111111111" + s);
//                        try {
//                            JSONObject json = new JSONObject(s);
//                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
//                            if (json.getBoolean("Status")) {
//                                if (jsonObject.getBoolean("Valid")) {
//                                    showToast(jsonObject.getString("Message"));
//                                    Gson gson = new Gson();
//                                    Type listType = new TypeToken<List<Building>>() {
//                                    }.getType();
//                                    buildings2 = gson.fromJson(jsonObject.getString("Obj"), listType);
//                                    if (buildings2 != null) {
//                                        mRoom_datas.clear();
//                                        mRoom_datas.addAll(buildings2);
//                                        adapter_room.notifyDataSetChanged();
//                                        if (buildings2.isEmpty()) {
//
//                                        } else {
//                                            mHouseId = buildings2.get(0).Id;
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_binding_house.setOnClickListener(this);
        aaa.setOnClickListener(this);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            try {
                if (!IsFromRegister) {
                    gotoActivityAndFinish(Community.class.getName(), null);
                } else {
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    //初始化楼栋信息
    private void initBuildings() {
        sr_binding_house_build = (Spinner) findViewById(R.id.sr_binding_house_build);
        mBuild_datas = new ArrayList<Building>();
        //将可选内容与ArrayAdapter连接起来
        adapter_build = new ArrayAdapter(this, R.layout.dropdown_check_item, mBuild_datas);
        //设置下拉列表的风格
        adapter_build.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_binding_house_build.setAdapter(adapter_build);
        //设置选择事件
        sr_binding_house_build.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Building building = mBuild_datas.get(i);
                        if (!TextUtils.isEmpty(building.Id)) {
                            //绑定单元
                            //   Units(building.Id);
                            service.Units(building.Id, handlerUnit);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }

        );
    }

//初始化单元信息

    private void initUnits() {
        sr_binding_house_unit = (Spinner) findViewById(R.id.sr_binding_house_unit);
        mUnit_datas = new ArrayList<Building>();
        //将可选内容与ArrayAdapter连接起来
        adapter_unit = new ArrayAdapter(this, R.layout.dropdown_check_item, mUnit_datas);
        //设置下拉列表的风格
        adapter_unit.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_binding_house_unit.setAdapter(adapter_unit);
        //设置选择事件
        sr_binding_house_unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Building building = mUnit_datas.get(i);
                if (!TextUtils.isEmpty(building.Id)) {
                    //绑定房屋
                    //  Houses(building.Id);
                    service.Houses(building.Id, handlerHouse);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //初始化楼栋房间信息
    private void initHouses() {
        sr_binding_house_room = (Spinner) findViewById(R.id.sr_binding_house_room);

        mRoom_datas = new ArrayList<Building>();
        //将可选内容与ArrayAdapter连接起来
        adapter_room = new ArrayAdapter(this, R.layout.dropdown_check_item, mRoom_datas);
        //设置下拉列表的风格
        adapter_room.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_binding_house_room.setAdapter(adapter_room);
        //设置选择事件


        sr_binding_house_room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Building building = mRoom_datas.get(i);
                mHouseId = building.Id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    //API/EntranceGuard/EGBind 修改用户与房屋绑定关系中的门禁信息状态
    public void postEGBind(String sDate, String eDate) {
        String url = Services.mHost + "API/EntranceGuard/EGBind";
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("residentId", UserInformation.getUserInfo().getUserId());
        extras.put("roomId", mHouseId);
        extras.put("ownerid", UserInformation.getUserInfo().getUserId());
        extras.put("leaseDateS", sDate);
        extras.put("leaseDateE", eDate);
        extras.put("residentType", 0 + "");
        Log.d("asdsdasd", "--" + Services.json(extras));
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        Log.d("asdsdasd", "--" + Services.json(extras));
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addParams("residentId", UserInformation.getUserInfo().getUserId())
                .addParams("roomId", mHouseId)
                .addParams("ownerid", UserInformation.getUserInfo().getUserId())
                .addParams("leaseDateS", sDate)
                .addParams("leaseDateE", eDate)
                .addParams("residentType", 0 + "")
                .build()
                .execute(new DataCallBack(this) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd", "修改绑定状态结果" + s);
                        closeProgressDialog();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (jsonObject.getBoolean("Valid")) {
                                showToast("修改绑定状态成功");
                                HashMap<String, String> extras1 = new HashMap<String, String>();
                                try {
                                    gotoActivityAndFinish(Community.class.getName(), extras1);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showToast("修改绑定状态失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取物业联系电话
    private void getPropertyTelphone() {
        String url = Services.mHost + "Api/Property/GetCommonTel/%s";
        url = String.format(url, mCommunityId == null ? UserInformation.getUserInfo().getCommunityId() : mCommunityId);
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
                .execute(new DataCallBack(this) {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        closeProgressDialog();
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<PPhones>>() {
                                    }.getType();
                                    List<PPhones> mDatas = gson.fromJson(jsonObject.getString("Obj"),
                                            listType);
                                    List<PPhones> newDatas = new ArrayList<PPhones>();
                                    if (mDatas != null && mDatas.size() > 0) {
                                        for (int t = 0; t < mDatas.size(); t++) {
                                            if (mDatas.get(t).getTitle().equals("物业管理处电话")) {
                                                newDatas.add(mDatas.get(t));
                                            }
                                        }

                                        Log.e("asdsdasd", "物业电话1111" + s);
                                        if (newDatas.size() == 0) {
                                            showCallPop(mDatas);
                                        } else {
                                            showCallPop(newDatas);
                                        }
                                    } else {
                                        showToast(R.string.Property_does_not_provide_phone_call);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void showCallPop(List<PPhones> phonesList) {
        ListViewAdapter<PPhones> mAdapter;
        LayoutInflater layoutInflater = LayoutInflater.from(BindingHouse.this);
        View popupView = layoutInflater.inflate(R.layout.pop_property_telphone, null);
        final PopupWindow mPopWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(popupView);
        View rootview = layoutInflater.inflate(R.layout.main, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        mPopWindow.setAnimationStyle(R.anim.slide_in_from_bottom);

        TextView title = (TextView) popupView.findViewById(R.id.poptitle);
        title.setText(getResources().getText(R.string.noPrpperty));
        ListView listTelPhone = (ListView) popupView.findViewById(R.id.list_propert_telphone);
        mAdapter = new ListViewAdapter<PPhones>(BindingHouse.this, R.layout.item_telephone, phonesList) {
            @Override
            public void convert(ViewHolder holder, final PPhones phones) {
                holder.setText(R.id.tv_title, phones.Title).setText(R.id.tv_telephone, phones.Tel);
                ImageButton telephone = holder.getView(R.id.ibtn_telephone);
                telephone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phones.Tel));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                });
            }
        };
        listTelPhone.setAdapter(mAdapter);
        popupView.findViewById(R.id.cancel_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopWindow.setAnimationStyle(R.anim.slide_out_to_bottom);
                mPopWindow.dismiss();
                backgroundAlpaha(BindingHouse.this, 1.0f);
            }
        });
        backgroundAlpaha(BindingHouse.this, 0.5f);
    }

    public void backgroundAlpaha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }


//    //获取我的小区和房产,判断用户是否绑定该房屋
//    public void MyProperties(String communityID, String houseID) {
//        // 请求的URL
//        String url = Services.mHost + "API/Resident/GetResidentBindInfo/%s";
//        url = String.format(url, UserInformation.getUserInfo().getUserId());
//        String aa = Services.timeFormat();
//        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
//        String aa2 = url;
//        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
//        OkHttpUtils.get().url(url)
//                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
//                .addHeader("timestamp", aa)
//                .addHeader("nonce", aa1)
//                .addHeader("signature", Services.textToMD5L32(md5))
//                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo()).build()
//                .execute(new DataCallBack(this) {
//                    @Override
//                    public void onError(Call call, Exception e, int i) {
//                        super.onError(call, e, i);
//                    }
//
//                    @Override
//                    public void onResponse(String s, int i) {
//                        super.onResponse(s, i);
//                        Log.e("asdsdasd", "是否有该房屋" + s);
//                        try {
//                            JSONObject json = new JSONObject(s);
//                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
//                            if (json.getBoolean("Status")) {
//                                if (jsonObject.getBoolean("Valid")) {
//                                    havebind = false;
//                                    Gson gson = new Gson();
//                                    Type type = new TypeToken<List<MyProperties>>() {
//                                    }.getType();
//                                    List<MyProperties> myProperties = gson.fromJson(jsonObject.getString("Obj"), type);
//                                    if (myProperties != null && myProperties.size() > 0) {
//
//                                        for (int t = 0; t < myProperties.size(); t++) {
//                                            if (myProperties.get(t).getCommunityId().equals(communityID)) {
//                                                if (myProperties.get(t).getRooms() != null && myProperties.get(t).getRooms().size() > 0) {
//                                                    int leg = myProperties.get(t).getRooms().size();
//                                                    for (int j = 0; j < leg; j++) {
//                                                        if (myProperties.get(t).getRooms().get(j).getRoomId().equals(houseID)) {
//                                                            havebind = true;
//                                                            break;
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (!havebind) {
//                                        //   getEntranceGuard(mHouseId);
//                                        service.getEntranceGuard(mHouseId, handlerGetEntranceGuard);
//                                    } else {
//                                        showToast(getString(R.string.havebind));
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerBuilding.removeCallbacksAndMessages(null);
        handlerBindingHouse.removeCallbacksAndMessages(null);
        handlerGetEntranceGuard.removeCallbacksAndMessages(null);
        handlerMyProperties.removeCallbacksAndMessages(null);
        handlerHouse.removeCallbacksAndMessages(null);
        handlerUnit.removeCallbacksAndMessages(null);

    }
}

