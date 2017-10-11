package com.ldnet.utility;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ldnet.activity.Splash;
import com.ldnet.activity.base.ImageLoaderWithCookie;
import com.ldnet.entities.Msg;
import com.ldnet.goldensteward.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.zhy.http.okhttp.OkHttpUtils;
import net.tsz.afinal.FinalDb;
import okhttp3.OkHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.tencent.mm.sdk.platformtools.MMApplicationContext.getContext;

//全局变量
public class GSApplication extends Application {
    private Services services;
    private static GSApplication instance;

    public static GSApplication getInstance() {
        return instance;
    }

    String aa = Services.timeFormat();
    String aa1 = (int) ((Math.random() * 9 + 1) * 100000) + "";
    String aa2 = "";
    String phone = "";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //语音导航
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.app_id));
        //创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
//                .createDefault(this);

        // Log.i("jush", "jush onCreate");
        JPushInterface.setDebugMode(true);//设置开启日志，发布关闭
        JPushInterface.init(this);//初始化jpush
        Set<String> tagSet = new LinkedHashSet<String>();
        String userid = UserInformation.getUserInfo().UserId;
        phone = UserInformation.getUserInfo().UserPhone;
        tagSet.add(UserInformation.getUserInfo().CommunityId);
        tagSet.add(UserInformation.getUserInfo().HouseId);
        JPushInterface.setAliasAndTags(this, userid, tagSet, null);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .imageDownloader(new ImageLoaderWithCookie(getApplicationContext()))
                .build();
        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .writeTimeout(10000L, TimeUnit.MILLISECONDS)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return false;
                    }
                }).build();
        OkHttpUtils.initClient(okHttpClient);
        services = new Services();
        Thread.setDefaultUncaughtExceptionHandler(restartHandler); // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
    }

//    public DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
//            .showImageForEmptyUri(R.drawable.default_info)     //url爲空會显示该图片，自己放在drawable里面的
//            .showImageOnFail(R.drawable.default_info)                //加载图片出现问题，会显示该图片
//            .cacheInMemory(true)
//            .cacheOnDisk(true)
//            .resetViewBeforeLoading(true)
//            .extraForDownloader("13060403423" + "," + aa + "," + aa1)
//            .build();

//    public String str() {
//        if (UserInformation != null) {
//            return UserInformation.getUserInfo().getUserPhone();
//        }
//        return "";
//    }

    public void restartApp() {
        Intent intent = new Intent(instance, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        instance.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }

    // 创建服务用于捕获崩溃异常
    private Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {

//            Log.i("Sandy", "uncaughtException, thread: " + thread
//                    + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
//                    + ex);
            String aa = "thread:" + thread
                    + "name:" + thread.getName() + "id:" + thread.getId() + "exception:"
                    + ex;
            aa = aa.replace("]", "");
            aa = aa.replace("[", "");
            services.PostError(aa);
           // restartApp();//发生崩溃异常时,重启应用
        }
    };
}
