package com.ldnet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.amap.api.maps.model.Text;
import com.dh.bluelock.imp.BlueLockPubCallBackBase;
import com.dh.bluelock.object.LEDevice;
import com.dh.bluelock.pub.BlueLockPub;
import com.dh.bluelock.util.Constants;
import com.ldnet.entities.EntranceGuard;
import com.ldnet.goldensteward.R;
import com.ldnet.service.EntranceGuardService;
import com.ldnet.utility.KeyCache;
import com.tencent.mm.sdk.Build;

import java.util.HashMap;
import java.util.Set;

/**
 * @author lpf
 * @since 2017/8/9
 */
public class EntranceGuardSplash extends Activity {
    private TextView mTvKeyChainPacket, tvHouseInfo;
    private ImageView imageViewBack;
    // private LoadingDotView mLoadingDotView;
    private BluetoothAdapter mBTAdapter;
    private TextView mTvOpenDoorTip;
    private HashMap<String, LEDevice> mScanDeviceResult = new HashMap<>();
    private String mDeviceId;
    private BlueLockPub mBlueLockPub;
    private String tag = EntranceGuardSplash.class.getSimpleName();
    private String deviceID;
    private boolean opened;
    private EntranceGuardService entranceGuardService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance_guard_splash);
        entranceGuardService = new EntranceGuardService(this);

        initView();
        requestPermission();
    }

    private void initView() {
        tvHouseInfo = (TextView) findViewById(R.id.tv_house_info);
        mTvOpenDoorTip = (TextView) findViewById(R.id.tv_show_open_door_information);
        imageViewBack = (ImageView) findViewById(R.id.imageView_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (KeyCache.getCurrentHouse() != null) {
            tvHouseInfo.setText(KeyCache.getCurrentHouse());
        }
    }


    private void initBlueTooth() {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mBlueLockPub = BlueLockPub.bleLockInit(EntranceGuardSplash.this);
        LocalCallBack localCallBack = new LocalCallBack();
        mBlueLockPub.setResultCallBack(localCallBack);
        if (mBTAdapter.isEnabled()) {
            mTvOpenDoorTip.setText("正在连接蓝牙门禁...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mBlueLockPub.setLockMode(Constants.LOCK_MODE_MANUL, null, false);
                    mBlueLockPub.scanDevice(3000);
                }
            }).start();
        } else {
            Toast.makeText(EntranceGuardSplash.this, "请手动打开蓝牙", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 是否开启蓝牙的对话框
     */
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EntranceGuardSplash.this);
        builder.setMessage("是否打开蓝牙设备?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        dialogInterface.dismiss();
                    }
                })

                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        mBTAdapter.enable();
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }



    //定义回调类（开门回调、扫描回调、扫描完毕回调）
    class LocalCallBack extends BlueLockPubCallBackBase {
        @Override
        public void openCloseDeviceCallBack(int i, int i1, String... strings) {
            mTvOpenDoorTip.setText("欢迎回家");
            Toast.makeText(EntranceGuardSplash.this, "开门成功", Toast.LENGTH_SHORT).show();
            //添加开门日志
           entranceGuardService.EGLog(deviceID, handlerEGlog);
        }

        @Override
        public void scanDeviceCallBack(LEDevice leDevice, int i1, int i2) {
            if (leDevice != null) {
                mDeviceId = leDevice.getDeviceId();
                mScanDeviceResult.put(mDeviceId, leDevice);
            } else {
                Toast.makeText(EntranceGuardSplash.this, "搜索设备失败-_-", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public void scanDeviceEndCallBack(int j) {
            Set<String> keySet = KeyCache.getKeyCache();
            if (keySet == null || keySet.size() == 0) {
                Toast.makeText(EntranceGuardSplash.this, "当前房屋暂无钥匙，无法开门", Toast.LENGTH_SHORT).show();
            } else {
                for (String keyMsg : keySet) {
                    Log.e(tag, keyMsg);
                    String[] msgArr = keyMsg.split(",");
                    String keyId = msgArr[0];
                    LEDevice device = mScanDeviceResult.get(keyId);
                    if (device != null) {
                        Log.e(tag, "开门");
                        opened = true;
                        deviceID = device.getDeviceId();
                        device.setDevicePsw(msgArr[1]);
                        mBlueLockPub.oneKeyOpenDevice(device, device.getDeviceId(), device.getDevicePsw());

                        break;
                    }
                }

                if (!opened) {
                    Toast.makeText(EntranceGuardSplash.this, "请靠近设备再试", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 110);
            } else {
                initBlueTooth();
            }
        } else {
            initBlueTooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initBlueTooth();
                } else {
                    Toast.makeText(EntranceGuardSplash.this, "请手动开启位置权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    Handler handlerEGlog = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 2000:
                    Toast.makeText(EntranceGuardSplash.this,"已添加开门日志",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
