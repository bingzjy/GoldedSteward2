package com.ldnet.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.find.FreaMarket_Create;
import com.ldnet.activity.find.FreaMarket_Details;
import com.ldnet.activity.find.Weekend_Create;
import com.ldnet.activity.find.Weekend_Details;
import com.ldnet.activity.home.HouseRentUpdate;
import com.ldnet.activity.home.HouseRent_Detail;
import com.ldnet.entities.HouseRent;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.third.SwipeListView.BaseSwipeListViewListener;
import com.third.SwipeListView.SwipeListView;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class Publish extends BaseActionBarActivity {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private SwipeListView slv_me_publish;
    private List<com.ldnet.entities.Publish> mDatas;
    private ListViewAdapter<com.ldnet.entities.Publish> mAdapter;
    private Handler mHandler;
    private List<HouseRent> houseRents = new ArrayList<HouseRent>();
    private HouseRent house_rent;
    private List<com.ldnet.entities.Publish> datas;
    private TextView tv_publish;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_publish);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_me_publish);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_publish = (TextView)findViewById(R.id.tv_publish);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        slv_me_publish = (SwipeListView) findViewById(R.id.slv_me_publish);
        slv_me_publish.setFocusable(false);
        mDatas = new ArrayList<com.ldnet.entities.Publish>();

        slv_me_publish.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                super.onClickFrontView(position);
                try {
                    if (position <= mDatas.size()) {
                        com.ldnet.entities.Publish publish = mDatas.get(position);
                        HashMap<String, String> extras = new HashMap<String, String>();
                        if (publish.Type.equals("闲置物品")) {
                            extras.clear();
                            extras.put("FREA_MARKET_ID", publish.Id);
                            extras.put("FROM_PUBLISH", "true");
                            extras.put("LEFT", "LEFT");
                            gotoActivity(FreaMarket_Details.class.getName(), extras);
                        } else if (publish.Type.equals("周末去哪")) {
                            extras.clear();
                            extras.put("WEEKEND_ID", publish.Id);
                            extras.put("FROM_PUBLISH", "true");
                            extras.put("LEFT", "LEFT");
                            gotoActivity(Weekend_Details.class.getName(), extras);
                        } else {
                            Intent intent = new Intent(Publish.this, HouseRent_Detail.class);
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable("HouseRent", houseRents.get(position));
                            intent.putExtras(mBundle);
                            intent.putExtra("FROM_PUBLISH", "true");
                            extras.put("LEFT", "LEFT");
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });

        initEvent();
        initEvents();
        //点击item关闭滑动
        slv_me_publish.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                slv_me_publish.closeOpenedItems();
            }
        });
        slv_me_publish.setOffsetLeft(this.getResources().getDisplayMetrics().widthPixels * 3 / 5);
        //初始化服务
        services = new Services();
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData(true);
            }
        }, 0);
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

    //获取房屋租赁信息列表
    public void getHouseRentById(String id) {
        String url = Services.mHost + "API/Property/GetRentailSaleById/%s";
        url = String.format(url, id);
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
                        Log.d("asdsdasd---====", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    house_rent = gson.fromJson(jsonObject.getString("Obj"), HouseRent.class);
                                    houseRents.add(house_rent);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //  删除房屋租赁信息
    // POST API/Property/RentailSaleDel?id={id}
    public void deleteHouseRentailSale(String id, final com.ldnet.entities.Publish publish) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Property/RentailSaleDel?id=%s";
        url = String.format(url, id);
        HashMap<String, String> extras = new HashMap<>();
        extras.put("id", id);
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
                .addParams("id", id)
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
                                    mDatas.remove(publish);
                                    mAdapter.notifyDataSetChanged();
                                    slv_me_publish.closeOpenedItems();
                                    showToast("删除成功");
                                } else {
                                    showToast("删除失败");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //API/Resident/DeleteWeekendById/{id}
    //删除周末去哪
    public void deleteWeekend(String id, final com.ldnet.entities.Publish publish) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/DeleteWeekendById/%s";
        url = String.format(url, id);
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
                                    mDatas.remove(publish);
                                    mAdapter.notifyDataSetChanged();
                                    slv_me_publish.closeOpenedItems();
                                    showToast("删除成功");
                                } else {
                                    showToast("删除失败");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //POST API/Resident/DeleteUnusedGoods?id={id}
    //删除闲置物品
    public void deleteUnusedGoods(String id, final com.ldnet.entities.Publish publish) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Resident/DeleteUnusedGoods?id=%s";
        url = String.format(url, id);
        HashMap<String, String> extras = new HashMap<>();
        extras.put("id", id);
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
                .addParams("id", id)
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
                                    mDatas.remove(publish);
                                    mAdapter.notifyDataSetChanged();
                                    slv_me_publish.closeOpenedItems();
                                    showToast("删除成功");
                                } else {
                                    showToast("删除失败");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //周末去哪儿 - 获取闲置物品的详情  自己的发布
    public void Publishs(String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetMyPublish/%s?lastId=%s";
        url = String.format(url, UserInformation.getUserInfo().UserId, lastId);
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
                .build().execute(new DataCallBack(this) {
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
                            Type type = new TypeToken<List<com.ldnet.entities.Publish>>() {
                            }.getType();
                            datas = gson.fromJson(jsonObject.getString("Obj"), type);
                            if (datas != null && datas.size() > 0) {
                                mDatas.addAll(datas);
                                mAdapter = new ListViewAdapter<com.ldnet.entities.Publish>(Publish.this, R.layout.item_publish, mDatas) {
                                    @Override
                                    public void convert(ViewHolder holder, final com.ldnet.entities.Publish publish) {
                                        holder.setText(R.id.tv_publish_title, publish.Title)
                                                .setText(R.id.tv_publish_type, publish.Type)
                                                .setText(R.id.tv_publish_time, Services.subStr(publish.DateTime));

                                        //设置默认
                                        Button btn_update = holder.getView(R.id.btn_update);
                                        Button btn_delete = holder.getView(R.id.btn_delete);

                                        getHouseRentById(publish.Id);

                                        //更新
                                        btn_update.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (publish.Type.equals("闲置物品")) {
                                                    Intent intent = new Intent(Publish.this, FreaMarket_Create.class);
                                                    intent.putExtra("FREA_MARKET_ID", publish.Id);
                                                    intent.putExtra("FROM_PUBLISH", "true");
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                                                } else if (publish.Type.equals("周末去哪")) {
                                                    Intent intent = new Intent(Publish.this, Weekend_Create.class);
                                                    intent.putExtra("FREA_MARKET_ID", publish.Id);
                                                    intent.putExtra("FROM_PUBLISH", "true");
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                                                } else {
                                                    Intent intent = new Intent(Publish.this, HouseRentUpdate.class);
                                                    intent.putExtra("HouseRent", house_rent);
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                                                }
                                            }
                                        });
                                        //删除
                                        btn_delete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (publish.Type.equals("闲置物品")) {
                                                    deleteUnusedGoods(publish.Id, publish);
                                                } else if (publish.Type.equals("周末去哪")) {
                                                    deleteWeekend(publish.Id, publish);
                                                } else {
                                                    //房屋租赁
                                                    deleteHouseRentailSale(publish.Id, publish);
                                                }
                                            }
                                        });
                                    }
                                };
                                slv_me_publish.setAdapter(mAdapter);
                                Utility.setListViewHeightBasedOnChildren(slv_me_publish);
                            } else {
                                if (mDatas != null && mDatas.size() > 0) {
                                    showToast("沒有更多数据");
                                } else {
                                    tv_publish.setVisibility(View.VISIBLE);
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

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
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
            default:
                break;
        }
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                Publishs("");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    Publishs(mDatas.get(mDatas.size() - 1).Id);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

//    @Override
//    public void onRefresh() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                loadData(true);
//            }
//        }, 2000);
//    }
//
//    @Override
//    public void onLoadMore() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                loadData(false);
//            }
//        }, 2000);
//    }

    //加载数据
    private void loadData(Boolean isFirst) {

        //显示上次刷新时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dataString = dateFormat.format(new Date(System.currentTimeMillis()));
        if (!isFirst) {
            Publishs(mDatas.get(mDatas.size() - 1).Id);
        } else {
            mDatas.clear();
            Publishs("");
        }
        //
//        if (datas != null) {
//            if (datas.size() < services.getPageSize()) {
//                lv_me_publish.setPullLoadEnable(false);
//            } else {
//                lv_me_publish.setPullLoadEnable(true);
//            }

    }
//        mAdapter.notifyDataSetChanged();
//        lv_me_publish.stopRefresh();
//        lv_me_publish.stopLoadMore();
//        lv_me_publish.setRefreshTime(dataString);
//    }
}
