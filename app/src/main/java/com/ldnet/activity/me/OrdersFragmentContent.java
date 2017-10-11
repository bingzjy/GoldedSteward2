package com.ldnet.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseFragment;
import com.ldnet.activity.mall.Goods_Details;
import com.ldnet.activity.mall.Order_Details;
import com.ldnet.activity.mall.Pay;
import com.ldnet.entities.Goods;
import com.ldnet.entities.OD;
import com.ldnet.entities.OrderPay;
import com.ldnet.entities.Orders;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.third.listviewshangxia.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *
 */
public class OrdersFragmentContent extends BaseFragment implements View.OnClickListener {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;

    //订单状态
    private RadioGroup rdg_orders_tabs;
    private LinearLayout ll_goods_balance;
    private TextView tv_goods_prices;
    private Button btn_goods_balance;

    //订单列表
    private List<Orders> mDatas;
    private ListViewAdapter<Orders> mAdapter;
    private MyListView lv_mall_orders;
    private Integer mCurrentTypeId = 1;
    private Integer mCurrentPageIndex = 1;

    //订单子状态ID 1:待付款，3:已发货，4:已签收，5:待发货，6:已关闭 ，7：已取消 有取消原因CM
    private static final Integer ORDERS_STATUS_NOTPAY = 1;
    private static final Integer ORDERS_STATUS_SENDED = 3;
    private static final Integer ORDERS_STATUS_SUCCESS = 4;
    private static final Integer ORDERS_STATUS_NOTSENDED = 5;
    private static final Integer ORDERS_STATUS_CLOSE = 6;
    private static final Integer ORDERS_STATUS_CANCEL = 7;

    // 支付信息，包含订单号和金额
    private OrderPay mPayInformation;
    // 商品标题
    private String mSubject;
    // 商品描述
    private String mDescription;
    private TextView mOrderEmpty;
    private Handler mHandler;
    private String mFromClassName;
    private List<Orders> datas;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    protected DisplayImageOptions imageOptions;
    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";

    public static Fragment getInstance(Bundle bundle) {
        OrdersFragmentContent fragment = new OrdersFragmentContent();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
                .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .extraForDownloader(UserInformation.getUserInfo().UserPhone + "," + aa + "," + aa1)
                .build();
        return inflater.inflate(R.layout.fragmnet_order, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }


    private void initView(View view) {
        services = new Services();
        mHandler = new Handler();
        //订单为空
        mOrderEmpty = (TextView) view.findViewById(R.id.order_empty);
        String title = getArguments().getString("title");
        if (title != null) {
            if (title.equals("待付款")) {
                mOrderEmpty.setText("您没有未支付的订单！");
                mCurrentTypeId = ORDERS_STATUS_NOTPAY;
            } else if (title.equals("待发货")) {
                mOrderEmpty.setText("您没有未发货的订单！");
                mCurrentTypeId = ORDERS_STATUS_NOTSENDED;
            } else if (title.equals("待收货")) {
                mOrderEmpty.setText("您没有未收货的订单！");
                mCurrentTypeId = ORDERS_STATUS_SENDED;
            } else if (title.equals("已完成")) {
                mOrderEmpty.setText("您没有已完成的订单！");
                mCurrentTypeId = ORDERS_STATUS_SUCCESS;
            } else if (title.equals("已取消")) {
                mOrderEmpty.setText("您没有已取消的订单！");
                mCurrentTypeId = ORDERS_STATUS_CANCEL;
            }
        }
        ll_goods_balance = (LinearLayout) view.findViewById(R.id.ll_goods_balance);
        tv_goods_prices = (TextView) view.findViewById(R.id.tv_goods_prices);
        btn_goods_balance = (Button) view.findViewById(R.id.btn_goods_balance);

        //订单列表

        mPullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(getActivity()));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(getActivity()));
        mDatas = new ArrayList<Orders>();
        lv_mall_orders = (MyListView) view.findViewById(R.id.lv_mall_orders);
        lv_mall_orders.setFocusable(false);
        mCurrentPageIndex = 1;//重置为第一页
        Orders(mCurrentTypeId, mCurrentPageIndex);
        mCurrentPageIndex++;//加载下一页

