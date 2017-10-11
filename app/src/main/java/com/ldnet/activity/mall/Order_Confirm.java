package com.ldnet.activity.mall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.me.AddressEdit;
import com.ldnet.activity.me.ChooseCoupon;
import com.ldnet.entities.AddressSimple;
import com.ldnet.entities.Goods;
import com.ldnet.entities.OrderPay;
import com.ldnet.entities.RS;
import com.ldnet.entities.SubOrders;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.DialogAddress;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.ldnet.utility.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.Alipay.PayKeys;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import com.ldnet.utility.ListViewAdapter;
import okhttp3.Call;

/**
 * Created by Alex on 2015/9/28.
 */
public class Order_Confirm extends BaseActionBarActivity {

    // 标题
    private TextView tv_page_title;
    // 返回
    private ImageButton btn_back;
    // 服务
    private Services services;
    //是否来自商品详细页面
    private Boolean mIsFromGoodsDetails;
    private String mIsFromChooseCoupon;
    //来自哪里-商品详细页或者购物车
    private String mFromClassName;
    //浏览的Web页面Url
    private String mUrl;
    //页面标题
    private String mTitle;
    //商品ID
    private Goods mGoods;
    //收货地址
    private List<AddressSimple> mAddress;
    private AddressSimple mCurrentAddress;
    private List<SubOrders> mSubOrders;
    //
    private LinearLayout ll_order_address_select;
    private TextView tv_address_name;
    private TextView tv_address_zipcode;
    private TextView tv_address_title;
    private ListView lv_order_details;
    private CheckBox chk_pay_type_checked;
    private CheckBox mUnionPayTypeChecked;
    private TextView tv_goods_numbers;
    private TextView tv_goods_prices;
    private Button btn_goods_balance;
    //订单总金额
    private BigDecimal totalPrices = new BigDecimal("0.00");
    private BigDecimal totalYhjjm = new BigDecimal("0.00");

    //
    private static final Integer PAY_TYPE_OFFLINE = 1;
    private static final Integer PAY_TYPE_ONLINE = 2;

    //
    // 支付宝支付结果标识
    private static final int SDK_PAY_FLAG = 1;
    // 支付宝账号检查标识
    private static final int SDK_CHECK_FLAG = 2;
    // 支付相关信息配置
    private PayKeys keys;
    // 支付信息，包含订单号和金额
    private OrderPay mPayInformation;
    // 商品标题
    private String mSubject;
    // 商品描述
    private String mDescription;
    private String mCID;
    private String serverMode = "01";
    private String tn;
    //    实际支付的钱
    protected Float mOnlinePay;
    private OrderPay orderPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局
        setContentView(R.layout.activity_mall_order_confirm);
        //初始化支付相关信息
        keys = new PayKeys();

        //接收传递的参数
        mIsFromGoodsDetails = Boolean.valueOf(getIntent().getStringExtra("IS_FROM_GOODSDETAILS"));
        mFromClassName = getIntent().getStringExtra("FROM_CLASS_NAME");
        if (mIsFromGoodsDetails) {
            mGoods = (Goods) getIntent().getSerializableExtra("GOODS");
            mTitle = getIntent().getStringExtra("PAGE_TITLE");
            mUrl = getIntent().getStringExtra("URL");
            mCID = getIntent().getStringExtra("CID");
        }

