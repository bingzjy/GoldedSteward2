package com.ldnet.activity.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.adapter.GridViewAdapter;
import com.ldnet.activity.adapter.LoopViewPager1;
import com.ldnet.activity.adapter.MyDialog1;
import com.ldnet.activity.adapter.TimeAdapter;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.Property;
import com.ldnet.entities.Repair;
import com.ldnet.entities.Score;
import com.ldnet.entities.User;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by lee on 2016/7/29.
 */
public class Property_Complain_Details extends BaseActionBarActivity {

    private ImageButton btn_back;
    private TextView tv_main_title, tv_property_details_no, tv_share;
    private TextView tv_property_details_title;
    private TextView tv_property_details_status;
    private TextView tv_property_details_type;
    private TextView tv_property_details_house;
    private TextView tv_property_details_time;
    private MyListView lv_list;
    private GridView gv_list;
    private GridViewAdapter gridViewAdapter;
    // 列表适配器
    private TimeAdapter adapter;
    private List<Property> mTemp;
    private List<Property> mDatas;
    private List<String> mDatas1;
    private String mRepairId = "";
    private String status = "";
    private Repair repair_complain;
    private String flag = "";

    private PopupWindows popupWindows;
    private int num;
    private LinearLayout ll_popup;

    private String[] pics;
    private String SCORE = "";

