package com.ldnet.activity.find;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ldnet.activity.adapter.ImageBucketChooseActivity;
import com.ldnet.activity.adapter.ImageChooseActivity;
import com.ldnet.activity.adapter.ImageItem;
import com.ldnet.activity.adapter.ImagePublishAdapter;
import com.ldnet.activity.adapter.ImageZoomActivity;
import com.ldnet.activity.adapter.MyDialog;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarFragmentActivity;
import com.ldnet.activity.me.Publish;
import com.ldnet.entities.User;
import com.ldnet.entities.WeekendDetails;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.CustomConstants;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.IntentConstants;
import com.ldnet.utility.SDCardFileCache;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.ldnet.view.SlideDateTimeListener;
import com.ldnet.view.SlideDateTimePicker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class Weekend_Create extends BaseActionBarFragmentActivity implements View.OnClickListener {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private EditText et_weekend_title;
    private EditText et_weekend_address;
    private EditText et_weekend_cost;
    private EditText et_weekend_start_date;
    private EditText et_weekend_start_time;
    private EditText et_weekend_end_date;
    private EditText et_weekend_end_time;
    private EditText et_weekend_content;
    private LinearLayout ll_weekend_picture_list;
    private ImageButton ibtn_weekend_pictrue_plus;
    private Button btn_weekend_confirm;
    private Calendar calendar = Calendar.getInstance();

    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int CROPIMAGE_REQUEST_CODE = 1002;
    private static final int mCropImageWidth = 1280;
    private static final int mCropImageHeight = 720;
    private static final String IMAGE_FILE_NAME = "WeekendImage.jpg";
    private File mFileCacheDirectory;
    private Uri mImageUri;
    public static String mImageIds;
    private SDCardFileCache mFileCaches;
    private static String mFreamarketId;
    private static Boolean mFromPublish = false;
    private String oldImg = "";
    private WeekendDetails details;
    private String fileId;
    private PopupWindow popWindow;
    private LayoutInflater layoutInflater;
    private TextView photograph, albums;
    private LinearLayout cancel;
    public String BOUNDARY = "--------httppost123";

    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private GridView mGridView;
    private ImagePublishAdapter mAdapter;
    public static List<ImageItem> mDataList = new ArrayList<ImageItem>();
    SharedPreferences sp;
    List<ImageItem> incomingDataList;
    private static String title = "", address = "", cost = "", date1 = "", date2 = "", content = "";
    private boolean flag = false;
    private String aaa = "";

    //初始化视图

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_weekend_create);
        if (ImageChooseActivity.instance != null) {
            ImageChooseActivity.instance.finish();
        }
        AppUtils.setupUI(findViewById(R.id.ll_weekend_create), this);
        sp = getSharedPreferences(
                CustomConstants.APPLICATION_NAME, MODE_PRIVATE);
        mFileCaches = new SDCardFileCache(this);
        //初始化服务
        services = new Services();
        aaa = getIntent().getStringExtra("flag");
//        if(!Services.isNotNullOrEmpty(aaa)) {
        initData();
//        }
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.weekend_publish);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //初始化表单控件
        et_weekend_title = (EditText) findViewById(R.id.et_weekend_title);
        et_weekend_address = (EditText) findViewById(R.id.et_weekend_address);
        et_weekend_cost = (EditText) findViewById(R.id.et_weekend_cost);
        et_weekend_start_date = (EditText) findViewById(R.id.et_weekend_start_date);
        et_weekend_end_date = (EditText) findViewById(R.id.et_weekend_end_date);
        et_weekend_content = (EditText) findViewById(R.id.et_weekend_content);

        ll_weekend_picture_list = (LinearLayout) findViewById(R.id.ll_weekend_picture_list);
