package com.ldnet.activity.home;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.adapter.*;
import com.ldnet.activity.base.AppUtils;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.find.*;
import com.ldnet.activity.me.*;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.utility.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Murray on 2015/9/6.
 */
public class HouseRent_Create extends BaseActionBarActivity {

    //控件
    private LinearLayout ll_houserent_images;
    private ImageButton btn_back;
    private GridView addhouserent_add;
    private TextView tv_page_title;
    private EditText et_house_rent_detail_title, et_house_rent_detail_room, et_house_rent_detail_hall, et_house_rent_detail_toilet,
            et_house_rent_detail_area, et_house_rent_detail_floor, et_house_rent_detail_total_floor, et_house_rent_detail_room_number,
            et_house_rent_detail_tel, id_spinner_room_config_memo, et_house_rent_detail_rent;
    private CheckBox addhouserent_rdoleft;
    private Spinner id_spinner_orientation, id_spinner_fitment, id_spinner_roomtype, id_spinner_renttype, id_spinner_room_config;
    private Button btn_house_rent_create;

    //标题，室，厅，卫，面积，几层，总层数，房号，联系电话，带家具备注，租赁价格
    private static String title, room, hall, toilet, area, floor, totalfloor, roomNumber, tel, configMemo, rentPrice, isElevator;
    public static String mImageIds;
    //服务
    private Services service;
    private static HouseProperties mHouseProperties;
    private ArrayAdapter<KValues> adapterOrientation, adapterFitmentType, adapterRoomType, adapterRoomDeploy, adapterRentType;
    private boolean Borientation = false, Bfitment = false, Broomtype = false, Brenttype = false, Broom_config = false;
    //其他
    /* 请求码*/
    private static final int IMAGE_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int CROPIMAGE_REQUEST_CODE = 1002;
    private static final int mCropImageWidth = 1080;
    private static final int mCropImageHeight = 720;
    private static final String IMAGE_FILE_NAME = "RentImage.jpg";
    private File mFileCacheDirectory;
    private Uri mImageUri;
    private SDCardFileCache mFileCaches;
    private String fileId;
    private PopupWindow popWindow;
    private LayoutInflater layoutInflater;
    private TextView photograph, albums;
    private LinearLayout cancel;
    private ImagePublishAdapter mAdapter;
    public static List<ImageItem> mDataList = new ArrayList<ImageItem>();
    SharedPreferences sp;
    List<ImageItem> incomingDataList;
    private String aaa = "";

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
        btn_house_rent_create.setOnClickListener(this);
    }

    //初始化视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载布局
        setContentView(R.layout.house_rent_layout);
        if (ImageChooseActivity.instance != null) {
            ImageChooseActivity.instance.finish();
        }
        AppUtils.setupUI(findViewById(R.id.ll_house_rent), this);
        sp = getSharedPreferences(
                CustomConstants.APPLICATION_NAME, MODE_PRIVATE);
        //服务初始化
        service = new Services();
        aaa = getIntent().getStringExtra("flag");
//        if(!Services.isNotNullOrEmpty(aaa)) {
        initData();
//        }
        mFileCaches = new SDCardFileCache(getApplication());

        //页面标题
        tv_page_title = (TextView) findViewById(R.id.tv_page_title);
        tv_page_title.setText(R.string.fragment_home_yellow_lease);
        //返回按钮
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_house_rent_create = (Button) findViewById(R.id.btn_house_rent_create);
        //房屋照片
        ll_houserent_images = (LinearLayout) findViewById(R.id.ll_houserent_images);
        addhouserent_add = (GridView) findViewById(R.id.addhouserent_add);
        addhouserent_add.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mDataList);
        addhouserent_add.setAdapter(mAdapter);
        addhouserent_add.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == getDataSize()) {
                    new PopupWindows(HouseRent_Create.this, addhouserent_add);
                } else {
                    Intent intent = new Intent(HouseRent_Create.this,
                            ImageZoomActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_IMAGE_LIST,
                            (Serializable) mDataList);
                    intent.putExtra(IntentConstants.EXTRA_CURRENT_IMG_POSITION, position);
                    intent.putExtra("classname", "HouseRent_Create");
                    startActivity(intent);
                    getUserInput();
                }
            }
        });
