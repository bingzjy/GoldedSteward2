package com.ldnet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.gson.Gson;
import com.ldnet.activity.base.BaseFragment;
import com.ldnet.activity.mall.Shopping_Carts;
import com.ldnet.activity.me.*;
import com.ldnet.activity.me.Address;
import com.ldnet.activity.me.Community;
import com.ldnet.activity.me.Coupon;
import com.ldnet.activity.me.Information;
import com.ldnet.activity.me.Publish;
import com.ldnet.activity.access.AccessControlMain;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * ***************************************************
 * 主框架 - 我
 * **************************************************
 */
public class FragmentMe extends BaseFragment implements OnClickListener {

    // 标题
    private TextView tv_main_title;
    //我的头像
    private CircleImageView ibtn_me_thumbnail;
    //我的信息
    private LinearLayout ll_me_information;
    //我的积分
    private LinearLayout ll_me_integral;
    //我的钱包
    private LinearLayout ll_me_wallet;

    //我的购物车
    private LinearLayout ll_me_shopping_cart;
    //我的订单
    private LinearLayout ll_me_orders;
    //我的小区
    private LinearLayout ll_me_community;
    //邀请好友
    private LinearLayout ll_me_invite;
    //我的消息
    private LinearLayout ll_me_message;
    //我的发布
    private LinearLayout ll_me_publish;
    // 意见反馈
    private LinearLayout ll_me_feedback;
    //关于
    private LinearLayout ll_me_about;
    // 退出登录
    private LinearLayout ll_me_logout;
    //帐号名称
    private TextView tv_me_phone;
    //我的地址（收货地址）
    private LinearLayout ll_me_address;
    private LinearLayout ll_me_entry_exit;
    //优惠劵
    private LinearLayout ll_me_coupon;
    private LinearLayout ll_me_check;
    private ImageView iv_msg_center, iv_feedback, iv_msg_order;
    //
    private Services services;
    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int CROPIMAGE_REQUEST_CODE = 1002;
    private static final int mCropImageWidth = 192;

    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
    private File mFileCacheDirectory;
    private Uri mImageUri;
    private String fileId;
    private PopupWindow popWindow;
    private PopupWindow popWindow1;
    private LayoutInflater layoutInflater;
    private TextView photograph, albums;
    private LinearLayout cancel;

    protected DisplayImageOptions imageOptions;
    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";

    // onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载布局
        View view = inflater.inflate(R.layout.fragment_main_me, container, false);
        //菜单
        setHasOptionsMenu(true);
        imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
                .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .extraForDownloader(UserInformation.getUserInfo().UserPhone + "," + aa + "," + aa1)
                .build();
        // 初始化视图
        services = new Services();
        initView(view);

