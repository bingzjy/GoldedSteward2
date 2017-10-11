package com.ldnet.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.*;
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
 * ***************************************************
 * 登录页面
 * **************************************************
 */
public class Login extends BaseActionBarActivity {

    private EditText et_login_phone;
    private EditText et_login_password;
    private Button btn_login_login;

    private String url = Services.mHost + "API/Account/Logon";
    private String password = "";
    private String phone = "";

    // 初始化事件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        // 控件事件绑定
        findViewById(R.id.ll_button_forgot).setOnClickListener(this);
        findViewById(R.id.ll_button_register).setOnClickListener(this);
        findViewById(R.id.btn_login_login).setOnClickListener(this);
    }


    // 初始化控件
    public void initView() {
        // 设置布局
        setContentView(R.layout.activity_login);

        AppUtils.setupUI(findViewById(R.id.rl_login), this);

        // 去掉信息栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");
        // 登录按钮
        btn_login_login = (Button) findViewById(R.id.btn_login_login);

        // 用户名 - 用户电话
        et_login_phone = (EditText) findViewById(R.id.et_login_phone);
        // 用户密码
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        if (password != null) {
            et_login_password.setText(password);
            et_login_phone.setText(phone);
            et_login_phone.setSelection(phone.length());
        }
    }

    public boolean isNUll() {
        if (TextUtils.isEmpty(et_login_phone.getText().toString().trim())) {
            showToast("手机号码不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_login_password.getText().toString().trim())) {
            showToast("密码不能为空");
            return false;
        }
        return true;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode==KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        // 异常捕获并输出相关信息
        try {
            switch (v.getId()) {
                // 忘记密码
                case R.id.ll_button_forgot:
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Forgot.class.getName(), extras);
                    break;
                // 注册
                case R.id.ll_button_register:
                    HashMap<String, String> extras1 = new HashMap<String, String>();
                    extras1.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Register.class.getName(), extras1);
                    break;
                // 登录
                case R.id.btn_login_login:
                    if (isNUll()) {
                        getToken1(et_login_phone.getText().toString().trim());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void getToken1(String phone) {
        String url1 = Services.mHost + "GetToken?phone=%s&clientType=22";
        url1 = String.format(url1, phone);
        OkHttpUtils.get().url(url1)
                .addHeader("phone", phone)
                .addHeader("timestamp", Services.timeFormat())
                .addHeader("nonce", (int) ((Math.random() * 9 + 1) * 100000) + "")
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
                        Log.e("asdsdasd", "getToken1----" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    String token = jsonObject.getString("Obj");
                                    TokenInformation.setTokenInfo(token);
                                    Services.TOKEN = TokenInformation.getTokenInfo().toString();
                                    getData();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void getData() {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("UserName", et_login_phone.getText().toString().trim());
        extras.put("Password", et_login_password.getText().toString().trim());
        extras.put("PlatForm", "Android");
        Services.json(extras);
        String md5 =  et_login_phone.getText().toString().trim() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        Log.d("Services.aa",aa+","+aa1+","+Services.TOKEN);
        OkHttpUtils.post().url(url)
                .addHeader("phone", et_login_phone.getText().toString().trim())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addParams("UserName", et_login_phone.getText().toString().trim())
                .addParams("Password", et_login_password.getText().toString().trim())
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
            public void onBefore(Request request, int id) {
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                super.onError(call, e, id);
                closeProgressDialog();
            }

            @Override
            public void onResponse(String s, int i) {
                closeProgressDialog();
                Log.e("asdsdasd", "Login" + s);
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                    if (json.getBoolean("Status")) {
                        if (jsonObject.getBoolean("Valid")) {
                            Gson gson = new Gson();
                            User user = gson.fromJson(jsonObject.getString("Obj"), User.class);
                            UserInformation.setUserInfo(user);
                            IntegralTip(url);
                        } else {
                            showToast(jsonObject.getString("Message"));
                        }
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
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Log.d("asdsdasd", "111111111" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));

                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {
                                        if (jsonObject.getString("Obj") != null) {
                                            JSONObject jsonObject1 = new JSONObject(jsonObject.getString("Obj"));
                                            showToast(jsonObject1.getString("Show"));
                                        }

                                        if (!TextUtils.isEmpty(UserInformation.getUserInfo().CommunityId)) {
                                            HashMap<String, String> extras = new HashMap<String, String>();
                                            extras.put("LEFT", "LEFT");
                                            gotoActivityAndFinish(MainActivity.class.getName(), extras);
                                        } else {
                                            gotoActivityAndFinish(BindingCommunity.class.getName(), null);
                                        }
                                    } else {
                                        if (!TextUtils.isEmpty(UserInformation.getUserInfo().CommunityId)) {
                                            HashMap<String, String> extras = new HashMap<String, String>();
                                            extras.put("LEFT", "LEFT");
                                            gotoActivityAndFinish(MainActivity.class.getName(), extras);
                                        } else {
                                            HashMap<String, String> extras = new HashMap<String, String>();
                                            extras.put("LEFT", "LEFT");
                                            gotoActivityAndFinish(BindingCommunity.class.getName(), extras);
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
