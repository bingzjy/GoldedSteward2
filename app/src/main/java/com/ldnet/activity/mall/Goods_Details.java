package com.ldnet.activity.mall;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Goods;
import com.ldnet.entities.SubOrders;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.BottomDialog;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.DialogGoods;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Alex on 2015/9/28.
 */
public class Goods_Details extends BaseActionBarActivity {

    // 标题
    private TextView tv_page_title;
    // 返回
    private ImageButton btn_back;
    //自定义按钮--分享商品
    private Button btn_custom;
    //浏览器
    private WebView wv_browser;
    //浏览的Web页面Url
    private String mUrl;
    //从哪个Activity跳转过来的
    private String mFromClassName;
    //页面标题
    private String mTitle;
    //商品ID
    private Goods mGoods;
    private String mCID;
    //临时URL
    //private String mGoodsUrl = "http://192.168.0.105:8081/Goods/Preview?ID=%s&IsApp=true";
    private LinearLayout ll_buttons_goods;
    private Button btn_goods_shopping_cart_add;
    private Button btn_goods_buy;
    private Services services;
    private Button mBtnShopStore;
    private List<SubOrders> orderses;
    private ProgressBar mProgressBar;
    private String urlParam;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //得到参数
        mGoods = (Goods) getIntent().getSerializableExtra("GOODS");
        mTitle = getIntent().getStringExtra("PAGE_TITLE");
        mUrl = getIntent().getStringExtra("URL");
        mFromClassName = getIntent().getStringExtra("FROM_CLASS_NAME");
        mCID = getIntent().getStringExtra("CID");
        // 设置布局
        setContentView(R.layout.activity_mall_goods_details);
        User user = UserInformation.getUserInfo();
        if (mGoods.Type.equals(2)) {
            urlParam = "&UID=" + user.UserId + "&UName=" + user.UserName + "&UImgID=" + user.UserThumbnail;
        } else {
            urlParam = "&IsApp=true";
        }

        //按钮列
        ll_buttons_goods = (LinearLayout) findViewById(R.id.ll_buttons_goods);
        if (!mGoods.Type.equals(2)) {
            ll_buttons_goods.setVisibility(View.VISIBLE);
        }
        //分享按钮
        btn_custom = (Button) findViewById(R.id.btn_custom);
        btn_custom.setText(R.string.share);
        btn_custom.setVisibility(View.VISIBLE);

        //--------------BUTTON BACK-------------
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        if (!Services.isNotNullOrEmpty(mFromClassName)) {
            btn_back.setVisibility(View.GONE);
        }
        //--------------TITLE-------------
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.goods_details);
        //进度条
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_loading);
        //--------------WEBVIEW-------------
        wv_browser = (WebView) findViewById(R.id.wv_browser);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        initView();
        //进入店铺
        mBtnShopStore = (Button) findViewById(R.id.btn_shop_store);
        // 如果从店铺里进入商品详情不显示进入店铺
        if (mFromClassName.equals(StoreGoods.class.getName()) || mFromClassName.equals(ShopStore.class.getName())) {
            mBtnShopStore.setVisibility(View.GONE);
            btn_custom.setVisibility(View.GONE);
        }
        //加入购物车
        btn_goods_shopping_cart_add = (Button) findViewById(R.id.btn_goods_shopping_cart_add);
        //立即购买
        btn_goods_buy = (Button) findViewById(R.id.btn_goods_buy);
        services = new Services();
        initEvent();
        initEvents();
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                initView();
            }
        });
    }

    public void initView(){
        //Settings
        WebSettings webSettings = wv_browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setLoadsImagesAutomatically(true);

        //ChromeClient
        WebChromeClient chromeClient = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                tv_page_title.setText(title);
                super.onReceivedTitle(view, title);
            }

            // 网页进度条的加载
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mPullToRefreshScrollView.onRefreshComplete();
                } else {
                    if (View.INVISIBLE == mProgressBar.getVisibility()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        };
        wv_browser.setWebChromeClient(chromeClient);
        //Client
        WebViewClient client = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                网页加载完成进度条消失
                mProgressBar.setVisibility(View.GONE);
            }
        };
        wv_browser.setWebViewClient(client);
        wv_browser.loadUrl(mUrl + urlParam);
    }

    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_goods_shopping_cart_add.setOnClickListener(this);
        btn_goods_buy.setOnClickListener(this);
        btn_custom.setOnClickListener(this);
        mBtnShopStore.setOnClickListener(this);
    }

    // 点击事件处理
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back://判断是否来自店铺的商品
                    if (mFromClassName.equals(StoreGoods.class.getName()) || mFromClassName.equals(ShopStore.class.getName()) || mFromClassName.equals(Order_Details.class.getName())) {
                        finish();
                    } else {
                        HashMap<String, String> extras = new HashMap<String, String>();
                        if (Services.isNotNullOrEmpty(mCID)) {
                            extras.put("PAGE_TITLE", mTitle);
                            extras.put("CID", mCID);
                        }
                        super.gotoActivityAndFinish(mFromClassName, extras);
                    }

                    break;
                case R.id.btn_shop_store://进入店铺
                    HashMap<String, String> extrasShopStore = new HashMap<String, String>();
                    extrasShopStore.put("RETAILERID", mGoods.RID);
                    extrasShopStore.put("CID", mCID);