//        registerForContextMenu(addhouserent_add);
        //是否带电梯
        addhouserent_rdoleft = (CheckBox) findViewById(R.id.addhouserent_rdoleft);
        //初始化EditText
        et_house_rent_detail_title = (EditText) findViewById(R.id.et_house_rent_detail_title);
        et_house_rent_detail_room = (EditText) findViewById(R.id.et_house_rent_detail_room);
        et_house_rent_detail_hall = (EditText) findViewById(R.id.et_house_rent_detail_hall);
        et_house_rent_detail_toilet = (EditText) findViewById(R.id.et_house_rent_detail_toilet);
        et_house_rent_detail_area = (EditText) findViewById(R.id.et_house_rent_detail_area);
        et_house_rent_detail_floor = (EditText) findViewById(R.id.et_house_rent_detail_floor);
        et_house_rent_detail_total_floor = (EditText) findViewById(R.id.et_house_rent_detail_total_floor);
        et_house_rent_detail_room_number = (EditText) findViewById(R.id.et_house_rent_detail_room_number);
//        et_house_rent_detail_room_number.addTextChangedListener(textWatcher);
        et_house_rent_detail_tel = (EditText) findViewById(R.id.et_house_rent_detail_tel);
        id_spinner_room_config_memo = (EditText) findViewById(R.id.id_spinner_room_config_memo);
        et_house_rent_detail_rent = (EditText) findViewById(R.id.et_house_rent_detail_rent);

        //下拉spinner
        id_spinner_orientation = (Spinner) findViewById(R.id.id_spinner_orientation);
        id_spinner_fitment = (Spinner) findViewById(R.id.id_spinner_fitment);
        id_spinner_roomtype = (Spinner) findViewById(R.id.id_spinner_roomtype);
        id_spinner_room_config = (Spinner) findViewById(R.id.id_spinner_room_config);
        id_spinner_renttype = (Spinner) findViewById(R.id.id_spinner_renttype);

        initEvent();
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //获取房屋信息中的配置信息
        getHouseRentInfo();

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
                    Intent intent = new Intent(HouseRent_Create.this,
                            ImageBucketChooseActivity.class);
                    intent.putExtra(IntentConstants.EXTRA_CAN_ADD_IMAGE_SIZE,
                            getAvailableSize());
                    intent.putExtra("classname", "HouseRent_Create");
                    startActivity(intent);
                    finish();
                    getUserInput();
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
        getUserInput();
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void data() {
        try {
            //朝向
            adapterOrientation = new ArrayAdapter<KValues>(this, R.layout.dropdown_check_item, mHouseProperties.getOrientation());
            adapterOrientation.setDropDownViewResource(R.layout.dropdown_item);
            id_spinner_orientation.setAdapter(adapterOrientation);
            id_spinner_orientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                    TextView tv= (TextView) view;
//                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    Borientation = true;
                    try {
                        service.orientation = mHouseProperties.getOrientation().get(i).Key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            //
            adapterFitmentType = new ArrayAdapter<KValues>(this, R.layout.dropdown_check_item, mHouseProperties.getFitmentType());
            //设置下拉列表的风格
            adapterFitmentType.setDropDownViewResource(R.layout.dropdown_item);
            id_spinner_fitment.setAdapter(adapterFitmentType);
            id_spinner_fitment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Bfitment = true;
                    try {
                        service.fitment = mHouseProperties.getFitmentType().get(i).Key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            //
            adapterRoomType = new ArrayAdapter<KValues>(this, R.layout.dropdown_check_item, mHouseProperties.getRoomType());
            //设置下拉列表的风格
            adapterRoomType.setDropDownViewResource(R.layout.dropdown_item);
            id_spinner_roomtype.setAdapter(adapterRoomType);
            id_spinner_roomtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Broomtype = true;
                    try {
                        service.roomType = mHouseProperties.getRoomType().get(i).Key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            //
            adapterRoomDeploy = new ArrayAdapter<KValues>(this, R.layout.dropdown_check_item, mHouseProperties.getRoomDeploy());
            adapterRoomDeploy.setDropDownViewResource(R.layout.dropdown_item);
            id_spinner_room_config.setAdapter(adapterRoomDeploy);
            id_spinner_room_config.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Broom_config = true;
                    try {
                        service.roomConfig = mHouseProperties.getRoomDeploy().get(i).Key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            //
            adapterRentType = new ArrayAdapter<KValues>(this, R.layout.dropdown_check_item, mHouseProperties.getRentType());
            adapterRentType.setDropDownViewResource(R.layout.dropdown_item);
            id_spinner_renttype.setAdapter(adapterRentType);
            id_spinner_renttype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Brenttype = true;
                    try {
                        service.rentType = mHouseProperties.getRentType().get(i).Key;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取房屋租赁信息
    public void getHouseRentInfo() {
        String url = Services.mHost + "API/Property/RentailSaleSelect";
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
                            JSONObject jsonObject1 = new JSONObject(jsonObject.getString("Obj"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    mHouseProperties = new HouseProperties();
                                    mHouseProperties.setOrientation(jsonObject1.getString("Orientation"));
                                    mHouseProperties.setFitmentType(jsonObject1.getString("FitmentType"));
                                    mHouseProperties.setRentType(jsonObject1.getString("RentType"));
                                    mHouseProperties.setRoomDeploy(jsonObject1.getString("RoomDeploy"));
                                    mHouseProperties.setRoomType(jsonObject1.getString("RoomType"));
                                    data();
                                    et_house_rent_detail_title.setText(title);
                                    et_house_rent_detail_room.setText(room);
                                    et_house_rent_detail_hall.setText(hall);
                                    et_house_rent_detail_toilet.setText(toilet);
                                    et_house_rent_detail_area.setText(area);
                                    et_house_rent_detail_floor.setText(floor);
                                    et_house_rent_detail_total_floor.setText(totalfloor);
                                    et_house_rent_detail_tel.setText(tel);
                                    et_house_rent_detail_rent.setText(rentPrice);
                                    if (isElevator != null && !isElevator.equals("")) {
                                        if (isElevator.equals("true")) {
                                            addhouserent_rdoleft.setChecked(true);
                                        } else {
                                            addhouserent_rdoleft.setChecked(false);
                                        }
                                    }
                                    if (Services.isNotNullOrEmpty(service.orientation)) {
                                        if (!service.orientation.equals("-1")) {
                                            id_spinner_orientation.setSelection(Integer.valueOf(service.orientation) + 1, true);
                                        }
                                    }
                                    if (Services.isNotNullOrEmpty(service.rentType)) {
                                        if (!service.rentType.equals("-1")) {
                                            id_spinner_renttype.setSelection(Integer.valueOf(service.rentType) + 1, true);
                                        }
                                    }
                                    if (Services.isNotNullOrEmpty(service.fitment)) {
                                        if (!service.fitment.equals("-1")) {
                                            id_spinner_fitment.setSelection(Integer.valueOf(service.fitment) + 1, true);
                                        }
                                    }
                                    if (Services.isNotNullOrEmpty(service.roomType)) {
                                        if (!service.roomType.equals("-1")) {
                                            id_spinner_roomtype.setSelection(Integer.valueOf(service.roomType) + 1, true);
                                        }
                                    }
                                    if (Services.isNotNullOrEmpty(service.roomConfig)) {
                                        if (!service.roomConfig.equals("-1")) {
                                            id_spinner_room_config.setSelection(Integer.valueOf(service.roomConfig) + 1, true);
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

    public boolean isNull() {
        if (TextUtils.isEmpty(mImageIds)) {
            showToast("请选择照片");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_title.getText().toString().trim())) {
            showToast("标题不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_room.getText().toString().trim())) {
            showToast("房室不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_hall.getText().toString().trim())) {
            showToast("厅室不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_toilet.getText().toString().trim())) {
            showToast("卫室不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_area.getText().toString().trim())) {
            showToast("面积不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_floor.getText().toString().trim())) {
            showToast("楼层不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_total_floor.getText().toString().trim())) {
            showToast("总楼层不能为空");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_tel.getText().toString().trim())) {
            showToast("联系方式不能为空");
            return false;
        }
        if (service.orientation.equals("-1")) {
            showToast("请选择房屋朝向");
            return false;
        }
        if (service.fitment.equals("-1")) {
            showToast("请选择装修情况");
            return false;
        }
        if (service.roomType.equals("-1")) {
            showToast("请选择住宅类型");
            return false;
        }
        if (service.rentType.equals("-1")) {
            showToast("请选择租金类型");
            return false;
        }
        if (service.roomConfig.equals("-1")) {
            showToast("请选择房屋配置");
            return false;
        }
        if (TextUtils.isEmpty(et_house_rent_detail_rent.getText().toString().trim())) {
            showToast("租金不能为空");
            return false;
        }
        return true;
    }


    private void getUserInput() {
        //标题，室，厅，卫，面积，几层，总层数，房号，联系电话，带家具备注，租赁价格
        title = et_house_rent_detail_title.getText().toString();
        room = et_house_rent_detail_room.getText().toString();
        hall = et_house_rent_detail_hall.getText().toString();
        toilet = et_house_rent_detail_toilet.getText().toString();
        area = et_house_rent_detail_area.getText().toString();
        floor = et_house_rent_detail_floor.getText().toString();
        totalfloor = et_house_rent_detail_total_floor.getText().toString();
        tel = et_house_rent_detail_tel.getText().toString();
        rentPrice = et_house_rent_detail_rent.getText().toString();
        isElevator = String.valueOf(addhouserent_rdoleft.isChecked());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_house_rent_create://发布
                //是否带电梯
                isElevator = String.valueOf(addhouserent_rdoleft.isChecked());
                User user = UserInformation.getUserInfo();
                if (isNull()) {
                    getUserInput();
                    addHouseRent("", user.CommunityId, title, title,
                            room, hall, toilet, area,
                            floor, totalfloor, service.orientation, service.fitment,
                            service.roomType, service.roomConfig, rentPrice, service.rentType, mImageIds, tel, "1", isElevator, user.UserId);
                }
                break;
            case R.id.btn_back:
                crenteCencalDialog();
                break;
            case R.id.addhouserent_add:
                showPopupWindow(addhouserent_add);
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

    //96房屋租赁添加
    public void addHouseRent(String Id, String CommunityId, String Title,
                             String Abstract, String Room, String Hall,
                             String Toilet, String Acreage, String Floor,
                             String FloorCount, String Orientation, String FitmentType,
                             String RoomType, String RoomDeploy, String Price,
                             String RentType, String Images, String ContactTel,
                             String Address, String Elevator, String Creator) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String url = Services.mHost + "API/Property/RentalSaleAdd";
        HashMap<String, String> extras = new HashMap<>();
        extras.put("RentType", RentType);
        extras.put("CommunityId", CommunityId);
        extras.put("Orientation", Orientation);
        extras.put("Creator", Creator);
        extras.put("Toilet", Toilet);
        extras.put("FloorCount", FloorCount);
        extras.put("Title", Title);
        extras.put("Floor", Floor);
        extras.put("Room", Room);
        extras.put("Hall", Hall);
        extras.put("RoomType", RoomType);
        extras.put("Price", Price);
        extras.put("Address", Address);
        extras.put("Acreage", Acreage);
        extras.put("Id", Id);
        extras.put("Elevator", Elevator);
        extras.put("RoomDeploy", RoomDeploy);
        extras.put("ContactTel", ContactTel);
        extras.put("FitmentType", FitmentType);
        extras.put("Abstract", Abstract);
        extras.put("Images", Images);
        Services.json(extras);
        String md5 = UserInformation.getUserInfo().getUserPhone() +
                aa + aa1 + Services.json(extras) + Services.TOKEN;
        OkHttpUtils.post().url(url)
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addParams("Id", Id)
                .addParams("CommunityId", CommunityId)
                .addParams("Title", Title)
                .addParams("Abstract", Abstract)
                .addParams("Room", Room)
                .addParams("Hall", Hall)
                .addParams("Toilet", Toilet)
                .addParams("Acreage", Acreage)
                .addParams("Floor", Floor)
                .addParams("FloorCount", FloorCount)
                .addParams("Orientation", Orientation)
                .addParams("FitmentType", FitmentType)
                .addParams("RoomType", RoomType)
                .addParams("RoomDeploy", RoomDeploy)
                .addParams("Price", Price)
                .addParams("RentType", RentType)
                .addParams("Images", Images)
                .addParams("ContactTel", ContactTel)
                .addParams("Address", Address)
                .addParams("Elevator", Elevator)
                .addParams("Creator", Creator)
                .build()
                .execute(new DataCallBack(this) {

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
                                        if (!TextUtils.isEmpty(UserInformation.getUserInfo().getPropertyId())) {
                                            showToast(R.string.tenement_review_has_been_submitted);
                                        }
                                        gotoActivityAndFinish(HouseRent_List.class.getName(), null);
                                        removeTempFromPref();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    showToast("发布失败");
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

    //关闭发布闲置物品显示对话框
    private void crenteCencalDialog() {
        MyDialog dialog = new MyDialog(this);
        dialog.show();
        dialog.setDialogCallback(dialogcallback);
    }

    MyDialog.Dialogcallback dialogcallback = new MyDialog.Dialogcallback() {
        @Override
        public void dialogdo() {
            try {
                removeTempFromPref();
                gotoActivityAndFinish(HouseRent_List.class.getName(), null);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dialogDismiss() {
        }
    };

    //头像裁剪方法
    private void onCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
//        Uri tempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + "small.jpg");
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
                        //得到图片写到本地
//                        Bundle extras = data.getExtras();
//                        Bitmap image = extras.getParcelable("data");
//                        if (image != null) {
//                            //上传服务器
//                            // 获取本地文件
//                            File file = new File(mImageUri.getPath());
//                            try {
//                                // 将图片写入到文件
//                                FileOutputStream fileOutStream = new FileOutputStream(file);
//                                image.compress(Bitmap.CompressFormat.JPEG, 100,
//                                        fileOutStream);
//                                fileOutStream.flush();
//                                fileOutStream.close();
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
                        //上传到服务器
                        String fileId = service.Upload(this, mImageUri.getPath()).FileName;

                        //上传图片的Ids
                        if (TextUtils.isEmpty(mImageIds)) {
                            mImageIds = fileId;
                        } else {
                            mImageIds += "," + fileId;
                        }

                        //缓存到本地
                        String imageUrl = service.getImageUrl(fileId);
                        mFileCaches.putImageToFileCache(imageUrl, BitmapFactory.decodeFile(mImageUri.getPath()));

                        //创建图片
                        ImageView iv = new ImageView(this);
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ll_houserent_images.addView(iv, ll_houserent_images.getChildCount() - 1);
                        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
                        linearParams.setMargins(linearParams.leftMargin, linearParams.topMargin, Utility.dip2px(this, getResources().getDimension(R.dimen.dimen_2dp)), linearParams.bottomMargin);
                        linearParams.width = Utility.dip2px(this, 48f);
                        linearParams.height = Utility.dip2px(this, 48f);
                        iv.setLayoutParams(linearParams);

                        //显示头像
                        ImageLoader.getInstance().displayImage(imageUrl, iv, imageOptions);
                        Log.d("imageUrl", imageUrl);
                        //最多上传5张照片
                        if (ll_houserent_images.getChildCount() == 6) {
                            addhouserent_add.setVisibility(View.GONE);
                        }
                    }

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
                            // TODO Auto-generated method stub
                            if (service.ping()) {
                                String fileId = service.Upload(HouseRent_Create.this, mDataList.get(mDataList.size() - 1).sourcePath).FileName;
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
        room = "";
        hall = "";
        toilet = "";
        area = "";
        floor = "";
        totalfloor = "";
        tel = "";
        rentPrice = "";
        isElevator = "";
        service.roomType = "";
        service.rentType = "";
        service.roomConfig = "";
        service.orientation = "";
        service.fitment = "";
        mHouseProperties = null;
    }

    @SuppressWarnings("unchecked")
    private void initData() {
//        getTempFromPref();
        incomingDataList = (List<ImageItem>) getIntent()
                .getSerializableExtra(IntentConstants.EXTRA_IMAGE_LIST);
        if (incomingDataList != null) {
            showToast("上传图片中...");
            if (service.ping()) {
                mDataList.addAll(incomingDataList);
                saveTempToPref();
                for (int i = mDataList.size() - incomingDataList.size(); i < mDataList.size(); i++) {
                    if (mDataList.get(i).sourcePath.contains("/")) {
                        final int finalI = i;
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
//                                service.showProgressDialog("",HouseRent_Create.this);
                                String fileId = service.Upload(HouseRent_Create.this, mDataList.get(finalI).sourcePath).FileName;
                                if (Services.isNotNullOrEmpty(mImageIds)) {
                                    mImageIds += "," + fileId;
                                } else {
                                    mImageIds = fileId;
                                }

                            }

                        }, 100);

                    }

                }
            }else {
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
}
