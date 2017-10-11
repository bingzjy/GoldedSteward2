package com.ldnet.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.entities.CommunityServicesModel;
import com.ldnet.entities.YellowPageSort;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by zxs on 2016/3/30.
 * 黄页
 */
public class CommunityServicesPageTabActivity extends BaseActionBarFragmentActivity implements View.OnClickListener {
    private String mYellowPageSortID;
    private String mYellowPageSortTitle;
    private List<CommunityServicesModel> mYellowPageSorts;
    private Services service;
    private TextView tv_main_title;
    private ImageButton btn_back;

    // 黄页的标签
    private PagerSlidingTabStrip mYellowPageTabs;
    private ViewPager mYellowPagePager;
    private Integer mCurrentIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //布局
        setContentView(R.layout.activity_yellowpage_tab);
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText("社区服务");
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        //初始化
        service = new Services();
        mYellowPagePager = (ViewPager) findViewById(R.id.pager);
        mYellowPageTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        //初始化分类
        getYellowPageSortById();
        setTabsValue();
    }

    //根据分类id获取子分类
    public void getYellowPageSortById() {
        String url = Services.mHost + "API/Property/GetHouseKeepType";
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
                        Log.d("asdsdasd---", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<CommunityServicesModel>>() {
                                    }.getType();
                                    mYellowPageSorts = gson.fromJson(jsonObject.getString("Obj"), listType);
                                    if (mYellowPageSorts != null) {
                                        mYellowPagePager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
                                            @Override
                                            public CharSequence getPageTitle(int position) {
                                                return mYellowPageSorts.get(position).getName();
                                            }

                                            @Override
                                            public int getCount() {
                                                return mYellowPageSorts == null ? 0 : mYellowPageSorts.size();
                                            }

                                            @Override
                                            public Object instantiateItem(ViewGroup container, int position) {
                                                return super.instantiateItem(container, position);
                                            }

                                            @Override
                                            public Fragment getItem(int position) {
                                                Bundle b = new Bundle();
                                                b.putString("Id", mYellowPageSorts.get(position).getId());
                                                b.putString("Name", mYellowPageSorts.get(position).getName());
                                                return CommunityServices.getInstance(b);
                                            }
                                        });
                                        mYellowPageTabs.setViewPager(mYellowPagePager);
                                        mYellowPagePager.setCurrentItem(mCurrentIndex);
                                        btn_back.setOnClickListener(CommunityServicesPageTabActivity.this);
                                    }
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
                finish();
                break;
        }
    }

    /**
     * 对PagerSlidingTabStrip的各项属性进行赋值。
     */

    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
//        mYellowPageTabs.setShouldExpand(true);
        // 设置Tab的分割线的颜色
        mYellowPageTabs.setDividerColor(getResources().getColor(R.color.green_light));
        // 设置分割线的上下的间距,传入的是dp
        mYellowPageTabs.setDividerPaddingTopBottom(12);
        // 设置Tab底部线的高度,传入的是dp
        mYellowPageTabs.setUnderlineHeight(1);
        //设置Tab底部线的颜色
        mYellowPageTabs.setUnderlineColor(getResources().getColor(R.color.green));

        // 设置Tab 指示器Indicator的高度,传入的是dp
        mYellowPageTabs.setIndicatorHeight(4);
        // 设置Tab Indicator的颜色
        mYellowPageTabs.setIndicatorColor(getResources().getColor(R.color.green_light1));

        // 设置Tab标题文字的大小,传入的是dp
        mYellowPageTabs.setTextSize(16);
        // 设置选中Tab文字的颜色
        mYellowPageTabs.setSelectedTextColor(getResources().getColor(R.color.green));
        //设置正常Tab文字的颜色
        mYellowPageTabs.setTextColor(getResources().getColor(R.color.black));

        //  设置点击Tab时的背景色
//        mYellowPageTabs.setTabBackground(R.drawable.back_button_back);

        //是否支持动画渐变(颜色渐变和文字大小渐变)
        mYellowPageTabs.setFadeEnabled(true);
        // 设置最大缩放,是正常状态的0.3倍
        mYellowPageTabs.setZoomMax(0.1F);
        //设置Tab文字的左右间距,传入的是dp
        mYellowPageTabs.setTabPaddingLeftRight(24);
    }

}
