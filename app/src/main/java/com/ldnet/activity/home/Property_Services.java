package com.ldnet.activity.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Property_Services extends BaseActionBarActivity implements MyNetWorkBroadcastReceive.onNewMessageListener {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private TextView tv_house_information_area;
    private TextView tv_house_information_name;
    private TextView txRepair, txComplaint, txCommunicate;
    private ImageView unread_goutong, unread_tousu, unread_baoxiu;
    //private LinearLayout ll_home_fee;
    private LinearLayout ll_home_repair;
    private LinearLayout ll_home_complaint;
    private LinearLayout ll_home_communicate;
    private LinearLayout ll_home_telephone;
    private LinearLayout ll_property_services;
    private CircleImageView iv_property_thumbnail;
    private TextView tv_property_name;
    private TextView tv_property_address;
    private BadgeView mBadgeView;
    private PropertyServicesType mServicesType;
    private RoomInformation information;

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_property_services);
        MyNetWorkBroadcastReceive.msgListeners.add(this);
        findView();
    }

    public void findView() {
        //初始化服务
        services = new Services();
        //获取服务类型
//        GetPropertyServicesTypes();
        ll_property_services = (LinearLayout) findViewById(R.id.ll_property_services);
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_home_property_services);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        unread_goutong = (ImageView) findViewById(R.id.unread_goutong);
        unread_tousu = (ImageView) findViewById(R.id.unread_tousu);
        unread_baoxiu = (ImageView) findViewById(R.id.unread_baoxiu);

        User user = UserInformation.getUserInfo();
        //物业图标
        iv_property_thumbnail = (CircleImageView) findViewById(R.id.iv_property_thumbnail);
        //default???
        ImageLoader.getInstance().displayImage(services.getImageUrl(user.PropertyThumbnail), iv_property_thumbnail);
        //物业名称
        tv_property_name = (TextView) findViewById(R.id.tv_property_name);
        tv_property_name.setText(user.PropertyName);
        //物业地址--小区地址
        tv_property_address = (TextView) findViewById(R.id.tv_property_address);
        tv_property_address.setText(user.CommuntiyAddress);
        //房屋缩写
        tv_house_information_name = (TextView) findViewById(R.id.tv_house_information_name);
        tv_house_information_name.setText(user.HouseName);
        //房屋建筑面积
        tv_house_information_area = (TextView) findViewById(R.id.tv_house_information_area);
        //缴费
        //ll_home_fee = (LinearLayout) findViewById(R.id.ll_home_fee);
        //报修
        ll_home_repair = (LinearLayout) findViewById(R.id.ll_home_repair);
        txRepair = (TextView) findViewById(R.id.tx_home_repair);

        //投诉
        ll_home_complaint = (LinearLayout) findViewById(R.id.ll_home_complaint);
        txComplaint = (TextView) findViewById(R.id.tx_home_complaint);

        //沟通
        ll_home_communicate = (LinearLayout) findViewById(R.id.ll_home_communicate);
        txCommunicate = (TextView) findViewById(R.id.tx_home_communicate);

        //物业电话
        ll_home_telephone = (LinearLayout) findViewById(R.id.ll_home_telephone);
        //房屋信息
        RoomInformation();
        initEvent();
        APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
    }

    @Override
    public void onNewMessage(String message) {
        APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
    }

    //获取房子信息
    public void RoomInformation() {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetMyRoomInfo/%s";
        url = String.format(url, UserInformation.getUserInfo().HouseId);
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
                        Log.d("asdsdasd12132", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    information = gson.fromJson(jsonObject.getString("Obj"), RoomInformation.class);
                                    if (information != null) {
                                        tv_house_information_area.append(information.Buildarea + "㎡");
                                    } else {
                                        tv_house_information_area.append("0㎡");
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        //缴费
        //ll_home_fee.setOnClickListener(this);
        //报修
        ll_home_repair.setOnClickListener(this);
        //投诉
        ll_home_complaint.setOnClickListener(this);
        //沟通
        ll_home_communicate.setOnClickListener(this);
        //物业电话
        ll_home_telephone.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "");
                    gotoActivityAndFinish(MainActivity.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            //报修
            case R.id.ll_home_repair:
                try {
                    Msg msg1 = MsgInformation.getMsg();
                    msg1.setREPAIRS(false);
                    MsgInformation.setMsgInfo(msg1);
                    unread_baoxiu.setVisibility(View.GONE);
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Property_Repair.class.getName(), extras);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            //投诉
            case R.id.ll_home_complaint:
                try {
                    Msg msg2 = MsgInformation.getMsg();
                    msg2.setCOMPLAIN(false);
                    MsgInformation.setMsgInfo(msg2);
                    unread_tousu.setVisibility(View.GONE);
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Property_Complain.class.getName(), extras);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            //沟通
            case R.id.ll_home_communicate:
                //移除红点标记
//                removeRemind();
                try {
                    Msg msg3 = MsgInformation.getMsg();
                    msg3.setCOMMUNICATION(false);
                    MsgInformation.setMsgInfo(msg3);
                    unread_goutong.setVisibility(View.GONE);
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Property_Communicate.class.getName(), extras);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ll_home_telephone:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("LEFT", "LEFT");
                    gotoActivityAndFinish(Property_Telephone.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            try {
                HashMap<String, String> extras = new HashMap<String, String>();
                extras.put("LEFT", "");
                gotoActivityAndFinish(MainActivity.class.getName(), extras);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void APPGetJpushNotification(int id) {
        // 请求的URL
//        String url = Services.mHost + "API/Property/APPGetNotification/%s?communityId=%s";
        String url = Services.mHost + "API/Property/APPGetJpushNotification/%s?communityId=%s&resultId=%s";
        url = String.format(url, UserInformation.getUserInfo().UserId, UserInformation.getUserInfo().CommunityId, id);
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
                        Log.d("asdsdasd31---31231", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    MessageCallBack messageCallBack = gson.fromJson(jsonObject.getString("Obj"), MessageCallBack.class);
                                    if (messageCallBack != null) {
                                        Msg msg = new Msg();
                                        msg.setCallbackId(Integer.parseInt(messageCallBack.getCallbackId()));
                                        msg.setCOMPLAIN(Boolean.parseBoolean(messageCallBack.getN().getCOMPLAIN()));
                                        msg.setCOMMUNICATION(Boolean.parseBoolean(messageCallBack.getN().getCOMMUNICATION()));
                                        msg.setFEE(Boolean.parseBoolean(messageCallBack.getN().getFEE()));
                                        msg.setFEEDBACK(Boolean.parseBoolean(messageCallBack.getN().getFEEDBACK()));
                                        msg.setMESSAGE(Boolean.parseBoolean(messageCallBack.getN().getMESSAGE()));
                                        msg.setNOTICE(Boolean.parseBoolean(messageCallBack.getN().getNOTICE()));
                                        msg.setORDER(Boolean.parseBoolean(messageCallBack.getN().getORDER()));
                                        msg.setPAGE(Boolean.parseBoolean(messageCallBack.getN().getPAGE()));
                                        msg.setREPAIRS(Boolean.parseBoolean(messageCallBack.getN().getREPAIRS()));
                                        msg.setOTHER(Boolean.parseBoolean(messageCallBack.getN().getOTHER()));
                                        MsgInformation.setMsgInfo(msg);
                                        if (MsgInformation.getMsg().isREPAIRS()) {
                                            unread_baoxiu.setVisibility(View.VISIBLE);
                                        }
                                        if (MsgInformation.getMsg().isCOMPLAIN()) {
                                            unread_tousu.setVisibility(View.VISIBLE);
                                        }
                                        if (MsgInformation.getMsg().isCOMMUNICATION()) {
                                            unread_goutong.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        if (MsgInformation.getMsg().isREPAIRS()) {
                                            unread_baoxiu.setVisibility(View.VISIBLE);
                                        }
                                        if (MsgInformation.getMsg().isCOMPLAIN()) {
                                            unread_tousu.setVisibility(View.VISIBLE);
                                        }
                                        if (MsgInformation.getMsg().isCOMMUNICATION()) {
                                            unread_goutong.setVisibility(View.VISIBLE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyNetWorkBroadcastReceive.msgListeners.remove(this);
    }
}
