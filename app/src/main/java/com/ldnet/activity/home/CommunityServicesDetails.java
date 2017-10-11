package com.ldnet.activity.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Item;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.ImageCycleView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxs on 2016/3/1.
 * 社区服务详情
 */
public class CommunityServicesDetails extends BaseActionBarActivity {
    // 标题
    private TextView tv_main_title;
    private ImageButton btn_back, btn_custom;
    private Services services;
    private String mCommunityServicesId;
    //    private WebView webView;
    private com.ldnet.entities.CommunityServicesDetails communityServicesDetails;
    //社区服务的图片
    private ImageCycleView mImgHousekeeping;
    private TextView mTvHousekeepingTitle, mTvHousekeepingAddress, mTvHousekeepingMemo;
    private List<Item> itemList;
    private ListView mLvcommunityServices;
    private ListViewAdapter mAdapter;
    private ArrayList<String> mImageUrl = null;
    //    private List<View> mImages;
//    private PagerAdapter mPgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommunityServicesId = getIntent().getStringExtra("COMMUNITY_SERVICES_ID");
        setContentView(R.layout.activity_community_services_details);
        services = new Services();
        getHouseKeepingById(mCommunityServicesId);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        //分享
        btn_custom = (ImageButton) findViewById(R.id.btn_custom);
        //社区服务的图片
        mImgHousekeeping = (ImageCycleView) findViewById(R.id.img_housekeeping);
        // 改线ViewPager的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mImgHousekeeping.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        linearParams.height = dm.widthPixels / 3 * 2;
        mImgHousekeeping.setLayoutParams(linearParams);
        mImageUrl = new ArrayList<String>();
        //社区服务的标题
        mTvHousekeepingTitle = (TextView) findViewById(R.id.tv_housekeeping_title);

        //社区服务的地址
        mTvHousekeepingAddress = (TextView) findViewById(R.id.tv_housekeeping_address);

        //社区服务的介绍
        mTvHousekeepingMemo = (TextView) findViewById(R.id.tv_housekeeping_memo);

        //服务项目
        mLvcommunityServices = (ListView) findViewById(R.id.lv_housekeeping);
        mLvcommunityServices.setFocusable(false);
        itemList = new ArrayList<Item>();

        //拨打商家电话
        Button imageButton = (Button) findViewById(R.id.btn_phone_housekeeping);
        //设置透明度
        imageButton.getBackground().setAlpha(150);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + communityServicesDetails.Phone));
                startActivity(intent);
            }
        });
        initEvent();
    }

    // 家政服务详细
    public void getHouseKeepingById(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Property/GetHouseKeepingById/%s";
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
                        super.onError(call,e,i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    communityServicesDetails = gson.fromJson(jsonObject.getString("Obj"), com.ldnet.entities.CommunityServicesDetails.class);
                                    tv_main_title.setText(communityServicesDetails.Title);
                                    if (communityServicesDetails.getImages() != null && !communityServicesDetails.getImages().equals("")) {
                                        if(communityServicesDetails.getImages().contains(",")){
                                            String[] str = communityServicesDetails.getImages().split(",");
                                            for (int j = 0; j < str.length; j++) {
                                                mImageUrl.add(Services.getImageUrl(str[j]));
                                            }
                                            mImgHousekeeping.setImageResources(mImageUrl, mAdCycleViewListener);
                                        }else{
                                            mImageUrl.add(Services.getImageUrl(communityServicesDetails.getImages()));
                                            mImgHousekeeping.setImageResources(mImageUrl, mAdCycleViewListener);
                                        }
                                    }else {
                                        mImgHousekeeping.setVisibility(View.GONE);
                                    }
                                    mTvHousekeepingTitle.setText(communityServicesDetails.Title);
                                    mTvHousekeepingAddress.setText(communityServicesDetails.Address);
                                    mTvHousekeepingMemo.setText(communityServicesDetails.Memo);
                                    itemList = communityServicesDetails.Item;
                                    mAdapter = new ListViewAdapter<Item>(CommunityServicesDetails.this, R.layout.item_housekeeping, itemList) {
                                        @Override
                                        public void convert(ViewHolder holder, Item item) {
                                            holder.setText(R.id.tv_item_housekeeping_name, item.Name).setText(R.id.tv_item_housekeeping_cost, "￥" + item.Cost);
                                        }
                                    };
                                    mLvcommunityServices.setAdapter(mAdapter);
                                    //ListViewHeight
                                    setListViewHeight(mLvcommunityServices);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_custom.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://返回社区服务列表
                finish();
                break;
            case R.id.btn_custom://分享
//                try {
//                    gotoActivity(FreaMarket_Create.class.getName(), null);
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
                break;
            default:
                break;
        }
    }

    /**
     * 重新计算ListView的高度，解决ScrollView和ListView两个View都有滚动的效果，在嵌套使用时起冲突的问题
     *
     * @param listView
     */
    public void setListViewHeight(ListView listView) {

        // 获取ListView对应的Adapter

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
