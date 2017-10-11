package com.ldnet.activity.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.HouseProperties;
import com.ldnet.entities.HouseRent;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.listviewshangxia.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Murray on 2015/9/10.
 */
public class HouseRent_List extends BaseActionBarActivity {
    private ListView houserent_list;
    private ImageButton btn_back, btn_create;
    private TextView tv_page_title;
    private TextView tv_rent_list;
    private ListViewAdapter mListViewAdapter;
    private List<HouseRent> mList;
    private Context context;
    private Services services;
    private Intent intent;
    private HouseProperties mHouseProperties;
    private Handler mHandler;
    private List<HouseRent> datas;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.houserent_list);

        //页面标题
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.fragment_home_yellow_lease);
        //发布房屋租赁
        btn_create = (ImageButton) findViewById(R.id.btn_custom);
        btn_create.setImageResource(R.drawable.plus);
        btn_create.setVisibility(View.VISIBLE);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_rent_list = (TextView) findViewById(R.id.tv_rent_list);

        //服务，获取数据
        services = new Services();
        //第一次加载数据
        mHandler = new Handler();
        getHouseRentInfo();
        mList = new ArrayList<HouseRent>();
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        houserent_list = (ListView) findViewById(R.id.houserent_list);
        houserent_list.setFocusable(false);
        houserent_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i <= mList.size()) {
                    HouseRent house = mList.get(i);
                    intent = new Intent(HouseRent_List.this, HouseRent_Detail.class);
                    intent.putExtra("HouseRent", house);
                    startActivity(intent);
                    finish();
                }
            }
        });
        initEvent();
        initEvents();
    }

    public void initEvent() {
        btn_create.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mList.clear();
                getHouseRentList("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mList != null && mList.size() > 0) {
                    getHouseRentList(mList.get(mList.size() - 1).Id);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //获取房屋租赁信息
    public void getHouseRentInfo() {
        String url = Services.mHost + "API/Property/RentailSaleSelect";
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
                        Log.d("asdsdasd", "111111111" + s);
                        closeProgressDialog();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            JSONObject jsonObject1 = new JSONObject(jsonObject.getString("Obj"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    if (jsonObject.getString("Obj").equals("[]")) {
                                        showToast("暂时没有数据");
                                        return;
                                    }
                                    Gson gson = new Gson();
//                                    mHouseProperties = gson.fromJson(jsonObject.getString("Obj"),HouseProperties.class);
                                    mHouseProperties = new HouseProperties();
                                    mHouseProperties.setOrientation(jsonObject1.getString("Orientation"));
                                    mHouseProperties.setFitmentType(jsonObject1.getString("FitmentType"));
                                    mHouseProperties.setRentType(jsonObject1.getString("RentType"));
                                    mHouseProperties.setRoomDeploy(jsonObject1.getString("RoomDeploy"));
                                    mHouseProperties.setRoomType(jsonObject1.getString("RoomType"));
                                    mListViewAdapter = new ListViewAdapter<HouseRent>(HouseRent_List.this, R.layout.item_houserent_list, mList) {
                                        @Override
                                        public void convert(ViewHolder holder, HouseRent h) {
                                            holder.setText(R.id.houserent_item_title, h.Title);
                                            try {
                                                holder.setText(R.id.houserent_item_housetype, mHouseProperties.getRoomType().get(Integer.valueOf(h.getRoomType()) + 1).Value);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            holder.setText(R.id.houserent_item_acreage, h.Acreage + "平米");
                                            holder.setText(R.id.houserent_item_address, h.Address);
                                            holder.setText(R.id.houserent_item_price, "￥" + h.Price + "元");
                                            holder.setText(R.id.houserent_item_status, h.Status);
                                            ImageView image = holder.getView(R.id.houserent_item_img);
                                            if (!TextUtils.isEmpty(h.Images)) {
                                                ImageLoader.getInstance().displayImage(services.getImageUrl(h.getThumbnail()), image,imageOptions);
                                            } else {
                                                image.setImageResource(R.drawable.default_info);
                                            }
                                        }
                                    };
                                    houserent_list.setAdapter(mListViewAdapter);
                                    mList.clear();
                                    getHouseRentList("");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //New 获取房屋租赁信息列表
    public void getHouseRentList(String lastId) {
        String url = Services.mHost + "API/Property/GetRentailSaleList?lstId=%s";
        url = String.format(url, lastId);
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
                        Log.d("asdsdasd", "2222222222" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<HouseRent>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (datas != null && datas.size() > 0) {
                                        mList.addAll(datas);
                                    } else {
                                        if (mList != null && mList.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            tv_rent_list.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    mListViewAdapter.notifyDataSetChanged();
                                    Services.setListViewHeightBasedOnChildren(houserent_list);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case R.id.btn_custom:
                intent = new Intent(this, HouseRent_Create.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                finish();
                break;
        }
    }
}
