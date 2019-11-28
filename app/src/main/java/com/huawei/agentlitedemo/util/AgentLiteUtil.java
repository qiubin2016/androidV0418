package com.huawei.agentlitedemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.huawei.agentlitedemo.bean.ConfigName;
import com.huawei.agentlitedemo.bean.GatewayInfo;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class AgentLiteUtil {
    private static final String LOG_TAG = "AgentLiteUtil";
    private static Map<String, String> map = new LinkedHashMap<String, String>();
    private static SharedPreferences mPreferences;

    public static void init(Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public synchronized static boolean loadProperty(InputStream inputStream)
    {
        Log.i(LOG_TAG, "start loadProperty");
        SharedPreferences.Editor editor = mPreferences.edit();
        try
        {
            Properties tempProp = new Properties();
            tempProp.load(inputStream);
            for (ConfigName configName: ConfigName.values())
            {
                String value = tempProp.getProperty(configName.name());
                editor.putString(configName.name(), value);
            }
            editor.putBoolean("hasInitialized", true);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Init Exception." + e);
        }finally {
            editor.apply();
        }
        return true;
    }

    public static boolean hasInitialized(){
        return mPreferences.getBoolean("hasInitialized", false);
    }

    public static String get(ConfigName key)
    {
        return mPreferences.getString(key.toString(), null);
    }
    
    public static void put(String key, String value)
    {
        mPreferences.edit().putString(key, value).apply();
    }
    
    public static void clearSharedPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("AgentLiteDemo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); 
        editor.apply();
    }
    
    public static void clearGatewayInfo() {
        GatewayInfo.setDeviceID(null);
        GatewayInfo.setAppID(null);
        GatewayInfo.setHaAddress(null);
        GatewayInfo.setHttpServerPort(null);
        GatewayInfo.setLvsAddress(null);
        GatewayInfo.setMqttClientId(null);
        GatewayInfo.setMqttServerPort(null);
        GatewayInfo.setMqttTopic(null);
        GatewayInfo.setNodeId(null);
        GatewayInfo.setSecret(null);
    }
}
