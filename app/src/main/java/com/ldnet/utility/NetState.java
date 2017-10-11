package com.ldnet.utility;

import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.autonavi.rtbt.IFrameForRTBT;
import com.ldnet.activity.qindian.ChargeBatteryActivity;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import java.util.Set;

import static android.content.Context.WIFI_SERVICE;
import static com.unionpay.mobile.android.global.a.I;

/**
 * Created by Administrator on 2015/1/19.
 * 网络状态监控
 */
public class NetState extends BroadcastReceiver {

    @Override
    public void onReceive(Context con, Intent arg1) {
        ConnectivityManager manager = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!gprs.isConnected() && !wifi.isConnected()) {
            com.ldnet.utility.Toast.makeText(con, "暂时无网络,请检查网络是否连接", 1000).show();
            Services.net = false;
        } else if (gprs.isConnected()) {
            Services.net = true;
            String className = con.getClass().getSimpleName();
//            if (!className.equals(ChargeBatteryActivity.class.getSimpleName())) {
//                com.ldnet.utility.Toast.makeText(con, "网络已恢复", 1000).show();
//            }
        } else if (wifi.isConnected()) {
            Services.net = true;
            String className = con.getClass().getSimpleName();

            Log.e("tag","className"+className);
//            if (!className.equals(ChargeBatteryActivity.class.getSimpleName())) {
//                com.ldnet.utility.Toast.makeText(con, "网络已恢复", 1000).show();
//            }
        }
    }
}
