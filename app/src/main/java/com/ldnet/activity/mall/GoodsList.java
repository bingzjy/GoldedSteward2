package com.ldnet.activity.mall;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Goods;
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
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by zxs on 2015/12/12.
 */
public class GoodsList extends BaseActionBarActivity {
    // 标题
    private TextView tv_page_title,tv_goods_list;
    // 返回
    private ImageButton btn_back;
    //自定义按钮--分享商品
    private Button btn_custom;
    //
    private String mCID;
    //页面标题
    private String mTitle;
    private Services services;
    private ListView mGoodListGv;
    private ListView lvGoodList;
    private ListViewAdapter mAdapter;
    private Handler mHandler;
    private List<Goods> goodList;
    //上次下拉刷新的时间
    private String dataString;
    private List<Goods> datas;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        services = new Services();
        mHandler = new Handler();
        //得到参数
        mCID = getIntent().getStringExtra("CID");
        mTitle = getIntent().getStringExtra("PAGE_TITLE");
        setContentView(R.layout.activity_goodslist);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_goods_list = (TextView) findViewById(R.id.tv_goods_list);
        tv_page_title.setText(mTitle);
        goodList = new ArrayList<Goods>();
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        lvGoodList = (ListView) findViewById(R.id.goods_list);
        lvGoodList.setFocusable(false);

        goodList.clear();
        appReturnGoodsList(mCID, "");
        lvGoodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GoodsList.this, Goods_Details.class);
                intent.putExtra("GOODS", goodList.get(position));
                intent.putExtra("URL", goodList.get(position).URL);
                intent.putExtra("CID", mCID);
                intent.putExtra("PAGE_TITLE", mTitle);
                intent.putExtra("FROM_CLASS_NAME", GoodsList.class.getName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });
        initEvent();
        initEvents();
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                goodList.clear();
                appReturnGoodsList(mCID, "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (goodList != null && goodList.size() > 0) {
                    appReturnGoodsList(mCID, goodList.get(goodList.size() - 1).GID);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //    获取商品分类
    public void appReturnGoodsList(String CID, String LastID) {
        String url = Services.mHost + "APPHomePageSet/APP_GetHomePageGoodsList?CID=%s&LastID=%s&PageCnt=%s";
        url = String.format(url, CID, LastID, Services.PAGE_SIZE);
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
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Goods>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (datas != null && datas.size() > 0) {
                                        goodList.addAll(datas);
                                        mAdapter = new ListViewAdapter<Goods>(GoodsList.this, R.layout.item_goodgrid, goodList) {
                                            @Override
                                            public void convert(ViewHolder holder, Goods goods) {
                                                ImageView imageView = holder.getView(R.id.iv_goods_image1);
                                                if (!TextUtils.isEmpty(goods.getThumbnail())) {
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(goods.getThumbnail()), imageView,imageOptions);
                                                } else {
                                                    imageView.setImageResource(R.drawable.default_goods);
                                                }
                                                holder.setText(R.id.tv_goods_name1, goods.T.trim()).setText(R.id.tv_goods_price1, "￥" + goods.GP);
                                            }
                                        };
                                        lvGoodList.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(lvGoodList);
                                    } else {
                                        if (goodList != null && goodList.size() > 0) {
                                            showToast("没有更多数据");
                                        } else {
                                            tv_goods_list.setVisibility(View.VISIBLE);
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

    public void initEvent() {
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back:
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                    break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}