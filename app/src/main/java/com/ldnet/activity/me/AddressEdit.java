package com.ldnet.activity.me;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Areas;
import com.ldnet.entities.HouseRent;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddressEdit extends BaseActionBarActivity {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private EditText et_address_contract;
    private EditText et_address_area_code;
    private EditText et_address_telephone;
    private EditText et_address_mobile;
    private EditText et_address_zipcode;
    private EditText et_address_details;
    private Button btn_address_confirm;
    private CheckBox chk_address_default;

    private com.ldnet.entities.Address mAddress;
    private Spinner sr_address_provinces;
    private Spinner sr_address_cities;
    private Spinner sr_address_areas;
    private List<Areas> mProvinces;
    private ArrayAdapter<Areas> mAdapter_Provinces;
    private List<Areas> mCities;
    private ArrayAdapter<Areas> mAdapter_Cities;
    private List<Areas> mAreas;
    private ArrayAdapter<Areas> mAdapter_Areas;
    private Boolean mFromOrderConfirm = false;

    private List<Areas> Areas;
    private List<Areas> Areas1;
    private List<Areas> Areas2;
    private  String addressId;
    private AddPopWindow menuWindow;

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_address_edit);
        AppUtils.setupUI(findViewById(R.id.ll_address),this);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_me_address);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_address_confirm = (Button) findViewById(R.id.btn_address_confirm);
        //来自提交订单的标记
        String fromOrderConfirm = getIntent().getStringExtra("FROM_ORDER_CONFIRM");
        if (!TextUtils.isEmpty(fromOrderConfirm)) {
            mFromOrderConfirm = Boolean.valueOf(fromOrderConfirm);
        }
        et_address_contract = (EditText) findViewById(R.id.et_address_contract);
        et_address_area_code = (EditText) findViewById(R.id.et_address_area_code);
        et_address_telephone = (EditText) findViewById(R.id.et_address_telephone);
        et_address_mobile = (EditText) findViewById(R.id.et_address_mobile);
        et_address_zipcode = (EditText) findViewById(R.id.et_address_zipcode);
        //省
        sr_address_provinces = (Spinner) findViewById(R.id.sr_address_provinces);
        //市
        sr_address_cities = (Spinner) findViewById(R.id.sr_address_cities);
        //区
        sr_address_areas = (Spinner) findViewById(R.id.sr_address_areas);
        et_address_details = (EditText) findViewById(R.id.et_address_details);
        chk_address_default = (CheckBox) findViewById(R.id.chk_address_default);

