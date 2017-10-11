package com.ldnet.activity.find;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class FreaMarket extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageButton btn_freamarket_create;
    private Services services;
    private Handler mHandler;
    private ListView lv_find_fleamarket;
    private ListViewAdapter<com.ldnet.entities.FreaMarket> mAdapter;
    private List<com.ldnet.entities.FreaMarket> mDatas;
    private List<com.ldnet.entities.FreaMarket> datas;
    private TextView tv_find_fleamarket;
    private PullToRefreshScrollView mPullToRefreshScrollView;
    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fleamarket);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.frea_market_title);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //发布闲置物品按钮
        btn_freamarket_create = (ImageButton) findViewById(R.id.btn_custom);
        btn_freamarket_create.setImageResource(R.drawable.plus);
        btn_freamarket_create.setVisibility(View.VISIBLE);
        tv_find_fleamarket = (TextView)findViewById(R.id.tv_find_fleamarket);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        //ListView
        lv_find_fleamarket = (ListView) findViewById(R.id.lv_find_fleamarket);
        lv_find_fleamarket.setFocusable(false);
        mDatas = new ArrayList<com.ldnet.entities.FreaMarket>();

        lv_find_fleamarket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i <= mDatas.size()) {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("FREA_MARKET_ID", mDatas.get(i).Id);
                    try {
                        gotoActivityAndFinish(FreaMarket_Details.class.getName(), extras);
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
        FreaMarkets("");
        showProgressDialog1();
        initEvent();
        initEvents();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_freamarket_create.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://返回主页
                try {
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_custom://跳转到发布
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT","LEFT");
                    gotoActivity(FreaMarket_Create.class.getName(), extras);
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
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                FreaMarkets("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    FreaMarkets(mDatas.get(mDatas.size() - 1).Id);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //跳蚤市场 - 列表
    public void FreaMarkets(String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetUnusedGoodsList/%s?lastId=%s";
        url = String.format(url, UserInformation.getUserInfo().CommuntiyCityId, lastId);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5)).build()
                .execute(new DataCallBack(this) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call,e,i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        closeProgressDialog1();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s,i);
                        Log.d("asdsdasd", "111111111" + s);
                        mPullToRefreshScrollView.onRefreshComplete();
                        closeProgressDialog1();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<com.ldnet.entities.FreaMarket>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (datas != null) {
                                        mDatas.addAll(datas);
                                        mAdapter = new ListViewAdapter<com.ldnet.entities.FreaMarket>(FreaMarket.this, R.layout.item_freamarket, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, com.ldnet.entities.FreaMarket freaMarket) {
                                                //设置图片
                                                if (!TextUtils.isEmpty(freaMarket.Cover)) {
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(freaMarket.Cover), (ImageView) holder.getView(R.id.iv_frea_market_image), imageOptions);
                                                }
                                                //标题、价格、时间、地址
                                                holder.setText(R.id.tv_frea_market_title, freaMarket.Title)
                                                        .setText(R.id.tv_frea_market_price, "￥" + freaMarket.Price)
                                                        .setText(R.id.tv_frea_market_time, Services.subStr(freaMarket.getDateTime()))
                                                        .setText(R.id.tv_frea_market_address, freaMarket.Address);
                                            }
                                        };
                                        lv_find_fleamarket.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(lv_find_fleamarket);
                                    }else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            tv_find_fleamarket.setVisibility(View.VISIBLE);
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
