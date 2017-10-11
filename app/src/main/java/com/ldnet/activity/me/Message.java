package com.ldnet.activity.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.*;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.ldnet.view.HeaderLayout;
import com.library.PullToRefreshBase;
import com.library.PullToRefreshScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class Message extends BaseActionBarActivity {

    private TextView tv_main_title;
    private ImageButton btn_back;
    private Services services;
    private List<MessageType> messageType;
    private MyListView lv_message;
    private ListViewAdapter<MessageType> mAdapter;
    private PullToRefreshScrollView mPullToRefreshScrollView;

    //初始化视图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_message);
//        MyNetWorkBroadcastReceive.msgListeners.add(this);
        //初始化控件
        // 标题
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText(R.string.fragment_me_message);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.main_act_scrollview);
        mPullToRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshScrollView.setHeaderLayout(new HeaderLayout(this));
        lv_message = (MyListView) findViewById(R.id.lv_message);
        lv_message.setFocusable(false);
        initEvent();
        initEvents();
        //初始化服务
        services = new Services();

        lv_message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Message.this, MessageList.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("message", messageType.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });
    }

    //初始化事件
    public void initEvent() {
        btn_back.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    //获取消息中心分类
    public void getJpushMessageType() {
        String url = Services.mHost + "API/Property/GetJpushMessageType?uid=%s";
        url = String.format(url, UserInformation.getUserInfo().getUserId());
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
                                    Type type = new TypeToken<List<MessageType>>() {
                                    }.getType();
                                    messageType = gson.fromJson(jsonObject.getString("Obj"), type);
                                    mAdapter = new ListViewAdapter<MessageType>(Message.this, R.layout.item_message, messageType) {
                                        @Override
                                        public void convert(ViewHolder holder, MessageType messageType) {
                                            //设置图片
                                            if (!TextUtils.isEmpty(messageType.getImage())) {
                                                ImageLoader.getInstance().displayImage(Services.mHost + messageType.getImage(), (ImageView) holder.getView(R.id.iv_message_image), imageOptions);
                                            }
                                            //标题、价格、时间、地址
                                            holder.setText(R.id.tv_message_title, messageType.getTitle())
                                                    .setText(R.id.tv_message_content, messageType.getContent())
                                                    .setText(R.id.tv_message_time, Services.subStr(messageType.getCreated()));
                                            ImageView iv_message = holder.getView(R.id.iv_message);
//
                                            if (MsgInformation.getMsg().isMESSAGE()) {
                                                iv_message.setVisibility(View.VISIBLE);
                                            } else {
                                                iv_message.setVisibility(View.GONE);
                                            }
                                        }
                                    };
                                    lv_message.setAdapter(mAdapter);
                                    Services.setListViewHeightBasedOnChildren(lv_message);
                                } else {
                                    showToast("沒有更多数据");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void initEvents() {
        mPullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                getJpushMessageType();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MyNetWorkBroadcastReceive.msgListeners.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getJpushMessageType();
    }
}
