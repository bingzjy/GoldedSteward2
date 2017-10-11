package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.me.Community;
import com.ldnet.activity.me.VisitorPsd;
import com.ldnet.entities.Building;
import com.ldnet.entities.EntranceGuard;
import com.ldnet.entities.MyProperties;
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
import java.util.HashMap;
import java.util.List;

/**
 * Created by lee on 2017/6/25.
 */
public class BindingService extends BaseService {

    public BindingService(Context context) {
        this.mContext = context;
    }


    //获取楼栋信息
    public void Buildings(final String communityId,final Handler handlerBuilding) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetBuildByCommunityId/%s";
        url = String.format(url, communityId);
        OkHttpService.get(url).execute(new DataCallBack(mContext, handlerBuilding) {
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
                Log.e("asdsdasd", "楼栋" + s);
                try {
                    JSONObject json = new JSONObject(s);
                    if (checkJsonData(s, handlerBuilding)) {
                        JSONObject jsonObject = new JSONObject(json.getString("Data"));
                        if (jsonObject.getBoolean("Valid") && jsonObject.getString("Obj") != null && !jsonObject.getString("Obj").equals("[]")) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Building>>() {
                            }.getType();
                            List<Building> buildings = gson.fromJson(jsonObject.getString("Obj"), listType);

                            Message msg = new Message();
                            msg.what = DATA_SUCCESS;
                            msg.obj = buildings;
                            handlerBuilding.sendMessage(msg);
                        } else {
                            sendErrorMessage(handlerBuilding, jsonObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //获取单元信息
    public void Units(final String buildingId, final Handler handlerUnits) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetUnitByBuildId/%s";
        url = String.format(url, buildingId);
        OkHttpService.get(url).execute(new DataCallBack(mContext, handlerUnits) {
            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
            }
            @Override
            public void onResponse(String s, int i) {
                super.onResponse(s, i);
                Log.d("asdsdasd", "111111111" + s);
                try {
                    if (checkJsonData(s, handlerUnits)) {
                        JSONObject json = new JSONObject(s);
                        JSONObject jsonObject = new JSONObject(json.getString("Data"));
                        if (jsonObject.getBoolean("Valid") && !jsonObject.getString("Obj").equals("") && !jsonObject.get("Obj").equals("[]")) {

                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Building>>() {
                            }.getType();
                            List<Building> buildings = gson.fromJson(jsonObject.getString("Obj"), listType);

                            Message msg = new Message();
                            msg.what = DATA_SUCCESS;
                            msg.obj = buildings;
                            handlerUnits.sendMessage(msg);
                        } else {
                            sendErrorMessage(handlerUnits, jsonObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    //获取房子列表信息
    public void Houses(final String unitId,final Handler handlerHouses) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetRoomByUnitId/%s";
        url = String.format(url, unitId);
        OkHttpService.get(url).execute(new DataCallBack(mContext, handlerHouses) {
            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
            }

            @Override
            public void onResponse(String s, int i) {
                super.onResponse(s, i);
                Log.d("asdsdasd", "111111111" + s);
                try {
                    if (checkJsonData(s, handlerHouses)) {
                        JSONObject json = new JSONObject(s);
                        JSONObject jsonObject = new JSONObject(json.getString("Data"));
                        if (jsonObject.getBoolean("Valid") && !jsonObject.getString("Obj").equals("") && !jsonObject.get("Obj").equals("[]")) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Building>>() {
                            }.getType();
                            List<Building> buildings2 = gson.fromJson(jsonObject.getString("Obj"), listType);

                            Message msg = new Message();
                            msg.what = DATA_SUCCESS;
                            msg.obj = buildings2;
                            handlerHouses.sendMessage(msg);
                        } else {
                            sendErrorMessage(handlerHouses, jsonObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //获取房屋的业主列表
    public void getEntranceGuard(final String roomId,final Handler handlerGetEntranceGuard) {

        String url = Services.mHost + "API/EntranceGuard/RoomOwners?roomId=" + roomId;
        url = String.format(url);
        OkHttpService.get(url).execute(new DataCallBack(mContext, handlerGetEntranceGuard) {
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
                Log.e("asdsdasd", "getEntranceGuard=" + s);
                try {
                    if (checkJsonData(s, handlerGetEntranceGuard)) {
                        JSONObject json = new JSONObject(s);
                        JSONObject jsonObject = new JSONObject(json.getString("Data"));
                        if (jsonObject.getBoolean("Valid")) {
                            if (jsonObject.getString("Obj") != null && !jsonObject.getString("Obj").equals("[]")) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<List<EntranceGuard>>() {
                                }.getType();
                                List<EntranceGuard> entranceGuards = gson.fromJson(jsonObject.getString("Obj"), type);
                                Message msg = new Message();
                                if (entranceGuards.size() > 0) {
                                    msg.what = DATA_SUCCESS;
                                    msg.obj = entranceGuards;
                                    handlerGetEntranceGuard.sendMessage(msg);

                                } else {
                                    msg.what = DATA_SUCCESS_OTHER;
                                    handlerGetEntranceGuard.sendMessage(msg);
                                    //  getPropertyTelphone();
                                }
                            } else {
                                Message msg = new Message();
                                msg.what = DATA_SUCCESS_OTHER;
                                handlerGetEntranceGuard.sendMessage(msg);
                                // getPropertyTelphone();
                            }
                        } else {
                            sendErrorMessage(handlerGetEntranceGuard, jsonObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    //绑定房子(绑定成功后，EGBind)
    public void BindingHouse(final String communityId, final String roomId,final Handler handlerBindingHouse) {
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
//        OkHttpService.post(url).addHeader("signature", Services.textToMD5L32(md5))
                .addParams("CommunityId", communityId)
                .addParams("ResidentId", UserInformation.getUserInfo().getUserId())
                .addParams("RoomId", roomId)
                .build()
                .execute(new DataCallBack(mContext, handlerBindingHouse) {

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
                        Log.e("asdsdasd", "绑定房子结果" + s);
                        try {
                            if (checkJsonData(s, handlerBindingHouse)) {
                                {
                                    JSONObject json = new JSONObject(s);
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && jsonObject.getString("Obj") != null) {
                                        Message msg = new Message();
                                        msg.what = DATA_SUCCESS;
                                        msg.obj = jsonObject.getString("Message");
                                        handlerBindingHouse.sendMessage(msg);
                                    } else {
                                        sendErrorMessage(handlerBindingHouse, jsonObject);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取我的小区和房产,判断用户是否绑定该房屋
    public void MyProperties(final String communityID, final String houseID, final Handler handerMyProperties) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetResidentBindInfo/%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId());
        OkHttpService.get(url).execute(new DataCallBack(mContext, handerMyProperties) {
            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
            }

            @Override
            public void onResponse(String s, int i) {
                super.onResponse(s, i);
                Log.e("asdsdasd", "是否有该房屋" + s);
                try {
                    if (checkJsonData(s, handerMyProperties)) {
                        JSONObject json = new JSONObject(s);
                        JSONObject jsonObject = new JSONObject(json.getString("Data"));
                        if (jsonObject.getBoolean("Valid") && jsonObject.getString("Obj") != null && !jsonObject.getString("Obj").equals("[]")) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<MyProperties>>() {
                            }.getType();
                            List<MyProperties> myProperties = gson.fromJson(jsonObject.getString("Obj"), type);

                            Message msg = new Message();
                            msg.what = DATA_SUCCESS;
                            msg.obj = myProperties;
                            handerMyProperties.sendMessage(msg);

                        } else {
                            sendErrorMessage(handerMyProperties, jsonObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
