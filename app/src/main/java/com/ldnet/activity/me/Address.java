package com.ldnet.activity.me;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.AddressSimple;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.third.SwipeListView.BaseSwipeListViewListener;
import com.third.SwipeListView.SwipeListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Address extends BaseActionBarActivity {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private ImageButton btn_address_create;
    private Services services;

    private ListViewAdapter mAdapter;
    private List<AddressSimple> mDatas;
    private SwipeListView slv_me_address;
    private TextView mAddressEmpty;

    private List<AddressSimple> address;
    private List<AddressSimple> ass;

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_address);
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_me_address);
//
        //回退按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
//
        //新增地址
        btn_address_create = (ImageButton) findViewById(R.id.btn_custom);
        btn_address_create.setImageResource(R.drawable.plus);
        btn_address_create.setVisibility(View.VISIBLE);
        //地址为空
        mAddressEmpty = (TextView) findViewById(R.id.address_empty);
        //来自提交订单的标记
//        fromOrderConfirm = getIntent().getStringExtra("FROM_ORDER_CONFIRM");
//        if (Valid.isNotNullOrEmpty(fromOrderConfirm)) {
//            mFromOrderConfirm = Boolean.valueOf(fromOrderConfirm);
//        }
        //初始化服务
        services = new Services();
        mDatas = new ArrayList<AddressSimple>();
        Addresses("0");
        //
        slv_me_address = (SwipeListView) findViewById(R.id.slv_me_address);


        slv_me_address.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                super.onClickFrontView(position);
//                slv_me_address.closeOpenedItems();
                AddressSimple address = mDatas.get(position);
                HashMap<String, String> extras = new HashMap<String, String>();
                extras.put("ADDRESS_ID", address.ID);
                try {
                    gotoActivityAndFinish(AddressEdit.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        slv_me_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                slv_me_address.closeOpenedItems();
            }
        });

        initEvent();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_address_create.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back://判断是否返回主页
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                    break;
                case R.id.btn_custom://编辑地址
                    gotoActivityAndFinish(AddressEdit.class.getName(), null);
                    break;
                default:
                    break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //获取收货地址列表
    public void Addresses(final String type1) {
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
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog1();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call,e,i);
                        closeProgressDialog1();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        Log.d("asdsdasd", "111111111" + s);
                        closeProgressDialog1();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    if(jsonObject.getString("Obj").equals("[]")){
                                        showToast("请先添加收货地址");
                                        return;
                                    }

                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<AddressSimple>>() {
                                    }.getType();
                                    if("1".equals(type1)){
                                        ass = gson.fromJson(jsonObject.getString("Obj"), type);
                                        if (ass != null) {
                                            mDatas.clear();
                                            mDatas.addAll(ass);
                                        }
                                    }else {
                                        address = gson.fromJson(jsonObject.getString("Obj"), type);
                                        if (address != null && address.size() > 0) {
                                            mDatas.addAll(address);
                                        } else {
                                            mAddressEmpty.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    mAdapter = new ListViewAdapter<AddressSimple>(Address.this, R.layout.item_me_address, mDatas) {
                                        @Override
                                        public void convert(ViewHolder holder, final AddressSimple addressSimple) {
                                            holder.setText(R.id.tv_me_address_title, addressSimple.AD)
                                                    .setText(R.id.tv_me_address_zipcode, addressSimple.ZC)
                                                    .setText(R.id.tv_me_address_name, addressSimple.NP);
                                            //设置默认
                                            TextView btn_address_default = holder.getView(R.id.btn_address_default);
                                            TextView btn_delete = holder.getView(R.id.btn_delete);
                                            TextView tv_me_address_title = holder.getView(R.id.tv_me_address_title);
                                            if (addressSimple.ISD) {
                                                tv_me_address_title.setTextColor(getResources().getColor(R.color.green));
                                                btn_address_default.setEnabled(false);
                                            } else {
                                                tv_me_address_title.setTextColor(getResources().getColor(R.color.gray_deep));
                                            }
                                            //设置默认
                                            btn_address_default.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Log.d("aaaaaaaaaaaa","1111111111");
                                                    AddressDefault(addressSimple.ID);
                                                }
                                            });

                                            //删除地址
                                            btn_delete.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AddressDelete(addressSimple.ID,addressSimple);
                                                }
                                            });
                                        }
                                    };
                                    slv_me_address.setAdapter(mAdapter);
                                    mAdapter.notifyDataSetChanged();
                                    //关闭打开的item
                                    slv_me_address.closeOpenedItems();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //设置为默认收货地址
    public void AddressDefault(String id) {
        // 请求的URL
        String url = Services.mHost + "DeliveryAddress/APP_SetDefaultAddress?AddressID=%s&ResidentID=%s";
        url = String.format(url, id, UserInformation.getUserInfo().UserId);
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
                                    Addresses("1");
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //删除收货地址
    public void AddressDelete(String id,final AddressSimple addressSimple) {
        // 请求的URL
        String url = Services.mHost + "DeliveryAddress/APP_DelAddress?AddressID=%s";
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
                                    mDatas.remove(addressSimple);
                                    mAdapter.notifyDataSetChanged();
                                    slv_me_address.closeOpenedItems();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