//        mAdapter_Provinces.notifyDataSetChanged();
        //初始化实体
        addressId = getIntent().getStringExtra("ADDRESS_ID");

        Provinces();
        initEvent();
    }

    //数据校验
    TextWatcher formValid = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String contract = et_address_contract.getText().toString().trim();
            String aCode = et_address_area_code.getText().toString().trim();
            String telephone = et_address_telephone.getText().toString().trim();
            String mobile = et_address_mobile.getText().toString().trim();
            String zipcode = et_address_zipcode.getText().toString().trim();
            String details = et_address_details.getText().toString().trim();
            Integer pPosition = sr_address_provinces.getSelectedItemPosition();
            Integer cPosition = sr_address_cities.getSelectedItemPosition();
            Integer aPosition = sr_address_areas.getSelectedItemPosition();
            if (!TextUtils.isEmpty(contract) &&
                    //手机和固定电话至少留一个
                    (!TextUtils.isEmpty(mobile) && Services.isPhone(mobile) ||
                            !TextUtils.isEmpty(telephone) && !TextUtils.isEmpty(aCode)) &&
                    !TextUtils.isEmpty(zipcode) &&
                    !TextUtils.isEmpty(details) &&
                    pPosition != null && cPosition != null && aPosition != null &&
                    Services.isPhone(mobile)) {
                btn_address_confirm.setEnabled(true);
            } else {
                btn_address_confirm.setEnabled(false);
            }
        }
    };

    //初始化省、直辖市信息
    private void initProvinces() {
//        mProvinces = new ArrayList<Areas>();
        //将可选内容与ArrayAdapter连接起来
        mAdapter_Provinces = new ArrayAdapter(this, R.layout.dropdown_check_item, mProvinces);
        //设置下拉列表的风格
        mAdapter_Provinces.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_address_provinces.setAdapter(mAdapter_Provinces);
        //设置选择事件
        sr_address_provinces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //改变城市
                Areas areas = mProvinces.get(i);
                if (areas.Id != null) {
                    Cities(areas.Id, "1",mAddress);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    //初始化城市信息
    private void initCities() {
//        mCities = new ArrayList<Areas>();
        //将可选内容与ArrayAdapter连接起来
        mAdapter_Cities = new ArrayAdapter(this, R.layout.dropdown_check_item, mCities);
        //设置下拉列表的风格
        mAdapter_Cities.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_address_cities.setAdapter(mAdapter_Cities);
        //设置选择事件
        sr_address_cities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //改变区
                Areas areas = mCities.get(i);
                if (areas != null) {
                    Areas(areas.Id, "2",mAddress);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //初始化区域信息
    private void initAreas() {
//        mAreas = new ArrayList<Areas>();
        //将可选内容与ArrayAdapter连接起来
        mAdapter_Areas = new ArrayAdapter(this, R.layout.dropdown_check_item, mAreas);
        //设置下拉列表的风格
        mAdapter_Areas.setDropDownViewResource(R.layout.dropdown_item);
        //将adapter 添加到spinner中
        sr_address_areas.setAdapter(mAdapter_Areas);
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_address_confirm.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_back:
                    if (mFromOrderConfirm) {
                        finish();
                    } else {
                        gotoActivityAndFinish(Address.class.getName(), null);
                    }

                    break;
                case R.id.btn_address_confirm:
                    String contract = et_address_contract.getText().toString().trim();
                    String aCode = et_address_area_code.getText().toString().trim();
                    String telephone = et_address_telephone.getText().toString().trim();
                    String mobile = et_address_mobile.getText().toString().trim();
                    String zipcode = et_address_zipcode.getText().toString().trim();
                    String details = et_address_details.getText().toString().trim();
                    Integer pId = ((Areas) sr_address_provinces.getSelectedItem()).Id;
                    Integer cId = ((Areas) sr_address_cities.getSelectedItem()).Id;
                    Integer aId = ((Areas) sr_address_areas.getSelectedItem()).Id;
                    Boolean isDefault = chk_address_default.isChecked();
                    //更新、创建
                    if (mAddress != null) {
                        AddressesUpdate(mAddress.ID, contract, mobile, aCode, telephone, zipcode, isDefault, details, pId, cId, aId);
                    } else {
                        AddressInsert(contract, mobile, aCode, telephone, zipcode, isDefault, details, pId, cId, aId);
                    }
                    break;
                default:
                    break;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mFromOrderConfirm) {
                finish();
            } else {
                try {
                    gotoActivityAndFinish(Address.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }

    //获取所有省、直辖市
    public void Provinces() {
        // 请求的URL
        String url = Services.mHost + "API/Common/GetProvince";
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
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Areas>>() {
                                    }.getType();
                                    mProvinces = gson.fromJson(jsonObject.getString("Obj"), type);
                                    initProvinces();
                                    if (!TextUtils.isEmpty(addressId)) {
                                        AddressById(addressId);
                                    } else {
                                        User user = UserInformation.getUserInfo();
                                        if (!TextUtils.isEmpty(user.UserName)) {
                                            et_address_contract.setText(user.UserName);
                                        }
                                        et_address_mobile.setText(user.UserPhone);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //获取收货地址
    public void AddressById(String addressId) {
        // 请求的URL
        String url = Services.mHost + "DeliveryAddress/APP_GetAddress_ByID?AddressID=%s";
        url = String.format(url, addressId);
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
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    mAddress = gson.fromJson(jsonObject.getString("Obj"), com.ldnet.entities.Address.class);
                                    if (mAddress != null) {
                                        et_address_contract.setText(mAddress.N);
                                        et_address_contract.addTextChangedListener(formValid);
                                        et_address_area_code.setText(mAddress.AC);
                                        et_address_area_code.addTextChangedListener(formValid);
                                        et_address_telephone.setText(mAddress.TP);
                                        et_address_telephone.addTextChangedListener(formValid);
                                        et_address_mobile.setText(mAddress.MP);
                                        et_address_mobile.addTextChangedListener(formValid);
                                        et_address_zipcode.setText(mAddress.ZC);
                                        et_address_zipcode.addTextChangedListener(formValid);
                                        et_address_details.setText(mAddress.AD);
                                        et_address_details.addTextChangedListener(formValid);
                                        chk_address_default.setChecked(mAddress.ISD);
//                mProvinces = services.Provinces();
//                mAdapter_Provinces.notifyDataSetChanged();
//                                        mCities.clear();
                                        Cities(mAddress.PID, "0",mAddress);

//                                        mAreas.clear();
                                        Areas(mAddress.CID, "0",mAddress);

                                        //省，默认值
                                        for (Areas b : mProvinces) {
                                            if (b.Id.equals(mAddress.PID)) {
                                                sr_address_provinces.setSelection(mProvinces.indexOf(b));
                                                break;
                                            }
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

    //获取市根据省、直辖市的ID
    public void Cities(Integer provinceId, final String type1, final com.ldnet.entities.Address mAddress) {
        // 请求的URL
        String url = Services.mHost + "API/Common/GetCity/%s";
        url = String.format(url, provinceId);
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
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Areas>>() {
                                    }.getType();
                                    mCities = gson.fromJson(jsonObject.getString("Obj"), type);
                                    initCities();
                                    //市，默认值
                                    if(mAddress != null){
                                        for (Areas b : mCities) {
                                            if (b.Id.equals(mAddress.CID)) {
                                                sr_address_cities.setSelection(mCities.indexOf(b));
                                                break;
                                            }
                                        }
                                    }

                                    if ("1".equals(type1)) {
                                        if (Areas != null) {
                                            mCities.clear();
                                            mCities.addAll(Areas);
                                            mAdapter_Cities.notifyDataSetChanged();
                                        }

                                        //改变区
                                        Areas areas1 = mCities.get(0);
                                        if (areas1 != null) {
                                            Areas(areas1.Id, "1",mAddress);
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

    //获取区域根据城市ID
    public void Areas(Integer cityId, final String type1,final com.ldnet.entities.Address mAddress) {
        // 请求的URL
        String url = Services.mHost + "API/Common/GetArea/%s";
        url = String.format(url, cityId);
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
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Areas>>() {
                                    }.getType();
                                    mAreas = gson.fromJson(jsonObject.getString("Obj"), type);
                                    initAreas();
                                    if(mAddress != null){
                                        //区，默认值
                                        for (Areas b : mAreas) {
                                            if (b.Id.equals(mAddress.AID)) {
                                                sr_address_areas.setSelection(mAreas.indexOf(b));
                                                break;
                                            }
                                        }
                                    }
                                    if ("1".equals(type1)) {
                                        if (Areas1 != null) {
                                            mAreas.clear();
                                            mAreas.addAll(Areas1);
                                            mAdapter_Areas.notifyDataSetChanged();
                                        }
                                    } else if ("2".equals(type1)) {
                                        if (Areas2 != null) {
                                            mAreas.clear();
                                            mAreas.addAll(Areas2);
                                            mAdapter_Areas.notifyDataSetChanged();
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

    //修改收货地址
    public void AddressesUpdate(String id, String n, String mp, String ac, String tp, String zc, Boolean isd, String ad, Integer pid, Integer cid, Integer aid) {
        try {
            //JSON对象
            JSONObject object = new JSONObject();
            object.put("ID", id);
            object.put("N", n);
            object.put("MP", mp);
            object.put("AC", ac);
            object.put("TP", tp);
            object.put("ZC", zc);
            object.put("ISD", isd);
            object.put("AD", ad);
            object.put("RID", UserInformation.getUserInfo().UserId);
            object.put("PID", pid);
            object.put("CID", cid);
            object.put("AID", aid);
            // 请求的URL
            String url = Services.mHost + "DeliveryAddress/APP_UpdAddress";
            HashMap<String, String> extras = new HashMap<>();
            extras.put("str", object.toString());
            Services.json(extras);
            String aa = Services.timeFormat();
            String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
            String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 +  Services.json(extras) + Services.TOKEN;
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
                                        gotoActivityAndFinish(Address.class.getName(), null);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //新增收货地址
    public void AddressInsert(String n, String mp, String ac, String tp, String zc, Boolean isd, String ad, Integer pid, Integer cid, Integer aid) {
        try {
            //JSON对象
            JSONObject object = new JSONObject();
            object.put("ID", "");
            object.put("N", n);
            object.put("MP", mp);
            object.put("AC", ac);
            object.put("TP", tp);
            object.put("ZC", zc);
            object.put("ISD", isd);
            object.put("AD", ad);
            object.put("RID", UserInformation.getUserInfo().UserId);
            object.put("PID", pid);
            object.put("CID", cid);
            object.put("AID", aid);

            // 请求的URL
            String url = Services.mHost + "DeliveryAddress/APP_InsertAddress";
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
                                        //判断是否来自提交订单
                                        if (mFromOrderConfirm) {
                                            finish();
                                        } else {
                                            gotoActivityAndFinish(Address.class.getName(), null);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
