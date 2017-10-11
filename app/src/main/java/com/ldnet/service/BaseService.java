package com.ldnet.service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.ldnet.goldensteward.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lee on 2017/6/24.
 */
public class BaseService {

    // 请求成功
    public static final int DATA_SUCCESS = 100;
    // 请求失败
    public static final int DATA_FAILURE = 101;

    public static final int DATA_REQUEST_ERROR = 102;

    public static final int DATA_SUCCESS_OTHER = 103;

    protected static Context mContext;


    public static void sendErrorMessage(Handler handerError, JSONObject data) {
        Message message = new Message();
        message.what = DATA_FAILURE;
        try {
            message.obj = data.get("Message");
        } catch (JSONException e) {
            e.printStackTrace();
            message.obj= mContext.getString(R.string.network_request_fail);
        } finally {
            handerError.sendMessage(message);
        }

    }



    public static void sendErrorMessage(Handler handerError, String errorMsg) {
        Message message = new Message();
        message.what = DATA_FAILURE;
        message.obj = errorMsg.equals("") ? mContext.getString(R.string.network_request_fail) : errorMsg;
        handerError.sendMessage(message);
    }


    public static boolean checkJsonData(String response,Handler handler){
        try {
            JSONObject json=new JSONObject(response);
            Message message = new Message();
            message.what = DATA_FAILURE;

            if (response.contains(mContext.getString(R.string.refuse))){
                String refuse=json.getString("Message");

                message.obj =refuse;
                handler.sendMessage(message);
                return false;
            }else{
                if (!json.optBoolean("Status")){
                    message.obj =mContext.getString(R.string.network_request_fail);
                    handler.sendMessage(message);
                }else{
                    return true;
                }
            }
        } catch (JSONException e) {
            Message message = new Message();
            message.what = DATA_FAILURE;
            message.obj =mContext.getString(R.string.network_request_fail);
            handler.sendMessage(message);

            e.printStackTrace();
        }
        return false;
    }



//    public void sendFailMessage(Handler handlerFail){
//        Message msg = new Message();
//        msg.what = DATA_REQUEST_ERROR;
//        msg.obj=mContext.getString(R.string.network_error);
//        handlerFail.sendMessage(msg);
//    }
}
