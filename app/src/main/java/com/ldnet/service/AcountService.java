package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.google.gson.Gson;
import com.ldnet.activity.BindingCommunity;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.adapter.MyDialog2;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lee on 2017/6/24.
 */
public class AcountService extends BaseService {

    public Handler Handle_Login;
    public Handler Handle_Login_Token;
    public Handler Handle_Send_SMS;
    public Handler Handle_Valid_SMS;
    public Handler Handle_Reset_Password;
    public Handler Handle_Update_Password;
    public Handler Handle_Contacts;
    public Handler Handle_Apply_Send_SMS;
    public Handler Handle_SetAppRequest;
    public Handler Handle_SetLog;

//    // 请求成功
//    public static Integer DATA_SUCCESS = 2000;
//    // 请求失败
//    public static Integer DATA_FAILURE = 2001;

    public String tag = AcountService.class.getSimpleName();

    public AcountService(Context context) {
        this.mContext = context;
    }


    //getToken
    public void getToken(final String phone, final Handler handlerToken) {
        String url1 = Services.mHost + "GetToken?phone=%s&clientType=22";
        url1 = String.format(url1, phone);
        OkHttpUtils.get().url(url1)
                .addHeader("phone", phone)
                .addHeader("timestamp", Services.timeFormat())
                .addHeader("nonce", (int) ((Math.random() * 9 + 1) * 100000) + "")
                .build()
                .execute(new DataCallBack(mContext) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd---====", "getToken----" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (s.contains(mContext.getString(R.string.refuse))) {
                                sendErrorMessage(handlerToken, json);
                            } else {
                                if (json.getBoolean("Status")) {
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                        String token = jsonObject.getString("Obj");
                                        TokenInformation.setTokenInfo(token);
                                        Services.TOKEN = TokenInformation.getTokenInfo().toString();

                                        Message msg = new Message();
                                        msg.what = DATA_SUCCESS;
                                        msg.obj = phone;
                                        handlerToken.sendMessage(msg);
                                        //  GetCode(phone, "0");
                                    } else {
                                        sendErrorMessage(handlerToken, jsonObject);
                                    }
                                } else {
                                    sendErrorMessage(handlerToken, "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    // 获取验证码
    public void GetCode(final String tel,final  String type,final  Handler handlerGetCode) {
        // 请求的URL
        // Get请求,第一个参数电话、第二个参数类型是指注册（0）还是忘记密码（1）
        String url = Services.mHost + "API/Account/RegisterSendSMS/%s/%s";
        url = String.format(url, tel, type);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = tel + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", tel)
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(mContext) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (s.contains(mContext.getString(R.string.refuse))) {
                                sendErrorMessage(handlerGetCode, json);
                            } else {
                                if (json.getBoolean("Status")) {
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                        Message message = new Message();
                                        message.what = DATA_SUCCESS;
                                        handlerGetCode.sendMessage(message);
                                    } else {
                                        sendErrorMessage(handlerGetCode, jsonObject);
                                    }
                                } else {
                                    sendErrorMessage(handlerGetCode, "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }


    // 验证验证码正确性
    public void VaildCode(final String tel,final  String code, final String type, final Handler handlerValidCode) {
        // 请求的URL
        // Get请求,第一个参数电话、第二个参数验证码、第三个参数类型是指注册（0）还是忘记密码（1）
        String url = Services.mHost + "API/Account/ValidSNSCode/%s/%s/%s";
        url = String.format(url, tel, code, type);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = tel + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", tel)
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
                        Log.e("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (s.contains(mContext.getString(R.string.refuse))) {
                                sendErrorMessage(handlerValidCode, json);
                            } else {
                                if (json.getBoolean("Status")) {
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                        Message message = new Message();
                                        message.what = DATA_SUCCESS;
                                        handlerValidCode.sendMessage(message);
                                    } else {
                                        sendErrorMessage(handlerValidCode, jsonObject);
                                    }
                                } else {
                                    sendErrorMessage(handlerValidCode, "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    // 用户注册
    public void Register(final String tel, final String code,final  String password,
                         final String recommendTel,final  Handler handlerRegister) {
        Log.e("asdsdasd", "注册---参数" + tel + "----" + code + "----" + password + "---" + recommendTel);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Account/Register";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("Tel", tel);
        extras.put("Code", code);
        extras.put("Password", password);
        extras.put("RecommendTel", recommendTel);
        Services.json(extras);
        String md5 = tel +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("phone", tel)
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("Tel", tel)
                .addParams("Code", code)
                .addParams("Password", password)
                .addParams("RecommendTel", recommendTel)
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
                        Log.e("asdsdasd", "注册---" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (s.contains(mContext.getString(R.string.refuse))) {
                                sendErrorMessage(handlerRegister, json);
                            } else {
                                if (json.getBoolean("Status")) {
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                        Message message = new Message();
                                        message.what = DATA_SUCCESS;
                                        handlerRegister.sendMessage(message);
                                    } else {
                                        sendErrorMessage(handlerRegister, jsonObject);
                                    }
                                } else {
                                    sendErrorMessage(handlerRegister, "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取用户数据
    public void getData(final String phone, final String psd, final int type,final  Handler handlerGetData) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        final String url = Services.mHost + "API/Account/Logon";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("UserName", phone);
        extras.put("Password", psd);
        extras.put("PlatForm", "Android");
        Services.json(extras);
        String md5 = phone +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("phone", phone)
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("UserName", phone)
                .addParams("Password", psd)
                .addParams("PlatForm", "Android")
                .build().execute(new DataCallBack(mContext) {

            @Override
            public String parseNetworkResponse(Response response, int id) throws IOException {
                if (response.isSuccessful()) {
                    Headers headers = response.headers();
                    if (headers.values("Set-Cookie").size() > 0) {
                        List<String> cookies = headers.values("Set-Cookie");
                        CookieInformation.setCookieInfo("cookies", cookies.get(0));
                    }
                }
                return super.parseNetworkResponse(response, id);
            }

            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
            }

            @Override
            public void onResponse(String s, int i) {
                Log.e("asdsdasd", "获取数据--" + s + "type-------" + type);
                try {
                    JSONObject json = new JSONObject(s);
                    if (s.contains(mContext.getString(R.string.refuse))) {
                        sendErrorMessage(handlerGetData, json);
                    } else {
                        if (json.getBoolean("Status")) {
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                Gson gson = new Gson();
                                User user = gson.fromJson(jsonObject.getString("Obj"), User.class);
                                UserInformation.setUserInfo(user);

                                Message msg = new Message();
                                msg.what = DATA_SUCCESS;
                                msg.obj = type;
                                handlerGetData.sendMessage(msg);
                            } else {
                                sendErrorMessage(handlerGetData, jsonObject);
                            }
                        } else {
                            sendErrorMessage(handlerGetData, "");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    //判断是否业主
    public void checkIsOwner(final Handler handlerIsOwner) {
        Log.e("asdsdasd", "判断是否业主参数" + UserInformation.getUserInfo().getUserPhone() + "---" + UserInformation.getUserInfo().getUserId());
        String url = Services.mHost + "API/Account/IsOwner?residentId=" + UserInformation.getUserInfo().getUserId();
        url = String.format(url);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        Log.e("asdsdasd", "判断是否业主URL-----" + url);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
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
                        Log.e("asdsdasd", "判断是否业主" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (s.contains(mContext.getString(R.string.refuse))) {
                                sendErrorMessage(handlerIsOwner, json);
                            } else {
                                if (json.getBoolean("Status")) {
                                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                    Message msg = new Message();
                                    if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                        msg.what = DATA_SUCCESS;
                                        msg.obj = jsonObject.getString("Message") == null ? mContext.getString(R.string.welcome_owner) : jsonObject.getString("Message");
                                    } else {
                                        msg.what = DATA_SUCCESS_OTHER;
                                    }
                                    handlerIsOwner.sendMessage(msg);
                                } else {
                                    sendErrorMessage(handlerIsOwner, "");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取积分
    public static void IntegralTip(final int type, final Handler handlerIntegralTip) {
        String pUrl = Services.mHost + "API/Account/Logon";
        try {
            pUrl = new URL(pUrl).getPath();
            // 请求的URL
            String url = Services.mHost + "API/Prints/Add/%s?route=%s";
            url = String.format(url, UserInformation.getUserInfo().UserId, URLEncoder.encode(pUrl, "UTF-8"));
            OkHttpService.get(url).execute(new DataCallBack(mContext) {
                @Override
                public void onError(Call call, Exception e, int i) {
                    super.onError(call, e, i);
                }

                @Override
                public void onResponse(String s, int i) {
                    try {
                        JSONObject json = new JSONObject(s);
                        if (s.contains(mContext.getString(R.string.refuse))) {
                            sendErrorMessage(handlerIntegralTip, json);
                        } else {
                            if (json.getBoolean("Status")) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid") && !TextUtils.isEmpty(jsonObject.getString("Obj"))) {
                                    Message msg = new Message();
                                    msg.what = DATA_SUCCESS;
                                    msg.obj = type;
                                    handlerIntegralTip.sendMessage(msg);
                                } else {
                                    sendErrorMessage(handlerIntegralTip, jsonObject);
                                }
                            } else {
                                sendErrorMessage(handlerIntegralTip, "");
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


    //判断房屋与APP用户绑定关系是否通过验证[业主]
    public void getApprove(final String roomId, final String residentId, final Handler handlerGetApprove) {
        String url = Services.mHost + "API/EntranceGuard/Approve?roomId=" + roomId + "&residentId=" + residentId;
        url = String.format(url);
        OkHttpService.get(url)
                .execute(new DataCallBack(mContext, handlerGetApprove) {
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
                        Log.e(tag, "getApprove:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s, handlerGetApprove)) {
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (!jsonObject.getBoolean("Valid")) {
                                    Message msg=handlerGetApprove.obtainMessage();
                                    msg.what = DATA_SUCCESS_OTHER;
                                    handlerGetApprove.sendMessage(msg);
                                } else {
                                    Message msg=handlerGetApprove.obtainMessage();
                                    msg.what = DATA_SUCCESS;
                                    handlerGetApprove.sendMessage(msg);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //获取用户最新数据
    public void SetCurrentInforamtion(final Handler handlerSetCurrentInforamtion) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Account/SetResidentLogonInfo";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("CommunityId", UserInformation.getUserInfo().getCommunityId());
        extras.put("HouseId", UserInformation.getUserInfo().getHouseId());
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
//        OkHttpService.post(url,extras)
                .addParams("ResidentId", UserInformation.getUserInfo().getUserId())
                .addParams("HouseId", UserInformation.getUserInfo().getHouseId())
                .addParams("CommunityId", UserInformation.getUserInfo().getCommunityId())
                .build()
                .execute(new DataCallBack(mContext) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d(tag, "SetCurrentInforamtion:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            if (checkJsonData(s,handlerSetCurrentInforamtion)){
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    User user = gson.fromJson(jsonObject.getString("Obj"), User.class);
                                    UserInformation.setUserInfo(user);
                                }else{
                                    sendErrorMessage(handlerSetCurrentInforamtion,jsonObject);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }



}
