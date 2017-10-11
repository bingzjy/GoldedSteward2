package com.ldnet.activity.find;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.Browser;
import com.ldnet.activity.base.BaseFragment;
import com.ldnet.entities.Information;
import com.ldnet.entities.InformationType;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.CookieInformation;
import com.ldnet.utility.DataCallBack;
import com.ldnet.utility.ListViewAdapter;
import com.ldnet.utility.ReadInfoIDs;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.ldnet.utility.ViewHolder;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

/**
 * Created by zxs on 2016/3/29.
 */
public class InforFragmentContent extends BaseFragment {
    private Services services;

    private RadioGroup ll_information_types;
    private ListView lv_find_informations;
    private ListViewAdapter mAdapter;
    private List<Information> mDatas;
    private List<InformationType> mTypeDatas;//类型集合
    private String mCurrentTypeId;
    private TextView tv_find_informations;
    private PullToRefreshScrollView mPullToRefreshScrollView;
    private ReadInfoIDs read = ReadInfoIDs.getInstance();
    private List<String> isReadIds;
    private Handler mHandler;
    private List<Information> datas;

    protected DisplayImageOptions imageOptions;
    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";

    public static Fragment getInstance(Bundle bundle) {
        InforFragmentContent fragment = new InforFragmentContent();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
                .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .extraForDownloader(UserInformation.getUserInfo().UserPhone + "," + aa + "," + aa1)
                .build();
        return inflater.inflate(R.layout.fragment_infor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public void initView(View view) {
        String titleId = getArguments().getString("titleId");
        mCurrentTypeId = titleId;
        //获取已读
        isReadIds = read.getRead(read.TYPE_INFORMATION);

        services = new Services();
        //
        mHandler = new Handler();
        tv_find_informations = (TextView)view.findViewById(R.id.tv_find_informations);
        mPullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(getActivity()));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(getActivity()));
        lv_find_informations = (ListView) view.findViewById(R.id.lv_find_informations);
        lv_find_informations.setFocusable(false);
        mDatas = new ArrayList<Information>();
        lv_find_informations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i <= mDatas.size()) {
                    Information information = mDatas.get(i);
                    User user = UserInformation.getUserInfo();
                    String userName = !TextUtils.isEmpty(user.UserName) ? user.UserName : user.UserPhone;
                    Intent intent = new Intent(getActivity(), Browser.class);
                    intent.putExtra("PAGE_TITLE", R.string.information_details);
                    intent.putExtra("PAGE_URL", information.InfoUrl + "&IsApp=1&UID=" + user.UserId + "&UName=" + userName + "&UImgID=" + (!TextUtils.isEmpty(user.UserThumbnail) ? user.UserThumbnail : "") + "");
                    intent.putExtra("FROM_CLASS_NAME", InforTabActivity.class.getName());
                    intent.putExtra("PAGE_IMAGE", information.TitleImageID);
                    //分享 - 标题、描述、URL
                    intent.putExtra("PAGE_TITLE_ORGIN", information.Title);
                    intent.putExtra("PAGE_DESCRIPTION_ORGIN", information.Description);
                    intent.putExtra("PAGE_URL_ORGIN", information.InfoUrl);
                    //标记已读
                    read.setRead(information.ID, read.TYPE_INFORMATION);
                    isReadIds.add(information.ID);
                    //改变状态
                    //获取标题,设置已读状态的标题颜色
                    TextView tv_find_detail_title = (TextView) view.findViewById(R.id.tv_find_detail_title);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_left,R.anim.slide_out_to_right);
                    tv_find_detail_title.setTextColor(getResources().getColor(R.color.gray_light_1));
                }
            }
        });
        loadData(true);
        initEvents();
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
               loadData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    loadData(false);
                } else {
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    protected void loadData(Boolean isFirst) {

        String uid = UserInformation.getUserInfo().UserId;
        String name = UserInformation.getUserInfo().UserName;
        String imageId = UserInformation.getUserInfo().UserThumbnail;
        //显示上次刷新时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String dataString = dateFormat.format(new Date(System.currentTimeMillis()));
        if (!isFirst) {
            if (TextUtils.isEmpty(imageId)) {
                imageId = "";
            }
            GetInformationsByType(mCurrentTypeId, mDatas.get(mDatas.size() - 1).ID, uid, name, imageId);
        } else {
            mDatas.clear();
            if (TextUtils.isEmpty(imageId)) {
                imageId = "";
            }
            GetInformationsByType(mCurrentTypeId, "", uid, name, imageId);
        }
    }

    //生活资讯 - 根据分类获取
    public void GetInformationsByType(String typeId, String lastId, String uid, String name, String imageId) {
        //请求的URL
        String url = Services.mHost + "Information/Sel_PageList?LastID=%s&PageCnt=%s&TypeID=%s&UID=%s&UName=%s&UImgID=%s";
        url = String.format(url, lastId, Services.PAGE_SIZE, typeId, uid, name, imageId);
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
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call,e,i);
                        mPullToRefreshScrollView.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        super.onResponse(s, i);
                        mPullToRefreshScrollView.onRefreshComplete();
                        Log.d("asdsdasd", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<Information>>(){}.getType();
                                    datas = gson.fromJson(jsonObject.getString("Obj"),type);
                                    //
                                    if (datas != null && datas.size() > 0) {
                                        mDatas.addAll(datas);
                                        mAdapter = new ListViewAdapter<Information>(getActivity(), R.layout.item_find_detail_infos, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, Information information) {
                                                //加载图片
                                                ImageView image = holder.getView(R.id.iv_infos_detail_image);
                                                if (!TextUtils.isEmpty(information.TitleImageID)) {
                                                    ImageLoader.getInstance().displayImage(services.getImageUrl(information.TitleImageID), image, imageOptions);
                                                } else {
                                                    image.setImageResource(R.drawable.default_info);
                                                }
                                                //标题和描述
                                                holder.setText(R.id.tv_find_detail_title, information.Title)
                                                        .setText(R.id.tv_find_detail_desc, information.Description);

                                                //获取标题,设置已读状态的标题颜色
                                                TextView tv_find_detail_title = holder.getView(R.id.tv_find_detail_title);
                                                if (isReadIds.contains(information.ID)) {
                                                    tv_find_detail_title.setTextColor(getResources().getColor(R.color.gray_light_1));
                                                } else {
                                                    tv_find_detail_title.setTextColor(getResources().getColor(R.color.gray_deep));
                                                }
                                            }
                                        };
                                        lv_find_informations.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(lv_find_informations);
                                    } else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("沒有更多数据");
                                        } else {
                                            tv_find_informations.setVisibility(View.VISIBLE);
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
