package com.ldnet.activity.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ldnet.goldensteward.R;
import com.ldnet.utility.Services;
import com.ldnet.utility.UserInformation;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import net.tsz.afinal.FinalBitmap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BaseActionBarFragmentActivity extends FragmentActivity {

    protected LoadingDialog dialog;
    Context context;
    protected FinalBitmap finalBitmap;
    private int flag;
    protected DisplayImageOptions imageOptions;
    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finalBitmap = FinalBitmap.create(this); //初始化
        finalBitmap.configBitmapLoadThreadSize(3);//定义线程数量
        finalBitmap.configDiskCachePath(Environment.getExternalStorageDirectory() + "/upload/upload.jpeg");//设置缓存目录；
        finalBitmap.configDiskCacheSize(1024 * 1024 * 10);//设置缓存大小
        finalBitmap.configLoadingImage(R.drawable.default_goods);//设置加载图片

        imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
                .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .extraForDownloader(UserInformation.getUserInfo().UserPhone + "," + aa + "," + aa1)
                .build();
    }

    /**
     * 判断checkBox弹出的图层是否显示
     *
     * @return
     */
    protected boolean isShownTopBar() {
        return false;
    }

    /**
     * 关闭checkBox弹出的图层
     *
     * @return
     */
    protected void closeTopBar() {
    }

    /**
     * 打开checkBox弹出的图层
     *
     * @return
     */
    protected void openTopBar() {
    }

    //---------------------------------------------------------------------------------------
    public void showToast(String str, String defaultTip) {
        if (!TextUtils.isEmpty(str.trim())) {
            com.ldnet.utility.Toast.makeText(this, str, 1000).show();
        } else {
            if (!TextUtils.isEmpty(defaultTip.trim())) {
                com.ldnet.utility.Toast.makeText(this, defaultTip, 1000).show();
            }
        }
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String str) {
        showToast(str, "");
    }

    public void showProgressDialog(String str) {
        if (dialog == null) {
            dialog = new LoadingDialog(this);
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.setText(str);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * 隐藏输入法面板
     */
    public static void hideKeyboard(Activity c, View v) {
        InputMethodManager imm = (InputMethodManager) c.getSystemService(c.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showProgressDialog() {
        this.showProgressDialog("正在加载...");
    }

    public void closeProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // 页面跳转方法
    public void gotoActivityAndFinish(String className,
                                      HashMap<String, String> extras) throws ClassNotFoundException {

        // 跳转到新的Activity
        gotoActivity(className, extras);
        // 结束当前Activity
        this.finish();
    }

    // 页面跳转方法
    public void gotoActivity(String className, HashMap<String, String> extras)
            throws ClassNotFoundException {

        // 定义 intent
        Intent intent = new Intent(this, Class.forName(className));

        // 添加参数
        if (extras != null) {
            Iterator<Map.Entry<String, String>> iterator = extras.entrySet()
                    .iterator();
            while (iterator.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) iterator.next();
                intent.putExtra(entry.getKey().toString(), entry.getValue()
                        .toString());
                if(entry.getKey().toString().equals("LEFT")){
                    flag = 1;
                }
            }
        }

        // 跳转
        startActivity(intent);
        if(flag == 1) {
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        }else {
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    public void openNetWork() {
        // 如果网络不可用，则弹出对话框，对网络进行设置
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("没有可用的网络");
        builder.setMessage("是否对网络进行设置?");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = null;
                        try {
                            String sdkVersion = android.os.Build.VERSION.SDK;
                            if (Integer.valueOf(sdkVersion) > 10) {
                                intent = new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                            } else {
                                intent = new Intent();
                                ComponentName comp = new ComponentName(
                                        "com.android.settings",
                                        "com.android.settings.WirelessSettings");
                                intent.setComponent(comp);
                                intent.setAction("android.intent.action.VIEW");
                            }
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );
        builder.show();
    }

    @Override
    protected void onDestroy() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
        super.onDestroy();
    }
}
