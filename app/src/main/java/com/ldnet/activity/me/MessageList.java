package com.ldnet.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.MessageData;
import com.ldnet.entities.MessageType;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.FooterLayout;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lee on 2016/7/27.
 */
public class MessageList extends BaseActionBarActivity {

    private TextView tv_main_title,tv_message,tv_message_detail_time,tv_message_detail_title,tv_message_detail_content;
    private ImageButton btn_back;
    private MessageType messageType;
    private List<MessageData> list;
    private ListView lv_message_detail;
    private ListViewAdapter<MessageData> mAdapter;
    private List<MessageData> mDatas;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_message_detail);
        messageType = (MessageType) getIntent().getSerializableExtra("message");
        findView();
        initEvents();
    }

    public void findView(){
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_main_title.setText(R.string.fragment_me_message_list);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        mPullToRefreshScrollView.setFooterLayout(new FooterLayout(this));
        lv_message_detail = (ListView)findViewById(R.id.lv_message_detail);
        lv_message_detail.setFocusable(false);
        mDatas = new ArrayList<MessageData>();

        getJpushMessageList(messageType.getType(),"0");

        lv_message_detail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(MessageList.this, MessageDetail.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("messageData", mDatas.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_back){
            finish();
        }
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mDatas.clear();
                getJpushMessageList(messageType.getType(),"0");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (mDatas != null && mDatas.size() > 0) {
                    getJpushMessageList(messageType.getType(),mDatas.get(mDatas.size() - 1).getId());
                }else{
                    mPullToRefreshScrollView.onRefreshComplete();
                }
            }
        });
    }

    //获取消息
    public void getJpushMessageList(String type,String lastid) {
        String url = Services.mHost + "API/Property/GetJpushMessageList/%s?type=%s&lastId=%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId(),type,lastid);
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
                        mPullToRefreshScrollView.onRefreshComplete();
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        mPullToRefreshScrollView.onRefreshComplete();
                        Log.d("asdsdasd1111111111", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<MessageData>>() {
                                    }.getType();
                                    list = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (list != null && list.size()>0) {
                                        mDatas.addAll(list);
                                        mAdapter = new ListViewAdapter<MessageData>(MessageList.this, R.layout.item_message_detail, mDatas) {
                                            @Override
                                            public void convert(ViewHolder holder, MessageData messageType) {
                                                //标题、价格、时间、地址
                                                holder.setText(R.id.tv_message_detail_title, messageType.getTitle())
                                                        .setText(R.id.tv_message_detail_content, messageType.getContent())
                                                        .setText(R.id.tv_message_detail_time, Services.subStr(messageType.getCreated()));
                                            }
                                        };
                                        lv_message_detail.setAdapter(mAdapter);
                                        Services.setListViewHeightBasedOnChildren(lv_message_detail);
                                    }else {
                                        if (mDatas != null && mDatas.size() > 0) {
                                            showToast("没有更多数据");
                                        } else {
                                            tv_message.setVisibility(View.VISIBLE);
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
