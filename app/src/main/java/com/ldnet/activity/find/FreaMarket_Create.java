package com.ldnet.activity.find;

import android.app.Activity;
import android.app.ActivityManager;
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

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ldnet.activity.adapter.ImageBucketChooseActivity;
import com.ldnet.activity.adapter.ImageChooseActivity;
import com.ldnet.activity.adapter.ImageItem;
import com.ldnet.activity.adapter.ImagePublishAdapter;
import com.ldnet.activity.adapter.ImageZoomActivity;
import com.ldnet.activity.adapter.MyDialog;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.me.Publish;
import com.ldnet.entities.FreaMarketDetails;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.CustomConstants;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.IntentConstants;
import com.ldnet.utility.SDCardFileCache;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.Utility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

public class FreaMarket_Create extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private EditText et_frea_market_title;
    private LinearLayout ll_frea_market_picture_list;
    private ImageButton ibtn_frea_market_pictrue_plus;
    private EditText et_frea_market_content;
    private EditText et_frea_market_new_price;
    private EditText et_frea_market_think_price;
    private Button btn_frea_market_confirm;

    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int CROPIMAGE_REQUEST_CODE = 1002;
    private static final int mCropImageWidth = 1080;
    private static final int mCropImageHeight = 720;
    private static final String IMAGE_FILE_NAME = "FreaMarketImage.jpg";
    private File mFileCacheDirectory;
    private Uri mImageUri;
    private Uri mTemplateImageUri;
    public static String mImageIds;
    private SDCardFileCache mFileCaches;
    private static String mFreamarketId;
    private static Boolean mFromPublish = false;
    private static Boolean mFromFreaMarketDetails = false;
    FreaMarketDetails details;
    private String oldImg = "";
    private String fileId;
    private PopupWindow popWindow;
    private LayoutInflater layoutInflater;
    private TextView photograph, albums;
    private LinearLayout cancel;
    private GridView mGridView;
    private ImagePublishAdapter mAdapter;
    public static List<ImageItem> mDataList = new ArrayList<ImageItem>();
    SharedPreferences sp;
    List<ImageItem> incomingDataList;
    private static String title = "", price = "", price1 = "", content = "";
    private boolean flag = false;
    private String aaa = "";
    private String FreaMarket = "";

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fleamarket_create);
        if (ImageChooseActivity.instance != null) {
            ImageChooseActivity.instance.finish();
        }
        sp = getSharedPreferences(
                CustomConstants.APPLICATION_NAME, MODE_PRIVATE);
        AppUtils.setupUI(findViewById(R.id.ll_freamark_creat), this);
        mFileCaches = new SDCardFileCache(this);
        services = new Services();
        aaa = getIntent().getStringExtra("flag");
        FreaMarket = getIntent().getStringExtra("FreaMarket");
//        if(!Services.isNotNullOrEmpty(aaa)) {
        initData();
//        }
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.frea_market_publish);

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        //初始化表单控件
        et_frea_market_title = (EditText) findViewById(R.id.et_frea_market_title);
        ll_frea_market_picture_list = (LinearLayout) findViewById(R.id.ll_frea_market_picture_list);
