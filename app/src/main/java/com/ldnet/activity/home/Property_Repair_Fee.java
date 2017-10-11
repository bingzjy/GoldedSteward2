package com.ldnet.activity.home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.entities.ChargingItem;
import com.ldnet.goldensteward.R;
import com.ldnet.utility.*;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lee on 2016/12/14.
 */
public class Property_Repair_Fee extends BaseActionBarActivity {

    private TextView tv_main_title,tv_text;
    private ImageButton btn_back;
    private ListViewAdapter adapter;
    private ListView listView;
    private List<ChargingItem> chargingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_repair_fee);
        findView();
    }

    public void findView() {
        tv_main_title = (TextView) findViewById(R.id.tv_page_title);
        tv_main_title.setText("收费标准");
        listView = (ListView) findViewById(R.id.lv_charge);
        tv_text = (TextView) findViewById(R.id.tv_text);
        btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);

        getData(UserInformation.getUserInfo().CommunityId);
    }

    public void getData(String cid) {
        String url = Services.mHost + "WFRepairs/APP_WY_GetSFOptionList?CID=%s";
        url = String.format(url, cid);
        String aa = Services.timeFormat();
        String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
        String aa2 = url;
        String md5 = UserInformation.getUserInfo().getUserPhone() + aa + aa1 + aa2 + Services.TOKEN;
        OkHttpUtils.get().url(url)
                .addHeader("phone", UserInformation.getUserInfo().getUserPhone())
                .addHeader("timestamp", aa)
                .addHeader("nonce", aa1)
                .addHeader("signature", Services.textToMD5L32(md5))
                .addHeader("Cookie", CookieInformation.getUserInfo().getCookieinfo())
                .build()
                .execute(new DataCallBack(this) {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        Log.d("asdsdasd---====", "111111111" + s);
                        try {
                            JSONObject json = new JSONObject(s);
                            JSONObject jsonObject = new JSONObject(json.getString("Data"));
                            if (json.getBoolean("Status")) {
                                if (jsonObject.getBoolean("Valid")) {
                                    Gson gson = new Gson();
                                    Type type = new TypeToken<List<ChargingItem>>() {
                                    }.getType();
                                    chargingItems = gson.fromJson(jsonObject.getString("Obj"), type);
                                    if (chargingItems != null && chargingItems.size() > 0) {
                                        tv_text.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);
                                        adapter = new ListViewAdapter<ChargingItem>(Property_Repair_Fee.this, R.layout.ly_pop_win_item, chargingItems) {
                                            @Override
                                            public void convert(ViewHolder holder, ChargingItem chargingItem) {
                                                holder.setText(R.id.tv_charge_name, chargingItem.getTITLE());
                                                holder.setText(R.id.tv_charge_money, chargingItem.getSFMONEY()+"元");
                                            }
                                        };
                                        listView.setAdapter(adapter);
                                        Services.setListViewHeightBasedOnChildren(listView);
                                    }else{
                                        tv_text.setVisibility(View.VISIBLE);
                                        listView.setVisibility(View.GONE);
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            finish();
        }
    }
}
