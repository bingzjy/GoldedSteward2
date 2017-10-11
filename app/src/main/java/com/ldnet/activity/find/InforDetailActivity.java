package com.ldnet.activity.find;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.Browser;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Information;
import com.ldnet.entities.InformationType;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.ReadInfoIDs;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.ldnet.utility.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.listviewshangxia.XListView;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Murray on 2015/8/27.
 */
public class InforDetailActivity extends BaseActionBarActivity implements XListView.IXListViewListener {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;

    private RadioGroup ll_information_types;
    private XListView lv_find_informations;
    private ListViewAdapter mAdapter;
    private List<Information> mDatas;
    private List<InformationType> mTypeDatas;//类型集合
    private String mCurrentTypeId;

    private ReadInfoIDs read = ReadInfoIDs.getInstance();
    private List<String> isReadIds;
    private Handler mHandler;
    private List<Information> datas;

    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_dettail);

        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_find_info);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //获取已读
        isReadIds = read.getRead(read.TYPE_INFORMATION);

        services = new Services();
        //
        mHandler = new Handler();

        //将分类并循环添加到页面
        GetInformationTypes();
        initEvent();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
    }

    //点击事件处理
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

    //刷新
    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData(true);
            }
        }, 2000);
    }

    //加载更多
    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData(false);
            }
        }, 2000);
    }

    protected void loadData(Boolean isFirst) {
        String uid = UserInformation.getUserInfo().UserId;
        String name = UserInformation.getUserInfo().UserName;
        String imageId = UserInformation.getUserInfo().UserThumbnail;
        //显示上次刷新时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dataString = dateFormat.format(new Date(System.currentTimeMillis()));
        if (!isFirst) {
            if (TextUtils.isEmpty(imageId)) {
                imageId = "";
            }
           GetInformationsByType(mCurrentTypeId, mDatas.get(mDatas.size() - 1).ID, uid, name, imageId);
        } else {
            mDatas.clear();
            if (TextUtils.isEmpty(imageId)) {
                imageId = "";
            }
            GetInformationsByType(mCurrentTypeId, "", uid, name, imageId);
        }

        //
        lv_find_informations.stopLoadMore();
        lv_find_informations.stopRefresh();
        lv_find_informations.setRefreshTime(dataString);
    }

    //获取资讯分类
    public void GetInformationTypes() {
        //请求的URL
        String url = Services.mHost + "Information/Sel_TypeList";
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
                        super.onError(call,e,i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<InformationType>>(){}.getType();
                                    mTypeDatas = gson.fromJson(jsonObject.getString("Obj"),type);
                                    if (mTypeDatas != null) {
                                        ll_information_types = (RadioGroup) findViewById(R.id.ll_information_types);
                                        for (InformationType type1 : mTypeDatas) {
                                            RadioButton ll_types = (RadioButton) getLayoutInflater().inflate(R.layout.item_find_detail_infos_type, null);
                                            ll_types.setText(type1.Title);
                                            ll_types.setTag(type1.ID);
                                            ll_types.setHeight(Utility.dip2px(InforDetailActivity.this, 48.0f));
                                            //
                                            ll_types.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    mCurrentTypeId = view.getTag().toString();
                                                    mHandler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            loadData(true);
                                                        }
                                                    }, 1000);
                                                }
                                            });
                                            ll_information_types.addView(ll_types);
                                        }

                                        //ListView
                                        lv_find_informations = (XListView) findViewById(R.id.lv_find_informations);
                                        // 设置xlistview可以加载、刷新
                                        lv_find_informations.setPullLoadEnable(false);
                                        lv_find_informations.setPullRefreshEnable(true);
                                        mDatas = new ArrayList<Information>();
                                        mAdapter = new ListViewAdapter<Information>(InforDetailActivity.this, R.layout.item_find_detail_infos, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, Information information) {
                                                //加载图片
                                                ImageView image = holder.getView(R.id.iv_infos_detail_image);
                                                if (!TextUtils.isEmpty(information.TitleImageID)) {
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(information.TitleImageID), image, imageOptions);
                                                } else {
                                                    image.setImageResource(R.drawable.default_info);
                                                }
                                                //标题和描述
                                                holder.setText(R.id.tv_find_detail_title, information.Title)
                                                        .setText(R.id.tv_find_detail_desc, information.Description);

                                                //获取标题,设置已读状态的标题颜色
                                                TextView tv_find_detail_title = holder.getView(R.id.tv_find_detail_title);
                                                if (isReadIds.contains(information.ID)) {
                                                    tv_find_detail_title.setTextColor(getResources().getColor(R.color.gray_light_1));
                                                } else {
                                                    tv_find_detail_title.setTextColor(getResources().getColor(R.color.gray_deep));
                                                }
                                            }
                                        };
                                        lv_find_informations.setAdapter(mAdapter);
                                        lv_find_informations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                if (i <= mDatas.size()) {
                                                    Information information = mDatas.get(--i);
                                                    User user = UserInformation.getUserInfo();
                                                    String userName = !TextUtils.isEmpty(user.UserName) ? user.UserName : user.UserPhone;
                                                    Intent intent = new Intent(InforDetailActivity.this, Browser.class);
                                                    intent.putExtra("PAGE_TITLE", R.string.information_details);
                                                    intent.putExtra("PAGE_URL", information.InfoUrl + "&IsApp=1&UID=" + user.UserId + "&UName=" + userName + "&UImgID=" + (!TextUtils.isEmpty(user.UserThumbnail) ? user.UserThumbnail : "") + "");
                                                    intent.putExtra("FROM_CLASS_NAME", InforDetailActivity.class.getName());
                                                    intent.putExtra("PAGE_IMAGE", information.TitleImageID);
                                                    //分享 - 标题、描述、URL
                                                    intent.putExtra("PAGE_TITLE_ORGIN", information.Title);
                                                    intent.putExtra("PAGE_DESCRIPTION_ORGIN", information.Description);
                                                    intent.putExtra("PAGE_URL_ORGIN", information.InfoUrl);

                                                    //标记已读
                                                    read.setRead(information.ID, read.TYPE_INFORMATION);

                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                        lv_find_informations.setXListViewListener(InforDetailActivity.this);
                                        //点击第一个标签
                                        ll_information_types.getChildAt(0).performClick();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //生活资讯 - 根据分类获取
    public void GetInformationsByType(String typeId, String lastId, String uid, String name, String imageId) {
        //请求的URL
        String url = Services.mHost + "Information/Sel_PageList?LastID=%s&PageCnt=%s&TypeID=%s&UID=%s&UName=%s&UImgID=%s";
        url = String.format(url, lastId, String.valueOf(Services.PAGE_SIZE), typeId, uid, URLEncoder.encode(name), imageId);
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
                        super.onError(call,e,i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Information>>(){}.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"),type);
                                    if (datas != null) {
                                        //判断是否出现“查看更多”
                                        if (datas.size() < services.getPageSize()) {
                                            lv_find_informations.setPullLoadEnable(false);
                                        } else {
                                            lv_find_informations.setPullLoadEnable(true);
                                        }
                                        mDatas.addAll(datas);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
