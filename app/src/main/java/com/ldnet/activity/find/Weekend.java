package com.ldnet.activity.find;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.ldnet.utility.ViewHolder;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class Weekend extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageButton btn_weekend_create;
    private Services services;
    private Handler mHandler;
    private ListView lv_find_weekend;
    private ListViewAdapter<com.ldnet.entities.Weekend> mAdapter;
    private List<com.ldnet.entities.Weekend> mDatas;
    private List<com.ldnet.entities.Weekend> datas;
    private TextView tv_find_weekend;
    private PullToRefreshScrollView mPullToRefreshScrollView;
    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_weekend);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_find_weekend);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //发布闲置物品按钮
        btn_weekend_create = (ImageButton) findViewById(R.id.btn_custom);
        btn_weekend_create.setImageResource(R.drawable.plus);
        btn_weekend_create.setVisibility(View.VISIBLE);
        tv_find_weekend = (TextView)findViewById(R.id.tv_find_weekend);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        //ListView
        lv_find_weekend = (ListView) findViewById(R.id.lv_find_weekend);
        lv_find_weekend.setFocusable(false);
        mDatas = new ArrayList<com.ldnet.entities.Weekend>();
        lv_find_weekend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i <= mDatas.size()) {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("WEEKEND_ID", mDatas.get(i).Id);
                    try {
                        gotoActivityAndFinish(Weekend_Details.class.getName(), extras);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //初始化服务
        services = new Services();
        mHandler = new Handler();
        mDatas.clear();
        Weekends("");
        showProgressDialog1();
        initEvent();
        initEvents();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_weekend_create.setOnClickListener(this);
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
            case R.id.btn_custom:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Weekend_Create.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            try {
                gotoActivityAndFinish(MainActivity.class.getName(), null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                Weekends("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    Weekends(mDatas.get(mDatas.size() - 1).Id);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //周末去哪儿 - 列表
    public void Weekends(String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetWeekendByLastId/%s?lastId=%s";
        url = String.format(url, UserInformation.getUserInfo().getCommuntiyCityId(), lastId);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .build()
                .execute(new DataCallBack(this) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        closeProgressDialog1();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        closeProgressDialog1();
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<com.ldnet.entities.Weekend>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (datas != null) {
                                        mDatas.addAll(datas);
                                        mAdapter = new ListViewAdapter<com.ldnet.entities.Weekend>(Weekend.this, R.layout.item_weekend, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, com.ldnet.entities.Weekend weekend) {
                                                //设置图片
                                                if (!TextUtils.isEmpty(weekend.Cover)) {
                                                    ImageView image = holder.getView(R.id.iv_weekend_image);
                                                    LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) image.getLayoutParams();
                                                    DisplayMetrics dm = getResources().getDisplayMetrics();
                                                    linearParams.height = (dm.widthPixels - Utility.dip2px(getApplicationContext(), 12.0f)) / 16 * 9;
                                                    image.setLayoutParams(linearParams);
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(weekend.Cover), image, imageOptions);
                                                }
                                                //标题、城市、已报名人数
                                                holder.setText(R.id.tv_weekend_title, weekend.Title)
                                                        .setText(R.id.tv_weekend_cityname, weekend.CityName)
                                                        .setText(R.id.tv_weekend_signup_number, String.valueOf(weekend.MemberCount));
                                            }
                                        };
                                        lv_find_weekend.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(lv_find_weekend);
                                    }else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            tv_find_weekend.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
