package com.qq.vip.singleangel.wifi_direct_demo;

import android.app.Application;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.NavUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by singl on 2017/8/9.
 */

public class WiFiDirectApp extends Application {

    private static final String TAG = "WifiDirectAPP";

    WifiManager wifiManager = null;
    WifiP2pManager.Channel p2pChannel = null;
    boolean p2pConnected = false;
    String myAddr = null;
    String deviceName = null;

    WifiP2pDevice thisDevice = null;
    WifiP2pInfo wifiP2pInfo = null;

    boolean isServer = false;

    WiFiDirectActivity homeActivity = null;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    @Override
    public void onCreate(){
        super.onCreate();
    }




}