//        ibtn_weekend_pictrue_plus = (ImageButton) findViewById(R.id.ibtn_weekend_pictrue_plus);
        btn_weekend_confirm = (Button) findViewById(R.id.btn_weekend_confirm);

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mDataList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == getDataSize()) {
                    new Weekend_Create.PopupWindows(Weekend_Create.this, mGridView);
                } else {
                    Intent intent = new Intent(Weekend_Create.this,
                            ImageZoomActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_IMAGE_LIST,
                            (Serializable) mDataList);
                    intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
                    intent.putExtra("classname", "Weekend_Create");
                    startActivity(intent);
                    title = et_weekend_title.getText().toString();
                    address = et_weekend_address.getText().toString();
                    cost = et_weekend_cost.getText().toString();
                    date1 = et_weekend_start_date.getText().toString();
                    date2 = et_weekend_end_date.getText().toString();
                    content = et_weekend_content.getText().toString();
                }
            }
        });

        String formPublish = getIntent().getStringExtra("FROM_PUBLISH");
        if (!TextUtils.isEmpty(formPublish)) {
            mFromPublish = Boolean.valueOf(formPublish);
        }
        if (mFreamarketId != null && !mFreamarketId.equals("")) {
            flag = true;
            et_weekend_title.setText(title);
            et_weekend_address.setText(address);
            et_weekend_cost.setText(cost);
            et_weekend_start_date.setText(date1);
            et_weekend_end_date.setText(date2);
            et_weekend_content.setText(content);
            btn_weekend_confirm.setText("确认");
            tv_main_title.setText("修改活动信息");
        } else {
            mFreamarketId = getIntent().getStringExtra("FREA_MARKET_ID");
        }
        //来自我的发布，显示修改信息
        if (mFromPublish) {
            if (!flag) {
                WeekendDetails(mFreamarketId);
            }
        }
        initEvent();
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //周末去哪儿 - 获取闲置物品的详情
    public void WeekendDetails(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetWeekendById/%s?residentId=%s";
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
                                    details = gson.fromJson(jsonObject.getString("Obj"), WeekendDetails.class);
                                    btn_weekend_confirm.setText("确认");
                                    tv_main_title.setText("修改活动信息");
                                    et_weekend_title.setText(details.Title);
                                    et_weekend_address.setText(details.ActiveAddress);
                                    et_weekend_content.setText(details.Memo);
                                    et_weekend_cost.setText(String.valueOf(details.Cost));
                                    et_weekend_start_date.setText(details.StartDatetime);
                                    et_weekend_end_date.setText(details.EndDatetime);
                                    //加载图片
                                    //加载图片
                                    if (details.getImg().length > 0) {
                                        mImageIds = "";
                                        mDataList.clear();
                                        for (String id : details.getImg()) {
                                            ImageItem imageItem = new ImageItem();
                                            imageItem.sourcePath = id;
                                            mDataList.add(imageItem);
                                            if (details.getImg().length == 1) {
                                                mImageIds = id;
                                            } else {
                                                mImageIds += id + ",";
                                            }
                                        }
                                        Log.d("----", mImageIds + "--");
                                        if (String.valueOf(mImageIds.charAt(mImageIds.length() - 1)).equals(",")) {
                                            mImageIds = mImageIds.substring(0, mImageIds.length() - 1);
                                        }
                                        mAdapter.notifyDataSetChanged();
                                        Log.d("----", mImageIds);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public boolean isNull() {
        if (TextUtils.isEmpty(et_weekend_title.getText().toString().trim())) {
            showToast("标题不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_weekend_address.getText().toString().trim())) {
            showToast("活动地点不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_weekend_start_date.getText().toString().trim())) {
            showToast("请选择开始时间");
            return false;
        }
        if (TextUtils.isEmpty(et_weekend_end_date.getText().toString().trim())) {
            showToast("请选择结束时间");
            return false;
        }
        if (TextUtils.isEmpty(et_weekend_content.getText().toString().trim())) {
            showToast("介绍不能为空");
            return false;
        }
        if (mFromPublish) {
            if (TextUtils.isEmpty(mImageIds)) {
                mImageIds = "";
                //  截取旧图片id，去掉最后一位
                if (!TextUtils.isEmpty(oldImg)) {
                    oldImg = oldImg.substring(0, oldImg.length() - 1);
                } else {
                    oldImg = "";
                }
            }
            if (TextUtils.isEmpty(oldImg + mImageIds)) {
                showToast("请选择照片");
                return false;
            }
        } else {
            if (TextUtils.isEmpty(mImageIds)) {
                showToast("请选择照片");
                return false;
            }
        }
        return true;
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_weekend_confirm.setOnClickListener(this);
//        ibtn_weekend_pictrue_plus.setOnClickListener(this);
//        registerForContextMenu(ibtn_weekend_pictrue_plus);
        et_weekend_start_date.setOnClickListener(this);
        et_weekend_end_date.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://返回我的发布或周末去哪列表
                crenteCencalDialog();
                break;
            case R.id.btn_weekend_confirm:
                String title = et_weekend_title.getText().toString().trim();
                String address = et_weekend_address.getText().toString().trim();
                String cost = et_weekend_cost.getText().toString().trim();
                String sDate = et_weekend_start_date.getText().toString().trim();
                String eDate = et_weekend_end_date.getText().toString().trim();
                String content = et_weekend_content.getText().toString().trim();
                //  修改信息
                if (mFromPublish) {
                    if (isNull()) {
                        weekendUpdate(mFreamarketId, title, sDate, eDate, address, cost, oldImg + mImageIds, content);
                    }
                    //发布信息
                } else {

                    if (isNull()) {
                        WeekendCreate(title, sDate, eDate, address, cost, mImageIds, content);
                    }
                }
                break;
//            case R.id.ibtn_weekend_pictrue_plus:
////                openContextMenu(ibtn_weekend_pictrue_plus);
//                showPopupWindow(ibtn_weekend_pictrue_plus);
//                break;
            case R.id.et_weekend_start_date:
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .setIs24HourTime(true)
                        .setMaxDate(new Date())
                        .build()
                        .show();
                break;
            case R.id.et_weekend_start_time:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        //更新EditText控件时间 小于10加0
                        et_weekend_start_time.setText(new StringBuilder().append(hour < 10 ? "0" + hour : hour).append(":").append(minute < 10 ? "0" + minute : minute).append(":00"));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                break;
            case R.id.et_weekend_end_date:
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener1)
                        .setInitialDate(new Date())
                        .setIs24HourTime(true)
                        .setMaxDate(new Date())
                        .build()
                        .show();
                break;
            case R.id.et_weekend_end_time:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        //更新EditText控件时间 小于10加0
                        et_weekend_end_time.setText(new StringBuilder().append(hour < 10 ? "0" + hour : hour).append(":").append(minute < 10 ? "0" + minute : minute).append(":00"));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                break;
            default:
                break;
        }
    }

    private int getDataSize() {
        return mDataList == null ? 0 : mDataList.size();
    }

    private int getAvailableSize() {
        int availSize = CustomConstants.MAX_IMAGE_SIZE - mDataList.size();
        if (availSize >= 0) {
            return availSize;
        }
        return 0;
    }

    public class PopupWindows extends PopupWindow {

        public PopupWindows(Context mContext, View parent) {

            View view = View.inflate(mContext, R.layout.item_popupwindow, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_ins));
            LinearLayout ll_popup = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.push_bottom_in_2));

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            setFocusable(true);
            setOutsideTouchable(true);
            ColorDrawable dw = new ColorDrawable(0x00000000);
            setBackgroundDrawable(dw);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();
            Button bt1 = (Button) view
                    .findViewById(R.id.item_popupwindows_camera);
            Button bt2 = (Button) view
                    .findViewById(R.id.item_popupwindows_Photo);
            Button bt3 = (Button) view
                    .findViewById(R.id.item_popupwindows_cancel);
            bt1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    takePhoto();
                    dismiss();
                }
            });
            bt2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                    // get the info from the currently running task
                    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    Intent intent = new Intent(Weekend_Create.this,
                            ImageBucketChooseActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_CAN_ADD_IMAGE_SIZE,
                            getAvailableSize());
                    intent.putExtra("classname", "Weekend_Create");
                    startActivity(intent);
                    finish();
                    dismiss();
                    title = et_weekend_title.getText().toString();
                    address = et_weekend_address.getText().toString();
                    cost = et_weekend_cost.getText().toString();
                    date1 = et_weekend_start_date.getText().toString();
                    date2 = et_weekend_end_date.getText().toString();
                    content = et_weekend_content.getText().toString();
                }
            });
            bt3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    private static final int TAKE_PICTURE = 0x000000;
    private static final int TAKE_CAMERA = 0x000001;
    private String path = "";

    public void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File vFile = new File(Environment.getExternalStorageDirectory()
                + "/myimage/", String.valueOf(System.currentTimeMillis())
                + ".jpg");
        if (!vFile.exists()) {
            File vDirPath = vFile.getParentFile();
            vDirPath.mkdirs();
        } else {
            if (vFile.exists()) {
                vFile.delete();
            }
        }
        path = vFile.getPath();
        Uri cameraUri = Uri.fromFile(vFile);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(openCameraIntent, TAKE_CAMERA);
        title = et_weekend_title.getText().toString();
        address = et_weekend_address.getText().toString();
        cost = et_weekend_cost.getText().toString();
        date1 = et_weekend_start_date.getText().toString();
        date2 = et_weekend_end_date.getText().toString();
        content = et_weekend_content.getText().toString();
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    //关闭发布闲置物品显示对话框
    private void crenteCencalDialog() {
        MyDialog dialog = new MyDialog(this);
        dialog.show();
        dialog.setDialogCallback(dialogcallback);
    }

    MyDialog.Dialogcallback dialogcallback = new MyDialog.Dialogcallback() {
        @Override
        public void dialogdo() {
            if (mFromPublish) {
                try {
                    gotoActivityAndFinish(Publish.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    gotoActivityAndFinish(Weekend.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            removeTempFromPref();
        }

        @Override
        public void dialogDismiss() {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            crenteCencalDialog();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            et_weekend_start_date.setText(mFormatter.format(date));
        }
    };

    private SlideDateTimeListener listener1 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            et_weekend_end_date.setText(mFormatter.format(date));
        }
    };

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

    public void initPop(View view) {
        photograph = (TextView) view.findViewById(R.id.photograph);//拍照
        albums = (TextView) view.findViewById(R.id.albums);//相册
        cancel = (LinearLayout) view.findViewById(R.id.cancel);//取消
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mFileCacheDirectory = new File(
                    Environment.getExternalStorageDirectory(),
                    getPackageName());

            // 判断当前目录是否存在
            if (!mFileCacheDirectory.exists()) {
                mFileCacheDirectory.mkdir();
            }

            //图片的存储位置
            mImageUri = Uri.fromFile(new File(mFileCacheDirectory.getPath(), IMAGE_FILE_NAME));
            photograph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                    Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(intent_capture, CAMERA_REQUEST_CODE);
                }
            });
            albums.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                    Intent intent_pick = new Intent(Intent.ACTION_PICK, null);
                    intent_pick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent_pick, IMAGE_REQUEST_CODE);
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                }
            });
        } else {
            showToast("没找到手机SD卡，图片无法上传！");
        }
    }

    //  POST API/Resident/WeekendUpdate
    //  修改周末去哪
    public void weekendUpdate(String id, String title, String sDatetime, String eDatetime, String address, String cost, String imageIds, String content) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Resident/WeekendUpdate";
        // 发起请求
        User user = UserInformation.getUserInfo();
        HashMap<String, String> extras = new HashMap<>();
        extras.put("Id", id);
        extras.put("Title", title);
        extras.put("Memo", content);
        extras.put("Img", imageIds);
        extras.put("ActiveAddress", address);
        extras.put("Cost", cost);
        extras.put("StartDatetime", sDatetime);
        extras.put("EndDatetime", eDatetime);
        extras.put("ContractName", user.getUserName());
        extras.put("ContractTel", user.getUserPhone());
        extras.put("CityId", user.getCommuntiyCityId());
        extras.put("ResidentId", user.getUserId());
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
                .addParams("Id", id)
                .addParams("Title", title)
                .addParams("Memo", content)
                .addParams("Img", imageIds)
                .addParams("ActiveAddress", address)
                .addParams("Cost", cost)
                .addParams("StartDatetime", sDatetime)
                .addParams("EndDatetime", eDatetime)
                .addParams("ContractName", user.getUserName())
                .addParams("ContractTel", user.getUserPhone())
                .addParams("CityId", user.getCommuntiyCityId())
                .addParams("ResidentId", user.getUserId())
                .build().execute(new DataCallBack(this) {
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
                            try {
                                gotoActivityAndFinish(Publish.class.getName(), null);
                                removeTempFromPref();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        IntegralTip(url);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //周末去哪儿 - 创建活动
    public void WeekendCreate(String title, String sDatetime, String eDatetime, String address, String cost, String imageIds, String content) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Resident/WeekendAdd";
        User user = UserInformation.getUserInfo();
        HashMap<String, String> extras = new HashMap<>();
        extras.put("Title", title);
        extras.put("Memo", content);
        extras.put("Img", imageIds);
        extras.put("ActiveAddress", address);
        extras.put("Cost", cost);
        extras.put("StartDatetime", sDatetime);
        extras.put("EndDatetime", eDatetime);
        extras.put("ContractName", user.getUserName());
        extras.put("ContractTel", user.getUserPhone());
        extras.put("CityId", user.getCommuntiyCityId());
        extras.put("ResidentId", user.getUserId());
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
                .addParams("Title", title)
                .addParams("Memo", content)
                .addParams("Img", imageIds)
                .addParams("ActiveAddress", address)
                .addParams("Cost", cost)
                .addParams("StartDatetime", sDatetime)
                .addParams("EndDatetime", eDatetime)
                .addParams("ContractName", user.getUserName())
                .addParams("ContractTel", user.getUserPhone())
                .addParams("CityId", user.getCommuntiyCityId())
                .addParams("ResidentId", user.getUserId())
                .build().execute(new DataCallBack(this) {

            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                showProgressDialog();
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                closeProgressDialog();
            }

            @Override
            public void onResponse(String s, int i) {
                Log.d("asdsdasd", "111111111" + s);
                closeProgressDialog();
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject jsonObject = new JSONObject(json.getString("Data"));
                    if (json.getBoolean("Status")) {
                        if (jsonObject.getBoolean("Valid")) {
                            try {
                                gotoActivityAndFinish(Weekend.class.getName(), null);
                                removeTempFromPref();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            IntegralTip(url);
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

    public void IntegralTip(String pUrl) {

        String url1 = "";
        try {
            pUrl = new URL(pUrl).getPath();
            url1 = Services.mHost + "API/Prints/Add/" + UserInformation.getUserInfo().UserId + "?route=" + pUrl;
            String aa = Services.timeFormat();
            String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
            String aa2 = url1;
            String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
            OkHttpUtils.get().url(url1)
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
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    //头像裁剪方法

    private void onCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 16);// 裁剪框比例
        intent.putExtra("aspectY", 9);
        intent.putExtra("outputX", mCropImageWidth);// 输出图片大小
        intent.putExtra("outputY", mCropImageHeight);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROPIMAGE_REQUEST_CODE);
    }

    protected void onPause() {
        super.onPause();
//        saveTempToPref();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void saveTempToPref() {
        String prefStr = JSON.toJSONString(mDataList);
        sp.edit().putString(CustomConstants.PREF_TEMP_IMAGES, prefStr).commit();
    }

    private void getTempFromPref() {
        String prefStr = sp.getString(CustomConstants.PREF_TEMP_IMAGES, null);
        if (!TextUtils.isEmpty(prefStr)) {
            List<ImageItem> tempImages = JSON.parseArray(prefStr,
                    ImageItem.class);
            mDataList = tempImages;
        }
    }

    private void removeTempFromPref() {
        sp.edit().clear().commit();
        mDataList.clear();
        mImageIds = "";
        title = "";
        address = "";
        cost = "";
        date1 = "";
        date2 = "";
        content = "";
        mFromPublish = false;
        mFreamarketId = "";
    }

    private void initData() {
//        getTempFromPref();
        incomingDataList = (List<ImageItem>) getIntent()
                .getSerializableExtra(IntentConstants.EXTRA_IMAGE_LIST);
        if (incomingDataList != null) {
            showToast("上传图片中...");
            if (services.ping()) {
                mDataList.addAll(incomingDataList);
                saveTempToPref();
                for (int i = mDataList.size() - incomingDataList.size(); i < mDataList.size(); i++) {
                    if (mDataList.get(i).sourcePath.contains("/")) {
                       final int finalI = i;
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                String fileId = services.Upload(Weekend_Create.this, mDataList.get(finalI).sourcePath).FileName;
                                if (Services.isNotNullOrEmpty(mImageIds)) {
                                    mImageIds += "," + fileId;
                                } else {
                                    mImageIds = fileId;
                                }
                            }

                        }, 100);
                    }
                }
            } else {
                showToast("网络连接失败，请检查您的网络");
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDataChanged(); //当在ImageZoomActivity中删除图片时，返回这里需要刷新
        et_weekend_title.setText(title);
        et_weekend_address.setText(address);
        et_weekend_cost.setText(cost);
        et_weekend_start_date.setText(date1);
        et_weekend_end_date.setText(date2);
        et_weekend_content.setText(content);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    onCropImage(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    onCropImage(mImageUri);
                    break;
                case CROPIMAGE_REQUEST_CODE:
                    if (data != null) {
                        //上传到服务器
                        String fileId = services.Upload(this, mImageUri.getPath()).FileName;

                        //上传图片的Ids
                        if (TextUtils.isEmpty(mImageIds)) {
                            mImageIds = fileId;
                        } else {
                            mImageIds += "," + fileId;
                        }

                        //缓存到本地
                        String imageUrl = services.getImageUrl(fileId);
                        mFileCaches.putImageToFileCache(imageUrl, BitmapFactory.decodeFile(mImageUri.getPath()));

                        //创建图片
                        ImageView iv = new ImageView(this);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ll_weekend_picture_list.addView(iv, ll_weekend_picture_list.getChildCount() - 1);
                        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
                        linearParams.setMargins(linearParams.leftMargin, linearParams.topMargin, Utility.dip2px(this, getResources().getDimension(R.dimen.dimen_2dp)), linearParams.bottomMargin);
                        linearParams.width = Utility.dip2px(this, 48f);
                        linearParams.height = Utility.dip2px(this, 48f);
                        iv.setLayoutParams(linearParams);

                        //显示头像
                        ImageLoader.getInstance().displayImage(imageUrl, iv, imageOptions);

                        //最多上传5张照片
                        if (ll_weekend_picture_list.getChildCount() == 6) {
                            ibtn_weekend_pictrue_plus.setVisibility(View.GONE);
                        }
                    }
//                    }
                case TAKE_PICTURE:
                    if (mDataList.size() < CustomConstants.MAX_IMAGE_SIZE
                            && resultCode == -1 && !TextUtils.isEmpty(path)) {
                        ImageItem item = new ImageItem();
                        item.sourcePath = path;
                        mDataList.add(item);
                    }
                    break;
                case TAKE_CAMERA:
                    showToast("上传图片中...");
                    if (services.ping()) {
                        if (mDataList.size() < CustomConstants.MAX_IMAGE_SIZE
                                && resultCode == -1 && !TextUtils.isEmpty(path)) {
                            ImageItem item = new ImageItem();
                            item.sourcePath = path;
                            mDataList.add(item);
                        }
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                String fileId = services.Upload(Weekend_Create.this, mDataList.get(mDataList.size() - 1).sourcePath).FileName;
                                if (TextUtils.isEmpty(mImageIds)) {
                                    mImageIds = fileId;
                                } else {
                                    mImageIds += "," + fileId;
                                }

                            }

                        }, 100);
                    } else {
                        showToast("网络连接失败，请检查您的网络");
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //根据分辨率压缩图片

    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1280f;//这里设置高度为800f
        float ww = 800f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    //图片质量压缩
    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 200) {  //循环判断如果压缩后图片是否大于300kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
