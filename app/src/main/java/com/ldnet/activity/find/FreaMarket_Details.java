package com.ldnet.activity.find;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.FreaMarketDetails;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.view.ImageCycleView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class FreaMarket_Details extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageCycleView vp_frea_market_images;
    private TextView tv_frea_market_title;
    private TextView tv_frea_market_contract_name;
    private TextView tv_frea_market_datetime;
    private TextView tv_frea_market_org_price;
    private TextView tv_frea_market_price;
    private TextView tv_frea_market_address;
    private TextView tv_frea_market_content;
    private Button btn_frea_market_call;

    private Services services;
    private String mContractPhone;
    private String mFreamarketId;
    private Boolean mFromPublish = false;

    private List<View> mImages;
    private PagerAdapter mAdapter;
    private FreaMarketDetails details;
    private ArrayList<String> mImageUrl = null;

    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fleamarket_details);

        //二手商品ID
        mFreamarketId = getIntent().getStringExtra("FREA_MARKET_ID");
        String formPublish = getIntent().getStringExtra("FROM_PUBLISH");
        if (!TextUtils.isEmpty(formPublish)) {
            mFromPublish = Boolean.valueOf(formPublish);
        }
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.frea_market_title);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //初始化控件
        vp_frea_market_images = (ImageCycleView) findViewById(R.id.vp_frea_market_images);
        // 改线ViewPager的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) vp_frea_market_images.getLayoutParams();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        linearParams.height = dm.widthPixels / 3 * 2;
        vp_frea_market_images.setLayoutParams(linearParams);
        mImageUrl = new ArrayList<String>();

        //标题
        tv_frea_market_title = (TextView) findViewById(R.id.tv_frea_market_title);
        //联系人姓名
        tv_frea_market_contract_name = (TextView) findViewById(R.id.tv_frea_market_contract_name);
        //发布时间
        tv_frea_market_datetime = (TextView) findViewById(R.id.tv_frea_market_datetime);
        //新品价格
        tv_frea_market_org_price = (TextView) findViewById(R.id.tv_frea_market_org_price);
        //二手价格
        tv_frea_market_price = (TextView) findViewById(R.id.tv_frea_market_price);
        //发布地点
        tv_frea_market_address = (TextView) findViewById(R.id.tv_frea_market_address);
        //二手介绍
        tv_frea_market_content = (TextView) findViewById(R.id.tv_frea_market_content);
        //致电物主
        btn_frea_market_call = (Button) findViewById(R.id.btn_frea_market_call);
        if (mFromPublish) {
            btn_frea_market_call.setText("编辑信息");
        }
        initEvent();
        //初始化服务
        services = new Services();
        FreaMarketDetails(mFreamarketId);
    }

    //跳蚤市场 - 获取闲置物品的详情
    public void FreaMarketDetails(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetUnusedGoodsById/%s?residentId=%s";
        url = String.format(url, id, UserInformation.getUserInfo().getUserId());
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
                                    details = gson.fromJson(jsonObject.getString("Obj"), FreaMarketDetails.class);
                                    tv_frea_market_title.setText(details.Title);
                                    tv_frea_market_contract_name.setText(details.ContractName);
                                    mContractPhone = details.ContractTel;
//                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                                    String dateString = dateFormat.format(details.Updated);
                                    tv_frea_market_datetime.setText(Services.subStr(details.Updated));
                                    if (!TextUtils.isEmpty(details.OrgPrice)) {
                                        tv_frea_market_org_price.setText("￥" + details.OrgPrice);
                                    }
                                    tv_frea_market_price.setText("￥" + details.Price);
                                    tv_frea_market_address.setText(details.Address);
                                    tv_frea_market_content.setText(details.Memo);
                                    if (details.getImg() != null) {
                                        for (int j = 0; j < details.getImg().length; j++) {
                                            mImageUrl.add(Services.getImageUrl(details.getImg()[j]));
                                        }
                                        vp_frea_market_images.setImageResources(mImageUrl, mAdCycleViewListener);
                                    }
                                } else {
                                    showToast(jsonObject.getString("Message"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private com.ldnet.view.ImageCycleView.ImageCycleViewListener mAdCycleViewListener = new com.ldnet.view.ImageCycleView.ImageCycleViewListener() {

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
        btn_frea_market_call.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    if (!mFromPublish) {
                        gotoActivityAndFinish(FreaMarket.class.getName(), null);
                    } else {
//                        try {
//                            gotoActivityAndFinish(Publish.class.getName(), null);
//                        } catch (ClassNotFoundException e) {
//                            e.printStackTrace();
//                        }
                        finish();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_frea_market_call:
                if (mFromPublish) {
                    Intent intent = new Intent(this, FreaMarket_Create.class);
                    intent.putExtra("FREA_MARKET_ID", mFreamarketId);
                    intent.putExtra("FROM_PUBLISH", "true");
                    intent.putExtra("FROM_FREAMARKET_DETAILS", "true");
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mContractPhone));
                    startActivity(intent);
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
                if (!mFromPublish) {
                    gotoActivityAndFinish(FreaMarket.class.getName(), null);
                } else {
//                    try {
//                        gotoActivityAndFinish(Publish.class.getName(), null);
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
                    finish();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
