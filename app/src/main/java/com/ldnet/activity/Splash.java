package com.ldnet.activity;

//import java.lang.ref.WeakReference;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import com.ldnet.entities.UpdateInformation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Cookies;
import com.ldnet.entities.HouseRent;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

//import android.os.Handler;
//import android.os.Message;

/**
 * ***************************************************
 * 开始广告页
 * **************************************************
 */
public class Splash extends BaseActionBarActivity {

    private ImageView iv;
    private String url = Services.mHost + "API/Account/Logon";
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉信息栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 设置内容
        final View view = View.inflate(this, R.layout.activity_splash, null);
        setContentView(view);
        init(view);


    }

    public void init(final View view) {
        iv = (ImageView) findViewById(R.id.iv);
        /**渐变展示启动屏**/
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(2000);
        view.startAnimation(aa);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (isNetworkAvailable()) {
                            // 如果Cookie存在，直接跳转，无需访问服务器进行登录
                            if (!TextUtils.isEmpty(CookieInformation.getUserInfo().getCookieinfo())) {
                                User user = UserInformation.getUserInfo();
                                getToken(user.getUserPhone());
//                                getData(user.getUserPhone(), user.getUserPassword());
                            } else {
                                Message msg = new Message();
                                msg.what = 1003;
                                handler.sendMessage(msg);
                            }
                        } else {
                            Message msg = new Message();
                            msg.what = 1001;
                            handler.sendMessage(msg);
                        }
                    }
                }.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
    }

    public void getToken(String phone) {
        String url1 = Services.mHost + "GetToken?phone=%s&clientType=22";
        url1 = String.format(url1, phone);
        Log.d("GetToken",url1);
        OkHttpUtils.get().url(url1)
                .addHeader("phone", phone)
                .addHeader("timestamp", Services.timeFormat())
                .addHeader("nonce", (int) ((Math.random() * 9 + 1) * 100000) + "")
                .build()
                .execute(new DataCallBack(this) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd---====", "----" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    token = jsonObject.getString("Obj");
                                    Log.d("asdsdasd---====", "token=" + token);
                                    TokenInformation.setTokenInfo(token);
                                    Services.TOKEN = TokenInformation.getTokenInfo().toString();
                                    getData(UserInformation.getUserInfo().getUserPhone(), UserInformation.getUserInfo().getUserPassword());
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void getData(String phone, String psd) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("UserName", phone);
        extras.put("Password", psd);
        extras.put("PlatForm", "Android");
        Services.json(extras);
        String md5 = phone + aa + aa1 + Services.json(extras) + token;
        OkHttpUtils.post().url(url)
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addParams("UserName", phone)
                .addParams("Password", psd)
                .addParams("PlatForm", "Android")
                .build().execute(new DataCallBack(this) {

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
            public void onBefore(okhttp3.Request request, int id) {
                super.onBefore(request, id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
            }

            @Override
            public void onResponse(String s, int i) {
                Log.d("asdsdasd", "==" + s);
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                    if (json.getBoolean("Status")) {
                        if (jsonObject.getBoolean("Valid")) {
                            //调用一次登录接口
                            Message msg = new Message();
                            msg.what = 1002;
                            handler.sendMessage(msg);
                            Gson gson = new Gson();
                            User user = gson.fromJson(jsonObject.getString("Obj"), User.class);
                            UserInformation.setUserInfo(user);
                            //判断是否绑定小区，未绑定直接跳转绑定小区
                            if (TextUtils.isEmpty(user.getCommunityId())) {
                                try {
                                    gotoActivityAndFinish(BindingCommunity.class.getName(),
                                            null);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                IntegralTip(url);
                            }
                        } else {
                            showToast(jsonObject.getString("Message"));
                        }
                    } else {
                        showToast(jsonObject.getString("Message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void IntegralTip(String pUrl) {
        String url1 = "";
        try {
            pUrl = new URL(pUrl).getPath();
            url1 = Services.mHost + "API/Prints/Add/" + UserInformation.getUserInfo().UserId + "?route=" + pUrl;
            String aa = Services.timeFormat();
            String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
            String aa2 = url1;
            String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
            // 请求的URL
            OkHttpUtils.get().url(url1)
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
                            Log.d("asdsdasd", "111111111" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {
                                        if (!TextUtils.isEmpty(UserInformation.getUserInfo().CommunityId)) {
                                            gotoActivityAndFinish(MainActivity.class.getName(), null);
                                        } else {
                                            gotoActivityAndFinish(BindingCommunity.class.getName(), null);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    //Handler处理消息
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    showToast(getString(R.string.network_none_tip));
                    break;
                case 1002:
                    try {
                        gotoActivityAndFinish(MainActivity.class.getName(), null);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1003:
                    try {
                        gotoActivityAndFinish(Login.class.getName(), null);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //判断当前是否有可用的网路链接
    private Boolean isNetworkAvailable() {
        Context context = getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }
        return false;
    }
}
