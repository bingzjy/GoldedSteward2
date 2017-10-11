package com.ldnet.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.ldnet.activity.BindingHouse;
import com.ldnet.activity.base.BaseActionBarActivity;
import com.ldnet.goldensteward.R;
import com.ldnet.service.BaseService;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lee on 2016/7/8.
 */
public class DataCallBack extends StringCallback{

    Context context;
    Handler handler;

    // 请求成功
    public static final int DATA_SUCCESS = 100;
    // 请求失败
    public static final int DATA_FAILURE = 101;

    public static final int DATA_REQUEST_ERROR = 102;

    public static final int DATA_SUCCESS_OTHER = 103;



    public DataCallBack(Context context) {
        this.context = context;
    }

    public DataCallBack(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }



    @Override
    public void onError(Call call, Exception e, int i) {
        Message msg=new Message();
        msg.what=DATA_REQUEST_ERROR;
        if (Services.net) {
            msg.obj=context.getString(R.string.network_request_fail);
          //  com.ldnet.utility.Toast.makeText(context, "请求网络失败，请稍后再试！", 1000).show();
        } else {
            msg.obj=context.getString(R.string.network_none_tip);
          //  com.ldnet.utility.Toast.makeText(context, "暂时无网络,请检查网络是否连接！", 1000).show();
        }
        handler.sendMessage(msg);
    }



    @Override
    public void onResponse(String s, int i) {
            if (TextUtils.isEmpty(s)) {
                com.ldnet.utility.Toast.makeText(context,context.getString(R.string.response_data_isNull), 1000).show();
                return;
            }
    }




}