        mAdapter = new ListViewAdapter<Orders>(getActivity(), R.layout.item_orders, mDatas) {
            @Override
            public void convert(ViewHolder holder, final Orders orders) {
                holder.setText(R.id.tv_goods_business, orders.BN)
                        .setText(R.id.tv_items_prices, String.valueOf(orders.AM));

                CheckBox chkbox = holder.getView(R.id.chk_goods_checked);
                if (mCurrentTypeId.equals(ORDERS_STATUS_NOTPAY)) {
                    chkbox.setChecked(orders.IsChecked);
                    chkbox.setVisibility(View.VISIBLE);
                    chkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            orders.IsChecked = b;
                            updateBlanceStatus();
                        }
                    });
                } else {
                    chkbox.setVisibility(View.GONE);
                }
                // 增加投诉
                final Button complain = holder.getView(R.id.btn_orders_complain);
                complain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), Complain.class);
                        intent.putExtra("ORDER_ID", orders.OID);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                });
                Button delCancel = holder.getView(R.id.btn_orders_delete_cancel);
                Button details = holder.getView(R.id.btn_orders_details);
                if (mCurrentTypeId.equals(ORDERS_STATUS_NOTPAY)) {
                    delCancel.setText("取消订单");
                    delCancel.setVisibility(View.VISIBLE);
                    complain.setVisibility(View.GONE);
                } else if (mCurrentTypeId.equals(ORDERS_STATUS_SENDED)) {
                    delCancel.setText("确认收货");
                    delCancel.setVisibility(View.VISIBLE);
                } else if (mCurrentTypeId.equals(ORDERS_STATUS_SUCCESS)) {
                    delCancel.setText("删除订单");
                    delCancel.setVisibility(View.VISIBLE);
                } else {
                    delCancel.setVisibility(View.GONE);
                }

                //取消 或 删除订单
                delCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mCurrentTypeId.equals(ORDERS_STATUS_NOTPAY)) {
                            OrderCancel(orders.OID);
                        } else if (mCurrentTypeId.equals(ORDERS_STATUS_SENDED)) {
                            ReceiveComfirm(orders.OID);
                        } else if (mCurrentTypeId.equals(ORDERS_STATUS_SUCCESS)) {
                            OrderDelete(orders.OID);
                        } else {
                            return;
                        }
                    }
                });

                //订单详细页
                details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), Order_Details.class);
                        intent.putExtra("ORDER_ID", orders.OID);
                        intent.putExtra("GOODS_ID", orders.getOD().get(0).getGID());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                });

                MyListView listView = holder.getView(R.id.lv_orders_goods);
                listView.setAdapter(new ListViewAdapter<OD>(this.mContext, R.layout.item_orders_item, orders.OD) {
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
        };
        lv_mall_orders.setAdapter(mAdapter);
        Utility.setListViewHeightBasedOnChildren(lv_mall_orders);
        btn_goods_balance.setOnClickListener(this);
        initEvents();
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                mCurrentPageIndex = 1;//重置为第一页
                Orders(mCurrentTypeId, mCurrentPageIndex);
                mCurrentPageIndex++;//加载下一页
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    Orders(mCurrentTypeId, mCurrentPageIndex);
                    mCurrentPageIndex++;//加载下一页
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_goods_balance:
                String orderIds = "";
                for (Orders orders : mDatas) {
                    if (orders.IsChecked) {
                        if (!TextUtils.isEmpty(orderIds)) {
                            orderIds += "," + orders.OID;
                        } else {
                            orderIds += orders.OID;
                        }
                    }
                }
                //根据订单id获取订单具体信息
                OrderPayInformation(orderIds);
                break;
        }
    }

    private void updateBlanceStatus() {
        mAdapter.notifyDataSetChanged();
        if (mCurrentTypeId.equals(ORDERS_STATUS_NOTPAY)) {
            BigDecimal totalPrices = new BigDecimal("0.00");
            for (Orders orders : mDatas) {
                if (orders.IsChecked) {
                    totalPrices = totalPrices.add(new BigDecimal(orders.AM.toString()));
                }
            }
            if (totalPrices.floatValue() == 0.00f) {
                ll_goods_balance.setVisibility(View.GONE);
                tv_goods_prices.setText("￥" + totalPrices.floatValue());
            } else {
                ll_goods_balance.setVisibility(View.VISIBLE);
                tv_goods_prices.setText("￥" + totalPrices.floatValue());
            }
        } else {
            ll_goods_balance.setVisibility(View.GONE);
        }
    }

    //取消订单
    //BOrder/APP_CancelOrder?OrderID={OrderID}
    public void OrderCancel(String orderID) {
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_CancelOrder?OrderID=%s";
        url = String.format(url, orderID);
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
                .execute(new DataCallBack(getActivity()) {
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
                                    mDatas.clear();
                                    mCurrentPageIndex = 1;//重置为第一页
                                    Orders(mCurrentTypeId, mCurrentPageIndex);
                                    mCurrentPageIndex++;//加载下一页
                                    showToast("取消成功");
//                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    showToast("取消失败");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //确认收货
    //BOrder/APP_ConfirmReceive?OrderID={OrderID}
    public void ReceiveComfirm(String orderID) {
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_ConfirmReceive?OrderID=%s";
        url = String.format(url, orderID);
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
                .execute(new DataCallBack(getActivity()) {
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
                                    mDatas.clear();
                                    mCurrentPageIndex = 1;//重置为第一页
                                    Orders(mCurrentTypeId, mCurrentPageIndex);
                                    mCurrentPageIndex++;//加载下一页
                                    showToast("订单已完成");
//                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    showToast("收货失败，请重试");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //删除订单
    //BOrder/APP_DeleteOrder?OrderID={OrderID}
    public void OrderDelete(String orderID) {
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_DeleteOrder?OrderID=%s";
        url = String.format(url, orderID);
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
                .execute(new DataCallBack(getActivity()) {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog1();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog1();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd", "111111111" + s);
                        closeProgressDialog1();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    mDatas.clear();
                                    mCurrentPageIndex = 1;//重置为第一页
                                    Orders(mCurrentTypeId, mCurrentPageIndex);
                                    mCurrentPageIndex++;//加载下一页
                                    showToast("删除成功");
//                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    showToast("删除失败");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
                .execute(new DataCallBack(getActivity()) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd23123124", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    mPayInformation = gson.fromJson(jsonObject.getString("Obj"), OrderPay.class);

                                    mSubject = getString(R.string.common_me_company);
                                    mDescription = "购买商品总价：" + mPayInformation.Amount + "元";
                                    Intent intent = new Intent(getActivity(), Pay.class);
                                    intent.putExtra("ORDER_PAY", mPayInformation);
                                    intent.putExtra("SUBJECT", mSubject);
                                    intent.putExtra("FROM_CLASS_NAME", mFromClassName);
                                    intent.putExtra("DESCRIPTION", mDescription);
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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

    //获取订单列表
    //BOrder/APP_GetOrderList?ResidentID={ResidentID}&ViceType={ViceType}&PageCnt={PageCnt}&PageIndex={PageIndex}
    public void Orders(Integer viceType, Integer pageIndex) {
        // 请求的URL
        String url = Services.mHost + "BOrder/APP_GetOrderList?ResidentID=%s&ViceType=%s&PageCnt=%s&PageIndex=%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId(), viceType, Services.PAGE_SIZE, pageIndex);
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
                .execute(new DataCallBack(getActivity()) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        Log.d("asdsdasd12312312111111111", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Orders>>() {
                                    }.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (datas != null && datas.size() > 0) {
                                        mDatas.addAll(datas);
                                        Log.d("aaaaaaaaaaaaaaa", "111111111");
                                    } else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            mOrderEmpty.setVisibility(View.VISIBLE);
                                        }
                                        Log.d("aaaaaaaaaaaaaaa", "2222222222222222");
                                    }
                                    updateBlanceStatus();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
