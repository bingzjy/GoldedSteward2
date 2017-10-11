package com.ldnet.activity.home;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ldnet.activity.adapter.*;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.find.FreaMarket_Create;
import com.ldnet.activity.me.Community;
import com.ldnet.entities.FileUpload;
import com.ldnet.entities.HouseRent;
import com.ldnet.entities.StatusBoolean;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.utility.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Property_Complain_Create extends BaseActionBarActivity {
    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private TextView tv_repair_houseinfo;
    private TextView tv_property_tel, tv_property_name;
    private ImageButton ibtn_complain_change_house;
    private EditText et_complain_content;
    private GridView ibtn_complain_picture_add;
    private Button btn_complain_call_property;
    private Button btn_complain_confirm;
    private LinearLayout ll_complain_picture_list;
    private SDCardFileCache mFileCaches;

    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final String IMAGE_FILE_NAME = "RepairComplainImage.jpg";
    private static final String IMAGE_TEMP_FILE_NAME = "RCITemplate.jpg";
    private File mFileCacheDirectory;
    private Uri mImageUri;
    private Uri mTemplateImageUri;
    public static String mImageIds;
    private String fileId;
    private PopupWindow popWindow;
    private LayoutInflater layoutInflater;
    private TextView photograph, albums;
    private LinearLayout cancel;
    private ImagePublishAdapter mAdapter;
    public static List<ImageItem> mDataList = new ArrayList<ImageItem>();
    SharedPreferences sp;
    List<ImageItem> incomingDataList;
    private static String content = "";
    private String aaa = "";
    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_property_complain_create);
        AppUtils.setupUI(findViewById(R.id.ll_complain_create), this);
        sp = getSharedPreferences(
                CustomConstants.APPLICATION_NAME, MODE_PRIVATE);
        services = new Services();
        aaa = getIntent().getStringExtra("flag");
//        if(!Services.isNotNullOrEmpty(aaa)) {
        initData();
//        }
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText("添加投诉");

        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);

        //初始化页面控件
        User user = UserInformation.getUserInfo();
        tv_repair_houseinfo = (TextView) findViewById(R.id.tv_complain_houseinfo);
        tv_repair_houseinfo.setText(user.CommuntiyName + "(" + user.HouseName + ")");

        et_complain_content = (EditText) findViewById(R.id.et_complain_content);
        ibtn_complain_change_house = (ImageButton) findViewById(R.id.ibtn_complain_change_house);
        ibtn_complain_picture_add = (GridView) findViewById(R.id.ibtn_complain_picture_add);
        ibtn_complain_picture_add.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mDataList);
        ibtn_complain_picture_add.setAdapter(mAdapter);
        ibtn_complain_picture_add.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == getDataSize()) {
                    new PopupWindows(Property_Complain_Create.this, ibtn_complain_picture_add);
                } else {
                    Intent intent = new Intent(Property_Complain_Create.this,
                            ImageZoomActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_IMAGE_LIST,
                            (Serializable) mDataList);
                    intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
                    intent.putExtra("classname", "Property_Complain_Create");
                    startActivity(intent);
                    content = et_complain_content.getText().toString();
                }
            }
        });
        btn_complain_call_property = (Button) findViewById(R.id.btn_complain_call_property);
        btn_complain_confirm = (Button) findViewById(R.id.btn_complain_confirm);
        ll_complain_picture_list = (LinearLayout) findViewById(R.id.ll_complain_picture_list);
        tv_property_tel = (TextView) findViewById(R.id.tv_property_tel);
        tv_property_name = (TextView) findViewById(R.id.tv_property_name);
        tv_property_tel.setText(UserInformation.getUserInfo().getUserPhone());
        tv_property_name.setText(UserInformation.getUserInfo().getUserName());
        //初始化服务
        mFileCaches = new SDCardFileCache(this);

        initEvent();
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    Intent intent = new Intent(Property_Complain_Create.this,
                            ImageBucketChooseActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_CAN_ADD_IMAGE_SIZE,
                            getAvailableSize());
                    intent.putExtra("classname", "Property_Complain_Create");
                    startActivity(intent);
                    finish();
                    content = et_complain_content.getText().toString();
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
        content = et_complain_content.getText().toString();
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        ibtn_complain_change_house.setOnClickListener(this);
        btn_complain_call_property.setOnClickListener(this);
        btn_complain_confirm.setOnClickListener(this);
//        ibtn_complain_picture_add.setOnClickListener(this);
//        registerForContextMenu(ibtn_complain_picture_add);//注册菜单
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                MyDialog dialog = new MyDialog(this);
                dialog.show();
                dialog.setDialogCallback(dialogcallback);
                break;
            case R.id.ibtn_complain_change_house:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("NOT_FROM_ME", "104");
                    extras.put("LEFT", "LEFT");
                    gotoActivity(Community.class.getName(), extras);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_complain_call_property:
                //给物业打电话
                String phone = UserInformation.getUserInfo().PropertyPhone;
                if (!TextUtils.isEmpty(phone)) {
                    phone = "tel:" + phone;
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(phone));
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                } else {
                    showToast(getString(R.string.property_phone_none));
                }
                break;
