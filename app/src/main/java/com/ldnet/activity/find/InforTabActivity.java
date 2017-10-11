package com.ldnet.activity.find;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.entities.InformationType;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.MyPagerAdapter;
import com.ldnet.utility.PagerSlidingTabStrip;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by zxs on 2016/3/29.
 * 资讯
 */
public class InforTabActivity extends BaseActionBarFragmentActivity implements View.OnClickListener {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private List<InformationType> mTypeDatas;//类型集合
    private Handler mHandler;
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private Integer mCurrentIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor_tab);
        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_find_info);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        services = new Services();
        //将分类并循环添加到页面
        GetInformationTypes();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(pager.getCurrentItem());
//        Log.i("getCurrentItem", pager.getCurrentItem() + "");
    }

    public void initEvent() {
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://返回主页
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }


    /**
     * 对PagerSlidingTabStrip的各项属性进行赋值。
     */
    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线的颜色
        tabs.setDividerColor(getResources().getColor(R.color.green_light));
        // 设置分割线的上下的间距,传入的是dp
        tabs.setDividerPaddingTopBottom(12);
        // 设置Tab底部线的高度,传入的是dp
        tabs.setUnderlineHeight(1);
        //设置Tab底部线的颜色
        tabs.setUnderlineColor(getResources().getColor(R.color.green));

        // 设置Tab 指示器Indicator的高度,传入的是dp
        tabs.setIndicatorHeight(4);
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(getResources().getColor(R.color.green_light1));

        // 设置Tab标题文字的大小,传入的是dp
        tabs.setTextSize(16);
        // 设置选中Tab文字的颜色
        tabs.setSelectedTextColor(getResources().getColor(R.color.green));
        //设置正常Tab文字的颜色
        tabs.setTextColor(getResources().getColor(R.color.black));

        //  设置点击Tab时的背景色
//        tabs.setTabBackground(R.drawable.back_button_back);

        //是否支持动画渐变(颜色渐变和文字大小渐变)
        tabs.setFadeEnabled(true);
        // 设置最大缩放,是正常状态的0.3倍
        tabs.setZoomMax(0.1F);
        //设置Tab文字的左右间距,传入的是dp
        tabs.setTabPaddingLeftRight(24);
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
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call,e,i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.d("asdsdasd", "111111111" + s);
                        closeProgressDialog();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<InformationType>>() {
                                    }.getType();
                                    mTypeDatas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (mTypeDatas != null) {
                                        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), mTypeDatas));
                                        tabs.setViewPager(pager);
                                        pager.setCurrentItem(mCurrentIndex);
                                    }
                                    setTabsValue();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
