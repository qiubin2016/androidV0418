package com.huawei.agentlitedemo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agentlite_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Main");
        }
        try {
            Context context = getApplicationContext();

            String workPath = FileUtil.getWorkDir(context).getAbsolutePath() + "/AgentLiteDemo";
            String logPath = workPath + "/log";
            File dir = new File(logPath);
            dir.mkdirs();

            Log.i(TAG, "workPath = " + workPath);
            Log.i(TAG, "logPath = " + logPath);

            FileUtil.copyAssetDirToFiles(context, "conf");

            loadProperties(workPath + "/conf/" + configFile);
            loadSharedData();

            if (BaseService.init(workPath, logPath, context)) {
                gotoNextPage();
            } else {
                Toast.makeText(this, "BaseService init failed", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
}
