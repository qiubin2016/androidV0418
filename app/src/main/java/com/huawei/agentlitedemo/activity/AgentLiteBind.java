package com.huawei.agentlitedemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.huawei.agentlitedemo.R;
import com.huawei.agentlitedemo.bean.ConfigName;
import com.huawei.agentlitedemo.bean.GatewayInfo;
import com.huawei.agentlitedemo.bean.ScrollableLogView;
import com.huawei.agentlitedemo.util.AgentLiteUtil;
import com.huawei.agentlitedemo.util.LogUtil;
import com.huawei.agentlitedemo.widget.BaseAty;
import com.huawei.iota.bind.BindConfig;
import com.huawei.iota.bind.BindService;
import com.huawei.iota.iodev.IotaDeviceInfo;
import com.huawei.iota.util.IotaMessage;

public class AgentLiteBind extends BaseAty implements ScrollableLogView {
    private static final String TAG = "AgentLiteBind";

    private Button startButton;
    private TextView infoText;
    private ScrollView scrollView;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agentlite_bind);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        startButton = (Button) findViewById(R.id.button_start);
        infoText = (TextView) findViewById(R.id.text_info);

        LogUtil.i(this, TAG, "onCreate");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bind");
        }

        startBind();
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unregisterBindReceiver();
    }
    
    //start binding device
    private void startBind() {
        LogUtil.i(this, TAG, "startBind");
        String nodeId = AgentLiteUtil.get(ConfigName.verifyCode);
        String verifyCode = AgentLiteUtil.get(ConfigName.verifyCode);
        String manufacturerId = AgentLiteUtil.get(ConfigName.ManufacturerId);
        String deviceType = AgentLiteUtil.get(ConfigName.DeviceType);
        String model = AgentLiteUtil.get(ConfigName.Model);
        String protocolType = AgentLiteUtil.get(ConfigName.ProtocolType);

        IotaDeviceInfo deviceInfo = new IotaDeviceInfo(nodeId, manufacturerId, deviceType, model, protocolType);
        
        configBindPara();
        registerBindReceiver();
        BindService.bind(verifyCode, deviceInfo);
    }
    
    //set binding parameters
    private void configBindPara() {
        BindConfig.setConfig(BindConfig.BIND_CONFIG_ADDR, AgentLiteUtil.get(ConfigName.platformIP));
        BindConfig.setConfig(BindConfig.BIND_CONFIG_PORT, AgentLiteUtil.get(ConfigName.httpPort));
        LogUtil.i(this, TAG, "startBind platformIp = "+ AgentLiteUtil.get(ConfigName.platformIP)
                                            + ":" + AgentLiteUtil.get(ConfigName.httpPort));
    }
    
    private void registerBindReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBindStatusReceiver, 
                new IntentFilter(BindService.TOPIC_BINDDEVICE_RSP));
    }

    private void unregisterBindReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBindStatusReceiver);
    }
    
    private BroadcastReceiver mBindStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage)intent.getSerializableExtra(BindService.BIND_BROADCAST_MSG_IE_IOTAMSG);
            int status = iotaMsg.getUint(BindService.BIND_IE_RESULT, -1);

            switch (status) {
                case 0:
                    saveBindParaAndGotoLogin(iotaMsg);
                    break;
                default:
                    LogUtil.i(AgentLiteBind.this, TAG, "bind fail...");
                    startBind();
                    break;
            }
        }
    };
    
    //保存绑定响应消息携带的参数
    private void saveBindParaAndGotoLogin(IotaMessage iotaMsg) {
        LogUtil.i(this, TAG, "saveBindParaAndGotoLogin");
        String appId = iotaMsg.getString(BindService.BIND_IE_APPID);
        String deviceId = iotaMsg.getString(BindService.BIND_IE_DEVICEID);
        String secret = iotaMsg.getString(BindService.BIND_IE_DEVICESECRET);
        String haAddress = AgentLiteUtil.get(ConfigName.platformIP);
        
        saveGatewayInfo(appId, deviceId, secret, haAddress, null);
        saveSharedPreferences(appId, deviceId, secret, haAddress, null);
        
        Intent intent = new Intent();
        intent.setClass(AgentLiteBind.this, AgentLiteLogin.class);
        startActivity(intent);
        finish();
    }
    
    private void saveGatewayInfo(String appId, String deviceId, String secret, String haAddress, String lvsAddress) {
        GatewayInfo.setAppID(appId);
        GatewayInfo.setDeviceID(deviceId);
        GatewayInfo.setSecret(secret);
        GatewayInfo.setHaAddress(haAddress);
        GatewayInfo.setLvsAddress(lvsAddress);
    }
    
    private void saveSharedPreferences(String appId, String deviceId, String secret, String haAddress, String lvsAddress) {
        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("appID", appId);
        editor.putString("deviceID", deviceId);
        editor.putString("secret", secret);
        editor.putString("haAddress", haAddress);
        editor.putString("lvsAddress", lvsAddress);
        editor.apply();
    }

    @Override
    public void appendLog(final String msg, final boolean newLine) {
        infoText.post(new Runnable() {
            @Override
            public void run() {
                infoText.append(msg);
                if (newLine) {
                    infoText.append("\n");
                }
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
