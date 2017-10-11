package com.ldnet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.*;
import android.net.Uri;
import android.os.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
//import com.autonavi.amap.mapcore.MapSourceGridData;
import com.dh.bluelock.imp.BlueLockPubCallBackBase;
import com.dh.bluelock.imp.OneKeyInterface;
import com.dh.bluelock.object.LEDevice;
import com.dh.bluelock.pub.BlueLockPub;
import com.dh.bluelock.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.adapter.*;
import com.ldnet.activity.base.BaseFragment;
import com.ldnet.activity.commen.Constant;
import com.ldnet.activity.home.*;
import com.ldnet.activity.mall.GoodsList;
import com.ldnet.activity.mall.Goods_Details;
import com.ldnet.activity.me.*;
import com.ldnet.activity.qindian.QinDianMain;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.service.*;
import com.ldnet.utility.*;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.Call;
import okhttp3.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.R.attr.rowHeight;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.media.CamcorderProfile.get;
import static com.ldnet.utility.Utility.getScreenWidthforPX;

/**
 * ***************************************************
 * 主框架 - 首页
 * **************************************************
 */
public class FragmentHome extends BaseFragment implements OnClickListener, BorderScrollView.OnBorderListener {

    private TextView tv_main_title;
    private LinearLayout ll_yellow_housekeeping;
    private LinearLayout ll_yellow_rental;
    private LinearLayout ll_yellow_education;
    private LinearLayout ll_yellow_pages;
    private ImageView iv_home_ads, iv_home_property_thumbnail, unread_fuwu, unread_notification, unread_fee, iv_qindian;
    private LinearLayout ll_property_notification;
    private LinearLayout ll_property_services;
    private LinearLayout ll_property_notice;
    private LinearLayout mAppHomePage;
    private RelativeLayout ll_home;
    private ImageButton bt_open_door;
    private Services services;
    private List<APPHomePage_Area> mAppHomePageArea;
    private List<APPHomePage_Column> mData = new ArrayList<>();
    private BadgeView badgeView;
    //分页常量
    static Integer PAGE_SIZE = Integer.MAX_VALUE;
    private MyGridView mGridViewGoods;
    private ListViewAdapter mAdapter;
    List<Goods> goods;
    private PullToRefreshScrollView mRefreshableView;
    private View view;
    private MessageCallBack messageCallBack;

    private ArrayList<String> mImageUrl = new ArrayList<>();
    private List<APPHomePage_Row> mImageUrl1 = new ArrayList<>();
    private ImageCycleView mAdView;
    private ImageView splash_iv;
    private LayoutInflater inflater1;

