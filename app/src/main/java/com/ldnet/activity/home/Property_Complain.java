package com.ldnet.activity.home;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.activity.me.Community;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Property_Complain extends BaseActionBarFragmentActivity implements View.OnClickListener{
    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageButton btn_create_complain;
    private Services services;
    private Handler mHandler;

    private TextView tv_complain_houseinfo;
    private ImageButton ibtn_complain_change_house;
    /**
     * 顶部三个LinearLayout
     */
    private LinearLayout ll_zc;//早餐
    private LinearLayout ll_wc;//午餐
    private LinearLayout ll_dc;//晚餐

    /**
     * 顶部的三个TextView
     */
    private TextView tv_zc;
    private TextView tv_wc;
    private TextView tv_dc;

    /**
     * 分别为每个TabIndicator创建一个BadgeView
     */
    private BadgeView zc;
    private BadgeView wc;
    private BadgeView dc;

    /**
     * Tab的那个引导线
     */
    private ImageView mTabLine;
    /**
     * ViewPager的当前选中页
     */
    private int currentIndex;
    /**
     * 屏幕的宽度
     */
    private int screenWidth;
    private int index;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private FragmentPagerAdapter mAdapter;
    private ViewPager mViewPager;

    //初始化视图

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_property_complain);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.property_services_complain);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

//        mServicesType = getIntent().getStringExtra("");
        User user = UserInformation.getUserInfo();
        tv_complain_houseinfo = (TextView) findViewById(R.id.tv_complain_houseinfo);
        tv_complain_houseinfo.setText(user.CommuntiyName + "(" + user.HouseName + ")");

        ibtn_complain_change_house = (ImageButton) findViewById(R.id.ibtn_complain_change_house);

        //现在报修按钮
        btn_create_complain = (ImageButton) findViewById(R.id.btn_custom);
        btn_create_complain.setImageResource(R.drawable.plus);
        btn_create_complain.setVisibility(View.VISIBLE);

        mViewPager = (ViewPager) findViewById(R.id.id_vp);
        initView();
        /**
         * 初始化Adapter
         */
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }
        };
        mViewPager.setAdapter(mAdapter);

        /**
         * 设置监听
         */
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // 重置所有TextView的字体颜色
                index = position;
                resetTextView();
                switch (position) {
                    case 0:
                        ll_zc.removeView(zc);
                        ll_zc.addView(zc);
                        tv_zc.setTextColor(getResources().getColor(R.color.white));
                        ll_zc.setBackgroundResource(R.drawable.sharp_rect_green);
                        break;
//                    case 1:
//                        tv_wc.setTextColor(getResources().getColor(R.color.white));
//                        ll_wc.setBackgroundResource(R.drawable.sharp_rect_green);
//                        ll_wc.removeView(wc);
//                        ll_wc.addView(wc);
//                        break;
                    case 1:
                        tv_dc.setTextColor(getResources().getColor(R.color.white));
                        ll_dc.setBackgroundResource(R.drawable.sharp_rect_green);
                        break;
                }

                currentIndex = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                /**
                 * 利用position和currentIndex判断用户的操作是哪一页往哪一页滑动
                 * 然后改变根据positionOffset动态改变TabLine的leftMargin
                 */
                /**
                 * currentIndex:当前行 ;positionOffsetPixels: 位置偏移像素;
                 * positionOffset:位置偏移
                 */
                System.out.println("\n" + "currentIndex:" + currentIndex + "\n"
                        + "position:" + position + "\n" + "positionOffset:"
                        + positionOffset + "\n" + "positionOffsetPixels:"
                        + positionOffsetPixels + "\n");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        initEvent();
        //初始化服务
        services = new Services();
        mHandler = new Handler();

    }

    /**
     * 重置颜色
     */
    protected void resetTextView() {
        tv_zc.setTextColor(getResources().getColor(R.color.gray_deep));
//        tv_wc.setTextColor(getResources().getColor(R.color.gray_deep));
        tv_dc.setTextColor(getResources().getColor(R.color.gray_deep));
        ll_zc.setBackgroundResource(R.drawable.sharp_rect_white);
//        ll_wc.setBackgroundResource(R.drawable.sharp_rect_white);
        ll_dc.setBackgroundResource(R.drawable.sharp_rect_white);
    }

    /**
     * 初始化控件，初始化Fragment
     */
    private void initView() {

        ll_zc = (LinearLayout) findViewById(R.id.ll_zc);
//        ll_wc = (LinearLayout) findViewById(R.id.ll_wc);
        ll_dc = (LinearLayout) findViewById(R.id.ll_dc);

        tv_zc = (TextView) findViewById(R.id.tv_zc);
//        tv_wc = (TextView) findViewById(R.id.tv_wc);
        tv_dc = (TextView) findViewById(R.id.tv_dc);

        ComplainIngFragment tab01 = new ComplainIngFragment();
//        ComplainCompleteFragment tab02 = new ComplainCompleteFragment();
        ComplainCommentFragment tab03 = new ComplainCommentFragment();
        mFragments.add(tab01);
//        mFragments.add(tab02);
        mFragments.add(tab03);

        zc = new BadgeView(this);
//        wc = new BadgeView(this);
        dc = new BadgeView(this);
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_create_complain.setOnClickListener(this);
        ibtn_complain_change_house.setOnClickListener(this);
        /**
         * 设置顶部三个标签页点击事件
         */
        ll_zc.setOnClickListener(this);
//        ll_wc.setOnClickListener(this);
        ll_dc.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    Services.comment = "";
                    gotoActivityAndFinish(Property_Services.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_custom:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Property_Complain_Create.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ibtn_complain_change_house:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("NOT_FROM_ME", "103");
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Community.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_zc:
                mViewPager.setCurrentItem(0);
                index = 0;
                break;
//            case R.id.ll_wc:
//                mViewPager.setCurrentItem(1);
//                index = 1;
//                break;
            case R.id.ll_dc:
                mViewPager.setCurrentItem(1);
                index = 1;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            try {
                Services.comment = "";
                gotoActivityAndFinish(Property_Services.class.getName(), null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