//        ibtn_frea_market_pictrue_plus = (ImageButton) findViewById(R.id.ibtn_frea_market_pictrue_plus);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mDataList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == getDataSize()) {
                    new PopupWindows(FreaMarket_Create.this, mGridView);
                } else {
                    Intent intent = new Intent(FreaMarket_Create.this,
                            ImageZoomActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_IMAGE_LIST,
                            (Serializable) mDataList);
                    intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
                    intent.putExtra("classname", "FreaMarket_Create");
                    startActivity(intent);
                    title = et_frea_market_title.getText().toString();
                    price = et_frea_market_new_price.getText().toString();
                    price1 = et_frea_market_think_price.getText().toString();
                    content = et_frea_market_content.getText().toString();
                }
            }
        });
        et_frea_market_content = (EditText) findViewById(R.id.et_frea_market_content);
        et_frea_market_new_price = (EditText) findViewById(R.id.et_frea_market_new_price);
        et_frea_market_think_price = (EditText) findViewById(R.id.et_frea_market_think_price);
        btn_frea_market_confirm = (Button) findViewById(R.id.btn_frea_market_confirm);
        //初始化服务
        //二手商品ID
        if (mFreamarketId != null && !mFreamarketId.equals("")) {
            flag = true;
            et_frea_market_title.setText(title);
            et_frea_market_new_price.setText(price);
            et_frea_market_think_price.setText(price1);
            et_frea_market_content.setText(content);
            btn_frea_market_confirm.setText("修改发布");
            tv_main_title.setText("修改闲置物品信息");
        } else {
            mFreamarketId = getIntent().getStringExtra("FREA_MARKET_ID");
        }
        String formPublish = getIntent().getStringExtra("FROM_PUBLISH");
        String fromFreaMarketDetails = getIntent().getStringExtra("FROM_FREAMARKET_DETAILS");
        if (!TextUtils.isEmpty(formPublish) || !TextUtils.isEmpty(fromFreaMarketDetails)) {
            mFromPublish = Boolean.valueOf(formPublish);
            mFromFreaMarketDetails = Boolean.valueOf(fromFreaMarketDetails);
        }

        initEvent();
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //来自我的发布，显示修改信息
        if (mFromPublish || mFromFreaMarketDetails) {
            if (!flag) {
                FreaMarketDetails(mFreamarketId);
            }
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
                    Intent intent = new Intent(FreaMarket_Create.this,
                            ImageBucketChooseActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_CAN_ADD_IMAGE_SIZE,
                            getAvailableSize());
                    intent.putExtra("classname", "FreaMarket_Create");
                    startActivity(intent);
                    finish();
                    title = et_frea_market_title.getText().toString();
                    price = et_frea_market_new_price.getText().toString();
                    price1 = et_frea_market_think_price.getText().toString();
                    content = et_frea_market_content.getText().toString();
                    dismiss();
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
        title = et_frea_market_title.getText().toString();
        price = et_frea_market_new_price.getText().toString();
        price1 = et_frea_market_think_price.getText().toString();
        content = et_frea_market_content.getText().toString();
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    //跳蚤市场 - 获取闲置物品的详情
    public void FreaMarketDetails(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Resident/GetUnusedGoodsById/%s?residentId=%s";
        url = String.format(url, id, UserInformation.getUserInfo().UserId);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5)).build()
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
                                    et_frea_market_title.setText(details.Title);
                                    et_frea_market_new_price.setText(details.OrgPrice);
                                    et_frea_market_think_price.setText("" + details.Price);
                                    et_frea_market_content.setText(details.Memo);
                                    btn_frea_market_confirm.setText("修改发布");
                                    tv_main_title.setText("修改闲置物品信息");
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

    //  POST API/Resident/UnUsedGoodsUpdate
    //  修改闲置物品
    public void unUsedGoodsUpdate(String id, String title, String content, String imageIds, String newPrice, String thinkPrice) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Resident/UnUsedGoodsUpdate";
        // 发起请求
        User user = UserInformation.getUserInfo();
        HashMap<String, String> extras = new HashMap<>();
        extras.put("Id", id);
        extras.put("Title", title);
        extras.put("Memo", content);
        extras.put("Img", imageIds);
        extras.put("Address", user.getCommuntiyAddress() + user.getCommuntiyName());
        extras.put("OrgPrice", newPrice);
        extras.put("Price", thinkPrice);
        extras.put("ContractName", user.getUserName());
        extras.put("ContractTel", user.getUserPhone());
        extras.put("CityId", user.getCommuntiyCityId());
        extras.put("ResidentId", user.getUserId());
        extras.put("CommunityId", user.getCommunityId());
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
                .addParams("Address", user.getCommuntiyAddress() + user.getCommuntiyName())
                .addParams("OrgPrice", newPrice)
                .addParams("Price", thinkPrice)
                .addParams("ContractName", user.getUserName())
                .addParams("ContractTel", user.getUserPhone())
                .addParams("CityId", user.getCommuntiyCityId())
                .addParams("ResidentId", user.getUserId())
                .addParams("CommunityId", user.getCommunityId())
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
                            showToast("修改成功");
                            try {
                                gotoActivityAndFinish(Publish.class.getName(), null);
                                removeTempFromPref();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            IntegralTip(url);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //跳蚤市场 - 新增闲置
    public void FreaMarketCreate(String title, String content, String imageIds, String newPrice, String thinkPrice) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "API/Resident/UnUsedGoodsAdd";
        User user = UserInformation.getUserInfo();
        HashMap<String, String> extras = new HashMap<>();
        extras.put("Title", title);
        extras.put("Memo", content);
        extras.put("Img", imageIds);
        extras.put("Address", user.getCommuntiyAddress() + user.getCommuntiyName());
        extras.put("OrgPrice", newPrice);
        extras.put("Price", thinkPrice);
        extras.put("ContractName", user.getUserName());
        extras.put("ContractTel", user.getUserPhone());
        extras.put("CityId", user.getCommuntiyCityId());
        extras.put("ResidentId", user.getUserId());
        extras.put("CommunityId", user.getCommunityId());
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        Log.d("----", imageIds);
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addParams("Title", title)
                .addParams("Memo", content)
                .addParams("Img", imageIds)
                .addParams("Address", user.getCommuntiyAddress() + user.getCommuntiyName())
                .addParams("OrgPrice", newPrice)
                .addParams("Price", thinkPrice)
                .addParams("ContractName", user.getUserName())
                .addParams("ContractTel", user.getUserPhone())
                .addParams("CityId", user.getCommuntiyCityId())
                .addParams("ResidentId", user.getUserId())
                .addParams("CommunityId", user.getCommunityId())
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
                            showToast("新增成功");
                            try {
                                gotoActivityAndFinish(FreaMarket.class.getName(), null);
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

    public boolean isNull() {
        if (TextUtils.isEmpty(et_frea_market_title.getText().toString().trim())) {
            showToast("标题不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_frea_market_new_price.getText().toString().trim())) {
            showToast("新品价格不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_frea_market_think_price.getText().toString().trim())) {
            showToast("一口价不能为空");
            return false;
        }
        if (mFromPublish || mFromFreaMarketDetails) {
            if (TextUtils.isEmpty(mImageIds)) {
                mImageIds = "";
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
        if (TextUtils.isEmpty(et_frea_market_content.getText().toString().trim())) {
            showToast("介绍不能为空");
            return false;
        }
        return true;
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_frea_market_confirm.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://判断是否取消
                crenteCencalDialog();
                break;
            case R.id.btn_frea_market_confirm://发布/修改
                String title = et_frea_market_title.getText().toString().trim();
                String content = et_frea_market_content.getText().toString().trim();
                String newPrice = et_frea_market_new_price.getText().toString().trim();
                String thinkPrice = et_frea_market_think_price.getText().toString().trim();
                // 判断是否来自我的发布,修改
                if (mFromPublish || mFromFreaMarketDetails) {
                    if (isNull()) {
                        unUsedGoodsUpdate(mFreamarketId, title, content, oldImg + mImageIds, newPrice, thinkPrice);
                    }
                    //发布
                } else {
                    if (isNull()) {
                        FreaMarketCreate(title, content, mImageIds, newPrice, thinkPrice);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            crenteCencalDialog();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

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
            removeTempFromPref();
            finish();
        }

        @Override
        public void dialogDismiss() {
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

    //头像裁剪方法
    private void onCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 3);// 裁剪框比例
        intent.putExtra("aspectY", 2);
        intent.putExtra("outputX", mCropImageWidth);// 输出图片大小
        intent.putExtra("outputY", mCropImageHeight);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROPIMAGE_REQUEST_CODE);
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
                        ImageView iv = new ImageView(FreaMarket_Create.this);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ll_frea_market_picture_list.addView(iv, ll_frea_market_picture_list.getChildCount() - 1);
                        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
                        linearParams.setMargins(linearParams.leftMargin, linearParams.topMargin, Utility.dip2px(FreaMarket_Create.this, getResources().getDimension(R.dimen.dimen_2dp)), linearParams.bottomMargin);
                        linearParams.width = Utility.dip2px(FreaMarket_Create.this, 48f);
                        linearParams.height = Utility.dip2px(FreaMarket_Create.this, 48f);
                        iv.setLayoutParams(linearParams);

                        //显示头像
                        ImageLoader.getInstance().displayImage(imageUrl, iv, imageOptions);

                        //最多上传5张照片
                        if (ll_frea_market_picture_list.getChildCount() == 6) {
                            ibtn_frea_market_pictrue_plus.setVisibility(View.GONE);
                        }
                    }
//                    }
                    break;
                case TAKE_PICTURE:
                    if (mDataList.size() < CustomConstants.MAX_IMAGE_SIZE
                            && resultCode == -1 && !TextUtils.isEmpty(path)) {
                        ImageItem item = new ImageItem();
                        item.sourcePath = path;
                        mDataList.add(item);
                    }
                    break;
                case TAKE_CAMERA:
                    if (mDataList.size() < CustomConstants.MAX_IMAGE_SIZE
                            && resultCode == -1 && !TextUtils.isEmpty(path)) {
                        ImageItem item = new ImageItem();
                        item.sourcePath = path;
                        mDataList.add(item);
                    }
                    showToast("上传图片中...");
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (services.ping()) {
                                // TODO Auto-generated method stub
                                String fileId = services.Upload(FreaMarket_Create.this, mDataList.get(mDataList.size() - 1).sourcePath).FileName;
                                if (TextUtils.isEmpty(mImageIds)) {
                                    mImageIds = fileId;
                                } else {
                                    mImageIds += "," + fileId;
                                }
                            } else {
                                showToast("网络连接失败，请检查您的网络");
                                return;
                            }
                        }

                    }, 100);

                    break;
                default:
                    break;
            }
        }
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
        price = "";
        price1 = "";
        content = "";
        mFromPublish = false;
        mFromFreaMarketDetails = false;
        mFreamarketId = "";
    }

    @SuppressWarnings("unchecked")
    private void initData() {
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
                                String fileId = services.Upload(FreaMarket_Create.this, mDataList.get(finalI).sourcePath).FileName;
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
        Log.d("ssssssssssssss", "111111111111" + mImageIds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDataChanged(); //当在ImageZoomActivity中删除图片时，返回这里需要刷新
        et_frea_market_title.setText(title);
        et_frea_market_new_price.setText(price);
        et_frea_market_think_price.setText(price1);
        et_frea_market_content.setText(content);
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
                    .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                    .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                    .addHeader("timestamp", aa)
                    .addHeader("nonce", aa1)
                    .addHeader("signature", Services.textToMD5L32(md5))
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
}
