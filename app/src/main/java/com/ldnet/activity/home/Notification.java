package com.ldnet.activity.home;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Notifications;
import com.ldnet.entities.SurveyEntity;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Notification extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private ListView lv_property_notification;
    private ListViewAdapter mAdapter;
    private ListViewAdapter mSurveyAdapter;
    private List<Notifications> mDatas;
    private List<String> isReadIds;
    private Handler mHandler;
    private RadioGroup mRdgTenementBottom;
    private PullToRefreshScrollView main_act_scrollview;
    private TextView mNotificationEmpty;
    private ProgressBar mProgressBar;
    private List<Notifications> datas;
    private List<SurveyEntity> surveyEntityList = new ArrayList<>();
    private List<SurveyEntity> surveyTempList;
    private User user;
    private String tag = Notification.class.getSimpleName();
    private boolean showSurvey = false;
    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_notification);
        // 标题
        mRdgTenementBottom = (RadioGroup) findViewById(R.id.rdg_tenement_bottom);
        mRdgTenementBottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mDatas.clear();
                if (((RadioButton) mRdgTenementBottom.getChildAt(0)).isChecked()) {
                    Notifications("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(1)).isChecked()) {
                    Announcements("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(2)).isChecked()) {
                    Survey("");
                }
            }
        });

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        //获取已读
        ReadInfoIDs read = ReadInfoIDs.getInstance();
        isReadIds = read.getRead(read.TYPE_NOTIFICATION);
        //无物业通知显示图
        mNotificationEmpty = (TextView) findViewById(R.id.notification_empty);
        main_act_scrollview = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        main_act_scrollview.setMode(PullToRefreshBase.Mode.BOTH);
        main_act_scrollview.setHeaderLayout(new HeaderLayout(this));
        main_act_scrollview.setFooterLayout(new FooterLayout(this));
        //通知列表
        lv_property_notification = (ListView) findViewById(R.id.lv_property_notification);
        lv_property_notification.setFocusable(false);
        mDatas = new ArrayList<Notifications>();
        lv_property_notification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //通知公告
                if (!showSurvey) {

                    if (i <= mDatas.size()) {
                        user = UserInformation.getUserInfo();
                        HashMap<String, String> extras = new HashMap<String, String>();
                        extras.put("NOTIFICATION_ID", mDatas.get(i).Id);
                        extras.put("PAGE_TITLE", "通知详情");
                        extras.put("FROM_CLASS_NAME", Notification.class.getName());
                        extras.put("PAGE_URL", mDatas.get(i).Url + "&IsApp=1&UID=" + user.UserId + "&UName=" + user.UserName + "&UImgID=" + (!TextUtils.isEmpty(user.UserThumbnail) ? user.UserThumbnail : ""));

                        //分享 - 标题、描述、URL
                        extras.put("PAGE_TITLE_ORGIN", mDatas.get(i).Title);
                        extras.put("PAGE_DESCRIPTION_ORGIN", mDatas.get(i).getCover());
                        extras.put("PAGE_URL_ORGIN", mDatas.get(i).Url);
                        extras.put("NOTIFICATION_TYPE", "1");

                        try {
                            gotoActivity(Notification_Details.class.getName(), extras);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (surveyEntityList != null && surveyEntityList.size() > 0) {

                        HashMap<String, String> extra = new HashMap<String, String>();
                        extra.put("NOTIFICATION_ID", surveyEntityList.get(i).getID());
                        extra.put("PAGE_TITLE", "内部调研");
                        extra.put("PAGE_DISPLAY_URL", surveyEntityList.get(i).getURL());
                        extra.put("PAGE_SHARE_URL", surveyEntityList.get(i).getShareURL());
                        extra.put("NOTIFICATION_TYPE", "2");
                        extra.put("PAGE_TITLE_ORGIN", surveyEntityList.get(i).getTitle());
                        extra.put("FROM_CLASS_NAME", Notification.class.getName());
                        extra.put("PAGE_DATE",surveyEntityList.get(i).getReleaseDate());
                        try {
                            gotoActivity(Notification_Details.class.getName(), extra);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //调研

            }
        });

        //初始化服务
        services = new Services();
        mHandler = new Handler();
        Notifications("");
        initEvent();
        initEvents();
    }

    public void setAdapterClear() {
        mAdapter = new ListViewAdapter<Notifications>(this, R.layout.item_home_notification, mDatas) {
            @Override
            public void convert(ViewHolder holder, Notifications notifications) {
                //绑定图片
                ImageView icon = holder.getView(R.id.iv_notification_icon);
                if (!TextUtils.isEmpty(notifications.Cover)) {
                    ImageLoader.getInstance().displayImage(services.getImageUrl(notifications.Cover), icon, imageOptions);
                } else {
                    icon.setImageBitmap(Utility.getBitmapByText(notifications.Title.trim().substring(0, 1), 64, 64, "#ffffffff", "#ff25B59E"));
                }
                holder.setText(R.id.tv_notification_title, notifications.Title).setText(R.id.tv_notification_date, Services.subStr(notifications.getDateTime()));
                //获取标题,设置已读状态的标题颜色
                TextView tv_notification_title = holder.getView(R.id.tv_notification_title);
                if (isReadIds.contains(notifications.Id)) {
                    tv_notification_title.setTextColor(getResources().getColor(R.color.gray_light_1));
                } else {
                    tv_notification_title.setTextColor(getResources().getColor(R.color.gray_deep));
                }
            }
        };
        lv_property_notification.setAdapter(mAdapter);
        services.setListViewHeightBasedOnChildren(lv_property_notification);
        showSurvey = false;
    }

    private void setSurveyAdapter() {
        mSurveyAdapter = new ListViewAdapter<SurveyEntity>(this, R.layout.item_home_notification, surveyEntityList) {
            @Override
            public void convert(ViewHolder holder, SurveyEntity surveyEntity) {
                ImageView icon = holder.getView(R.id.iv_notification_icon);
                icon.setImageBitmap(Utility.getBitmapByText(surveyEntity.getTitle().trim().substring(0, 1), 64, 64, "#ffffffff", "#ff25B59E"));
                holder.setText(R.id.tv_notification_title, surveyEntity.getTitle()).setText(R.id.tv_notification_date, Services.subStr(surveyEntity.getReleaseDate()));
            }
        };

        lv_property_notification.setAdapter(mSurveyAdapter);
        services.setListViewHeightBasedOnChildren(lv_property_notification);
        showSurvey = true;
    }


    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
    }

    @Override
    protected void onRestart() {
        //
        ReadInfoIDs read = ReadInfoIDs.getInstance();
        isReadIds = read.getRead(read.TYPE_NOTIFICATION);
        //
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDatas.clear();
                if (((RadioButton) mRdgTenementBottom.getChildAt(0)).isChecked()) {
                    Notifications("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(1)).isChecked()) {
                    Announcements("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(2)).isChecked()) {
                    Survey("");
                }
            }
        }, 0);
        super.onRestart();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                try {
                    gotoActivityAndFinish(MainActivity.class.getName(), null);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initEvents() {
        main_act_scrollview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                if (((RadioButton) mRdgTenementBottom.getChildAt(0)).isChecked()) {
                    Notifications("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(1)).isChecked()) {
                    Announcements("");
                } else if (((RadioButton) mRdgTenementBottom.getChildAt(2)).isChecked()) {
                    Survey("");
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    if (((RadioButton) mRdgTenementBottom.getChildAt(0)).isChecked()) {
                        Notifications(mDatas.get(mDatas.size() - 1).Id);
                    } else if (((RadioButton) mRdgTenementBottom.getChildAt(1)).isChecked()) {
                        Announcements(mDatas.get(mDatas.size() - 1).Id);
                    } else if (((RadioButton) mRdgTenementBottom.getChildAt(2)).isChecked()) {
                        Survey(mDatas.get(mDatas.size() - 1).Id);
                    }
                } else {
                    main_act_scrollview.onRefreshComplete();
                }
            }
        });
    }

    //获取小区通知
    public void Notifications(String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetNewsByRoomId/%s?roomId=%s&lastId=%s";
        String houseId = !TextUtils.isEmpty(UserInformation.getUserInfo().getHouseId()) ? UserInformation.getUserInfo().getHouseId() : "";
        url = String.format(url, UserInformation.getUserInfo().getCommunityId(), houseId, lastId);
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
                        main_act_scrollview.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        main_act_scrollview.onRefreshComplete();
                        Log.e(tag, "通知Notifications：" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Notifications>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    //
                                    if (datas != null && datas.size() > 0) {
                                        mNotificationEmpty.setVisibility(View.GONE);
                                        lv_property_notification.setVisibility(View.VISIBLE);
                                        mDatas.addAll(datas);
                                        setAdapterClear();
                                    } else {
                                        // data是null或者等于0显示提示图
                                        if (mDatas == null || mDatas.size() == 0) {
                                            mNotificationEmpty.setVisibility(View.VISIBLE);
                                            lv_property_notification.setVisibility(View.GONE);
                                        } else {
                                            mNotificationEmpty.setVisibility(View.GONE);
                                            lv_property_notification.setVisibility(View.VISIBLE);
                                            showToast("沒有更多数据");
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

    // 获取小区公告
    public void Announcements(String lastId) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetAnnouncementByRoomId/%s?roomId=%s&lastId=%s";
        String houseId = !TextUtils.isEmpty(UserInformation.getUserInfo().getHouseId()) ? UserInformation.getUserInfo().getHouseId() : "";
        url = String.format(url, UserInformation.getUserInfo().getCommunityId(), houseId, lastId);
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
                        main_act_scrollview.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        main_act_scrollview.onRefreshComplete();
                        Log.e(tag, "公告Announcements:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Notifications>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    //
                                    if (datas != null && datas.size() > 0) {
                                        mNotificationEmpty.setVisibility(View.GONE);
                                        lv_property_notification.setVisibility(View.VISIBLE);
                                        mDatas.addAll(datas);
                                        setAdapterClear();
                                    } else {
                                        // data是null或者等于0显示提示图
                                        if (mDatas == null || mDatas.size() == 0) {
                                            mNotificationEmpty.setVisibility(View.VISIBLE);
                                            lv_property_notification.setVisibility(View.GONE);
                                        } else {
                                            mNotificationEmpty.setVisibility(View.GONE);
                                            lv_property_notification.setVisibility(View.VISIBLE);
                                            showToast("沒有更多数据");
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



    //获取小区调研
    public void Survey(String lastId) {
        // 请求的URL
        String url = Services.mHost + "Survey/APP_GetSurveyList?CID=%s&UID=%s&PageCnt=%s&LastID=%s";
        url = String.format(url, UserInformation.getUserInfo().getCommunityId(), UserInformation.getUserInfo().getUserId(), Services.PAGE_SIZE, lastId);
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
                        main_act_scrollview.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        main_act_scrollview.onRefreshComplete();
                        Log.e(tag, "Survey:" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<SurveyEntity>>() {
                                    }.getType();
                                    surveyTempList = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (surveyTempList != null && surveyTempList.size() > 0) {
                                        mNotificationEmpty.setVisibility(View.GONE);
                                        lv_property_notification.setVisibility(View.VISIBLE);
                                        surveyEntityList = surveyTempList;
                                        setSurveyAdapter();
                                    } else {
                                        // data是null或者等于0显示提示图
                                        if (surveyEntityList == null || surveyEntityList.size() == 0) {
                                            mNotificationEmpty.setVisibility(View.VISIBLE);
                                            lv_property_notification.setVisibility(View.GONE);
                                        } else {
                                            mNotificationEmpty.setVisibility(View.GONE);
                                            lv_property_notification.setVisibility(View.VISIBLE);
                                            showToast("沒有更多数据");
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