//            case R.id.ibtn_complain_picture_add:
//                //弹出菜单
////                openContextMenu(ibtn_complain_picture_add);
//                showPopupWindow(ibtn_complain_picture_add);
//                break;
            case R.id.btn_complain_confirm:
                //提交投诉
                if (TextUtils.isEmpty(mImageIds)) {
                    mImageIds = "";
                }
                if (!TextUtils.isEmpty(et_complain_content.getText().toString().trim())) {
                    showProgressDialog();
                    InsertComplain(mImageIds, et_complain_content.getText().toString().trim());
                } else {
                    showToast("投诉内容不能为空");
                }
                break;
            default:
                break;
        }
    }

    MyDialog.Dialogcallback dialogcallback = new MyDialog.Dialogcallback() {
        @Override
        public void dialogdo() {
            try {
                gotoActivityAndFinish(Property_Repair.class.getName(), null);
                removeTempFromPref();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dialogDismiss() {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            MyDialog dialog = new MyDialog(this);
            dialog.show();
            dialog.setDialogCallback(dialogcallback);
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    //投诉
    public void InsertComplain(String images, String content) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        final String url = Services.mHost + "WFComplaint/APP_YZ_CreateComplaint";
        User user = UserInformation.getUserInfo();
        if (TextUtils.isEmpty(mImageIds)) {
            mImageIds = "";
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ResidentId", user.UserId);
            jsonObject.put("Name", user.getUserName());
            jsonObject.put("Tel", user.getUserPhone());
            jsonObject.put("Content", et_complain_content.getText().toString());
            jsonObject.put("CommunityId", user.CommunityId);
            jsonObject.put("ContentImg", mImageIds);
            jsonObject.put("RoomId", user.HouseId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String dd = "{" + "\"str\"" + ":" + "\"" + jsonObject.toString() + "\"}";
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + dd + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32
                        (md5))
                .addParams("str", jsonObject.toString())
                .build()
                .execute(new DataCallBack(this) {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        closeProgressDialog();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.e("asdsdasd", "111111111" + s);
                        closeProgressDialog();
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    try {
                                        Services.IntegralTip(url);
                                        HashMap<String, String> extras = new HashMap<String, String>();
                                        extras.put("LEFT", "LEFT");
                                        gotoActivityAndFinish(Property_Complain.class.getName(), extras);
                                        removeTempFromPref();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
            mTemplateImageUri = Uri.fromFile(new File(mFileCacheDirectory.getPath(), IMAGE_TEMP_FILE_NAME));
            photograph.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    popWindow.dismiss();
                    Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, mTemplateImageUri);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            //图片处理，另存为数据较小的JPEG文件，然后上传
//            String mImgPath = "";
//            if (requestCode == IMAGE_REQUEST_CODE) {
//                Uri selectedImage = data.getData();
//                String[] filePathColumn = {MediaStore.Images.Media.DATA};
//                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
//                if (cursor != null) {
//                    cursor.moveToFirst();
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    String imageAbsolutePath = cursor.getString(columnIndex);
//                    cursor.close();
//                    mImgPath = imageAbsolutePath;
//                } else {
//                    mImgPath = selectedImage.getPath();
//                }
//            } else if (requestCode == CAMERA_REQUEST_CODE) {
//                mImgPath = mTemplateImageUri.getPath();
//            }
//
//            Bitmap image = getimage(mImgPath);
//            if (image != null) {
//                //上传服务器
//                // 获取本地文件
//                File file = new File(mImageUri.getPath());
//                try {
//                    // 将图片写入到文件
//                    FileOutputStream fileOutStream = new FileOutputStream(file);
//                    image.compress(Bitmap.CompressFormat.JPEG, 100,
//                            fileOutStream);
//                    fileOutStream.flush();
//                    fileOutStream.close();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//                //上传到服务器
//                String fileId = services.Upload(mImageUri.getPath()).FileName;
//
//                //上传图片的Ids
//                if (TextUtils.isEmpty(mImageIds)) {
//                    mImageIds = fileId;
//                } else {
//                    mImageIds += "," + fileId;
//                }
//
//                //缓存到本地
//                String imageUrl = services.getImageUrl(fileId);
//                mFileCaches.putImageToFileCache(imageUrl, image);
//
//                //创建图片
//                ImageView iv = new ImageView(this);
//                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                ll_complain_picture_list.addView(iv, ll_complain_picture_list.getChildCount() - 1);
//                LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
//                linearParams.setMargins(linearParams.leftMargin, linearParams.topMargin, Utility.dip2px(this, getResources().getDimension(R.dimen.dimen_2dp)), linearParams.bottomMargin);
//                linearParams.width = Utility.dip2px(this, getResources().getDimension(R.dimen.dimen_31dp));
//                linearParams.height = Utility.dip2px(this, getResources().getDimension(R.dimen.dimen_31dp));
//                iv.setLayoutParams(linearParams);
//                //显示头像
//                ImageLoader.getInstance().displayImage(imageUrl, iv, GSApplication.getInstance().imageOptions);
//                //最多上传6张照片
//                if (ll_complain_picture_list.getChildCount() == 6) {
//                    ibtn_complain_picture_add.setVisibility(View.GONE);
//                }
//            }
            switch (requestCode) {
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
                            // TODO Auto-generated method stub
                            if (services.ping()) {
                                String fileId = services.Upload(Property_Complain_Create.this, mDataList.get(mDataList.size() - 1).sourcePath).FileName;
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
        content = "";
    }

    @SuppressWarnings("unchecked")
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
                                String fileId = services.Upload(Property_Complain_Create.this, mDataList.get(finalI).sourcePath).FileName;
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
        et_complain_content.setText(content);
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
        while (baos.toByteArray().length / 1024 > 300) {  //循环判断如果压缩后图片是否大于300kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
