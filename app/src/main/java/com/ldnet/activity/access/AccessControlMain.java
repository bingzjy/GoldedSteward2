package com.ldnet.activity.access;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ldnet.activity.adapter.MainPagerAdapter;
import com.ldnet.goldensteward.R;
import com.ldnet.service.AccessControlService;
import com.ldnet.service.BaseService;
import com.ldnet.utility.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccessControlMain extends FragmentActivity implements View.OnClickListener {

    private ViewPager passViewPager;
    private TextView title, titleVisitor, titleGoods, bar1, bar2;
    private ImageButton back;
    private GoodsRecordFragment goodsRecordFragment;
    private VisitorRecordFragment visitorRecordFragment;
    private final String GOODS_FRAGMENT = "goods_fragment";
    private final String Visitor_FRAGMENT = "visitor_fragment";
    private FragmentManager manager = getSupportFragmentManager();
    private List<Fragment> fragmentList = new ArrayList<>();
    private MainPagerAdapter adapter;
    private AccessControlService service;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_exit_main);

        service = new AccessControlService(AccessControlMain.this);

        service.getAccessRecord("2017-09-03 00:00:00", "2017-10-03 00:00:00", "1", getRecordHandler);
        service.addAccessInvite(Utility.generateGUID(), "0", "卡芙卡", "18603419370", "2017-10-03 00:00:00", "作客", "false", "", addRecordHandler);

        initFragment(savedInstanceState);
        initView();
        initEvent();
    }


    private void initView() {
        passViewPager = (ViewPager) findViewById(R.id.viewpager_entry_exit);
        title = (TextView) findViewById(R.id.tv_page_title);
        back = (ImageButton) findViewById(R.id.btn_back);
        titleVisitor = (TextView) findViewById(R.id.tv_title_visitor);
        titleGoods = (TextView) findViewById(R.id.tv_title_goods);
        bar1 = (TextView) findViewById(R.id.tab_bar_visitor);
        bar2 = (TextView) findViewById(R.id.tab_bar_goods);
        title.setText("出入管理");

        adapter = new MainPagerAdapter(getSupportFragmentManager(),
                AccessControlMain.this,
                new String[]{"访客记录", "物品出入"},
                fragmentList);
        passViewPager.setAdapter(adapter);
    }


    private void initEvent() {

        back.setOnClickListener(this);
        titleGoods.setOnClickListener(this);
        titleVisitor.setOnClickListener(this);

        passViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    titleVisitor.setTextColor(Color.parseColor("#1FB79F"));
                    titleGoods.setTextColor(Color.parseColor("#4A4A4A"));
                    bar1.setBackgroundResource(R.color.green);
                    bar2.setBackgroundResource(R.color.bg_gray);
                } else {
                    titleVisitor.setTextColor(Color.parseColor("#4A4A4A"));
                    titleGoods.setTextColor(Color.parseColor("#1FB79F"));
                    bar1.setBackgroundResource(R.color.bg_gray);
                    bar2.setBackgroundResource(R.color.green);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void initFragment(Bundle bundle) {
        FragmentManager manager = getSupportFragmentManager();
        if (bundle != null) {
            goodsRecordFragment = (GoodsRecordFragment) manager.getFragment(bundle, GOODS_FRAGMENT);
            visitorRecordFragment = (VisitorRecordFragment) manager.getFragment(bundle, Visitor_FRAGMENT);
        } else {
            goodsRecordFragment = GoodsRecordFragment.newInstance();
            visitorRecordFragment = VisitorRecordFragment.newInstance();
        }

        fragmentList.clear();
        fragmentList.add(visitorRecordFragment);
        fragmentList.add(goodsRecordFragment);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_add_invite_visitor:
                Intent intent = new Intent(this, AddVisitorInviteActivity.class);
                startActivity(intent);
                break;
            case R.id.tab_bar1:
                passViewPager.setCurrentItem(0);
                break;
            case R.id.tab_bar2:
                passViewPager.setCurrentItem(1);
                break;
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        FragmentManager manager = getSupportFragmentManager();
        manager.putFragment(savedInstanceState, GOODS_FRAGMENT, goodsRecordFragment);
        manager.putFragment(savedInstanceState, Visitor_FRAGMENT, visitorRecordFragment);
    }

    Handler addRecordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    Toast.makeText(AccessControlMain.this, "添加成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    Handler getRecordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    Toast.makeText(AccessControlMain.this, "查询成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
