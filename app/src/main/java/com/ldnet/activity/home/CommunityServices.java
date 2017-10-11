package com.ldnet.activity.home;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
//import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.activity.base.BaseFragment;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.third.listviewshangxia.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zxs on 2016/3/1.
 * 社区服务
 */
public class CommunityServices extends BaseFragment implements View.OnClickListener {
    private TextView tv_community_services;
    private ImageButton btn_back;
    private Services services;
    private Handler mHandler;
    private ListView mLvCommunityServices;
    private ListViewAdapter<com.ldnet.entities.CommunityServices> mAdapter;
    private List<com.ldnet.entities.CommunityServices> mDatas;
    private List<com.ldnet.entities.CommunityServices> datas;

    private String mSortId;
    private String mSortKeywords;
    private String mSortTypes;
    private String communityId;
    private String mCityCode = "西安";

    private PullToRefreshScrollView mPullToRefreshScrollView;

    public static Fragment getInstance(Bundle bundle) {
        Log.i("Fragment", "");
        CommunityServices fragment = new CommunityServices();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_community_services, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findView(view);
        initEvents();
    }

    public void findView(View view) {
        mSortId = getArguments().getString("Id");
        mSortKeywords = getArguments().getString("Name");
        communityId = UserInformation.getUserInfo().CommunityId;
//        Log.i("communityId++++", communityId);
        mCityCode = UserInformation.getUserInfo().CommuntiyCityId;
        //ListView
        tv_community_services = (TextView)view.findViewById(R.id.tv_community_services);
        mPullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(getActivity()));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(getActivity()));
        mLvCommunityServices = (ListView) view.findViewById(R.id.lv_community_services);
        mDatas = new ArrayList<com.ldnet.entities.CommunityServices>();
        mLvCommunityServices.setFocusable(false);
        mLvCommunityServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i <= mDatas.size()) {
                    HashMap<String, String> extras = new HashMap<String, String>();
                    extras.put("COMMUNITY_SERVICES_ID", mDatas.get(i).Id);
                    extras.put("LEFT", "LEFT");
                    try {
                        gotoActivity(CommunityServicesDetails.class.getName(), extras);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //初始化服务
        services = new Services();
        mDatas.clear();
        getHouseKeeping(mSortId, "");
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back://返回主页
                try {
                    gotoActivityAndFinish(MainActivity.class.getName(), null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_custom://跳转到
//
                break;
            default:
                break;
        }
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                getHouseKeeping(mSortId, "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    getHouseKeeping(mSortId, mDatas.get(mDatas.size() - 1).Id);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //    获取家政服务列表
    public void getHouseKeeping(String typeId, String lastID) {
        // 请求的URL
//        String url = Services.mHost + "API/Property/GetHouseKeeping/%s/%S?lastId=%S";
        String url = Services.mHost + "API/Property/GetHouseKeeping/%s/%s/%s?lastId=%s";
        url = String.format(url, typeId, UserInformation.getUserInfo().CommunityId, UserInformation.getUserInfo().CommuntiyCityId, lastID);
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
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        Log.d("asdsdasd---", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<com.ldnet.entities.CommunityServices>>() {
                                    }.getType();
                                    if (jsonObject.getString("Obj").equals("[]")) {
                                        showToast("暂时没有数据");
                                        return;
                                    }
                                    datas = gson.fromJson(jsonObject.getString("Obj"), listType);
                                    if (datas != null && datas.size() > 0) {
                                        mDatas.addAll(datas);
                                        mAdapter = new ListViewAdapter<com.ldnet.entities.CommunityServices>(getActivity(), R.layout.item_community_services, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, final com.ldnet.entities.CommunityServices communityServices) {
                                                //标题、地址
                                                holder.setText(R.id.tv_training_title, communityServices.Title)
                                                        .setText(R.id.tv_training_address, communityServices.Address);
                                                //点击电话，拨打电话给商家
                                                holder.getView(R.id.tel_training).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + communityServices.Phone));
                                                        mContext.startActivity(intent);
                                                    }
                                                });
                                                //点击地址，开启导航
                                                holder.getView(R.id.tv_training_address).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        if (!TextUtils.isEmpty(communityServices.getLat()) && !TextUtils.isEmpty(communityServices.getLng())) {
                                                            try {
                                                                HashMap<String, String> extras = new HashMap<String, String>();
                                                                extras.put("LATITUDE", communityServices.getLat());
                                                                extras.put("LONGITUDE", communityServices.getLng());
                                                                extras.put("LEFT", "LEFT");
                                                                gotoActivity(YellowPages_Map.class.getName(), extras);
                                                            } catch (ClassNotFoundException e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            showToast(R.string.position_on);
                                                        }
                                                    }
                                                });
                                            }
                                        };
                                        mLvCommunityServices.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(mLvCommunityServices);
                                    } else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            tv_community_services.setVisibility(View.VISIBLE);
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
}
