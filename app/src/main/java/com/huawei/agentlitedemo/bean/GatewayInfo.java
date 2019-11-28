package com.huawei.agentlitedemo.bean;

public class GatewayInfo
{ 
    private static String deviceID;
    
    private static String secret;
    
    private static String appID;
    
    private static String haAddress;
    
    private static String lvsAddress;
    
    private static String mqttTopic;
    
    private static String mqttClientId;
    
    private static String mqttServerPort;
    
    private static String httpServerPort;
    
    private static String nodeId;
    
    private static String sensorId;
    
    private static String sensorNodeId;
    
    private static String accessToken;
    /**
     * @return Returns the deviceID.
     */
    public static String getDeviceID()
    {
        return deviceID;
    }
    
    /**
     * @param deviceID The deviceID to set.
     */
    public static void setDeviceID(String deviceID)
    {
        GatewayInfo.deviceID = deviceID;
    }
    
    /**
     * @return Returns the secret.
     */
    public static String getSecret()
    {
        return secret;
    }
    
    /**
     * @param secret The secret to set.
     */
    public static void setSecret(String secret)
    {
        GatewayInfo.secret = secret;
    }
    
    /**
     * @return Returns the appID.
     */
    public static String getAppID()
    {
        return appID;
    }
    
    /**
     * @param appID The appID to set.
     */
    public static void setAppID(String appID)
    {
        GatewayInfo.appID = appID;
    }
    
    public static String getMqttTopic()
    {
        return mqttTopic;
    }
    
    public static void setMqttTopic(String mqttTopic)
    {
        GatewayInfo.mqttTopic = mqttTopic;
    }
    
    public static String getMqttClientId()
    {
        return mqttClientId;
    }
    
    public static void setMqttClientId(String mqttClientId)
    {
        GatewayInfo.mqttClientId = mqttClientId;
    }
    
    public static String getHaAddress()
    {
        return haAddress;
    }
    
    public static void setHaAddress(String haAddress)
    {
        GatewayInfo.haAddress = haAddress;
    }
    
    public static String getLvsAddress()
    {
        return lvsAddress;
    }
    
    public static void setLvsAddress(String lvsAddress)
    {
        GatewayInfo.lvsAddress = lvsAddress;
    }
    
    public static String getMqttServerPort()
    {
        return mqttServerPort;
    }
    
    public static void setMqttServerPort(String port)
    {
        GatewayInfo.mqttServerPort = port;
    }
    
    public static String getHttpServerPort()
    {
        return httpServerPort;
    }
    
    public static void setHttpServerPort(String port)
    {
        GatewayInfo.httpServerPort = port;
    }
    
    public static boolean isAvailable()
    {
        if (null != deviceID && !deviceID.isEmpty())
        {
            return true;
        }
        
        return false;
    }

    public static String getNodeId() 
    {
        return nodeId;
    }

    public static void setNodeId(String nodeId) 
    {
        GatewayInfo.nodeId = nodeId;
    }
    
    public static String getSensorId() {
        return sensorId;
    }
    
    public static void setSensorId(String sensorId) {
        GatewayInfo.sensorId = sensorId;
    }
    
    public static String getSensorNodeId() {
        return sensorNodeId;
    }
    
    public static void setSensorNodeId(String nodeId) {
        GatewayInfo.sensorNodeId = nodeId;
    }
    public static String getAccessToken() {
        return accessToken;
    }
    public static void setAccessToken(String accessToken) {
        GatewayInfo.accessToken = accessToken;
    }
}