        // 来自选择优惠劵
//        mIsFromChooseCoupon = getIntent().getStringExtra("FROMCHOOSECOUPON");
//        if (Valid.isNotNullOrEmpty(mIsFromChooseCoupon)) {
//        mOrderInfo = (SubOrders) getIntent().getSerializableExtra("SUB_ORDER");
//        }
        // 页面标题
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.mall_order_sure);
        // 返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        ll_order_address_select = (LinearLayout) findViewById(R.id.iv_order_address_select);
        tv_address_name = (TextView) findViewById(R.id.tv_address_name);
        tv_address_zipcode = (TextView) findViewById(R.id.tv_address_zipcode);
        tv_address_title = (TextView) findViewById(R.id.tv_address_title);
        lv_order_details = (ListView) findViewById(R.id.lv_order_details);

        // 服务初始化
        services = new Services();
        //初始化订单商品详细
        mSubOrders = (List<SubOrders>) getIntent().getSerializableExtra("SUB_ORDERS");
        lv_order_details.setAdapter(new ListViewAdapter<SubOrders>(this, R.layout.item_order, mSubOrders) {
            @Override
            public void convert(final ViewHolder holder, final SubOrders orders) {
                BigDecimal a1 = new BigDecimal(Float.toString(orders.TotalPrices()));
                BigDecimal a2 = new BigDecimal(Float.toString(orders.YHJJM));
                mOnlinePay = a1.subtract(a2).floatValue();
                holder.setText(R.id.tv_goods_business, orders.BN) //商家名称
                        .setText(R.id.tv_items_prices, "￥" + mOnlinePay);//小计

                //运费描述
                TextView tv_goods_freight_desc = holder.getView(R.id.tv_goods_freight_desc);//运费描述
                if (orders.ISP) {
                    holder.setText(R.id.tv_goods_freight, "￥" + orders.PE);//运费
                    if (orders.ISPH) {
                        tv_goods_freight_desc.setVisibility(View.VISIBLE);
                        holder.setText(R.id.tv_goods_freight_desc, "购满" + orders.MPE + "元，享免运费服务");
                    } else {
                        tv_goods_freight_desc.setVisibility(View.GONE);
                    }
                } else {
                    holder.setText(R.id.tv_goods_freight, "￥" + orders.PE);//运费
                    tv_goods_freight_desc.setVisibility(View.GONE);
                }

                //给商家的留言
                EditText et_order_message = holder.getView(R.id.et_order_message);
                et_order_message.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        orders.Message = editable.toString().trim();
                    }
                });

                //商品子列表
                ListView lv_shopping_carts_goods = holder.getView(R.id.lv_orders_goods);
                List<RS> rss = orders.RS;
                lv_shopping_carts_goods.setAdapter(new ListViewAdapter<RS>(this.mContext, R.layout.item_order_item, rss) {
                    @Override
                    public void convert(ViewHolder holder, final RS rs) {
                        //商家名称
                        holder.setText(R.id.tv_goods_title, rs.GN)
                                .setText(R.id.tv_goods_stock, rs.GGN)
                                .setText(R.id.tv_goods_price, "￥" + rs.GP)
                                .setText(R.id.tv_goods_numbers, String.valueOf(rs.GC));
                        //商品图片
                        ImageView image = holder.getView(R.id.iv_goods_image);
                        if (!TextUtils.isEmpty(rs.GI)) {
                            ImageLoader.getInstance().displayImage(services.getImageUrl(rs.GI), image, imageOptions);
                        } else {
                            image.setImageResource(R.drawable.default_goods);
                        }
                    }
                });
                Utility.setListViewHeightBasedOnChildren(lv_shopping_carts_goods);
                // -----------------------------------
                if (orders.ISYHJ.equals(false)) {
                    orders.YHJID = "";
                }
                // 是否使用优惠劵 点击选择优惠劵
                LinearLayout llCoupon = holder.getView(R.id.ll_me_coupon);
                llCoupon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Order_Confirm.this, ChooseCoupon.class);
                        intent.putExtra("SUB_ORDERS", (Serializable) mSubOrders);
                        intent.putExtra("ORDERS_POSITION", holder.getPosition());
                        intent.putExtra("IS_FROM_GOODSDETAILS", "false");
                        intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                        intent.putExtra("GOODS", mGoods);
                        intent.putExtra("PAGE_TITLE", mTitle);
                        intent.putExtra("URL", mUrl);
                        intent.putExtra("CID", mCID);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                        Order_Confirm.this.finish();
                    }
                });
