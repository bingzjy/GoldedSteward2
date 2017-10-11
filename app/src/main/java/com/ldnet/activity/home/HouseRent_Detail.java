package com.ldnet.activity.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.me.Publish;
import com.ldnet.entities.HouseProperties;
import com.ldnet.entities.HouseRent;
import com.ldnet.entities.KValues;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.ImageCycleView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.autoscrollviewpager.AutoScrollViewPager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Murray on 2015/9/10.
 */
public class HouseRent_Detail extends BaseActionBarActivity {
    private TextView houserent_detail_price, houserent_detail_rentType,
            houserent_detail_room, houserent_detail_acreage,
            houserent_detail_fitmenttype, houserent_detail_roomtype,
            houserent_detail_floor, houserent_detail_orientation, btn_update,
            houserent_detail_title, houserent_detail_address, houserent_detail_tv_address,
            tv_page_title,houserent_detail_RoomDeploy;
    private ImageView houserent_detail_img;
    private Button houserent_detail_contracttel;
    private RelativeLayout houserent_detail_rl_address;
    private ViewPager vp;
    private Services service;
    private ImageButton btn_back;
    //    private String id;
    private HouseProperties mHouseProperties;
    private ImageCycleView vp_house_rent_images;
    private List<View> mImages;
    private PagerAdapter mAdapter;
    private HouseRent houseRent;
    private ArrayList<String> mImageUrl = null;
    private Boolean mFromPublish = false;

    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        houserent_detail_contracttel.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.houserent_item_detail);
        String formPublish = getIntent().getStringExtra("FROM_PUBLISH");
        if (!TextUtils.isEmpty(formPublish)) {
            mFromPublish = Boolean.valueOf(formPublish);
        }
        vp_house_rent_images = (ImageCycleView) findViewById(R.id.vp_house_rent_images);
        // 改线ViewPager的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) vp_house_rent_images.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        linearParams.height = dm.widthPixels / 3 * 2;
        vp_house_rent_images.setLayoutParams(linearParams);
        mImageUrl = new ArrayList<String>();
        service = new Services();
        getHouseRentInfo();
        btn_back = (ImageButton) findViewById(R.id.btn_back);


        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.houserent_detail_title);

        houserent_detail_RoomDeploy = (TextView)findViewById(R.id.houserent_detail_RoomDeploy);
        houserent_detail_rl_address = (RelativeLayout)findViewById(R.id.houserent_detail_rl_address);
        houserent_detail_title = (TextView) findViewById(R.id.houserent_detail_title);
        houserent_detail_price = (TextView) findViewById(R.id.houserent_detail_price);
        houserent_detail_rentType = (TextView) findViewById(R.id.houserent_detail_rentType);
        houserent_detail_room = (TextView) findViewById(R.id.houserent_detail_room);
        houserent_detail_acreage = (TextView) findViewById(R.id.houserent_detail_acreage);
        houserent_detail_fitmenttype = (TextView) findViewById(R.id.houserent_detail_fitmenttype);
        houserent_detail_roomtype = (TextView) findViewById(R.id.houserent_detail_roomtype);
        houserent_detail_floor = (TextView) findViewById(R.id.houserent_detail_floor);
        houserent_detail_orientation = (TextView) findViewById(R.id.houserent_detail_orientation);
        houserent_detail_address = (TextView) findViewById(R.id.houserent_detail_address);
        houserent_detail_tv_address = (TextView) findViewById(R.id.houserent_detail_tv_address);
        houserent_detail_contracttel = (Button) findViewById(R.id.houserent_detail_contracttel);
        btn_update = (TextView) findViewById(R.id.btn_update);

        houseRent = (HouseRent) getIntent().getSerializableExtra("HouseRent");
        //标题
        houserent_detail_title.setText(houseRent.Title);
        //租金
        houserent_detail_price.setText("￥" + houseRent.Price + "元");
        //房屋结构
        houserent_detail_room.setText(houseRent.Room + "室" + houseRent.Hall + "厅" + houseRent.Toilet + "卫");
        //房屋面积
        houserent_detail_acreage.setText(houseRent.Acreage + "平米");

        //楼层情况
        houserent_detail_floor.setText(houseRent.Floor + "/" + houseRent.FloorCount);

        //地址
        houserent_detail_address.setText(houseRent.Address);
        if (mFromPublish) {
            houserent_detail_rl_address.setVisibility(View.GONE);
            houserent_detail_tv_address.setVisibility(View.GONE);
            houserent_detail_contracttel.setText("编辑信息");
        }

        //图片加载
        if (!TextUtils.isEmpty(houseRent.Images)) {
            for (String imageid : houseRent.Images.split(",")) {
                mImageUrl.add(Services.getImageUrl(imageid));
            }
            vp_house_rent_images.setImageResources(mImageUrl, mAdCycleViewListener);
        }

        initEvent();
    }

    private ImageCycleView.ImageCycleViewListener mAdCycleViewListener = new ImageCycleView.ImageCycleViewListener() {

        @Override
        public void onImageClick(int position, View imageView) {

        }

        @Override
        public void displayImage(String imageURL, ImageView imageView) {
            ImageLoader.getInstance().displayImage(imageURL, imageView, imageOptions);// 此处本人使用了ImageLoader对图片进行加装！
        }
    };

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
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call,e,i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd222", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            JSONObject jsonObject1 = new JSONObject(jsonObject.getString("Obj"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    List<KValues> maps = new ArrayList<KValues>();
                                    maps.add(new KValues("-1", "租金类型"));
                                    JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("RentType"));
                                    JSONObject jsonObject3 = new JSONObject(jsonObject1.getString("Orientation"));
                                    JSONObject jsonObject4 = new JSONObject(jsonObject1.getString("FitmentType"));
                                    JSONObject jsonObject5 = new JSONObject(jsonObject1.getString("RoomType"));
                                    JSONObject jsonObject6 = new JSONObject(jsonObject1.getString("RoomDeploy"));
                                    for (int j = 0; j < 12; j++) {
                                        KValues values = new KValues();
                                        values.Key = String.valueOf(j);
                                        values.Value = jsonObject2.getString(j + "");
                                        maps.add(values);
                                    }
                                    List<KValues> maps1 = new ArrayList<KValues>();
                                    maps1.add(new KValues("-1", "朝向"));
                                    for (int z = 0; z < 10; z++) {
                                        KValues values = new KValues();
                                        values.Key = String.valueOf(z);
                                        values.Value = jsonObject3.getString(z + "");
                                        maps1.add(values);
                                    }
                                    List<KValues> maps2 = new ArrayList<KValues>();
                                    maps2.add(new KValues("-1", "住宅类型"));
                                    for (int w = 0; w < 6; w++) {
                                        KValues values = new KValues();
                                        values.Key = String.valueOf(w);
                                        values.Value = jsonObject5.getString(w + "");
                                        maps2.add(values);
                                    }
                                    List<KValues> maps4 = new ArrayList<KValues>();
                                    maps4.add(new KValues("-1", "房屋配置"));
                                    for (int f = 0; f< 2; f++) {
                                        KValues values = new KValues();
                                        values.Key = String.valueOf(f);
                                        values.Value = jsonObject6.getString(f + "");
                                        maps4.add(values);
                                    }
                                    List<KValues> maps3 = new ArrayList<KValues>();
                                    maps3.add(new KValues("-1", "装修情况"));
                                    for (int p = 0; p < 5; p++) {
                                        KValues values = new KValues();
                                        values.Key = String.valueOf(p);
                                        values.Value = jsonObject4.getString(p + "");
                                        maps3.add(values);
                                    }
