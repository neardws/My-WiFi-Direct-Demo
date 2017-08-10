package com.qq.vip.singleangel.wifi_direct_demo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;

/**
 * Created by singl on 2017/8/9.
 */

public class DeviceGroupListener implements WifiP2pManager.GroupInfoListener {
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        WifiP2pGroup wifiP2pGroup = group;
        WifiP2pDevice groupOwner = wifiP2pGroup.getOwner();
        Collection<WifiP2pDevice> clientList = wifiP2pGroup.getClientList();



    }
}
