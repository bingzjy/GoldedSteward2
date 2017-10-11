package com.ldnet.utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import android.util.Log;
import com.ldnet.activity.Browser;
import com.ldnet.activity.MainActivity;
import com.ldnet.activity.home.Notification;
import com.ldnet.activity.home.Property_Fee;

import com.ldnet.activity.home.Property_Services;
import com.ldnet.activity.mall.Orders;
import com.ldnet.activity.me.Feedback;
import com.ldnet.activity.me.Message;
import com.ldnet.entities.Msg;
import com.ldnet.entities.MsgCenter;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zxs on 2015/12/24.
 */
public class MyNetWorkBroadcastReceive extends BroadcastReceiver {
    Context mContext;
    String type = null;
    String cid = null;
    MsgCenter msgCenter = new MsgCenter();
    String center = "";

    public static ArrayList<onNewMessageListener> msgListeners = new ArrayList<onNewMessageListener>();

    public static interface onNewMessageListener {
        public abstract void onNewMessage(String message);
    }

    public MyNetWorkBroadcastReceive() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            System.out.println("收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            Log.e("asdsdasd","NetWorkBroadcastReceive"+bundle.getString(JPushInterface.EXTRA_MESSAGE)+"---");
//            wakeAndUnlock(true);
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //type=1 投诉 =2报修  =3沟通 =0通知 公告
            // 在这里可以做些统计，或者做些其他工作
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            // get the info from the currently running task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            System.out.println("收到了通知" + bundle.getString(JPushInterface.EXTRA_EXTRA));
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (extras != null) {
                Log.e("asdsdasd","NetWorkBroadcastReceive......."+extras);
                try {
                    type = new JSONObject(extras).getString("type");
                    cid = new JSONObject(extras).getString("cid");
                    if (cid.equals(UserInformation.getUserInfo().CommunityId)) {
                        center = "false";
                        if (type.equals("0")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setNOTICE(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("4")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setFEE(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("5")) {
                        } else if (type.equals("1")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setCOMPLAIN(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("2")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setREPAIRS(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("3")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setCOMMUNICATION(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("6")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setFEEDBACK(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("7")) {
                            Msg msg = MsgInformation.getMsg();
                            msg.setORDER(true);
                            MsgInformation.setMsgInfo(msg);
                        } else if (type.equals("8")) {

                        }
                    } else {
                        center = "true";
                        Msg msg = MsgInformation.getMsg();
                        msg.setMESSAGE(true);
                        MsgInformation.setMsgInfo(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String content = bundle.getString(JPushInterface.EXTRA_ALERT);
                for (int i = 0; i < msgListeners.size(); i++) {
                    msgListeners.get(i).onNewMessage(type + "," + componentInfo + "," + center);
                }
            }
//            wakeAndUnlock(true);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (extras != null) {
                try {
                    type = new JSONObject(extras).getString("type");
                    cid = new JSONObject(extras).getString("cid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("用户点击打开了通知" + type);
                // 在这里可以自己写代码去定义用户点击后的行为
                if (cid.equals(UserInformation.getUserInfo().CommunityId)) {
                    if (type.equals("0")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setNOTICE(false);
                        MsgInformation.setMsgInfo(msg);
                        Intent intent0 = new Intent(context, Notification.class);
                        intent0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent0);
                    } else if (type.equals("4")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setFEE(false);
                        MsgInformation.setMsgInfo(msg);
                        Intent intent1 = new Intent(context, Property_Fee.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent1);
                    } else if (type.equals("5")) {
                        Intent intent5 = new Intent(context, Browser.class);
                        intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent5);
                    } else if (type.equals("1")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setCOMPLAIN(true);
                        MsgInformation.setMsgInfo(msg);
                        Intent intentOther = new Intent(context, Property_Services.class);
                        intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intentOther);
                    } else if (type.equals("2")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setREPAIRS(true);
                        MsgInformation.setMsgInfo(msg);
                        Intent intentOther = new Intent(context, Property_Services.class);
                        intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intentOther);
                    } else if (type.equals("3")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setCOMMUNICATION(true);
                        MsgInformation.setMsgInfo(msg);
                        Intent intentOther = new Intent(context, Property_Services.class);
                        intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intentOther);
                    } else if (type.equals("6")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setFEEDBACK(false);
                        MsgInformation.setMsgInfo(msg);
                        Intent intentOther = new Intent(context, Message.class);
                        intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intentOther);
                    } else if (type.equals("7")) {
                        Msg msg = MsgInformation.getMsg();
                        msg.setORDER(false);
                        MsgInformation.setMsgInfo(msg);
                        Intent intentOther = new Intent(context, Orders.class);
                        intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intentOther);
                    } else if (type.equals("8")) {

                    }
                }else{
                    Msg msg = MsgInformation.getMsg();
                    msg.setMESSAGE(false);
                    MsgInformation.setMsgInfo(msg);
                    Intent intentOther = new Intent(context, Message.class);
                    intentOther.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intentOther);
                }
            }
        }
    }

    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;


    private void wakeAndUnlock(boolean b) {
        if (b) {
            //获取电源管理器对象
            pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire();
            //得到键盘锁管理器对象
            km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");
            //解锁
//            kl.disableKeyguard();
        } else {
            //锁屏
            kl.reenableKeyguard();
            //释放wakeLock，关灯
            wl.release();
        }
    }

}
