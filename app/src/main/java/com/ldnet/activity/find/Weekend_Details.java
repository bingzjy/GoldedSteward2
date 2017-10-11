package com.ldnet.activity.find;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.User;
import com.ldnet.entities.WeekendDetails;
import com.ldnet.entities.WeekendSignUp;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.view.ImageCycleView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class Weekend_Details extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageCycleView vp_weekend_images;
    private TextView tv_weekend_title;
    private TextView tv_weekend_content;
    private TextView tv_weekend_cost;
    private TextView tv_weekend_signup_number;
    private TextView tv_weekend_start_datetime;
    private TextView tv_weekend_end_datetime;
    private TextView tv_weekend_address;
    private Button btn_weekend_signup_information;
    private Button btn_weekend_call;
    private Button btn_weekend_signup;

    private Services services;
    private String mContractPhone;
    private String mWeekendId;
    private Boolean mFromPublish = false;

    private List<View> mImages;
    private PagerAdapter mAdapter;
    private WeekendDetails mDetails;
    private List<WeekendSignUp> infos;
    private ArrayList<String> mImageUrl = null;

    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_weekend_details);

        //二手商品ID
        mWeekendId = getIntent().getStringExtra("WEEKEND_ID");
        String formPublish = getIntent().getStringExtra("FROM_PUBLISH");
        if (!TextUtils.isEmpty(formPublish)) {
            mFromPublish = Boolean.valueOf(formPublish);
        }

        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_find_weekend);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //初始化控件
        vp_weekend_images = (ImageCycleView) findViewById(R.id.vp_weekend_images);
        // 改线ViewPager的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) vp_weekend_images.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        linearParams.height = dm.widthPixels / 16 * 9;
        vp_weekend_images.setLayoutParams(linearParams);
        mImageUrl = new ArrayList<String>();
        //标题
        tv_weekend_title = (TextView) findViewById(R.id.tv_weekend_title);

        //介绍
        tv_weekend_content = (TextView) findViewById(R.id.tv_weekend_content);
        //费用
        tv_weekend_cost = (TextView) findViewById(R.id.tv_weekend_cost);
        //
        tv_weekend_signup_number = (TextView) findViewById(R.id.tv_weekend_signup_number);
        tv_weekend_start_datetime = (TextView) findViewById(R.id.tv_weekend_start_datetime);
        tv_weekend_end_datetime = (TextView) findViewById(R.id.tv_weekend_end_datetime);
        tv_weekend_address = (TextView) findViewById(R.id.tv_weekend_address);
        btn_weekend_signup_information = (Button) findViewById(R.id.btn_weekend_signup_information);
        btn_weekend_call = (Button) findViewById(R.id.btn_weekend_call);
        btn_weekend_signup = (Button) findViewById(R.id.btn_weekend_signup);

        //初始化服务
        services = new Services();
        WeekendDetails(mWeekendId);

        initEvent();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    //周末去哪儿 - 获取闲置物品的详情
    public void WeekendDetails(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetWeekendById/%s?residentId=%s";
        url = String.format(url, id, UserInformation.getUserInfo().getUserId());
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
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd", "WeekendDetails:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    mDetails = gson.fromJson(jsonObject.getString("Obj"), WeekendDetails.class);

                                    //是否可以报名
                                    tv_weekend_end_datetime.setText(Services.subStr(mDetails.EndDatetime));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                                    //显示和隐藏按钮
                                    if (mDetails.ResidentId.equals(UserInformation.getUserInfo().UserId)) {  //是业主本人发布的
                                        btn_weekend_signup_information.setVisibility(View.VISIBLE);
                                        btn_weekend_call.setVisibility(View.GONE);
                                        btn_weekend_signup.setVisibility(View.GONE);
                                    } else {                                                              //非业主本人，可报名
                                        btn_weekend_signup_information.setVisibility(View.GONE);
                                        btn_weekend_call.setVisibility(View.VISIBLE);
                                        btn_weekend_signup.setVisibility(View.VISIBLE);

                                        Date endDate = dateFormat.parse(tv_weekend_end_datetime.getText().toString());
                                        if (mDetails.IsRecord) {
                                            btn_weekend_signup.setEnabled(false);
                                            btn_weekend_signup.setText("已报名");
                                        } else if (endDate.getTime() < new Date().getTime() && !mDetails.IsRecord) {
                                            btn_weekend_signup.setEnabled(false);
                                            btn_weekend_signup.setText("已截止");
                                        }
                                    }

                                    mContractPhone = mDetails.ContractTel;
                                    tv_weekend_title.setText(mDetails.Title);
                                    tv_weekend_content.setText(mDetails.Memo);
                                    tv_weekend_cost.setText("￥" + mDetails.Cost);
                                    tv_weekend_signup_number.setText(String.valueOf(mDetails.MemberCount));
                                    tv_weekend_start_datetime.setText(Services.subStr(mDetails.StartDatetime));
                                    tv_weekend_address.setText(mDetails.ActiveAddress);

                                    //图片加载
                                    if (mDetails.getImg() != null) {
                                        if (mDetails.getImg() != null) {
                                            for (int j = 0; j < mDetails.getImg().length; j++) {
                                                mImageUrl.add(Services.getImageUrl(mDetails.getImg()[j]));
                                            }
                                        }
                                        vp_weekend_images.setImageResources(mImageUrl, mAdCycleViewListener);
                                    }
                                } else {
                                    showToast(jsonObject.getString("Message"));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private com.ldnet.view.ImageCycleView.ImageCycleViewListener mAdCycleViewListener = new com.ldnet.view.ImageCycleView.ImageCycleViewListener() {

        @Override
        public void onImageClick(int position, View imageView) {

        }

        @Override
        public void displayImage(String imageURL, ImageView imageView) {
            ImageLoader.getInstance().displayImage(imageURL, imageView, imageOptions);// 此处本人使用了ImageLoader对图片进行加装！
        }
    };

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_weekend_signup_information.setOnClickListener(this);
        btn_weekend_call.setOnClickListener(this);
        btn_weekend_signup.setOnClickListener(this);

    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    if (!mFromPublish) {
                        gotoActivityAndFinish(Weekend.class.getName(), null);
                    } else {
                        finish();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_weekend_signup_information://查看报名
                dialogSignUp();
                break;
            case R.id.btn_weekend_call://致电组织者
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mContractPhone));
                startActivity(intent);
                break;
            case R.id.btn_weekend_signup://点击报名
                if (!TextUtils.isEmpty(UserInformation.getUserInfo().getUserName())) {
                    weekendApplyDialog();
                } else {
                    showToast(getResources().getString(R.string.weekend_signup_tip));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    //报名的对话框
    private void weekendApplyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.weekend_signup)
                .setCancelable(false)
                .setPositiveButton("确定", new weekendApplyDialogClass())
                .setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogSignUp() {
        WeekendSignUpInformation(mWeekendId, "");
    }

    //获取报名信息
    public void WeekendSignUpInformation(String weekId, String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetWeekendRecord/%s?lastId=%s";
        url = String.format(url, weekId, lastId);
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
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int o) {
                        super.onResponse(s, o);
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<WeekendSignUp>>() {
                                    }.getType();
                                    infos = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (infos != null) {
                                        final String items[] = new String[infos.size()];
                                        for (int i = 0; i < infos.size(); i++) {
                                            items[i] = infos.get(i).toString();
                                        }

                                        //dialog参数设置
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Weekend_Details.this);  //先得到构造器
                                        builder.setTitle(R.string.apply_information); //设置标题
                                        //设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
                                        builder.setItems(items, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        builder.setPositiveButton(getResources().getString(R.string.sure_information), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        builder.create().show();
                                    } else {
                                        showToast(getResources().getString(R.string.weekend_signup_none));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //周末去哪儿 - 报名
    public void WeekendSignUp(String weekendId) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Resident/WeekendRecordAdd";
        User user = UserInformation.getUserInfo();
        HashMap<String, String> extras = new HashMap<>();
        extras.put("WeekendId", weekendId);
        extras.put("Name", user.getUserName());
        extras.put("Tel", user.getUserPhone());
        extras.put("ResidentId", user.getUserId());
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
                .addParams("WeekendId", weekendId)
                .addParams("Name", user.getUserName())
                .addParams("Tel", user.getUserPhone())
                .addParams("ResidentId", user.getUserId())
                .build().execute(new DataCallBack(this) {
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
                            showToast(getResources().getString(R.string.weekend_signup_success));
                            btn_weekend_signup.setEnabled(false);
                            btn_weekend_signup.setText("已报名");
                            IntegralTip(url);
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

    class weekendApplyDialogClass implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:// 报名
                    WeekendSignUp(mWeekendId);
                    break;

            }

        }
    }
}
