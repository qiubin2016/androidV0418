package com.huawei.agentlitedemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.huawei.agentlitedemo.R;
import com.huawei.agentlitedemo.bean.ConfigName;
import com.huawei.agentlitedemo.bean.GatewayInfo;
import com.huawei.agentlitedemo.util.AgentLiteUtil;
import com.huawei.agentlitedemo.util.FileUtil;
import com.huawei.agentlitedemo.util.LogUtil;
import com.huawei.agentlitedemo.widget.BaseAty;
import com.huawei.iota.base.BaseService;
import com.huawei.iota.bind.BindConfig;
import com.huawei.iota.iodev.IotaDeviceInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class AgentLiteMain extends BaseAty {

    private static final String TAG = "AgentLiteMain";
    private static String configFile = "config.properties";

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agentlite_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Main");
        }
        try {
            Context context = getApplicationContext();

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                }
            }

            //获取sdcard路径
            File sdcardPath = Environment.getExternalStorageDirectory();
            Log.i(TAG, "sdcardPath = " + sdcardPath.toString());
            //sdcardPath = /storage/emulated/0

            String workPath = FileUtil.getWorkDir(context).getAbsolutePath() + "/AgentLiteDemo";
            //默认workPath无权限进入，修改路径
            workPath = "/sdcard" + "/AgentLiteDemo";
            String logPath = workPath + "/log";
            File dir = new File(logPath);
            dir.mkdirs();

            Log.i(TAG, "workPath = " + workPath);
            Log.i(TAG, "logPath = " + logPath);

            FileUtil.copyAssetDirToFiles(context, "conf");

            //  /sdcard/AgentLiteDemo/conf/config.properties
            loadProperties(workPath + "/conf/" + configFile);  //读取配置文件
            loadSharedData();  //读取数据库数据

            if (BaseService.init(workPath, logPath, context)) {
                gotoNextPage();
            } else {
                Toast.makeText(this, "BaseService init failed", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            //Throwable有两个重要的子类：Exception（异常）和 Error（错误），二者都是 Java 异常处理的重要子类，各自都包含大量子类
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        Log.i(TAG, "onResume++++++++++++++++++++++:" + count);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        BaseService.destroy();
    }

    //
    private void gotoNextPage() {
        String deviceID = GatewayInfo.getDeviceID();
        if (deviceID != null) {
            gotoLogin();
        } else {
            gotoSettings();
        }
    }

    private void gotoLogin() {
        Intent intent = new Intent();
        intent.setClass(AgentLiteMain.this, AgentLiteLogin.class);
        startActivity(intent);
    }

    private void gotoSettings() {
        Intent intent = new Intent();
        intent.setClass(AgentLiteMain.this, AgentLiteSettings.class);
        startActivity(intent);
    }

    //读取数据库数据
    private void loadSharedData() {
        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        String deviceID = preferences.getString("deviceID", null);
        if (deviceID != null) {
            GatewayInfo.setDeviceID(deviceID);
        }

        String secret = preferences.getString("secret", null);
        if (secret != null) {
            GatewayInfo.setSecret(secret);
        }

        String appID = preferences.getString("appID", null);
        if (appID != null) {
            GatewayInfo.setAppID(appID);
        }

        String haAddress = preferences.getString("haAddress", null);
        if (haAddress != null) {
            GatewayInfo.setHaAddress(haAddress);
        }

        String lvsAddress = preferences.getString("lvsAddress", null);
        if (lvsAddress != null) {
            GatewayInfo.setLvsAddress(lvsAddress);
        }

        String mqttTopic = preferences.getString("mqttTopic", null);
        if (mqttTopic != null) {
            GatewayInfo.setMqttTopic(mqttTopic);
        }

        String mqttClientId = preferences.getString("mqttClientId", null);
        if (mqttClientId != null) {
            GatewayInfo.setMqttClientId(mqttClientId);
        }

        String mqttServerPort = preferences.getString("mqttServerPort", null);
        if (mqttServerPort != null) {
            GatewayInfo.setMqttServerPort(mqttServerPort);
        }

        String httpServerPort = preferences.getString("httpServerPort", null);
        if (httpServerPort != null) {
            GatewayInfo.setHttpServerPort(httpServerPort);
        }

        String nodeId = preferences.getString("nodeId", null);
        if (nodeId != null) {
            GatewayInfo.setNodeId(nodeId);
        }

        String sensorId = preferences.getString("SENSORID", null);
        if (sensorId != null) {
            GatewayInfo.setSensorId(sensorId);
        }
    }

    //读取配置文件
    private void loadProperties(String configFile) {
        try {
            AgentLiteUtil.init(this);
            if (AgentLiteUtil.hasInitialized()) {
                return;
            }
            // /sdcard/AgentLiteDemo/conf/config.properties
            File configfile = new File(configFile);
            /*加载调测的配置文件*/
            if (configfile.exists()) {
                Log.i(TAG, "load properties from " + configFile);
                InputStream inputStream = new FileInputStream(configfile);
                AgentLiteUtil.loadProperty(inputStream);
                Log.i(TAG, "platformIP = " + AgentLiteUtil.get(ConfigName.platformIP));
            }
        } catch (Throwable t) {
            Log.e(TAG, "setSystemInfo error.reason:{0}.", t);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }
}
