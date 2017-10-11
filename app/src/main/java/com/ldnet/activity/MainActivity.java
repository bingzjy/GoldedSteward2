package com.ldnet.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.amap.api.navi.AMapNavi;
import com.ldnet.activity.adapter.FragmentViewPagerAdapter;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import java.util.ArrayList;
import java.util.List;

/******************************************************
 * 主框架
 ****************************************************/
public class MainActivity extends BaseActionBarFragmentActivity implements MyNetWorkBroadcastReceive.onNewMessageListener, View.OnClickListener {

    // ViewPager
    private ViewPager m_viewPager;
    // FragmentPagerAdapter
    private FragmentViewPagerAdapter m_adapter;
    // Fragment
    private List<Fragment> m_fragments;
    private Fragment m_fragment_home;
    private Fragment m_fragment_find;
    private Fragment m_fragment_me;
    // RadioGroup
    private RadioGroup rdg_m_bottom;

    //两次Back退出
    private long exitTime = 0;

    //版本更新
    UpdateManager update = new UpdateManager(MainActivity.this);
    private Handler mHandler;

    /**
     * 顶部三个LinearLayout
     */
    private LinearLayout ll_zc;
    private LinearLayout ll_wc;
    private LinearLayout ll_dc;

    /**
     * 顶部的三个TextView
     */
    private TextView tv_zc;
    private TextView tv_wc;
    private TextView tv_dc;
    private ImageView iv_zc;
    private ImageView iv_wc;
    private ImageView iv_dc;

    /**
     * 分别为每个TabIndicator创建一个BadgeView
     */
    private BadgeView zc;
    private BadgeView wc;
    private BadgeView dc;

    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private FragmentPagerAdapter mAdapter;
    private int index;




    // onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 主布局
        setContentView(R.layout.activity_main);
        MyNetWorkBroadcastReceive.msgListeners.add(this);
        // 初始化事件
        initEvnet();
        //
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                update.checkUpdate();
            }
        }, 1000);



    }

    @Override
    public void onNewMessage(String message) {
        String[] str = message.split(",");
        int index = m_viewPager.getCurrentItem();
        if (str[1].contains("com.ldnet.activity.MainActivity")) {
            if (str[2].equals("false")) {
                if (mAdapter.getItem(index).toString().contains("FragmentHome")) {
                    mFragments.clear();
                    initEvnet();
                }else if(mAdapter.getItem(index).toString().contains("FragmentFind")){
                    mFragments.clear();
                    initEvnet1();
                }else if(mAdapter.getItem(index).toString().contains("FragmentMe")){
                    mFragments.clear();
                    initEvnet2();
                }
            } else {
                if (mAdapter.getItem(index).toString().contains("FragmentHome")) {
                    mFragments.clear();
                    initEvnet();
                }else if(mAdapter.getItem(index).toString().contains("FragmentFind")){
                    mFragments.clear();
                    initEvnet1();
                }else if(mAdapter.getItem(index).toString().contains("FragmentMe")){
                    mFragments.clear();
                    initEvnet2();
                }
            }
        }
    }

    // 初始化事件
    private void initEvnet() {
        m_viewPager = (ViewPager) findViewById(R.id.vp_main_viewPager);
        ll_zc = (LinearLayout) findViewById(R.id.ll_zc);
        ll_wc = (LinearLayout) findViewById(R.id.ll_wc);
        ll_dc = (LinearLayout) findViewById(R.id.ll_dc);

        tv_zc = (TextView) findViewById(R.id.tv_zc);
        tv_wc = (TextView) findViewById(R.id.tv_wc);
        tv_dc = (TextView) findViewById(R.id.tv_dc);

        iv_zc = (ImageView) findViewById(R.id.iv_zc);
        iv_wc = (ImageView) findViewById(R.id.iv_wc);
        iv_dc = (ImageView) findViewById(R.id.iv_dc);

        m_fragment_home = new FragmentHome();
        m_fragment_find = new FragmentFind();
        m_fragment_me = new FragmentMe();
        mFragments.add(m_fragment_home);
        mFragments.add(m_fragment_find);
        mFragments.add(m_fragment_me);

        zc = new BadgeView(this);
        wc = new BadgeView(this);
        dc = new BadgeView(this);

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
        m_viewPager.setAdapter(mAdapter);
        m_viewPager.setCurrentItem(0);
        // 页面切换效果
        m_viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // 重置所有TextView的字体颜色
                index = arg0;
                resetTextView();
                switch (arg0) {
                    case 0:
                        ll_zc.removeView(zc);
                        ll_zc.addView(zc);
                        tv_zc.setTextColor(getResources().getColor(R.color.green));
                        iv_zc.setImageResource(R.drawable.rdb_main_home_s);
                        break;
                    case 1:
                        tv_wc.setTextColor(getResources().getColor(R.color.green));
                        iv_wc.setImageResource(R.drawable.rdb_main_find_s);
                        ll_wc.removeView(wc);
                        ll_wc.addView(wc);
                        break;
                    case 2:
                        tv_dc.setTextColor(getResources().getColor(R.color.green));
                        iv_dc.setImageResource(R.drawable.rdb_main_me_s);
                        break;
                }

                pageSelect(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        ll_zc.setOnClickListener(this);
        ll_wc.setOnClickListener(this);
        ll_dc.setOnClickListener(this);
    }

    // 初始化事件
    private void initEvnet1() {
        m_viewPager = (ViewPager) findViewById(R.id.vp_main_viewPager);
        ll_zc = (LinearLayout) findViewById(R.id.ll_zc);
        ll_wc = (LinearLayout) findViewById(R.id.ll_wc);
        ll_dc = (LinearLayout) findViewById(R.id.ll_dc);

        tv_zc = (TextView) findViewById(R.id.tv_zc);
        tv_wc = (TextView) findViewById(R.id.tv_wc);
        tv_dc = (TextView) findViewById(R.id.tv_dc);

        iv_zc = (ImageView) findViewById(R.id.iv_zc);
        iv_wc = (ImageView) findViewById(R.id.iv_wc);
        iv_dc = (ImageView) findViewById(R.id.iv_dc);

        m_fragment_home = new FragmentHome();
        m_fragment_find = new FragmentFind();
        m_fragment_me = new FragmentMe();
        mFragments.add(m_fragment_home);
        mFragments.add(m_fragment_find);
        mFragments.add(m_fragment_me);

        zc = new BadgeView(this);
        wc = new BadgeView(this);
        dc = new BadgeView(this);

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
        m_viewPager.setAdapter(mAdapter);
        m_viewPager.setCurrentItem(1);
        // 页面切换效果
        m_viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // 重置所有TextView的字体颜色
                index = arg0;
                resetTextView();
                switch (arg0) {
                    case 0:
                        ll_zc.removeView(zc);
                        ll_zc.addView(zc);
                        tv_zc.setTextColor(getResources().getColor(R.color.green));
                        iv_zc.setImageResource(R.drawable.rdb_main_home_s);
                        break;
                    case 1:
                        tv_wc.setTextColor(getResources().getColor(R.color.green));
                        iv_wc.setImageResource(R.drawable.rdb_main_find_s);
                        ll_wc.removeView(wc);
                        ll_wc.addView(wc);
                        break;
                    case 2:
                        tv_dc.setTextColor(getResources().getColor(R.color.green));
                        iv_dc.setImageResource(R.drawable.rdb_main_me_s);
                        break;
                }

                pageSelect(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        ll_zc.setOnClickListener(this);
        ll_wc.setOnClickListener(this);
        ll_dc.setOnClickListener(this);
    }

    // 初始化事件
    private void initEvnet2() {
        m_viewPager = (ViewPager) findViewById(R.id.vp_main_viewPager);
        ll_zc = (LinearLayout) findViewById(R.id.ll_zc);
        ll_wc = (LinearLayout) findViewById(R.id.ll_wc);
        ll_dc = (LinearLayout) findViewById(R.id.ll_dc);

        tv_zc = (TextView) findViewById(R.id.tv_zc);
        tv_wc = (TextView) findViewById(R.id.tv_wc);
        tv_dc = (TextView) findViewById(R.id.tv_dc);

        iv_zc = (ImageView) findViewById(R.id.iv_zc);
        iv_wc = (ImageView) findViewById(R.id.iv_wc);
        iv_dc = (ImageView) findViewById(R.id.iv_dc);

        m_fragment_home = new FragmentHome();
        m_fragment_find = new FragmentFind();
        m_fragment_me = new FragmentMe();
        mFragments.add(m_fragment_home);
        mFragments.add(m_fragment_find);
        mFragments.add(m_fragment_me);

        zc = new BadgeView(this);
        wc = new BadgeView(this);
        dc = new BadgeView(this);

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
        m_viewPager.setAdapter(mAdapter);
        m_viewPager.setCurrentItem(2);
        // 页面切换效果
        m_viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // 重置所有TextView的字体颜色
                index = arg0;
                resetTextView();
                switch (arg0) {
                    case 0:
                        ll_zc.removeView(zc);
                        ll_zc.addView(zc);
                        tv_zc.setTextColor(getResources().getColor(R.color.green));
                        iv_zc.setImageResource(R.drawable.rdb_main_home_s);
                        break;
                    case 1:
                        tv_wc.setTextColor(getResources().getColor(R.color.green));
                        iv_wc.setImageResource(R.drawable.rdb_main_find_s);
                        ll_wc.removeView(wc);
                        ll_wc.addView(wc);
                        break;
                    case 2:
                        tv_dc.setTextColor(getResources().getColor(R.color.green));
                        iv_dc.setImageResource(R.drawable.rdb_main_me_s);
                        break;
                }

                pageSelect(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        ll_zc.setOnClickListener(this);
        ll_wc.setOnClickListener(this);
        ll_dc.setOnClickListener(this);
    }

    /**
     * 重置颜色
     */
    protected void resetTextView() {
        tv_zc.setTextColor(getResources().getColor(R.color.black));
        tv_wc.setTextColor(getResources().getColor(R.color.black));
        tv_dc.setTextColor(getResources().getColor(R.color.black));
        iv_zc.setImageResource(R.drawable.rdb_main_home_p);
        iv_wc.setImageResource(R.drawable.rdb_main_find_p);
        iv_dc.setImageResource(R.drawable.rdb_main_me_p);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_zc:
                m_viewPager.setCurrentItem(0);
                index = 0;
                break;
            case R.id.ll_wc:
                m_viewPager.setCurrentItem(1);
                index = 1;
                break;
            case R.id.ll_dc:
                m_viewPager.setCurrentItem(2);
                index = 2;
                break;
            default:
                break;
        }
    }

    // 联动事件处理
    private void pageSelect(int index) {
        switch (index) {
            case 0:
                ll_zc.removeView(zc);
                ll_zc.addView(zc);
                tv_zc.setTextColor(getResources().getColor(R.color.green));
                iv_zc.setImageResource(R.drawable.rdb_main_home_s);
                break;
            case 1:
                tv_wc.setTextColor(getResources().getColor(R.color.green));
                iv_wc.setImageResource(R.drawable.rdb_main_find_s);
                ll_wc.removeView(wc);
                ll_wc.addView(wc);
                break;
            case 2:
                tv_dc.setTextColor(getResources().getColor(R.color.green));
                iv_dc.setImageResource(R.drawable.rdb_main_me_s);
                break;
        }
    }

    // 监听返回按键
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) // System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                showToast(R.string.exit_app);
                exitTime = System.currentTimeMillis();
            } else {

                //地图、导航相关资源清理
                AMapNavi navi = AMapNavi.getInstance(GSApplication.getInstance());//.destroy();
                if (navi != null) {
                    navi.destroy();
                }
                TTSController tts = TTSController.getInstance(GSApplication.getInstance());//.stopSpeaking();
                if (tts != null) {
                    tts.startSpeaking();
                    tts.destroy();
                }
//                TTSController.getInstance(GSApplication.getInstance()).destroy();

                //结束应用
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }

            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //调用FragmentMe中的方法
        m_fragment_me.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Services.notification = false;
        Services.fee = false;
        MyNetWorkBroadcastReceive.msgListeners.remove(this);
    }


}