//                    extrasShopStore.put("URL", mUrl);
                    gotoActivity(ShopStore.class.getName(), extrasShopStore);
                    break;
                case R.id.btn_goods_shopping_cart_add://添加购物车
                    showDialog(false);
                    break;
                case R.id.btn_goods_buy://立即购买
                    showDialog(true);
                    break;
                case R.id.btn_custom:
                    if (Services.isNotNullOrEmpty(mGoods.getThumbnail())) {
                        BottomDialog dialog = new BottomDialog(this, mUrl, mGoods.GSN, services.getImageUrl(mGoods.getThumbnail()), mGoods.T);
                        dialog.uploadImageUI(this);
                    } else {
                        BottomDialog dialog = new BottomDialog(this, mUrl, mGoods.T);
                        dialog.uploadImageUI(this);
                    }
                    break;
                default:
                    break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    String textView;

    //弹出对话框
    private void showDialog(Boolean isBuy) {
        DialogGoods dialog;

        if (isBuy) {
            textView = "确认";
            dialog = new DialogGoods(this, mGoods, new DirectBuy(), textView);
        } else {
            textView = "加入购物车";
            dialog = new DialogGoods(this, mGoods, new ShoppingCart(), textView);
        }
        dialog.show();
    }

    class ShoppingCart implements DialogGoods.OnGoodsDialogListener {
        @Override
        public void Confirm(String businessId, String goodsId, String stockId, Integer number) {
            if (mGoods.ST == 0) {
                showToast(getResources().getString(R.string.mall_goods_not));
            } else {
                ShoppingCartsAdd(businessId, goodsId, stockId, number);
            }

        }
    }


    class DirectBuy implements DialogGoods.OnGoodsDialogListener {
        @Override
        public void Confirm(String businessId, String goodsId, String stockId, Integer number) {
            if (mGoods.ST <= 0) {
                showToast(getResources().getString(R.string.mall_goods_not));
            } else {
                OrderPreSubmitNew(businessId, goodsId, stockId, number);
            }

        }
    }

    //订单预提交
//    获取提交订单列表页数据(post) （新 优惠卷） 返回APPResult 对象 List(_APP_Return_SubOrderList)
//    BOrder/APP_GetSubOrderList_Post_New
    public void OrderPreSubmitNew(String bid, String gid, final String ggid, Integer n) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        try {
            // 请求的URL
            String url = Services.mHost + "BOrder/APP_GetSubOrderList_Post_New";
            JSONArray array = new JSONArray();
            JSONObject subOrderInfos = new JSONObject();
            //构造订单详细信息
            subOrderInfos.put("BID", bid);
            subOrderInfos.put("MS", "");
            //订单中商品的详细信息
            JSONArray goodsArray = new JSONArray();
            JSONObject goodsInfos = new JSONObject();
            goodsInfos.put("GID", gid);
            goodsInfos.put("GGID", ggid);
            goodsInfos.put("SID", "");
            goodsInfos.put("N", n);
            goodsInfos.put("GIM", "");
            goodsArray.put(goodsInfos);
            //添加商品详细信息到订单中
            subOrderInfos.put("SD", goodsArray);
            subOrderInfos.put("UID", UserInformation.getUserInfo().UserId);
            array.put(subOrderInfos);
            HashMap<String, String> extras = new HashMap<>();
            extras.put("str", array.toString());
            Services.json(extras);
            String md5 = UserInformation.getUserInfo().getUserPhone() +
                    aa + aa1 + Services.json(extras) + Services.TOKEN;
            OkHttpUtils.post().url(url)
                    .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                    .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                    .addHeader("timestamp", aa)
                    .addHeader("nonce", aa1)
                    .addHeader("signature", Services.textToMD5L32
                            (md5))
                    .addParams("str", array.toString())
                    .build()
                    .execute(new DataCallBack(this) {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            super.onError(call, e, i);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            super.onResponse(s, i);
                            Log.d("asdsdasd", "111111111" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {
                                        Gson gson = new Gson();
                                        Type type = new TypeToken<List<SubOrders>>() {
                                        }.getType();
                                        orderses = gson.fromJson(jsonObject.getString("Obj"), type);
                                        if (orderses != null) {
                                            Intent intent = new Intent(Goods_Details.this, Order_Confirm.class);
                                            intent.putExtra("SUB_ORDERS", (Serializable) orderses);
                                            intent.putExtra("IS_FROM_GOODSDETAILS", "true");
                                            intent.putExtra("GOODS", mGoods);
                                            intent.putExtra("PAGE_TITLE", mTitle);
                                            intent.putExtra("URL", mUrl);
                                            intent.putExtra("CID", mCID);
                                            intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                                            startActivity(intent);
                                            Goods_Details.this.finish();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //添加到购物车
    public void ShoppingCartsAdd(String bid, String gid, String ggid, Integer n) {
        try {
            //JSON对象
            JSONObject object = new JSONObject();
            object.put("BID", bid);
            object.put("RID", UserInformation.getUserInfo().UserId);
            object.put("GID", gid);
            object.put("GGID", ggid);
            object.put("N", n);
            object.put("GI", "");

            // 请求的URL
            String url = Services.mHost + "BShoppingCart/APP_InsertShopping";
            HashMap<String, String> extras = new HashMap<>();
            extras.put("str", object.toString());
            Services.json(extras);
            String aa = Services.timeFormat();
            String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
            String aa2 = url;
            String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + Services.json(extras) + Services.TOKEN;
            OkHttpUtils.post().url(url)
                    .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                    .addHeader("timestamp", aa)
                    .addHeader("nonce", aa1)
                    .addHeader("signature", Services.textToMD5L32(md5))
                    .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                    .addParams("str",object.toString())
                    .build()
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
                                        showToast(getResources().getString(R.string.mall_goods_shooping_cart_success));
                                    } else {
                                        showToast(getResources().getString(R.string.mall_goods_shooping_cart_error));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
