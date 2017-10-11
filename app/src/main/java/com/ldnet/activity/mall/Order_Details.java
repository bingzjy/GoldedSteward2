package com.ldnet.activity.mall;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ldnet.activity.Browser;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Goods;
import com.ldnet.entities.Goods1;
import com.ldnet.entities.OD;
import com.ldnet.entities.OrderPay;
import com.ldnet.entities.Orders;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.ldnet.utility.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.Alipay.PayKeys;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by Alex on 2015/9/28.
 */
public class Order_Details extends BaseActionBarActivity {

    // 标题
    private TextView tv_page_title;
    // 返回
    private ImageButton btn_back;

    private TextView tv_orders_numbers;
    private TextView tv_orders_created;
    private TextView tv_orders_status;
    //    订单取消原因
    private TextView tv_order_cancel_reason;
    private TextView tv_address_title;
    private TextView tv_address_zipcode;
    private TextView tv_address_name;
    private ListView lv_order_details;
    private LinearLayout ll_goods_balance;
    private TextView tv_orders_prices;
    private Button btn_orders_balance;
    private TextView tv_business_name;
    private TextView tv_business_phone;
    private ImageButton ibtn_call_business;

    // 服务
    private Services services;
    // 获取订单详细
    private com.ldnet.entities.Orders mOrders;


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
    private String mFromClassName;
    private static final Integer ORDERS_STATUS_CANCEL = 7;
    private LinearLayout mLlOrderCancelReason;
    private Button mOrderQuery;
    private Goods goods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mall_order_details);
        // 标题
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.mall_order_details);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_orders_numbers = (TextView) findViewById(R.id.tv_orders_numbers);
        tv_orders_created = (TextView) findViewById(R.id.tv_orders_created);
        tv_orders_status = (TextView) findViewById(R.id.tv_orders_status);
        mLlOrderCancelReason = (LinearLayout) findViewById(R.id.ll_order_cancel);
        tv_order_cancel_reason = (TextView) findViewById(R.id.tv_orders_cancel);
        tv_address_title = (TextView) findViewById(R.id.tv_address_title);
        tv_address_zipcode = (TextView) findViewById(R.id.tv_address_zipcode);
        tv_address_name = (TextView) findViewById(R.id.tv_address_name);
        lv_order_details = (ListView) findViewById(R.id.lv_order_details);
        ll_goods_balance = (LinearLayout) findViewById(R.id.ll_goods_balance);
        tv_orders_prices = (TextView) findViewById(R.id.tv_orders_prices);
        btn_orders_balance = (Button) findViewById(R.id.btn_orders_balance);
        tv_business_name = (TextView) findViewById(R.id.tv_business_name);
        tv_business_phone = (TextView) findViewById(R.id.tv_business_phone);
        ibtn_call_business = (ImageButton) findViewById(R.id.ibtn_call_business);
        //订单查询
        mOrderQuery = (Button) findViewById(R.id.bt_orders_query);
        initData();
        initEvent();
    }

    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_orders_balance.setOnClickListener(this);
        ibtn_call_business.setOnClickListener(this);
        mOrderQuery.setOnClickListener(this);
    }

    //初始化数据
    public void initData() {
        //加载数据
        keys = new PayKeys();
        String orderId = getIntent().getStringExtra("ORDER_ID");
        String goodsId = getIntent().getStringExtra("GOODS_ID");
        //未支付订单时不显示订单查询
//        Integer currentTypeId = Integer.valueOf(getIntent().getStringExtra("CURRENT_TYPE_ID"));
//        if (currentTypeId == 1) {
//            mOrderQuery.setVisibility(View.GONE);
//        }
        services = new Services();
        OrderDetails(orderId);
        GetGoodsInfo(goodsId);
        Utility.setListViewHeightBasedOnChildren(lv_order_details);
    }

    public void GetGoodsInfo(String goodsId) {
        // 请求的URL
        String url = Services.mHost + "BGoods/App_GetGoodsInfo?GoodsID=%s";
        url = String.format(url, goodsId);
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
                                    Goods1 goods1 = gson.fromJson(jsonObject.getString("Obj"),Goods1.class);
                                    goods = new Goods();
                                    goods.setGID(goods1.getGID());
                                    goods.setGP(goods1.getGP());
                                    goods.setGSID(goods1.getGSID());
                                    goods.setGSN(goods1.getGSN());
                                    goods.setIMG(goods1.getIMG());
                                    goods.setRID(goods1.getRID());
                                    goods.setRP(goods1.getRP());
                                    goods.setSN(goods1.getSN());
                                    goods.setST(goods1.getST());
                                    goods.setT(goods1.getT());
                                    goods.setType(1);
                                    goods.setURL("http://b.goldwg.com//Goods/Preview?ID=" + goods1.getGID());
                                    lv_order_details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(Order_Details.this, Goods_Details.class);
                                            intent.putExtra("GOODS", goods);
                                            intent.putExtra("PAGE_TITLE", "");
                                            intent.putExtra("FROM_CLASS_NAME", Order_Details.class.getName());
                                            intent.putExtra("URL", goods.getURL());
                                            intent.putExtra("CID", goods.getGID());
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //获取订单详细
    //BOrder/APP_GetOrderInfo?OrderID={OrderID}&ResidentID={ResidentID}
    public void OrderDetails(String orderID) {
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_GetOrderInfo?OrderID=%s&ResidentID=%s";
        url = String.format(url, orderID, UserInformation.getUserInfo().UserId);
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
                                    mOrders = gson.fromJson(jsonObject.getString("Obj"), Orders.class);
                                    if (mOrders != null) {
                                        //订单信息
                                        tv_orders_numbers.setText(mOrders.ONB);
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        tv_orders_created.setText(Services.subStr(mOrders.PD));
                                        tv_orders_status.setText(mOrders.OVN);
                                        if (mOrders.OVID.equals(ORDERS_STATUS_CANCEL)) {
                                            mLlOrderCancelReason.setVisibility(View.VISIBLE);
                                            tv_order_cancel_reason.setText(mOrders.CM);
                                        }

                                        //收货地址
                                        if (TextUtils.isEmpty(mOrders.AAD)) {
                                            tv_address_title.setText(mOrders.AR + mOrders.ACT + mOrders.AA);
                                        } else {
                                            tv_address_title.setText(mOrders.AAD);
                                        }
                                        tv_address_zipcode.setVisibility(View.GONE);
                                        tv_address_name.setText(mOrders.AN + " " + mOrders.AMP);

                                        //商家信息
                                        tv_business_name.setText(mOrders.BN);
                                        tv_business_phone.setText(mOrders.BM);

                                        //是否需要支付
                                        if (mOrders.OVID.equals(1)) {
                                            ll_goods_balance.setVisibility(View.VISIBLE);
                                            //支付Banner
                                            tv_orders_prices.setText("￥" + mOrders.AM);
                                        } else {
                                            ll_goods_balance.setVisibility(View.GONE);
                                        }

                                        //订单详情
                                        lv_order_details.setAdapter(new ListViewAdapter<OD>(Order_Details.this, R.layout.item_orders_item, mOrders.OD) {
                                            @Override
                                            public void convert(ViewHolder holder, OD od) {
                                                //商品图片
                                                ImageView view = holder.getView(R.id.iv_goods_image);
                                                if (!TextUtils.isEmpty(od.GI)) {
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(od.GI), view, imageOptions);
                                                } else {
                                                    view.setImageResource(R.drawable.default_goods);
                                                }
                                                //商品信息
                                                holder.setText(R.id.tv_goods_title, od.GN)
                                                        .setText(R.id.tv_goods_stock, od.GTN)
                                                        .setText(R.id.tv_goods_price, "￥" + od.GP)
                                                        .setText(R.id.tv_goods_numbers, String.valueOf(od.N));
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // 点击事件处理
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_orders_balance://去支付
                OrderPayInformation(mOrders.OID);
                //-----TEST AMOUNT START-----
//                    mPayInformation.Amount = 0.01f;
                //-----TEST AMOUNT END-----
                break;
            case R.id.ibtn_call_business://拨打商家电话
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mOrders.BM));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                break;
            case R.id.bt_orders_query://订单查询
                if (mOrders != null) {
                    if (mOrders.ECode != null) {
                        String eCode = mOrders.ECode; //快递编码
                        String eNumber = mOrders.ENumber;//快递单号
                        Intent intentQuery = new Intent(this, Browser.class);
                        String url = "http://m.kuaidi100.com/index_all.html?type=" + eCode + "&postid=" + eNumber;
//                  String url = "http://m.kuaidi100.com/index_all.html?type=quanfengkuaidi&postid=123456";
                        intentQuery.putExtra("PAGE_URL", url);
                        intentQuery.putExtra("PAGE_TITLE", "快递查询");
                        intentQuery.putExtra("FROM_CLASS_NAME", Order_Details.class.getName());
                        startActivity(intentQuery);
                        overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                    } else {
                        showToast("暂无快递信息！");
                    }
                }
                break;
            default:
                break;
        }
    }

    //获取订单的支付信息
    //
    public void OrderPayInformation(String orderIDs) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_GetGoPayInfo_Post";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("IDS", orderIDs);
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
                .addParams("IDS", orderIDs)
                .build()
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
                                    mPayInformation = gson.fromJson(jsonObject.getString("Obj"), OrderPay.class);
                                    mSubject = getString(R.string.common_me_company);
                                    mDescription = "购买商品总价：" + mOrders.AM + "元";
                                    Intent intent = new Intent(Order_Details.this, Pay.class);
                                    intent.putExtra("ORDER_PAY", mPayInformation);
                                    intent.putExtra("SUBJECT", mSubject);
                                    intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                                    intent.putExtra("DESCRIPTION", mDescription);
//                        intent.putExtra("SUB_ORDERS", (Serializable) mSubOrders);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
//                        onPay();

                                } else {
                                    showToast(R.string.mall_pay_submit_failure);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
//                PayTask alipay = new PayTask(Order_Details.this);
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

//订单信息
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
//                        Toast.makeText(Order_Details.this, "支付成功", Toast.LENGTH_SHORT).show();
//                        initData();
//                    } else {
//                        // 判断resultStatus 为非“9000”则代表可能支付失败
//                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//                        if (TextUtils.equals(resultStatus, "8000")) {
//                            Toast.makeText(Order_Details.this, "支付结果确认中", Toast.LENGTH_SHORT).show();
//                            initData();
//                        } else {
//                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                            Toast.makeText(Order_Details.this, "支付失败", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    break;
//                }
//                case SDK_CHECK_FLAG: {
//                    Toast.makeText(Order_Details.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                default:
//                    break;
//            }
//        }
//    };
}