        return view;
    }

    // 初始化视图
    private void initView(View view) {

        services = new Services();

        // 标题
        tv_main_title = (TextView) view.findViewById(R.id.tv_main_title);
        tv_main_title.setText(R.string.module_title_me);

        //设置用户名
        tv_me_phone = (TextView) view.findViewById(R.id.tv_me_phone);
        //我的头像
        ibtn_me_thumbnail = (CircleImageView) view.findViewById(R.id.ibtn_me_thumbnail);
        //我的信息
        ll_me_information = (LinearLayout) view.findViewById(R.id.ll_me_Information);
        //我的积分
        ll_me_integral = (LinearLayout) view.findViewById(R.id.ll_me_integral);
        //我的钱包
        ll_me_wallet = (LinearLayout) view.findViewById(R.id.ll_me_wallet);

        //我的购物车
        ll_me_shopping_cart = (LinearLayout) view.findViewById(R.id.ll_me_shopping_cart);
        //优惠劵
        ll_me_coupon = (LinearLayout) view.findViewById(R.id.ll_me_coupon);
        //我的订单
        ll_me_orders = (LinearLayout) view.findViewById(R.id.ll_me_orders);
        //我的小区
        ll_me_community = (LinearLayout) view.findViewById(R.id.ll_me_community);
        //我的地址（收货地址）
        ll_me_address = (LinearLayout) view.findViewById(R.id.ll_me_address);
        //邀请好友
        ll_me_invite = (LinearLayout) view.findViewById(R.id.ll_me_invite);
        ll_me_invite.setVisibility(View.GONE);
        //我的消息
        ll_me_message = (LinearLayout) view.findViewById(R.id.ll_me_message);
        //我的发布
        ll_me_publish = (LinearLayout) view.findViewById(R.id.ll_me_publish);
        //关于
        ll_me_about = (LinearLayout) view.findViewById(R.id.ll_me_about);
        //意见反馈
        ll_me_feedback = (LinearLayout) view.findViewById(R.id.ll_me_feedback);
        ll_me_check = (LinearLayout) view.findViewById(R.id.ll_me_check);
        //出入管理
        ll_me_entry_exit=(LinearLayout)view.findViewById(R.id.ll_me_entry_exit_manage);
        // 退出登录
        ll_me_logout = (LinearLayout) view.findViewById(R.id.ll_me_logout);
        iv_msg_center = (ImageView) view.findViewById(R.id.iv_msg_center);
        iv_feedback = (ImageView) view.findViewById(R.id.iv_feedback);
        iv_msg_order = (ImageView) view.findViewById(R.id.iv_msg_order);
        initEvents();
        ScreenUtils.initScreen(getActivity());
        layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // 初始化事件
    private void initEvents() {
        //我的头像
//        registerForContextMenu(ibtn_me_thumbnail);//注册菜单
        ibtn_me_thumbnail.setOnClickListener(this);
        //我的信息
        ll_me_information.setOnClickListener(this);
        //我的积分
        ll_me_integral.setOnClickListener(this);
        //我的钱包
        ll_me_wallet.setOnClickListener(this);
        //我的购物车
        ll_me_shopping_cart.setOnClickListener(this);
        // 优惠劵
        ll_me_coupon.setOnClickListener(this);
        //我的订单
        ll_me_orders.setOnClickListener(this);
        //我的小区
        ll_me_community.setOnClickListener(this);
        //我的地址（收货地址）
        ll_me_address.setOnClickListener(this);
        //邀请好友
        ll_me_invite.setOnClickListener(this);
        //我的消息
        ll_me_message.setOnClickListener(this);
        //我的发布
        ll_me_publish.setOnClickListener(this);
        //关于
        ll_me_about.setOnClickListener(this);
        //意见反馈
        ll_me_feedback.setOnClickListener(this);
        // 退出登录
        ll_me_logout.setOnClickListener(this);
        ll_me_check.setOnClickListener(this);
        ll_me_entry_exit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //设置用户姓名
        String userName;
        User user = UserInformation.getUserInfo();
        if (!TextUtils.isEmpty(user.UserName)) {
            userName = user.UserName;
        } else {
            userName = user.UserPhone;
        }
        tv_me_phone.setText(userName);
        //设置用户头像
        if (!TextUtils.isEmpty(user.UserThumbnail)) {
            ImageLoader.getInstance().displayImage(services.getImageUrl(user.UserThumbnail), ibtn_me_thumbnail, imageOptions);
        }

        if (MsgInformation.getMsg().isMESSAGE()) {
            iv_msg_center.setVisibility(View.VISIBLE);
        } else {
            iv_msg_center.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isFEEDBACK()) {
            iv_feedback.setVisibility(View.VISIBLE);
        } else {
            iv_feedback.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isORDER()) {
            iv_msg_order.setVisibility(View.VISIBLE);
        } else {
            iv_msg_order.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isMESSAGE() || MsgInformation.getMsg().isFEEDBACK() || MsgInformation.getMsg().isORDER()) {
            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_me_thumbnail://我的头像
                showPopupWindow(ibtn_me_thumbnail);
                break;
            case R.id.ll_me_Information: // 我的信息
                Intent intent_information = new Intent(getActivity(), Information.class);
                startActivity(intent_information);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_integral://我的积分
                Intent intent_integral = new Intent(getActivity(), Integral.class);
                startActivity(intent_integral);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_wallet://我的钱包
                Intent intent_wallet = new Intent(getActivity(), Recharge.class);
                startActivity(intent_wallet);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;

            case R.id.ll_me_shopping_cart://我的购物车
                Intent intent_shopping = new Intent(getActivity(), Shopping_Carts.class);
                startActivity(intent_shopping);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_coupon://我的优惠劵
                Intent intent_coupon = new Intent(getActivity(), Coupon.class);
                startActivity(intent_coupon);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_orders://我的订单
                Msg msg3 = MsgInformation.getMsg();
                if (!MsgInformation.getMsg().isMESSAGE() && MsgInformation.getMsg().isFEEDBACK()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_msg_order.setVisibility(View.GONE);
                    iv_msg_center.setVisibility(View.GONE);
                    msg3.setORDER(false);
                    msg3.setMESSAGE(false);
                } else if (MsgInformation.getMsg().isMESSAGE() && !MsgInformation.getMsg().isFEEDBACK()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_msg_order.setVisibility(View.GONE);
                    iv_feedback.setVisibility(View.GONE);
                    msg3.setORDER(false);
                    msg3.setFEEDBACK(false);
                } else if (!MsgInformation.getMsg().isMESSAGE() && !MsgInformation.getMsg().isFEEDBACK()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.GONE);
                    iv_msg_order.setVisibility(View.GONE);
                    iv_feedback.setVisibility(View.GONE);
                    iv_msg_center.setVisibility(View.GONE);
                    msg3.setMESSAGE(false);
                    msg3.setFEEDBACK(false);
                    msg3.setORDER(false);
                }
                MsgInformation.setMsgInfo(msg3);
                Intent intent_orders = new Intent(getActivity(), OrdersTabActivity.class);
                startActivity(intent_orders);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_community: //我的小区
                Intent intent_community = new Intent(getActivity(), Community.class);
                startActivity(intent_community);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_address://我的地址、收货地址
                Intent intent_address = new Intent(getActivity(), Address.class);
                startActivity(intent_address);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_invite://邀请好友
                Intent intent_invite = new Intent(getActivity(), Invite.class);
                startActivity(intent_invite);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_message: //我的消息
                Msg msg = MsgInformation.getMsg();
                if (MsgInformation.getMsg().isFEEDBACK() && !MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_msg_center.setVisibility(View.GONE);
                    iv_msg_order.setVisibility(View.GONE);
                    msg.setMESSAGE(false);
                    msg.setORDER(false);
                } else if (!MsgInformation.getMsg().isFEEDBACK() && MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_feedback.setVisibility(View.GONE);
                    iv_msg_center.setVisibility(View.GONE);
                    msg.setMESSAGE(false);
                    msg.setFEEDBACK(false);
                } else if (!MsgInformation.getMsg().isFEEDBACK() && !MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.GONE);
                    iv_msg_order.setVisibility(View.GONE);
                    iv_msg_center.setVisibility(View.GONE);
                    iv_feedback.setVisibility(View.GONE);
                    msg.setMESSAGE(false);
                    msg.setFEEDBACK(false);
                    msg.setORDER(false);
                }
                MsgInformation.setMsgInfo(msg);
                Intent intent_message = new Intent(getActivity(), Message.class);
                startActivity(intent_message);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_publish://我的发布
                Intent intent_publish = new Intent(getActivity(), Publish.class);
                startActivity(intent_publish);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_about: //关于
                Intent intent_about = new Intent(getActivity(), About.class);
                startActivity(intent_about);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_feedback:// 意见反馈
                Msg msg1 = MsgInformation.getMsg();
                if (MsgInformation.getMsg().isMESSAGE() && !MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_feedback.setVisibility(View.GONE);
                    iv_msg_order.setVisibility(View.GONE);
                    msg1.setFEEDBACK(false);
                    msg1.setORDER(false);
                } else if (!MsgInformation.getMsg().isMESSAGE() && MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                    iv_msg_center.setVisibility(View.GONE);
                    iv_feedback.setVisibility(View.GONE);
                    msg1.setMESSAGE(false);
                    msg1.setFEEDBACK(false);
                } else if (!MsgInformation.getMsg().isMESSAGE() && !MsgInformation.getMsg().isORDER()) {
                    getActivity().findViewById(R.id.iv_dc1).setVisibility(View.GONE);
                    iv_msg_center.setVisibility(View.GONE);
                    iv_feedback.setVisibility(View.GONE);
                    iv_msg_order.setVisibility(View.GONE);
                    msg1.setMESSAGE(false);
                    msg1.setFEEDBACK(false);
                    msg1.setORDER(false);
                }
                MsgInformation.setMsgInfo(msg1);
                Intent intent_feedback = new Intent(getActivity(), Feedback.class);
                startActivity(intent_feedback);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_logout: // 退出登录
                quitPopupWindow(ll_me_logout);
                break;
            case R.id.ll_me_check: // 版本检测
                Intent intent_check = new Intent(getActivity(), Check.class);
                startActivity(intent_check);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            case R.id.ll_me_entry_exit_manage: //出入管理
                Intent intent_pass = new Intent(getActivity(), AccessControlMain.class);
                startActivity(intent_pass);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            default:
                break;
        }
    }


    public void showPopupWindow(View parent) {
        if (popWindow == null) {
            View view = layoutInflater.inflate(R.layout.pop_select_photo, null);
            popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            initPop(view);
        }
        popWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void quitPopupWindow(View parent) {
        if (popWindow1 == null) {
            View view = layoutInflater.inflate(R.layout.quit_pop, null);
            popWindow1 = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            quitPop(view);
        }
        popWindow1.setAnimationStyle(android.R.style.Animation_InputMethod);
        popWindow1.setFocusable(true);
        popWindow1.setOutsideTouchable(true);
        popWindow1.setBackgroundDrawable(new BitmapDrawable());
        popWindow1.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow1.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void quitPop(View view) {
        TextView quit = (TextView) view.findViewById(R.id.quit);//tuichu
        LinearLayout cancel1 = (LinearLayout) view.findViewById(R.id.cancel);//取消

        quit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popWindow1.dismiss();
                // 定义 intent
                Intent intent = new Intent(getActivity(), Login.class);
                intent.putExtra("password", UserInformation.getUserInfo().getUserPassword());
                intent.putExtra("phone", UserInformation.getUserInfo().getUserPhone());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                getActivity().finish();
                // 清除保存的Cookie
                CookieInformation.clearCookieInfo();
//                MsgInformation.clearMsgInfo();
                // 清除保存的用户信息
                UserInformation.clearUserInfo();
                TokenInformation.clearTokenInfo();
            }
        });
        cancel1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popWindow1.dismiss();
            }
        });
    }

    public void initPop(View view) {
        photograph = (TextView) view.findViewById(R.id.photograph);//拍照
        albums = (TextView) view.findViewById(R.id.albums);//相册
        cancel = (LinearLayout) view.findViewById(R.id.cancel);//取消
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mFileCacheDirectory = new File(
                    Environment.getExternalStorageDirectory(),
                    getActivity().getPackageName());

            // 判断当前目录是否存在
            if (!mFileCacheDirectory.exists()) {
                mFileCacheDirectory.mkdir();
            }

            //图片的存储位置
            mImageUri = Uri.fromFile(new File(mFileCacheDirectory.getPath(), IMAGE_FILE_NAME));
            photograph.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                    Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    getActivity().startActivityForResult(intent_capture, CAMERA_REQUEST_CODE);
                }
            });
            albums.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                    Intent intent_pick = new Intent(Intent.ACTION_PICK, null);
                    intent_pick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    getActivity().startActivityForResult(intent_pick, IMAGE_REQUEST_CODE);
                }
            });
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                }
            });
        } else {
            showToast("没找到手机SD卡，图片无法上传！");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getActivity().RESULT_OK == resultCode) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    onCropImage(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    onCropImage(mImageUri);
                    break;
                case CROPIMAGE_REQUEST_CODE:

                    if (data != null) {
                        //得到图片写到本地
                        Bundle extras = data.getExtras();
                        Bitmap image = extras.getParcelable("data");
                        if (image != null) {
                            //上传服务器
                            // 获取本地文件
                            File file = new File(mImageUri.getPath());
                            try {
                                // 将图片写入到文件
                                FileOutputStream fileOutStream = new FileOutputStream(file);
                                image.compress(Bitmap.CompressFormat.JPEG, 100,
                                        fileOutStream);
                                fileOutStream.flush();
                                fileOutStream.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            showToast("上传图片中...");
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if (services.ping()) {
                                    // TODO Auto-generated method stub
                                        //上传到服务器
                                        String fileId = services.Upload(getActivity(), mImageUri.getPath()).FileName;
                                        //修改服务器中用户的头像
                                        User user = UserInformation.getUserInfo();
                                        user.UserThumbnail = fileId;
                                        UserInformation.setUserInfo(user);
                                        ChangeInformation(user.UserName, user.UserThumbnail, fileId);
                                    } else {
                                        showToast("网络连接失败，请检查您的网络");
                                        return;
                                    }
                                }

                            }, 100);

                        }
                    }

                    break;
                default:
                    break;
            }
        }
    }

    //头像裁剪方法
    private void onCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mCropImageWidth);// 输出图片大小
        intent.putExtra("outputY", mCropImageWidth);
        intent.putExtra("return-data", true);
        getActivity().startActivityForResult(intent, CROPIMAGE_REQUEST_CODE);
    }

    //上传图片
    public void Upload(String fileName) {
        //文件上传的路径
        final String url = Services.mHost + "API/File/UploadFile";
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addParams("contentType", fileName)
                .build()
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
                                    Gson gson = new Gson();
                                    fileId = gson.fromJson(jsonObject.getString("Obj"), FileUpload.class).getFileName();
                                    //修改服务器中用户的头像
                                    User user = UserInformation.getUserInfo();
                                    user.UserThumbnail = fileId;
                                    UserInformation.setUserInfo(user);
                                    ChangeInformation(user.getUserName(), user.getUserThumbnail(), fileId);
                                    IntegralTip(url);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
        }
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
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .build()
                .execute(new DataCallBack(getActivity()) {
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
                                        Msg msg = MsgInformation.getMsg();
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
                                        if (msg.isNOTICE() || msg.isCOMMUNICATION() || msg.isFEE() || msg.isCOMPLAIN() || msg.isREPAIRS()) {
                                            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
                                        }
                                        if (msg.isMESSAGE() || msg.isFEEDBACK() || msg.isORDER()) {
                                            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                                        }
                                        if (msg.isMESSAGE()) {
                                            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                                            iv_msg_center.setVisibility(View.VISIBLE);
                                        }
                                        if (msg.isFEEDBACK()) {
                                            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                                            iv_feedback.setVisibility(View.VISIBLE);
                                        }
                                        if (msg.isORDER()) {
                                            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                                            iv_msg_order.setVisibility(View.VISIBLE);
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

    public void ChangeInformation(String name, String image, final String str) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "API/Resident/UpdateResident";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("img", image);
        extras.put("nickName", name);
        extras.put("Id", UserInformation.getUserInfo
                ().getUserId());
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
                .addParams("Id", UserInformation.getUserInfo().getUserId())
                .addParams("nickName", name)
                .addParams("img", image)
                .build()
                .execute(new DataCallBack(getActivity()) {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd123123", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    //显示头像
                                    ImageLoader.getInstance().displayImage(services.getImageUrl(str), ibtn_me_thumbnail, imageOptions);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void IntegralTip(String pUrl) {

        String url1 = "";
        try {
            pUrl = new URL(pUrl).getPath();
            // 请求的URL
            String url = Services.mHost + "API/Prints/Add/%s?route=%s";
            url = String.format(url, UserInformation.getUserInfo().UserId, URLEncoder.encode(pUrl, "UTF-8"));
            String aa = Services.timeFormat();
            String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
            String aa2 = url;
            String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
            OkHttpUtils.get().url(url)
                    .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                    .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                    .addHeader("timestamp", aa)
                    .addHeader("nonce", aa1)
                    .addHeader("signature", Services.textToMD5L32(md5))
                    .build()
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
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