    private MyDialog1 alertDialog;
    private TextView tv_socre;
    private EditText et_say;
    private RatingBar room_ratingbar, rb_score;
    private Button btn_cancel, btn_confirm;
    private Score score;
    private float s;
    private boolean aaa = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);
        SCORE = getIntent().getStringExtra("SCORE");
        flag = getIntent().getStringExtra("FLAG");
        mRepairId = getIntent().getStringExtra("REPAIR_ID");
        status = getIntent().getStringExtra("REPAIR_STATUS");
        repair_complain = (Repair) getIntent().getSerializableExtra("REPAIR");
        findView();
    }

    public void findView() {
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_share = (TextView) findViewById(R.id.tv_share);
        tv_share.setVisibility(View.GONE);
        if (flag.equals("COMPLAIN")) {
            tv_main_title.setText("投诉详情");
        } else if (flag.equals("REPAIR")) {
            tv_main_title.setText("报修详情");
        }
        if (!TextUtils.isEmpty(SCORE) && SCORE.equals("TRUE")) {
            tv_share.setVisibility(View.VISIBLE);
            tv_share.setText("评价");
        }
        lv_list = (MyListView) findViewById(R.id.lv_list);
        gv_list = (GridView) findViewById(R.id.gv_list);
        lv_list.setFocusable(false);
        tv_property_details_title = (TextView) findViewById(R.id.tv_property_details_title);
        tv_property_details_no = (TextView) findViewById(R.id.tv_property_details_no);
        tv_property_details_status = (TextView) findViewById(R.id.tv_property_details_status);
        tv_property_details_type = (TextView) findViewById(R.id.tv_property_details_type);
        tv_property_details_time = (TextView) findViewById(R.id.tv_property_details_time);
        tv_property_details_house = (TextView) findViewById(R.id.tv_property_details_house);
        rb_score = (RatingBar) findViewById(R.id.rb_score);
        tv_property_details_type.setVisibility(View.GONE);
        tv_property_details_title.setText(repair_complain.getContent());
        tv_property_details_status.setText(repair_complain.getNodesName());
        tv_property_details_no.setText("订单编号:" + repair_complain.getOrderNumber());
        tv_property_details_house.setText("房号:" + repair_complain.getRoomName());
        tv_property_details_type.setText("报修类型:" + repair_complain.getRtypeName());
        tv_property_details_time.setText(Services.subStr(repair_complain.getCreateDay()));
        btn_back.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        mDatas = new ArrayList<Property>();
        mDatas1 = new ArrayList<String>();
        RCCommunicate(mRepairId);
        GetScoreInfo(mRepairId);
        AnimationSet set = new AnimationSet(false);
        Animation animation = new AlphaAnimation(0, 1);   //AlphaAnimation 控制渐变透明的动画效果
        animation.setDuration(500);     //动画时间毫秒数
        set.addAnimation(animation);    //加入动画集合
        LayoutAnimationController controller = new LayoutAnimationController(set, 1);
        lv_list.setLayoutAnimation(controller);   //ListView 设置动画效果
        if (!TextUtils.isEmpty(repair_complain.getContentImg())) {
            if (repair_complain.getContentImg().contains(",")) {
                pics = repair_complain.getContentImg().split(",");
                for (int i = 0; i < pics.length; i++) {
                    mDatas1.add(pics[i]);
                }
            } else {
                mDatas1.add(repair_complain.getContentImg());
            }
            gridViewAdapter = new GridViewAdapter(this, mDatas1);
            gv_list.setAdapter(gridViewAdapter);
        }

        gv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                num = position;
                popupWindows = new PopupWindows(Property_Complain_Details.this, view, mDatas1);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            finish();
            if(aaa) {
                Services.comment = mRepairId;
            }
        } else if (view.getId() == R.id.tv_share) {
            ScoreDialog();
//            Intent intent = new Intent(Property_Complain_Details.this, Property_Score.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            if(aaa) {
                Services.comment = mRepairId;
            }
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }

    public void ScoreDialog() {
        alertDialog = new MyDialog1(this);
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.score_dialog);
        tv_socre = (TextView) alertDialog.findViewById(R.id.tv_socre);
        et_say = (EditText) alertDialog.findViewById(R.id.et_say);
        room_ratingbar = (RatingBar) alertDialog.findViewById(R.id.room_ratingbar);
        btn_cancel = (Button) alertDialog.findViewById(R.id.btn_cancel);
        btn_confirm = (Button) alertDialog.findViewById(R.id.btn_confirm);
        WindowManager.LayoutParams lp = window.getAttributes();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        lp.width = (int) (d.getWidth() * 0.9); // 宽度设置为屏幕的0.65
        window.setGravity(Gravity.CENTER);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setAttributes(lp);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        room_ratingbar.setOnRatingBarChangeListener(new RatingBarChangeListenerImpl());
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int)s != 0) {
                    CreateScore(Math.abs(6 - s), et_say.getText().toString().trim());
                } else {
                    showToast("请先评分");
                    return;
                }
                alertDialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                hintKbTwo(et_say);
            }
        });
    }

    private class RatingBarChangeListenerImpl implements RatingBar.OnRatingBarChangeListener {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            s = rating;
            if (rating == 0) {
                tv_socre.setText("评分");
            } else if (rating == 1) {
                tv_socre.setText("非常不满意");
            } else if (rating == 2) {
                tv_socre.setText("不满意");
            } else if (rating == 3) {
                tv_socre.setText("一般");
            } else if (rating == 4) {
                tv_socre.setText("满意");
            } else if (rating == 5) {
                tv_socre.setText("非常满意");
            }
        }

    }

    //获取评分
    public void GetScoreInfo(final String id) {
        // 请求的URL
        String url = Services.mHost + "WFComplaint/APP_YZ_GetScoreInfo?RID=%s";
        url = String.format(url, id);
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
                        Log.d("asdsdasd5555555", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    score = gson.fromJson(jsonObject.getString("Obj"), Score.class);
                                    if (score != null) {
                                        rb_score.setVisibility(View.VISIBLE);
                                        tv_share.setVisibility(View.GONE);
                                        rb_score.setRating(Math.abs(6 - Float.parseFloat(score.getSocreCnt())));
                                    } else {
                                        rb_score.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //提交评分
    public void CreateScore(final float rate,final  String et) {
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        // 请求的URL
        String url = Services.mHost + "WFComplaint/APP_YZ_CreateScore";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RID", mRepairId);
            jsonObject.put("SocreCnt", rate);
            jsonObject.put("OrtherContent", et);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String dd = "{"+"\"str\""+":"+"\""+jsonObject.toString()+"\"}";
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
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd5555555", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    showToast("评价成功");
                                    hintKbTwo(et_say);
                                    rb_score.setVisibility(View.VISIBLE);
                                    rb_score.setRating(Math.abs(6 -rate));
                                    aaa = true;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //报修&投诉沟通信息
    public void RCCommunicate(String id) {
        // 请求的URL
        User user = UserInformation.getUserInfo();
        String url = Services.mHost + "WFComplaint/APP_YZ_GetOperateList?RID=%s&IsDesc=%s";
        url = String.format(url, id, true);
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
                        super.onResponse(s, i);
                        Log.d("asdsdasd--", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<Property>>() {
                                    }.getType();
                                    mTemp = gson.fromJson(jsonObject.getString("Obj"), listType);
                                    adapter = new TimeAdapter(Property_Complain_Details.this, mTemp);
                                    lv_list.setAdapter(adapter);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //报修沟通状态更新
    public void UpdateRCCommunicateStatus(String id) {
        // 请求的URL
        String url = Services.mHost + "API/Property/SetCommunicationStatus/%s?residentId=%s";
        url = String.format(url, id, UserInformation.getUserInfo().UserId);
        final String finalUrl = url;
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
                                    Services.IntegralTip(finalUrl);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public class PopupWindows extends PopupWindow {

        public PopupWindows(Context mContext, View parent, List<String> TimeBean) {

            View view = View
                    .inflate(mContext, R.layout.pop_viewpager, null);
            view.startAnimation(AnimationUtils.loadAnimation(mContext,
                    R.anim.fade_in));
            ll_popup = (LinearLayout) view
                    .findViewById(R.id.ll_popup);
//            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
//                    R.anim.slide_in_from_left));
            setAnimationStyle(R.style.AnimationPreview);
            setWidth(getWindowManager().getDefaultDisplay().getWidth());
            setHeight(getWindowManager().getDefaultDisplay().getHeight());
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM | Gravity.TOP | Gravity.LEFT | Gravity.RIGHT, -10, -10);
            update();
            ll_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            LoopViewPager1 iv_show = (LoopViewPager1) view
                    .findViewById(R.id.pager);
            iv_show.setAdapter(new MyAdapter(Property_Complain_Details.this,
                    TimeBean));
            iv_show.setCurrentItem(num);
        }
    }

    private class MyAdapter extends PagerAdapter {

        /**
         * 图片资源列表
         */
        private List<String> mAdList = new ArrayList<String>();
        private Context mContext;

        public MyAdapter(Context context, List<String> adList) {
            this.mContext = context;
            this.mAdList = adList;
        }

        @Override
        public int getCount() {
            return mAdList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String imageUrl = mAdList.get(position);
            final View view = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_popwindow, null);
            ImageView imageView = (ImageView) view
                    .findViewById(R.id.iv_show);
            TextView textView = (TextView) view.findViewById(R.id.tv_count1);
            // 设置图片点击监听
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindows.dismiss();
                }
            });
            textView.setText((position + 1) + "/" + mAdList.size());
            ImageLoader.getInstance().displayImage(Services.getImageUrl(imageUrl), imageView,
                    imageOptions);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 这里不需要做任何事情
        }

    }
}
