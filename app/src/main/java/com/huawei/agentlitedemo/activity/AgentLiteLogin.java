package com.huawei.agentlitedemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.huawei.agentlitedemo.R;
import com.huawei.agentlitedemo.bean.GatewayInfo;
import com.huawei.agentlitedemo.bean.ScrollableLogView;
import com.huawei.agentlitedemo.util.AgentLiteUtil;
import com.huawei.agentlitedemo.util.LogUtil;
import com.huawei.agentlitedemo.widget.BaseAty;
import com.huawei.iota.iodev.IotaDeviceInfo;
import com.huawei.iota.iodev.datatrans.DataTransService;
import com.huawei.iota.iodev.hub.HubService;
import com.huawei.iota.login.LoginConfig;
import com.huawei.iota.login.LoginService;
import com.huawei.iota.util.IotaMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class AgentLiteLogin extends BaseAty implements ScrollableLogView {
    private final String TAG = "AgentLiteLogin";
    private TextView infoText;
    private ScrollView scrollView;
    private Button startButton;
    private String sensorId = "";

    private LocalBroadcastManager localBroadcastManager ;

    private static final String MSG_TIMESTAMP_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agentlite_login);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        startButton = (Button) findViewById(R.id.button_start);
        infoText = (TextView) findViewById(R.id.text_info);

        LogUtil.i(this, TAG, "onCreate");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Login");
        }
        registerLoginReceiver();
        startLogin();

        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance( this );
        localBroadcastManager.registerReceiver( new MyBroadcastReceiver() , new IntentFilter( "com.huawei.agent.base.KeepAliveService"));
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( "tttt" , "线程：============== " + Thread.currentThread().getName() );
            String action = intent.getAction();
            if ( "com.huawei.agent.base.KeepAliveService".equals( action )){
                Log.d( "tttt" , "线程： " + Thread.currentThread().getName() );
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.i(this, TAG, "onDestroy");
        super.onDestroy();
        LoginService.logout();
        unregisterLoginReceiver();
    }

    private void startLogin() {
        LogUtil.i(this, TAG, "startLogin");
        configLoginPara();
        LoginService.login();
    }

    private void registerLoginReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(LoginConnectReceiver,
                new IntentFilter(LoginService.TOPIC_LOGIN_CONNECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(LoginDisconnectReceiver,
                new IntentFilter(LoginService.TOPIC_LOGIN_DISCONNECT));
        LocalBroadcastManager.getInstance(this).registerReceiver(dataReportRsp,
                new IntentFilter(DataTransService.TOPIC_DATA_REPORT_RSP));
        LocalBroadcastManager.getInstance(this).registerReceiver(commandReceiver,
                new IntentFilter(DataTransService.TOPIC_COMMAND_RECEIVE));
        LocalBroadcastManager.getInstance(this).registerReceiver(rmvGatewayReceiver,
                new IntentFilter(HubService.TOPIC_UNBIND_DEVICE));
        LocalBroadcastManager.getInstance(this).registerReceiver(addDeviceReceiver,
                new IntentFilter(HubService.TOPIC_ADDDEV_RSP));
        LocalBroadcastManager.getInstance(this).registerReceiver(rmvDeviceReceiver,
                new IntentFilter(HubService.TOPIC_RMVDEV_RSP));
        LocalBroadcastManager.getInstance(this).registerReceiver(devStatusUpdateReceiver,
                new IntentFilter(HubService.TOPIC_DEVSTATUS_RSP));
        LocalBroadcastManager.getInstance(this).registerReceiver(subMqttTopicReceiver,
                new IntentFilter(DataTransService.TOPIC_MQTT_SUB_RSP));
        LocalBroadcastManager.getInstance(this).registerReceiver(mqttDataPubReceiver,
                new IntentFilter(DataTransService.TOPIC_MQTT_PUB_RSP));
    }

    private void unregisterLoginReceiver() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(LoginConnectReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(LoginDisconnectReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(dataReportRsp);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(commandReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rmvGatewayReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(addDeviceReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rmvDeviceReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(devStatusUpdateReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(subMqttTopicReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mqttDataPubReceiver);
    }

    private BroadcastReceiver LoginConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i(AgentLiteLogin.this, TAG, "Login Success!!!");
            gatewayDataReport();

            gatewayDataReportByMqttDataPub();
            updateDeviceStatus("ONLINE", "NONE");
        }
    };

    private BroadcastReceiver LoginDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(LoginService.LOGIN_BROADCAST_MSG_IE_IOTAMSG);
            int reason = iotaMsg.getUint(LoginService.LOGIN_IE_REASON, -1);
            LogUtil.i(AgentLiteLogin.this, TAG, "reason is : " + reason);
            switch (reason) {
                case LoginService.LOGIN_REASON_DEVICE_NOEXIST:
                case LoginService.LOGIN_REASON_DEVICE_REMOVED:
                    gotoBind();
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver dataReportRsp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(DataTransService.DATATRANS_BROADCAST_IE_IOTAMSG);
            String deviceId = iotaMsg.getString(DataTransService.DATATRANS_IE_DEVICEID);
            int retcode = iotaMsg.getUint(DataTransService.DATATRANS_IE_RESULT, 0);
            LogUtil.i(AgentLiteLogin.this, TAG, "deviceId: " + deviceId + " data report, ret = " + retcode);
            if (deviceId.equals(GatewayInfo.getDeviceID())) {
                LogUtil.i(AgentLiteLogin.this, TAG, "report gateway data success, cookie = " + iotaMsg.getUint(DataTransService.DATATRANS_IE_COOKIE, 0));
            }
//            if (deviceId.equals(GatewayInfo.getSensorId())) {
//                updateDeviceStatus("OFFLINE", "NONE");
//            }
        }
    };

    //添加设备响应
    private BroadcastReceiver addDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(HubService.HUB_BROADCAST_IE_IOTAMSG);
            String deviceId = iotaMsg.getString(HubService.HUB_IE_DEVICEID);
            sensorId = deviceId;
            int ret = iotaMsg.getUint(HubService.HUB_IE_RESULT, HubService.HUB_RESULT_FAILED);
            if (ret == HubService.HUB_RESULT_SUCCESS) {

                LogUtil.i(AgentLiteLogin.this, TAG, "Add device[" + deviceId + "] success!");
                saveSensorDeviceId(deviceId);
                updateDeviceStatus("ONLINE", "NONE");
            }
            if (ret == HubService.HUB_RESULT_DEVICE_EXIST) {
                LogUtil.i(AgentLiteLogin.this, TAG, "Add device[" + deviceId + "] has exit!");
                updateDeviceStatus("ONLINE", "NONE");
            }
        }
    };

    private BroadcastReceiver devStatusUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(HubService.HUB_BROADCAST_IE_IOTAMSG);
            int result = iotaMsg.getUint(HubService.HUB_IE_RESULT, 0);
            int cookie = iotaMsg.getUint(HubService.HUB_IE_COOKIE, 0);
            String deviceId = iotaMsg.getString(HubService.HUB_IE_DEVICEID);

            LogUtil.i(AgentLiteLogin.this, TAG, "cookie = " + cookie);
            if (result == 0) {
                LogUtil.i(AgentLiteLogin.this, TAG, "update device[" + deviceId + "] status success!");
                return;
            }
            if (result == HubService.HUB_RESULT_DEVICE_NOTEXIST) {
                LogUtil.i(AgentLiteLogin.this, TAG, "device[" + deviceId + "] is not exit!");
            } else {
                LogUtil.i(AgentLiteLogin.this, TAG, "update device[" + deviceId + "] status fail!");
            }
        }
    };

    private BroadcastReceiver subMqttTopicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(DataTransService.DATATRANS_BROADCAST_IE_IOTAMSG);
            int result = iotaMsg.getUint(DataTransService.DATATRANS_IE_RESULT, 0);
            int cookie = iotaMsg.getUint(DataTransService.DATATRANS_IE_COOKIE, 0);

            LogUtil.i(AgentLiteLogin.this, TAG, "subMqttTopicReceiver cookie = " + cookie);
            if (result == 0) {
                LogUtil.i(AgentLiteLogin.this, TAG, "subMqttTopicReceiver success!");
                return;
            }

        }
    };

    private BroadcastReceiver mqttDataPubReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(DataTransService.DATATRANS_BROADCAST_IE_IOTAMSG);
            int result = iotaMsg.getUint(DataTransService.DATATRANS_IE_RESULT, 0);
            int cookie = iotaMsg.getUint(DataTransService.DATATRANS_IE_COOKIE, 0);

            LogUtil.i(AgentLiteLogin.this, TAG, "mqttDataPubReceiver cookie = " + cookie);
            if (result == 0) {
                LogUtil.i(AgentLiteLogin.this, TAG, "mqttDataPubReceiver success!");
                return;
            }

        }
    };

    //删除设备响应
    private BroadcastReceiver rmvDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(HubService.HUB_BROADCAST_IE_IOTAMSG);
            int result = iotaMsg.getUint(HubService.HUB_IE_RESULT, 0);
            int cookie = iotaMsg.getUint(HubService.HUB_IE_COOKIE, 0);
            LogUtil.i(AgentLiteLogin.this, TAG, "rmv device result = " + result);
            LogUtil.i(AgentLiteLogin.this, TAG, "cookie = " + cookie);
            if (result == 0) {
                SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("SENSORID", null);
                editor.apply();

                LogUtil.i(AgentLiteLogin.this, TAG, "rmv device success!");
            } else {
                LogUtil.i(AgentLiteLogin.this, TAG, "rmv device fail!");
            }
        }
    };

    private BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IotaMessage iotaMsg = (IotaMessage) intent.getSerializableExtra(DataTransService.DATATRANS_BROADCAST_IE_IOTAMSG);
            String deviceId = iotaMsg.getString(DataTransService.DATATRANS_IE_DEVICEID);
            String requestId = iotaMsg.getString(DataTransService.DATATRANS_IE_REQUSTID);
            String serviceId = iotaMsg.getString(DataTransService.DATATRANS_IE_SERVICEID);
            String method = iotaMsg.getString(DataTransService.DATATRANS_IE_METHOD);
            String cmd = iotaMsg.getString(DataTransService.DATATRANS_IE_CMDCONTENT);

            if (method.equals("REMOVE") && deviceId.equals(sensorId)) {
                //do something, e.g. show a dialog
                //rmvSensor();
            }

            LogUtil.i(AgentLiteLogin.this, TAG, "Receive cmd :"
                    + "\ndeviceId  = " + deviceId
                    + "\nrequestId = " + requestId
                    + "\nserviceId = " + serviceId
                    + "\nmethod    = " + method
                    + "\ncmd       = " + cmd);
        }
    };

    private BroadcastReceiver rmvGatewayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i(AgentLiteLogin.this, TAG, "Receive remove gateway cmd...");
            rmvGateway(context);
            startLogin();
        }
    };


    private void addSensor() {
        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        if (preferences.getString("SENSORID", null) != null) {
            Toast.makeText(this, "The sensor is already added.", Toast.LENGTH_SHORT).show();
            return;
        }
        LogUtil.i(this, TAG, "addSensor!");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        IotaDeviceInfo deviceInfo = new IotaDeviceInfo("0123456test", "Huawei", "Motion", "test01", "MQTT");
        HubService.addDevice(cookie, deviceInfo);
    }

    private void subMqttTopic() {

        LogUtil.i(this, TAG, "subMqttTopic!");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        DataTransService.mqttSubTopic(cookie,"/huawei/testtopic/#",1);
    }

    private void updateDeviceStatus(String status, String statusDetail) {
        LogUtil.i(this, TAG, "updateDeviceStatus! status " + status);
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        String deviceId = preferences.getString("SENSORID", null);

        if (deviceId != null) {
            HubService.updateDeviceStatus(cookie, deviceId, status, statusDetail);
        }
    }


    private void gatewayDataReport() {
        LogUtil.i(this, TAG, "gatewayDataReport!");
        int cookie;
        String deviceId = GatewayInfo.getDeviceID();

        Random random = new Random();
        cookie = random.nextInt(65535);
        LogUtil.i(this, TAG, "cookie = " + cookie);
        DataTransService.dataReport(cookie, null, deviceId, "Storage", "{\"storage\":10240,\"usedPercent\":20}");
    }

    private void gatewayDataReportByMqttDataPub() {
        LogUtil.i(this, TAG, "gatewayDataReportByMqttDataPub!");
        int cookie;
        String deviceId = GatewayInfo.getDeviceID();

        Random random = new Random();
        cookie = random.nextInt(65535);

        JsonObject headerData = new JsonObject();
        headerData.addProperty("method", "PUT");
        String fromStr = "/device/"+deviceId+"/services/Storage";
        String toStr = "/data/v1.1.0/devices/"+deviceId+"/services/Storage";
        headerData.addProperty("from", fromStr);
        headerData.addProperty("to", toStr);

        headerData.addProperty("access_token", GatewayInfo.getAccessToken());

        SimpleDateFormat df = new SimpleDateFormat(MSG_TIMESTAMP_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String curTime = df.format(new Date(System.currentTimeMillis()));
        headerData.addProperty("timestamp", curTime);
        headerData.addProperty("eventTime", curTime);

        JsonObject bodyData = new JsonObject();
        bodyData.addProperty("storage", "10240");
        bodyData.addProperty("usedPercent", "18");

        JsonObject mqttMsg = new JsonObject();
        mqttMsg.add("header", headerData);
        mqttMsg.add("body", bodyData);

        DataTransService.mqttDataPub(cookie, "/cloud/signaltrans/v2/categories/data", 1, mqttMsg.toString().getBytes());
    }

    private void sensorDataReport() {
        LogUtil.i(this, TAG, "sensorDataReport!");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        String deviceId = preferences.getString("SENSORID", null);

        updateDeviceStatus("ONLINE", "NONE");

        DataTransService.dataReport(cookie, null, deviceId, "Battery", "{\"batteryLevel\":98}");
        DataTransService.dataReport(cookie, null, deviceId, "Motion", "{\"motion\":\"DETECTED\"}");
    }

    private void rmvSensor() {
        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        if (preferences.getString("SENSORID", null) == null) {
            Toast.makeText(this, "The sensor is already removed.", Toast.LENGTH_SHORT).show();
            return;
        }
        LogUtil.i(this, TAG, "rmvSensor!");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        String deviceId = preferences.getString("SENSORID", null);
        LogUtil.i(this, TAG, "rmvSensor deviceId = " + deviceId);
        HubService.rmvDevice(cookie, deviceId);
    }

    private void rmvGateway(Context context) {
        LogUtil.i(this, TAG, "rmvGateway!");
        AgentLiteUtil.clearGatewayInfo();
        AgentLiteUtil.clearSharedPreferences(context);
        Toast.makeText(this, "The gateway is deleted.", Toast.LENGTH_SHORT).show();
    }

    private void configLoginPara() {
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_DEVICEID, GatewayInfo.getDeviceID());
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_APPID, GatewayInfo.getAppID());
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_SECRET, GatewayInfo.getSecret());
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_IOCM_ADDR, GatewayInfo.getHaAddress());
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_IOCM_PORT, "8943");
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_MQTT_ADDR, GatewayInfo.getHaAddress());
        LoginConfig.setConfig(LoginConfig.LOGIN_CONFIG_MQTT_PORT, "8883");
    }

    private void saveSensorDeviceId(String deviceId) {
        GatewayInfo.setSensorId(deviceId);


        SharedPreferences preferences = getSharedPreferences("AgentLiteDemo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("SENSORID", deviceId);

        editor.apply();
    }

    private void gotoBind() {
        LogUtil.i(this, TAG, "gotoBind");
        Intent intent = new Intent();
        intent.setClass(AgentLiteLogin.this, AgentLiteBind.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0, 0, 0, "add sensor");
        menu.add(0, 1, 1, "delete sensor");
        menu.add(0, 2, 2, "report sensor data");
        menu.add(0, 3, 3, "clear the screen");
        menu.add(0, 4, 4, "sub mqtt topic");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return super.onOptionsItemSelected(item);
//            return true;
//        }
        switch (id) {
            case 0:
                addSensor();
                break;
            case 1:
                rmvSensor();
                break;
            case 2:
                sensorDataReport();
                break;
            case 3:
                clearScreenLog();
                break;
            case 4:
                subMqttTopic();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
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

    private void clearScreenLog() {
        infoText.setText("");
    }

}