//                                    mHouseProperties = gson.fromJson(jsonObject.getString("Obj"), HouseProperties.class);
                                    try {
                                        //租金交纳方式
                                        houserent_detail_rentType.setText(maps.get(Integer.valueOf(houseRent.RentType)+1).Value);
                                        //装修情况
                                        houserent_detail_fitmenttype.setText(maps3.get(Integer.valueOf(houseRent.FitmentType)+1).Value);
                                        //房屋类型
                                        houserent_detail_roomtype.setText(maps2.get(Integer.valueOf(houseRent.RoomType)+1).Value);
                                        //房屋朝向
                                        houserent_detail_orientation.setText(maps1.get(Integer.valueOf(houseRent.Orientation)+1).Value);
                                        //房屋配置
                                        houserent_detail_RoomDeploy.setText(maps4.get(Integer.valueOf(houseRent.RoomDeploy)+1).Value);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

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
        switch (v.getId()) {
            case R.id.btn_back:
                if (mFromPublish) {
//                    try {
//                        gotoActivityAndFinish(Publish.class.getName(), null);
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    finish();
                } else {
                    try {
                        gotoActivityAndFinish(HouseRent_List.class.getName(), null);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case R.id.houserent_detail_contracttel://如果来自我的发布跳转到编辑
                if (mFromPublish) {
                    Intent intent = new Intent(this, HouseRentUpdate.class);
                    intent.putExtra("HouseRent", houseRent);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
//                    finish();
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + houseRent.ContactTel));
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mFromPublish) {
//                try {
//                    gotoActivityAndFinish(Publish.class.getName(), null);
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
                finish();
            } else {
                try {
                    gotoActivityAndFinish(HouseRent_List.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