    protected DisplayImageOptions imageOptions;
    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
    private BluetoothAdapter bluetoothAdapter;    //本地蓝牙适配器
    private OneKeyInterface blueLockPub;
    private LockCallBack lockCallBack;
    private String deviceID = "";//设备id
    private List<KeyChain> keyChain = new ArrayList<>();
    private String feeArrearage = "";
    private int ifGetKeyChain;
    private boolean openEntranceState;  //是否开通门禁 ，true开通
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private boolean approvePass;
    private AcountService acountService;
    private PropertyFeeService propertyFeeService;
    private EntranceGuardService entranceGuardService;
    private HomeService homeService;
    private GoodsService goodsService;
    private String tag = FragmentHome.class.getSimpleName();
    private boolean openDoorBykeyChain;
    private LayoutInflater currentHomePageLayout;
    //scanResult数据
    HashMap<String, LEDevice> scanDeviceResult = new HashMap<String, LEDevice>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_home, container,
                false);

        initService();
        initView(view, inflater);
        inflater1 = inflater;
        // 初始化事件
        initEvents(view);
        imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
                .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .extraForDownloader(UserInformation.getUserInfo().UserPhone + "," + aa + "," + aa1)
                .build();
        APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
        initEvents();
        splash_iv.setVisibility(View.VISIBLE);

        sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 注册监听器
        if (sensorManager != null) {
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
        //获取最新用户信息
        SetCurrentInforamtion();

        //是否开通门禁
        checkOpenEntrance();

        //判断当前房屋是否通过验证
        if (UserInformation.getUserInfo().UserId != null && UserInformation.getUserInfo().getHouseId() != null
                && !UserInformation.getUserInfo().getHouseId().equals("")) {
            getApprove();
        }

        //判断用户是否有物业欠费
        if (UserInformation.getUserInfo().getHouseId() != null) {
            getArrearageAmount();
        }

        //获取当前房间钥匙串
        getKeyChain(true);

        // 标题
        tv_main_title.setText(UserInformation.getUserInfo().CommuntiyName);
        //物业图标
        User user = UserInformation.getUserInfo();
        if (!TextUtils.isEmpty(user.PropertyThumbnail)) {
            iv_home_property_thumbnail.setBackgroundColor(Color.WHITE);
            ImageLoader.getInstance().displayImage(Services.getImageUrl(user.PropertyThumbnail), iv_home_property_thumbnail, imageOptions);
        } else {
            iv_home_property_thumbnail.setBackgroundColor(Color.TRANSPARENT);
            iv_home_property_thumbnail.setImageResource(R.drawable.home_services_n);
        }
        APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
        if (MsgInformation.getMsg().isNOTICE()) {
            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
            unread_notification.setVisibility(View.VISIBLE);
        } else {
            unread_notification.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isFEE()) {
            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
            unread_fee.setVisibility(View.VISIBLE);
        } else {
            unread_fee.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isCOMMUNICATION() || MsgInformation.getMsg().isREPAIRS()
                || MsgInformation.getMsg().isCOMPLAIN()) {
            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
            unread_fuwu.setVisibility(View.VISIBLE);
        } else {
            unread_fuwu.setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isCOMMUNICATION() || MsgInformation.getMsg().isREPAIRS()
                || MsgInformation.getMsg().isCOMPLAIN() || MsgInformation.getMsg().isNOTICE()
                || MsgInformation.getMsg().isFEE()) {
            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.iv_zc1).setVisibility(View.GONE);
        }
        if (MsgInformation.getMsg().isMESSAGE() || MsgInformation.getMsg().isFEEDBACK()) {
            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.iv_dc1).setVisibility(View.GONE);
        }

    }


    private void initEvents() {
        mRefreshableView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mAppHomePage.removeAllViews();
                APPGetJpushNotification(MsgInformation.getMsg().getCallbackId());
                getData(true);
                getHomePageArea(inflater1);
            }
        });
    }

    // 初始化事件
    private void initEvents(View view) {
        ll_property_notification.setOnClickListener(this);
        ll_property_services.setOnClickListener(this);
        ll_property_notice.setOnClickListener(this);
        ll_yellow_rental.setOnClickListener(this);
        ll_yellow_education.setOnClickListener(this);
        ll_yellow_housekeeping.setOnClickListener(this);
        ll_yellow_pages.setOnClickListener(this);
        tv_main_title.setOnClickListener(this);
        bt_open_door.setOnClickListener(this);
        bt_open_door.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //创建一个添加快捷方式的Intent
                Intent addSC = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                //快捷键的标题
                String title = "金牌门禁";
                //快捷键的图标
                Parcelable icon = Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.home_entrance_guard);
                //创建单击快捷键启动本程序的Intent
                Intent launcherIntent = new Intent(getActivity(), EntranceGuardSplash.class);
                //设置快捷键的标题
                addSC.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                //设置快捷键的图标
                addSC.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                //设置单击此快捷键启动的程序
                addSC.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
                addSC.putExtra("duplicate", false);
                //向系统发送添加快捷键的广播
                getActivity().sendBroadcast(addSC);
                return true;
            }
        });

    }


    private void initService() {
        services = new Services();
        acountService = new AcountService(getActivity());
        propertyFeeService = new PropertyFeeService(getActivity());
        entranceGuardService = new EntranceGuardService(getActivity());
        homeService = new HomeService(getActivity());
        goodsService = new GoodsService(getActivity());

    }

    // 初始化视图
    private void initView(View view, final LayoutInflater inflater) {
        if (Services.TOKEN != null && !Services.TOKEN.equals("")) {
        } else {
            Services.TOKEN = TokenInformation.getTokenInfo().toString();
        }
        mData = new ArrayList<>();

        lockCallBack = new LockCallBack();
        ll_home = (RelativeLayout) view.findViewById(R.id.ll_home);
        // 标题
        tv_main_title = (TextView) view.findViewById(R.id.tv_main_title);
        bt_open_door = (ImageButton) view.findViewById(R.id.bt_open_door);
        bt_open_door.setOnClickListener(this);
        //物业图标
        iv_home_property_thumbnail = (ImageView) view.findViewById(R.id.iv_home_property_thumbnail);
        unread_fuwu = (ImageView) view.findViewById(R.id.unread_fuwu);
        unread_fee = (ImageView) view.findViewById(R.id.unread_fee);
        unread_notification = (ImageView) view.findViewById(R.id.unread_notification);
        //通知
        ll_property_notification = (LinearLayout) view.findViewById(R.id.ll_property_notification);
        //服务
        ll_property_services = (LinearLayout) view.findViewById(R.id.ll_property_services);
        RelativeLayout rl_home_property_thumbnail = (RelativeLayout) view.findViewById(R.id.rl_home_property_thumbnail);
        badgeView = new BadgeView(getActivity());
        badgeView.setWidth(Utility.dip2px(getActivity(), 10));
        badgeView.setHeight(Utility.dip2px(getActivity(), 10));
        badgeView.setBackground(R.drawable.round_tip, Color.RED);
        badgeView.setVisibility(View.GONE);
        badgeView.setTextColor(Color.RED);
        badgeView.setTargetView(rl_home_property_thumbnail);
        //缴费
        ll_property_notice = (LinearLayout) view.findViewById(R.id.ll_property_notice);
        //房屋租赁
        ll_yellow_rental = (LinearLayout) view.findViewById(R.id.ll_yellow_rental);
        //教育培训
        ll_yellow_education = (LinearLayout) view.findViewById(R.id.ll_yellow_education);
        //黄页
        ll_yellow_pages = (LinearLayout) view.findViewById(R.id.ll_yellow_pages);
        // 刷新的控件
        mRefreshableView = (PullToRefreshScrollView) view.findViewById(R.id.refresh_root);
        mRefreshableView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mRefreshableView.setHeaderLayout(new HeaderLayout(getActivity()));
        // 黄页，布局调整
        ll_yellow_housekeeping = (LinearLayout) view
                .findViewById(R.id.ll_yellow_housekeeping);
        LayoutParams linearParams = (LayoutParams) ll_yellow_housekeeping
                .getLayoutParams();
        DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        linearParams.width = (int) (dm.widthPixels * 0.4f);
        ll_yellow_housekeeping.setLayoutParams(linearParams);
        splash_iv = (ImageView) view.findViewById(R.id.splash_iv);

        //社区小店
        iv_home_ads = (ImageView) view.findViewById(R.id.iv_home_ads);
        iv_home_ads.setImageResource(R.drawable.community_shop);
        iv_home_ads.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goodsService.communityshops(handlerCommunityShop);
            }
        });

        //蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueLockPub = BlueLockPub.bleLockInit(getActivity());//3000:扫描时间毫秒

        //商品
        mAppHomePage = (LinearLayout) view.findViewById(R.id.app_home_page);
        getHomePageArea(inflater);

        mGridViewGoods = (MyGridView) view.findViewById(R.id.grid_goods);
        mGridViewGoods.setFocusable(false);
        mGridViewGoods.setSelector(R.color.white);
        goods = new ArrayList<Goods>();
        mAdapter = new ListViewAdapter<Goods>(getActivity(), R.layout.item_home_goods, goods) {
            @Override
            public void convert(ViewHolder holder, Goods goods1) {
                // 商品图片
                // 改线ViewPager的高度
                ImageView thumbnail = holder.getView(R.id.iv_goods_image);
                //设置商品图片的高度
                LayoutParams linearParams_good = (LayoutParams) thumbnail.getLayoutParams();
                int height = (getScreenWidthforPX(getActivity()) - Utility.dip2px(getActivity(), 8.0f)) / 2;
                linearParams_good.height = height;
                thumbnail.setLayoutParams(linearParams_good);
                ImageLoader.getInstance().displayImage(Services.getImageUrl(goods1.getThumbnail()), thumbnail, imageOptions);
                // 商品标题
                ((TextView) holder.getView(R.id.tv_goods_name)).setText(goods1.T.trim());

                // 商品价格
                TextView tv_goods_price = holder.getView(R.id.tv_goods_price);
                if (goods1.Type.equals(2)) {
                    //tv_goods_price.setVisibility(View.GONE);
                    tv_goods_price.setText("报名" + " " + goods1.GP);
                    tv_goods_price.setTextColor(getResources().getColor(R.color.gray));
                } else {
                    tv_goods_price.setVisibility(View.VISIBLE);
                    tv_goods_price.setText("￥" + goods1.GP);
                }
            }
        };
        mGridViewGoods.setAdapter(mAdapter);
        mGridViewGoods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), Goods_Details.class);
                intent.putExtra("GOODS", goods.get(position));
                intent.putExtra("PAGE_TITLE", "");
                intent.putExtra("FROM_CLASS_NAME", MainActivity.class.getName());
                intent.putExtra("URL", goods.get(position).URL);
                intent.putExtra("CID", goods.get(position).GID);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });

        getData(true);

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "本地蓝牙不可用", Toast.LENGTH_SHORT).show();
        }
        ((BlueLockPub) blueLockPub).setResultCallBack(lockCallBack);

    }

    class LockCallBack extends BlueLockPubCallBackBase {
        @Override
        public void openCloseDeviceCallBack(int i, int i1, String... strings) {
            closeProgressDialog1();
            if (i == 0) {
                if (!TextUtils.isEmpty(feeArrearage)) {
                    MyDailogTag tag = new MyDailogTag(getActivity(), feeArrearage);
                    tag.show();
                }
                //添加开门日志
                if (deviceID != null && !deviceID.equals("")) {
                    entranceGuardService.EGLog(deviceID, handlerEGlog);
                }
            } else {
                showToast("开门失败，请靠近设备再试");
            }
        }

        @Override
        public void scanDeviceCallBack(LEDevice leDevice, int i, int i1) {
            if (leDevice != null) {
                scanDeviceResult.put(leDevice.getDeviceId(), leDevice);
            } else {
                showToast("扫描无果");
            }
        }

        @Override
        public void scanDeviceEndCallBack(int i) {
            openDoor();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 家政服务-|-公共服务-|
            case R.id.ll_yellow_housekeeping:
                Intent intent_housekeep = new Intent(getActivity(), CommunityServicesPageTabActivity.class);
                intent_housekeep.putExtra("YELLOW_PAGE_SORT_ID", "254c8473cd98410aa5d73001ad715ff4");
                intent_housekeep.putExtra("YELLOW_PAGE_SORT_NAME", getResources().getString(R.string.fragment_home_yellow_housekeeping));
                startActivity(intent_housekeep);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            // 生活黄页
            case R.id.ll_yellow_pages:
                Intent intent_yellowpage = new Intent(getActivity(), YellowPageTabActivity.class);
                intent_yellowpage.putExtra("YELLOW_PAGE_SORT_ID", "8f1f1e4092784199bbec0229e1cca9b0");
                intent_yellowpage.putExtra("YELLOW_PAGE_SORT_NAME", getResources().getString(R.string.fragment_home_yellow_pages));
                intent_yellowpage.putExtra("flag", "yellow");
                startActivity(intent_yellowpage);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            // 房屋租售
            case R.id.ll_yellow_rental:
                Intent intent_rent = new Intent(getActivity(), HouseRent_List.class);
                startActivity(intent_rent);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            // 教育培训
            case R.id.ll_yellow_education:
                Intent intent_train = new Intent(getActivity(), YellowPageTabActivity.class);
                intent_train.putExtra("YELLOW_PAGE_SORT_ID", "141de9dc1eb54d5aabe492843998b907");
                intent_train.putExtra("YELLOW_PAGE_SORT_NAME", getResources().getString(R.string.fragment_home_yellow_education));
                intent_train.putExtra("flag", "edu");
                startActivity(intent_train);
                getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            //物业通知
            case R.id.ll_property_notification:
                Msg msg = MsgInformation.getMsg();
                msg.setNOTICE(false);
                MsgInformation.setMsgInfo(msg);
                unread_notification.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(UserInformation.getUserInfo().PropertyId)) {
                    Intent intent = new Intent(getActivity(), Notification.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                } else {
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_URL", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                    intent.putExtra("PAGE_URL_ORGIN", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("PAGE_TITLE_ORGIN", "金牌管家邀请函");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                }
                break;
            //物业服务
            case R.id.ll_property_services:
                if (!TextUtils.isEmpty(UserInformation.getUserInfo().PropertyId)) {
                    if (!TextUtils.isEmpty(UserInformation.getUserInfo().HouseId)) {
                        Intent intent = new Intent(getActivity(), Property_Services.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    } else {
                        DialogAlert dialog = new DialogAlert(getActivity(), getString(R.string.nobind_room), new AlertGotoActivity());
                        dialog.show();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_URL", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                    intent.putExtra("PAGE_URL_ORGIN", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("PAGE_TITLE_ORGIN", "金牌管家邀请函");
                    startActivity(intent);
                }
                break;
            //物业缴费
            case R.id.ll_property_notice:
                Msg msg2 = MsgInformation.getMsg();
                msg2.setFEE(false);
                MsgInformation.setMsgInfo(msg2);
                unread_fee.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(UserInformation.getUserInfo().PropertyId)) {
                    if (!TextUtils.isEmpty(UserInformation.getUserInfo().HouseId)) {
                        Intent intent = new Intent(getActivity(), Property_Fee.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    } else {
                        DialogAlert dialog = new DialogAlert(getActivity(), getString(R.string.nobind_room), new AlertGotoActivity());
                        dialog.show();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_URL", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                    intent.putExtra("PAGE_URL_ORGIN", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("PAGE_TITLE_ORGIN", "金牌管家邀请函");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                }
                break;
            case R.id.tv_main_title:
                try {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("NOT_FROM_ME", "105");
                    gotoActivity(com.ldnet.activity.me.Community.class.getName(), extras);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_open_door:
                //开门条件：开启门禁、开启蓝牙、入住金管家、有房屋，再请求钥匙，在handle中做处理
                openClick();
                break;
            default:
                break;
        }
    }

    //商品首页
    public void getHomePageArea(final LayoutInflater inflater) {
        currentHomePageLayout = inflater;
        splash_iv.setVisibility(View.GONE);
      //  goodsService.getHomePageArea(handlerGetHomePageArea);
    }

    //推送
    public void APPGetJpushNotification(int id) {
        homeService.APPGetJpushNotification(id, handlerGetJpushNotification);
    }

    //获取商品
    public void getData(Boolean isFirst) {
        String lastId;
        if (!isFirst) {
            lastId = goods.get(goods.size() - 1).GID;
        } else {
            goods.clear();
            lastId = "";
        }
        goodsService.getGoodsData(lastId, PAGE_SIZE, handlerGetGoodsData);
    }

    //获取钥匙串
    public void getKeyChain(boolean init) {
        ifGetKeyChain = 0;
        keyChain.clear();
        openDoorBykeyChain = !init;
        if (entranceGuardService != null) {
            showProgressDialog();
            entranceGuardService.getKeyChain(true, handerGetKeyChain);
        }
    }

    //用户欠费记录
    private void getArrearageAmount() {
        feeArrearage = "";
        if (propertyFeeService != null) {
            showProgressDialog();
            propertyFeeService.getArrearageAmount(handlerGetArrageAmount);
        }
    }

    //获取用户最新信息
    public void SetCurrentInforamtion() {
        if (acountService != null) {
            showProgressDialog();
            acountService.SetCurrentInforamtion(handlerSetCurrentInforamtion);
        }
    }

    //判断是否验证
    public void getApprove() {
        if (acountService != null) {
            showProgressDialog();
            acountService.getApprove(UserInformation.getUserInfo().getHouseId(), UserInformation.getUserInfo().UserId, handlerGetApprove);
        }

    }

    //跳转接口实现
    class AlertGotoActivity implements DialogAlert.OnAlertDialogListener {
        @Override
        public void GotoActivity() {
            HashMap<String, String> extras = new HashMap<String, String>();
            extras.put("IsFromRegister", "false");
            extras.put("COMMUNITY_ID", UserInformation.getUserInfo().getCommunityId());
            extras.put("COMMUNITY_NAME", UserInformation.getUserInfo().CommuntiyName);
            try {
                gotoActivityAndFinish(BindingHouse.class.getName(), extras);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private LinearLayout initArea(Integer rowCount, Float rowHeightBI) {
        Integer screenWidth = getScreenWidthforPX(getActivity());
        LinearLayout ll_area = new LinearLayout(getActivity());
        LayoutParams area_lp = new LayoutParams(
                screenWidth, Float.valueOf(screenWidth * rowHeightBI / 100.f).intValue());
        area_lp.setMargins(0, 20, 0, 0);
        ll_area.setLayoutParams(area_lp);
        ll_area.setPadding(0, 1, 0, 1);
        ll_area.setOrientation(LinearLayout.VERTICAL);
        ll_area.setBackgroundColor(getActivity().getResources().getColor(R.color.gray_light_2));
        return ll_area;
    }

    private LinearLayout initRow(Integer columnCount, Float heightBI) {
        Integer screenWidth = getScreenWidthforPX(getActivity());
        Integer rowHeight = Float.valueOf(screenWidth * heightBI / 100.00F).intValue();
        LinearLayout ll_row = new LinearLayout(getActivity());
        LayoutParams row_lp = new LayoutParams(
                screenWidth, rowHeight);
        row_lp.setMargins(0, 0, 0, Utility.dip2px(getActivity(), 1.0f));
        ll_row.setLayoutParams(row_lp);
        if (columnCount.equals(1)) {
            ll_row.setOrientation(LinearLayout.VERTICAL);
        } else {
            ll_row.setOrientation(LinearLayout.HORIZONTAL);
        }
        return ll_row;
    }

    private LinearLayout initColumn(Integer columnCount, APPHomePage_Column column) {
        Integer screenWidth = getScreenWidthforPX(getActivity());
        LinearLayout ll_column = new LinearLayout(getActivity());
        LayoutParams column_lp = new LayoutParams(screenWidth / columnCount, LayoutParams.MATCH_PARENT);
        column_lp.setMargins(0, 0, Utility.dip2px(getActivity(), 1.0f), 0);
        ll_column.setLayoutParams(column_lp);
        initContent(ll_column, screenWidth / columnCount, column);
        ll_column.setPadding(12, 12, 12, 12);
        ll_column.setBackgroundColor(Color.WHITE);
        return ll_column;
    }

    TextView title;
    TextView description;
    ImageView image;

    private void initContent(LinearLayout ll_column, Integer column_width, APPHomePage_Column column) {

        title = new TextView(getActivity());
        description = new TextView(getActivity());

        title.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        //标题

        title.setText(column.TITLE);
        title.setTextColor(Color.parseColor(column.TITLECOLOR));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);

        //描述

        description.setText(column.DESCRIPTION);
        description.setTextColor(Color.parseColor(column.DESCRIPTIONCOLOR));
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
        // }
//        description.setGravity(Gravity.CENTER);
        //图片
        image = new ImageView(getActivity());
        Integer imageWidth;
        if (column.SHOWTITLE) {
            imageWidth = Float.valueOf(column_width * column.ImgWidthPro / 100.0f).intValue();
            title.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
        } else {
            imageWidth = column_width;
            title.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
        }
        LayoutParams image_lp = new LayoutParams(imageWidth, Float.valueOf(imageWidth * column.ImgHeightPro).intValue());

        image.setLayoutParams(image_lp);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageLoader.getInstance().displayImage(services.getImageUrl(column.IMGID), image,
                imageOptions);
        if (column.ImgPosition.equals(0) || column.ImgPosition.equals(2)) {
            ll_column.setOrientation(LinearLayout.VERTICAL);
//            image_lp.setMargins(22, 0, 22, 0);
            if (column.ImgPosition.equals(0)) {
                ll_column.addView(image);
                ll_column.addView(title);
                ll_column.addView(description);
            } else {
                ll_column.addView(title);
                ll_column.addView(description);
                ll_column.addView(image);
            }
        } else {
            ll_column.setOrientation(LinearLayout.HORIZONTAL);
            if (column.SHOWTITLE) {
                LinearLayout ll_column1 = new LinearLayout(getActivity());
                LayoutParams column1_lp = new LayoutParams(
                        Float.valueOf(column_width * column.ImgWidthPro / 100.0f).intValue(), LayoutParams.MATCH_PARENT);
                ll_column1.setLayoutParams(column1_lp);
                ll_column1.setOrientation(LinearLayout.HORIZONTAL);
                ll_column1.addView(image);
                LinearLayout ll_column2 = new LinearLayout(getActivity());
                LayoutParams column2_lp = new LayoutParams(
                        Float.valueOf(column_width * (1 - column.ImgWidthPro / 100.0f)).intValue(), LayoutParams.MATCH_PARENT);
                ll_column2.setLayoutParams(column2_lp);
                ll_column2.setOrientation(LinearLayout.VERTICAL);
                ll_column2.addView(title);
                ll_column2.addView(description);
                if (column.ImgPosition.equals(1)) {
                    ll_column.addView(ll_column2);
                    ll_column.addView(ll_column1);
                } else {
                    ll_column.addView(ll_column1);
                    ll_column.addView(ll_column2);
                }
            } else {
                LinearLayout ll_column1 = new LinearLayout(getActivity());
                LayoutParams column1_lp = new LayoutParams(
                        column_width, LayoutParams.MATCH_PARENT);
                ll_column1.setLayoutParams(column1_lp);
                ll_column1.setOrientation(LinearLayout.HORIZONTAL);
                ll_column1.addView(image);
                ll_column.addView(ll_column1);
            }
        }
    }

    //开门动作：当前设备和钥匙串匹配成功，即可开门
    private void openDoor() {
        boolean ifOpen = false;
        deviceID = "";
        closeProgressDialog1();
        for (KeyChain key : keyChain) {
            LEDevice device = scanDeviceResult.get(key.getId());
            if (device != null) {
                ifOpen = true;
                deviceID = device.getDeviceId();
                device.setDevicePsw(key.getPassword());
                blueLockPub.oneKeyOpenDevice(device, device.getDeviceId(), device.getDevicePsw());
                Log.e(tag, "开门啦-------钥匙已找到");
                break;
            }
        }
        if (ifOpen == false) {
            showToast(getString(R.string.long_distance));
        }
    }


    //开门点击事件
    private void openClick() {
        scanDeviceResult.clear();
        if (openEntranceState) {
            if (bluetoothAdapter.isEnabled() == true) {    //蓝牙是否开启
                if (TextUtils.isEmpty(UserInformation.getUserInfo().getPropertyId())) {      //有无物业入驻
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_URL", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                    intent.putExtra("PAGE_URL_ORGIN", "http://www.goldwg.com:88/mobile/yaoqingwuye");
                    intent.putExtra("PAGE_TITLE_ORGIN", "金牌管家邀请函");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                } else if (TextUtils.isEmpty(UserInformation.getUserInfo().getHouseId())) {                     //有无绑定房屋
                    DialogAlert dialog = new DialogAlert(getActivity(), getString(R.string.not_exist_house), new AlertGotoActivity());
                    dialog.show();
                } else if (!approvePass) {
                    getApprove();//判断房屋是否验证过
                } else {    //满足条件 ,如果已经获取到钥匙串，则直接蓝牙扫描设备；否则先获取钥匙串，在获取成功后，再开启扫描
                    showProgressDialog1();
                    if (keyChain == null || keyChain.size() == 0) {
                        getKeyChain(false);
                    } else {
                        ((BlueLockPub) blueLockPub).setLockMode(Constants.LOCK_MODE_MANUL,
                                null, false);
                        ((BlueLockPub) blueLockPub).scanDevice(2000);
                    }
                }
            } else {
                //提示开启蓝牙
                showToast(getString(R.string.noopen_bulebooth));
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                blueLockPub = BlueLockPub.bleLockInit(getActivity());
            }
        } else {  //提示开启门禁
            openEntrance();
        }
    }

    private void checkOpenEntrance() {
        //true 表示未开通门禁；false表示开通门禁
        openEntranceState = false;
        entranceGuardService.checkOpenEntrance(handlerCheckOpenEntrance);
    }

    //未开通门禁提示对话框
    private void openEntrance() {
        TextView log_off_cancel;
        TextView log_off_confirm;
        TextView tv_dialog_title;
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.ly_off);
        alertDialog.findViewById(R.id.line).setVisibility(View.VISIBLE);
        tv_dialog_title = (TextView) alertDialog.findViewById(R.id.tv_dialog_title);
        tv_dialog_title.setText(getString(R.string.no_entrance));
        log_off_cancel = (TextView) alertDialog.findViewById(R.id.log_off_cancel);
        log_off_confirm = (TextView) alertDialog.findViewById(R.id.log_off_confirm);
        log_off_confirm.setText("确定");
        log_off_cancel.setText("取消");
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setAttributes(lp);
        log_off_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getPropertyTelphone();
                alertDialog.dismiss();
            }
        });
        log_off_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();


    }

    //获取物业联系电话
    private void getPropertyTelphone() {
        propertyFeeService.getPropertyTelphone(handlerGetPropertyTel);
    }

    //弹出物业电话
    private void showCallPop(List<PPhones> phonesList) {
        ListViewAdapter<PPhones> mAdapter;
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View popupView = layoutInflater.inflate(R.layout.pop_property_telphone, null);
        final PopupWindow mPopWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(popupView);
        View rootview = layoutInflater.inflate(R.layout.main, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        mPopWindow.setAnimationStyle(R.anim.slide_in_from_bottom);


        TextView title = (TextView) popupView.findViewById(R.id.poptitle);
        title.setVisibility(View.GONE);
        //  title.setText(getResources().getText(R.string.no_entrance));
        ListView listTelPhone = (ListView) popupView.findViewById(R.id.list_propert_telphone);
        mAdapter = new ListViewAdapter<PPhones>(getActivity(), R.layout.item_telephone, phonesList) {
            @Override
            public void convert(ViewHolder holder, final PPhones phones) {
                holder.setText(R.id.tv_title, phones.Title).setText(R.id.tv_telephone, phones.Tel);
                ImageButton telephone = holder.getView(R.id.ibtn_telephone);
                telephone.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phones.Tel));
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                });
            }
        };
        listTelPhone.setAdapter(mAdapter);
        popupView.findViewById(R.id.cancel_call).getBackground().setAlpha(200);
        popupView.findViewById(R.id.cancel_call).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopWindow.setAnimationStyle(R.anim.slide_out_to_bottom);
                mPopWindow.dismiss();
            }
        });

        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpaha(getActivity(), 1f);
            }
        });
        backgroundAlpaha(getActivity(), 0.5f);
    }

    //为弹出层设置遮罩层
    public void backgroundAlpaha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    private ImageCycleView.ImageCycleViewListener mAdCycleViewListener = new ImageCycleView.ImageCycleViewListener() {

        @Override
        public void onImageClick(int position, View imageView) {
            // TODO 单击图片处理事件
            Log.e(tag, "onImageClick---000000");
            if (mData != null && mData.size() > 0 && mData.get(position) != null) {

                if (mData.get(position).TYPES.equals(3)) {
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_URL", mData.get(position).URL);
                    intent.putExtra("PAGE_TITLE", mData.get(position).TITLE);
                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                    startActivity(intent);
                } else {
                    Intent goodsListIntent = new Intent(getActivity(), GoodsList.class);
                    goodsListIntent.putExtra("PAGE_TITLE", mData.get(position).TITLE);
                    goodsListIntent.putExtra("CID", mData.get(position).ID);
                    startActivity(goodsListIntent);
                }
            }
        }


        @Override
        public void displayImage(String imageURL, ImageView imageView) {
            ImageLoader.getInstance().displayImage(imageURL, imageView, imageOptions);// 此处本人使用了ImageLoader对图片进行加装！
        }
    };

    //未验证弹出对话框
    MyDialog2.Dialogcallback dialogcallback = new MyDialog2.Dialogcallback() {
        @Override
        public void dialogdo(String type) {
            HashMap<String, String> extras = new HashMap<String, String>();
            extras.put("APPLY", type);
            extras.put("ROOM_ID", UserInformation.getUserInfo().getHouseId());
            extras.put("phone", UserInformation.getUserInfo().getPropertyPhone());
            extras.put("CLASS_FROM", VisitorPsd.class.getName());
            extras.put("COMMUNITY_ID", UserInformation.getUserInfo().getCommunityId());
            try {
                gotoActivityAndFinish(VisitorPsd.class.getName(), extras);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dialogDismiss() {

        }
    };
    //重力感应监听
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // 传感器信息改变时执行该方法
            float[] values = sensorEvent.values;
            //设备坐标系是固定于设备的，与设备的方向（在世界坐标系中的朝向）无关
            float x = values[0]; // x轴方向的重力加速度，向右为正
            float y = values[1]; // y轴方向的重力加速度，向前为正
            float z = values[2]; // z轴方向的重力加速度，向上为正

            // 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
            int medumValue = 19;// 三星 i9250怎么晃都不会超过20，没办法，只设置19了
            if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
                //设置震动时长
                vibrator.vibrate(200);
                Message msg = new Message();
                msg.what = 123;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

    };


    //摇摇动作
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 123:
                    openClick();
                    break;
            }
        }

    };

    Handler handlerEGlog = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    Handler handerGetKeyChain = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    ifGetKeyChain = 1;
                    keyChain = (List<KeyChain>) msg.obj;
                    //缓存钥匙串、房屋信息
                    KeyCache.saveKey(keyChain, UserInformation.getUserInfo().getCommuntiyName() + "  " + UserInformation.getUserInfo().getHouseName());
                    if (openDoorBykeyChain) { //如果是用于开门，需要开启扫描，否则只是获取钥匙串
                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        blueLockPub = BlueLockPub.bleLockInit(getActivity());//2000:扫描时间毫秒

                        ((BlueLockPub) blueLockPub).setLockMode(Constants.LOCK_MODE_MANUL,
                                null, false);
                        ((BlueLockPub) blueLockPub).scanDevice(2000);
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    ifGetKeyChain = 2;
                    if (openDoorBykeyChain) { //如果是用于开门，则显示错误提示，否则只是获取钥匙串
                        showToast(msg.obj.toString());
                    }
                    break;
            }
        }
    };

    Handler handlerGetJpushNotification = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null) {
                        messageCallBack = (MessageCallBack) msg.obj;
                        if (messageCallBack != null) {
                            Msg msg2 = MsgInformation.getMsg();
                            msg2.setCallbackId(Integer.parseInt(messageCallBack.getCallbackId()));
                            msg2.setCOMPLAIN(Boolean.parseBoolean(messageCallBack.getN().getCOMPLAIN()));
                            msg2.setCOMMUNICATION(Boolean.parseBoolean(messageCallBack.getN().getCOMMUNICATION()));
                            msg2.setFEE(Boolean.parseBoolean(messageCallBack.getN().getFEE()));
                            msg2.setFEEDBACK(Boolean.parseBoolean(messageCallBack.getN().getFEEDBACK()));
                            msg2.setMESSAGE(Boolean.parseBoolean(messageCallBack.getN().getMESSAGE()));
                            msg2.setNOTICE(Boolean.parseBoolean(messageCallBack.getN().getNOTICE()));
                            msg2.setORDER(Boolean.parseBoolean(messageCallBack.getN().getORDER()));
                            msg2.setPAGE(Boolean.parseBoolean(messageCallBack.getN().getPAGE()));
                            msg2.setREPAIRS(Boolean.parseBoolean(messageCallBack.getN().getREPAIRS()));
                            msg2.setOTHER(Boolean.parseBoolean(messageCallBack.getN().getOTHER()));
                            MsgInformation.setMsgInfo(msg2);
                            if (msg2.isNOTICE()) {
                                getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
                                unread_notification.setVisibility(View.VISIBLE);
                            }
                            if (msg2.isFEE()) {
                                getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
                                unread_fee.setVisibility(View.VISIBLE);
                            }
                            if (msg2.isCOMMUNICATION() || msg2.isREPAIRS()
                                    || msg2.isCOMPLAIN()) {
                                getActivity().findViewById(R.id.iv_zc1).setVisibility(View.VISIBLE);
                                unread_fuwu.setVisibility(View.VISIBLE);
                            }
                            if (msg2.isMESSAGE() || msg2.isFEEDBACK()) {
                                getActivity().findViewById(R.id.iv_dc1).setVisibility(View.VISIBLE);
                            }
                        }

                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };


    //获取周边小店
    Handler handlerCommunityShop = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    CommunityShopId communityShopId = (CommunityShopId) msg.obj;
                    if (communityShopId == null) {
                        Intent intent = new Intent(getActivity(), Browser.class);
                        String url = "http://b.goldwg.com/GoodsShop/investment";
                        intent.putExtra("PAGE_URL", url);
                        intent.putExtra("PAGE_TITLE", "");
                        intent.putExtra("PAGE_URL_ORGIN", url);
                        intent.putExtra("PAGE_TITLE_ORGIN", "社区小店");
                        intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    } else {
                        Intent intent1 = new Intent(getActivity(), CommunityShops.class);
                        intent1.putExtra("communityShopId", communityShopId);
                        startActivity(intent1);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;

            }
        }
    };

    //获取物业联系电话
    Handler handlerGetPropertyTel = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null) {
                        List<PPhones> phonesList = (List<PPhones>) msg.obj;
                        if (phonesList != null && phonesList.size() > 0) {
                            showCallPop(phonesList);
                        }
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };

    //是否开通门禁
    Handler handlerCheckOpenEntrance = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case BaseService.DATA_SUCCESS:  //开通门禁
                    openEntranceState = true;
                    break;
                case BaseService.DATA_SUCCESS_OTHER: //未开通门禁
                    openEntranceState = false;
                    sensorManager.unregisterListener(sensorEventListener);
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }

        }
    };


    //物业欠费
    Handler handlerGetArrageAmount = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (TextUtils.isEmpty(msg.obj.toString()) || msg.obj.equals("0.0")) {
                        feeArrearage = "";
                    } else {
                        feeArrearage = msg.obj.toString();
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    break;
            }
        }
    };

    //判断当前用户和当前房屋是否通过验证
    Handler handlerGetApprove = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:  //审核通过
                    approvePass = true;
                    break;
                case BaseService.DATA_SUCCESS_OTHER:  //审核未通过
                    MyDialog2 dialog2 = new MyDialog2(getActivity(), "PASS");
                    dialog2.show();
                    dialog2.setDialogCallback(dialogcallback);

                    sensorManager.unregisterListener(sensorEventListener); //未审核禁用摇一摇
                    approvePass = false;
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };

    //获取用户最新信息
    Handler handlerSetCurrentInforamtion = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            closeProgressDialog();
            switch (msg.what) {
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };

    //货物商品
    Handler handlerGetGoodsData = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mRefreshableView.onRefreshComplete();
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null) {
                        goods.addAll((List<Goods>) msg.obj);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:
                    showToast(msg.obj.toString());
                    break;
            }
        }
    };

    //获取首页
    Handler handlerGetHomePageArea = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            splash_iv.setVisibility(View.GONE);
            switch (msg.what) {
                case BaseService.DATA_SUCCESS:
                    if (msg.obj != null) {
                        mAppHomePageArea = (List<APPHomePage_Area>) msg.obj;
                        mImageUrl.clear();
                        mImageUrl1.clear();
                        mData.clear();
                        mAppHomePage.removeAllViews();
                        int allHomePageRows = mAppHomePageArea.get(0).getAPPHomePage_Row().size();

                        if (mAppHomePageArea != null) {
                            for (int j = 0; j < allHomePageRows; j++) {   //遍历每行

                                APPHomePage_Row rowData = mAppHomePageArea.get(0).APPHomePage_Row.get(j);

                                if (rowData.getIsSlide().equals("true")) {   //可以幻灯片播放，首要显示

                                    mData = rowData.getAPPHomePage_Column();
                                    for (int k = 0; k < rowData.getAPPHomePage_Column().size(); k++) {  //遍历一行的所有列
                                        APPHomePage_Column columnData = rowData.getAPPHomePage_Column().get(0);


                                        mImageUrl.add(Services.getImageUrl(columnData.getIMGID()));

                                        LinearLayout linearLayout = (LinearLayout) currentHomePageLayout.inflate(R.layout.ly_image_cycle, null);

                                        //设置幻灯片的尺寸
                                        Integer screenWidth = Utility.getScreenWidthforPX(getActivity());
                                        Integer rowHeight = Float.valueOf(screenWidth * rowData.getRowHeightBI() / 100.00F).intValue();
                                        LayoutParams row_lp = new LayoutParams(screenWidth, rowHeight);
                                        row_lp.setMargins(0, 0, 0, Utility.dip2px(getActivity(), 1.0f));
                                        linearLayout.setLayoutParams(row_lp);

                                        mAdView = (ImageCycleView) linearLayout.findViewById(R.id.ad_view);
                                        mAdView.setImageResources(mImageUrl, mAdCycleViewListener);
                                        mAppHomePage.addView(linearLayout);
                                    }
                                } else if (mAppHomePageArea.get(0).APPHomePage_Row.get(j).getIsSlide().equals("false")) {     //不采用幻灯片的，初始化区域、行、列
                                    mImageUrl1.add(mAppHomePageArea.get(0).getAPPHomePage_Row().get(j));
                                    //初始化行，给行填充数据，并添加到mAppHomePage
                                    LinearLayout ll_row = initRow(rowData.APPHomePage_Column.size(), Float.parseFloat(rowData.HEIGHTBI));

                                    for (final APPHomePage_Column column : rowData.APPHomePage_Column) {
                                        LinearLayout ll_column = initColumn(rowData.APPHomePage_Column.size(), column);
                                        if ((rowData.APPHomePage_Column.size() - 1) == rowData.APPHomePage_Column.indexOf(column)) {
                                            LayoutParams column_lp = (LayoutParams) ll_column.getLayoutParams();
                                            column_lp.setMargins(0, 0, 0, 0);
                                            ll_column.setLayoutParams(column_lp);
                                        }

                                        ll_row.addView(ll_column);
                                        ll_column.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (column.TYPES.equals(3)) {
                                                    Intent intent = new Intent(getActivity(), Browser.class);
                                                    intent.putExtra("PAGE_URL", column.URL);
                                                    intent.putExtra("PAGE_TITLE", column.TITLE);
                                                    intent.putExtra("FROM_CLASS_NAME", getActivity().getClass().getName());
                                                    startActivity(intent);
                                                } else {
                                                    Intent goodsListIntent = new Intent(getActivity(), GoodsList.class);
                                                    goodsListIntent.putExtra("PAGE_TITLE", column.TITLE);
                                                    goodsListIntent.putExtra("CID", column.ID);
                                                    startActivity(goodsListIntent);
                                                }
                                            }
                                        });
                                    }
                                    mAppHomePage.addView(ll_row);
                                }
                            }
                        }
                    }
                    break;
                case BaseService.DATA_FAILURE:
                case BaseService.DATA_REQUEST_ERROR:

                    showToast(msg.obj.toString()

                    );
                    break;
            }
        }
    };

    @Override
    public void onBottom() {
    }

    @Override
    public void onTop() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        approvePass = false;
        sensorManager.unregisterListener(sensorEventListener);
    }


    private void initBlueTooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueLockPub = BlueLockPub.bleLockInit(getActivity());//2000:扫描时间毫秒

        ((BlueLockPub) blueLockPub).setLockMode(Constants.LOCK_MODE_MANUL,
                null, false);
        ((BlueLockPub) blueLockPub).scanDevice(2000);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 110);
            } else {
                initBlueTooth();
            }
        } else {
            initBlueTooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("tag", "权限申请成功");

                } else {
                    showToast("请手动开启位置权限");
                }
            }
        }
    }
}





