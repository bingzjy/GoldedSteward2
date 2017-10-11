package com.ldnet.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.activity.me.Community;
import com.ldnet.entities.ChargingItem;
import com.ldnet.entities.Coupon;
import com.ldnet.entities.HouseRent;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.third.listviewshangxia.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Property_Repair extends BaseActionBarFragmentActivity implements View.OnClickListener {
    private TextView tv_main_title, tv_charge,tv_text;
    private ImageButton btn_back;
    private ImageButton btn_create_repair;
    private ViewPager mViewPager;
    private TextView tv_repair_houseinfo;
    private ImageButton ibtn_repair_change_house;
    private XListView lv_home_repairs;
    private PopupWindow popupWindow;
    private ListViewAdapter adapter;
    private ListView listView;
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
    private List<ChargingItem> chargingItems;

    //初始化视图
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_property_repair);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.property_services_repair);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_charge = (TextView) findViewById(R.id.tv_charge);
        User user = UserInformation.getUserInfo();
        tv_repair_houseinfo = (TextView) findViewById(R.id.tv_repair_houseinfo);
        tv_repair_houseinfo.setText(user.CommuntiyName + "(" + user.HouseName + ")");
        ibtn_repair_change_house = (ImageButton) findViewById(R.id.ibtn_repair_change_house);
        //现在报修按钮
        btn_create_repair = (ImageButton) findViewById(R.id.btn_custom);
        btn_create_repair.setImageResource(R.drawable.plus);
        btn_create_repair.setVisibility(View.VISIBLE);
        mViewPager = (ViewPager) findViewById(R.id.id_vp);
        initView();
//        initTabLine();

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
    }

    public void getData(String cid) {
        String url = Services.mHost + "WFRepairs/APP_WY_GetSFOptionList?CID=%s";
        url = String.format(url, cid);
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
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

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
                                    Type type = new TypeToken<List<ChargingItem>>() {
                                    }.getType();
                                    chargingItems = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (chargingItems != null && chargingItems.size() > 0) {
                                        tv_text.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);
                                        adapter = new ListViewAdapter<ChargingItem>(Property_Repair.this, R.layout.ly_pop_win_item, chargingItems) {
                                            @Override
                                            public void convert(ViewHolder holder, ChargingItem chargingItem) {
                                                holder.setText(R.id.tv_charge_name, chargingItem.getTITLE());
                                                holder.setText(R.id.tv_charge_money, chargingItem.getSFMONEY()+"元");
                                            }
                                        };
                                        listView.setAdapter(adapter);
                                        Services.setListViewHeightBasedOnChildren(listView);
                                    }else{
                                        tv_text.setVisibility(View.VISIBLE);
                                        listView.setVisibility(View.GONE);
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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

        RepairIngFragment tab01 = new RepairIngFragment();
//        RepairCompleteFragment tab02 = new RepairCompleteFragment();
        RepairCommentFragment tab03 = new RepairCommentFragment();
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
        btn_create_repair.setOnClickListener(this);
        ibtn_repair_change_house.setOnClickListener(this);
        tv_charge.setOnClickListener(this);
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
                    gotoActivityAndFinish(Property_Repair_Create.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ibtn_repair_change_house:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("NOT_FROM_ME", "101");
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
            case R.id.tv_charge:
//                popWin();
                Intent intent = new Intent(this,Property_Repair_Fee.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void popWin() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.ly_pop_win, null);
        //自适配长、框设置
        popupWindow = new PopupWindow(view, 500, 800);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.update();
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        listView = (ListView) view.findViewById(R.id.lv_charge);
        tv_text = (TextView) view.findViewById(R.id.tv_text);
        TextView textView = (TextView) view.findViewById(R.id.tv_confirm);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        getData(UserInformation.getUserInfo().CommunityId);
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
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