//                if (Valid.isNotNullOrEmpty(mIsFromChooseCoupon)) {
//                    holder.setText(R.id.tv_goods_coupon, "优惠劵减免" + mYhjJm + "元");
//                } else {
                holder.setText(R.id.tv_goods_coupon, "优惠劵减免" + orders.YHJJM + "元");
//                }
//                } else {
//                    holder.setText(R.id.tv_goods_coupon, "优惠劵减免" + orders.YHJJM + "元");
//                }
//                ---------------------------------------
            }

        });
        Utility.setListViewHeightBasedOnChildren(lv_order_details);

        //订单总数量和总金额
        tv_goods_numbers = (TextView) findViewById(R.id.tv_goods_numbers);
        tv_goods_prices = (TextView) findViewById(R.id.tv_goods_prices);
        Integer totalNumbers = 0;
        for (SubOrders so : mSubOrders) {
            totalNumbers += so.TotalNumbers();
            totalPrices = totalPrices.add(new BigDecimal(so.TotalPrices().toString()));
            totalYhjjm = totalYhjjm.add(new BigDecimal(so.TotalYhjjm().toString()));
        }
        tv_goods_numbers.setText(String.valueOf(totalNumbers));
        mOnlinePay = totalPrices.subtract(totalYhjjm).floatValue();
        tv_goods_prices.setText("￥" + String.valueOf(mOnlinePay));
        //确认订单按钮
        btn_goods_balance = (Button) findViewById(R.id.btn_goods_balance);
        //初始化收货地址
        Addresses();
        initEvent();
    }

    //获取收货地址列表
    public void Addresses() {
        // 请求的URL
        String url = Services.mHost + "DeliveryAddress/APP_GetAddressSimpleList?ResidentID=%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId());
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
                        Log.d("asdsdasd11111111111", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<AddressSimple>>() {
                                    }.getType();
                                    mAddress = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (mAddress != null) {
                                        for (AddressSimple as : mAddress) {
                                            if (as.ISD) {
                                                mCurrentAddress = as;
                                                bindingAddress();
                                            }else{
                                                mCurrentAddress = mAddress.get(0);
                                                bindingAddress();
                                            }
                                        }
                                    } else {
                                        showToast(R.string.mall_not_goods_address);
                                        try {
                                            gotoActivityAndFinish(AddressEdit.class.getName(), null);
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //绑定收货地址信息
    private void bindingAddress() {
        tv_address_title.setText(mCurrentAddress.AD);
        tv_address_zipcode.setText(mCurrentAddress.ZC);
        tv_address_name.setText(mCurrentAddress.NP);
    }

    public void initEvent() {
        btn_back.setOnClickListener(this);
        ll_order_address_select.setOnClickListener(this);
        btn_goods_balance.setOnClickListener(this);
    }

    // 点击事件处理
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back:
                    if (mIsFromGoodsDetails) {
//                        Toast.makeText(this, "dd", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Order_Confirm.this, Goods_Details.class);
                        intent.putExtra("GOODS", mGoods);
                        intent.putExtra("PAGE_TITLE", mTitle);
                        intent.putExtra("URL", mUrl);
                        intent.putExtra("CID", mCID);
                        intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
                        this.finish();
                    } else if (mFromClassName.equals(ShopStore.class.getName()) || mFromClassName.equals(StoreGoods.class.getName())
                            || mFromClassName.equals(GoodsList.class.getName())) {
                        finish();
                    } else {
//                        Toast.makeText(this, "aa" + mFromClassName, Toast.LENGTH_LONG).show();
                        //返回购物车或者商品详细页？mFromClassName
                        gotoActivityAndFinish(Shopping_Carts.class.getName(), null);
                    }
                    break;
                case R.id.iv_order_address_select://选择地址
                    if (mCurrentAddress == null) {
                        showToast("请设置默认地址");
                        HashMap<String, String> extras = new HashMap<String, String>();
                        extras.put("FROM_ORDER_CONFIRM", "true");
                        extras.put("LEFT", "LEFT");
                        try {
                            gotoActivity(AddressEdit.class.getName(), extras);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        new DialogAddress(Order_Confirm.this, mCurrentAddress.ID, new AddressSelect()).show();
                    }

                    break;
                case R.id.btn_goods_balance://提交订单
                    if (mCurrentAddress == null) {
                        showToast(getResources().getString(R.string.mall_not_goods_address));
                    } else {
                        if (mPayInformation == null) {
                            //提交订单
                            OrderSubmitNew(mSubOrders, mCurrentAddress.ID);
                        } else {
                            Intent intent = new Intent(this, Pay.class);
                            intent.putExtra("ORDER_PAY", mPayInformation);
                            intent.putExtra("SUBJECT", mSubject);
                            intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                            intent.putExtra("DESCRIPTION", mDescription);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                        }
                    }

                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mIsFromGoodsDetails) {
//                        Toast.makeText(this, "dd", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Order_Confirm.this, Goods_Details.class);
                intent.putExtra("GOODS", mGoods);
                intent.putExtra("PAGE_TITLE", mTitle);
                intent.putExtra("URL", mUrl);
                intent.putExtra("CID", mCID);
                intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_left);
                this.finish();
            } else if (mFromClassName.equals(ShopStore.class.getName()) || mFromClassName.equals(StoreGoods.class.getName())
                    || mFromClassName.equals(GoodsList.class.getName())) {
                finish();
            } else {
//                        Toast.makeText(this, "aa" + mFromClassName, Toast.LENGTH_LONG).show();
                //返回购物车或者商品详细页？mFromClassName
                try {
                    gotoActivityAndFinish(Shopping_Carts.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }

    //提交订单
//    POST BOrder/APP_SubOrder_Two_Post_New
//    提交订单 返回去支付数据2.0(post) (新 优惠卷) 返回APPResult 对象 JSON:_APP_Return_PayOrderInfo。
    public void OrderSubmitNew(List<SubOrders> orderses, String addressID) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        try {
            // 请求的URL
            String url = Services.mHost + "BOrder/APP_SubOrder_Two_Post_New";
            JSONArray array = new JSONArray();
            for (SubOrders orders : orderses) {
                JSONObject subOrderInfos = new JSONObject();
                //构造订单详细信息
                subOrderInfos.put("BID", orders.BID);
                subOrderInfos.put("MS", orders.Message);
                subOrderInfos.put("ISYHJ", orders.ISYHJ);
                subOrderInfos.put("YHJID", orders.YHJID);
                subOrderInfos.put("UID", UserInformation.getUserInfo().UserId);
                //订单中商品的详细信息
                JSONArray goodsArray = new JSONArray();
                for (RS s : orders.RS) {
                    JSONObject goodsInfos = new JSONObject();
                    goodsInfos.put("GID", s.GID);
                    goodsInfos.put("GGID", s.GGID);
                    goodsInfos.put("SID", s.SID);
                    goodsInfos.put("N", s.GC);
                    goodsInfos.put("GIM", "");
                    goodsArray.put(goodsInfos);
                }
                //添加商品详细信息到订单中
                subOrderInfos.put("SD", goodsArray);
                array.put(subOrderInfos);
            }
            HashMap<String, String> extras = new HashMap<>();
            extras.put("AddressID", addressID);
            extras.put("ResidentID", UserInformation.getUserInfo
                    ().getUserId());
            extras.put("str",array.toString());
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
                    .addParams("ResidentID", UserInformation.getUserInfo().getUserId())
                    .addParams("AddressID", addressID)
                    .build()
                    .execute(new DataCallBack(this) {
                        @Override
                        public void onError(Call call, Exception e, int i) {
                            super.onError(call,e,i);
                        }

                        @Override
                        public void onResponse(String s, int i) {
                            Log.d("asdsdasd123123", "111111111" + s);
                            try {
                                JSONObject json = new JSONObject(s);
                                JSONObject jsonObject = new JSONObject(json.getString("Data"));
                                if (json.getBoolean("Status")) {
                                    if (jsonObject.getBoolean("Valid")) {
                                        Gson gson = new Gson();
                                        mPayInformation = gson.fromJson(jsonObject.getString("Obj"),OrderPay.class);
                                        //跳转到支付页面
                                        //-----TEST AMOUNT END-----
                                        mSubject = getString(R.string.common_me_company);
                                        mDescription = "购买商品总价：" + totalPrices.floatValue() + "元";
                                        Intent intent = new Intent(Order_Confirm.this, Pay.class);

                                        intent.putExtra("SUBJECT", mSubject);
//                                        intent.putExtra("ORDER_PAY", mPayInformation);
                                        intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                                        intent.putExtra("totalPrices", totalPrices.floatValue());
                                        intent.putExtra("DESCRIPTION", mDescription);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("ORDER_PAY", mPayInformation);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                                    } else {
                                        showToast(R.string.you_submit);
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

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (chk_pay_type_checked.isChecked() || mUnionPayTypeChecked.isChecked()) {
//            btn_goods_balance.setEnabled(true);
//        } else {
//            btn_goods_balance.setEnabled(false);
//        }
//    }

    class AddressSelect implements DialogAddress.OnAddressDialogListener {
        @Override
        public void Confirm(String addressId) {
            for (AddressSimple as : mAddress) {
                if (as.ID.equals(addressId)) {
                    as.IsChecked = true;
                    mCurrentAddress = as;
                    bindingAddress();
                } else {
                    as.IsChecked = false;
                }
            }
        }
    }


    //支付按钮点击事件
//    private void onPay() {
//        //检查支付配置信息
//        if (TextUtils.isEmpty(keys.PARTNER) || TextUtils.isEmpty(keys.RSA_PRIVATE) || TextUtils.isEmpty(keys.SELLER)) {
//            new AlertDialog.Builder(this)
//                    .setTitle("警告")
//                    .setMessage("需要配置[PARTNER | RSA_PRIVATE | SELLER]")
//                    .setPositiveButton(R.string.sure_information,
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialoginterface, int i) {
//                                    finish();
//                                }
//                            }).show();
//            return;
//        }
//
//        // 订单
//        String orderInfo = getOrderInfo();
//        // 对订单做RSA 签名
//        String sign = SignUtils.sign(orderInfo, keys.RSA_PRIVATE);
//        try {
//            // 仅需对sign 做URL编码
//            sign = URLEncoder.encode(sign, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        // 完整的符合支付宝参数规范的订单信息
//        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&sign_type=\"RSA\"";
//
//        //异步调用支付接口
//        Runnable payRunnable = new Runnable() {
//
//            @Override
//            public void run() {
//                // 构造PayTask 对象
//                PayTask alipay = new PayTask(Order_Confirm.this);
//                // 调用支付接口，获取支付结果
//                String result = alipay.pay(payInfo);
//                Message msg = new Message();
//                msg.what = SDK_PAY_FLAG;
//                msg.obj = result;
//                mHandler.sendMessage(msg);
//            }
//        };
//        // 必须异步调用
//        Thread payThread = new Thread(payRunnable);
//        payThread.start();
//    }
//
//    //订单信息
//    private String getOrderInfo() {
//        // 签约合作者身份ID
//        String orderInfo = "partner=" + "\"" + keys.PARTNER + "\"";
//        // 签约卖家支付宝账号
//        orderInfo += "&seller_id=" + "\"" + keys.SELLER + "\"";
//        // 商户网站唯一订单号
//        orderInfo += "&out_trade_no=" + "\"" + mPayInformation.OrderPayNumber + "\"";
//        // 商品名称
//        orderInfo += "&subject=" + "\"" + mSubject + "\"";
//        // 商品详情
//        orderInfo += "&body=" + "\"" + mDescription + "\"";
//        // 商品金额
//        orderInfo += "&total_fee=" + "\"" + mPayInformation.Amount + "\"";
//        // 服务器异步通知页面路径
//        orderInfo += "&notify_url=" + "\"" + services.getPayCallBackByTaobao() + "\"";
//        // 服务接口名称， 固定值
//        orderInfo += "&service=\"mobile.securitypay.pay\"";
//        // 支付类型， 固定值
//        orderInfo += "&payment_type=\"1\"";
//        // 参数编码， 固定值
//        orderInfo += "&_input_charset=\"utf-8\"";
//        // 设置未付款交易的超时时间
//        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
//        // 取值范围：1m～15d。
//        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
//        // 该参数数值不接受小数点，如1.5h，可转换为90m。
//        orderInfo += "&it_b_pay=\"30m\"";
//        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
//        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";
//        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
//        orderInfo += "&return_url=\"m.alipay.com\"";
//        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
//        // orderInfo += "&paymethod=\"expressGateway\"";
//        return orderInfo;
//    }
//
//    //异步支付完成后接收支付结果
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case SDK_PAY_FLAG: {
//                    PayResult payResult = new PayResult((String) msg.obj);
//                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
//                    String resultInfo = payResult.getResult();
//                    String resultStatus = payResult.getResultStatus();
//                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
//                    if (TextUtils.equals(resultStatus, "9000")) {
//                        Toast.makeText(Order_Confirm.this, "支付成功", Toast.LENGTH_SHORT).show();
//                        try {
//                            gotoActivityAndFinish(Orders.class.getName(), null);
//                        } catch (ClassNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        // 判断resultStatus 为非“9000”则代表可能支付失败
//                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//                        if (TextUtils.equals(resultStatus, "8000")) {
//                            Toast.makeText(Order_Confirm.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
//                            try {
//                                gotoActivityAndFinish(Orders.class.getName(), null);
//                            } catch (ClassNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                            Toast.makeText(Order_Confirm.this, "支付失败", Toast.LENGTH_SHORT).show();
//                            btn_goods_balance.setText("重新支付");
//                        }
//                    }
//                    break;
//                }
//                case SDK_CHECK_FLAG: {
//                    Toast.makeText(Order_Confirm.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                default:
//                    break;
//            }
//        }
//    };
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*************************************************
         * 处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }
        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
//        Log.i("data", data.toString() + "");
        String str = data.getExtras().getString("pay_result");
//        Log.i("str", data.getExtras() + "");
//        Bundle s = data.getExtras();
        if (str.equalsIgnoreCase("success")) {
            // 支付成功后，extra中如果存在result_data，取出校验
            // result_data结构见c）result_data参数说明
//            if (data.hasExtra("result_data")) {
//                String result = data.getExtras().getString("result_data");
//                try {
//                    JSONObject resultJson = new JSONObject(result);
//                    String sign = resultJson.getString("sign");
//                    String dataOrg = resultJson.getString("data");
//                    // 验签证书同后台验签证书
//                    // 此处的verify，商户需送去商户后台做验签
//                    boolean ret = RSAUtil.verify(dataOrg, sign, serverMode);
//                    if (ret) {
//                        // 验证通过后，显示支付结果
//                        msg = "支付成功！";
//                    } else {
//                        // 验证不通过后的处理
//                        // 建议通过商户后台查询支付结果
//                        msg = "支付失败！";
//                    }
//                } catch (JSONException e) {
//                }
//            } else {
            // 未收到签名信息
            // 建议通过商户后台查询支付结果
            msg = "支付成功！";

        } else if (str.equalsIgnoreCase("fail")) {
            msg = "支付失败！";
        } else if (str.equalsIgnoreCase("cancel")) {
            msg = "用户取消了支付";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支付结果通知");
        builder.setMessage(msg);
        builder.setInverseBackgroundForced(true);
//         builder.setCustomTitle();
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}
