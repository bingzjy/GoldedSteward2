package com.ldnet.activity.me;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Feedback extends BaseActionBarActivity {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private Button btn_me_feedback;
    private EditText et_me_feedback;
    private Services services;

    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_feedback);
        AppUtils.setupUI(findViewById(R.id.ll_feedback),this);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_me_feedback);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_me_feedback = (Button) findViewById(R.id.btn_me_feedback);
        et_me_feedback = (EditText) findViewById(R.id.et_me_feedback);
        initEvent();
        //初始化服务
        services = new Services();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_me_feedback.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_me_feedback:
                if(!TextUtils.isEmpty(et_me_feedback.getText().toString().trim())){
                    Feedback(et_me_feedback.getText().toString().trim());
                }else{
                    showToast("意见或建议不能为空");
                }

                break;
            default:
                break;
        }
    }

    //意见反馈
    public void Feedback(String content) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        try {
            // 请求的URL
            String url = Services.mHost + "API/File/AppFeedback";

            //获取当前应用的版本号
            GSApplication application = GSApplication.getInstance();
            String appVersion = application.getPackageManager().getPackageInfo(application.getPackageName(), 0).versionName;
            HashMap<String, String> extras = new HashMap<>();
            extras.put("Content", content);
            extras.put("AppVersion", appVersion);
            extras.put("AppSystem", "android");
            extras.put("AppSystemVersion", Build.MODEL + " - Android " + Build.VERSION.RELEASE);
            extras.put("AppType", "业主App");
            extras.put("UserId", UserInformation.getUserInfo().getUserId());
            extras.put("UserName", UserInformation.getUserInfo().getUserName() + "[" + UserInformation.getUserInfo().UserPhone + "]");
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
                    .addParams("Content", content)
                    .addParams("AppVersion", appVersion)
                    .addParams("AppSystem", "android")
                    .addParams("AppSystemVersion", Build.MODEL + " - Android " + Build.VERSION.RELEASE)
                    .addParams("AppType", "业主App")
                    .addParams("UserId", UserInformation.getUserInfo().getUserId())
                    .addParams("UserName", UserInformation.getUserInfo().getUserName() + "[" + UserInformation.getUserInfo().UserPhone + "]")
                    .build()
                    .execute(new DataCallBack(this) {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            super.onError(call,e,i);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Log.d("asdsdasd", "111111111" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {
                                        showToast(getResources().getString(R.string.activity_me_feedback_success));
                                        finish();
                                    } else {
                                        showToast(jsonObject.getString("Message"));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            OkHttpUtils.get().url(url1)
                    .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                    .addHeader("timestamp", aa)
                    .addHeader("nonce", aa1)
                    .addHeader("signature", Services.textToMD5L32(md5))
                    .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo()).build()
                    .execute(new DataCallBack(this) {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            super.onError(call,e,i);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Log.d("asdsdasd", "111111111" + s);
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
        }
    }
}
