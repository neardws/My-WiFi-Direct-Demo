package com.qq.vip.singleangel.wifi_direct_demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by singl on 2017/9/3.
 */

public class AutoService extends Service {

    private static final String TAG = "AutoService";

    private WifiP2pManager manager;
    private Channel channel;
    /**
    public static final String ONESHOTACTION = "oneshot_action";
    public static final String DISCOVER_PEERS_ACTION = "discover_peers_action";
    public static final String CONNECT_ACTION = "connect_action";
    **/

    private WiFiDirectActivity activity;
    private DeviceListFragment listFragment;
    private DeviceDetailFragment detailFragment;


    private WifiP2pDevice mDevice = null; //this device
    private List<WifiP2pDevice> peers = new ArrayList<>(); //this device`s peers list



    public AutoService(WiFiDirectActivity activity, WifiP2pManager wifiP2pManager, Channel mChannel){
        this.activity = activity;
        manager = wifiP2pManager;
        channel = mChannel;
        listFragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
        detailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);

    }


    public AutoService(){
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //initService();

    }

    /**
     * 对Service完成初始化
     */
    private void initService(){
        if (activity != null){
            listFragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
            detailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
            manager = activity.getManager();
            channel = activity.getChannel();
        }else {
            listFragment = null;
            detailFragment = null;
            Log.d(TAG, "activity is null");
        }
    }

    private void initListFragment(){
        if (activity != null){
            listFragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
        }else {
            listFragment = null;
            Log.d(TAG, "initListFragment failed");
        }
    }

    private void initDetailFragment(){
        if (activity != null){
            detailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
        }else {
            detailFragment = null;
            Log.d(TAG, "initDetailFragment failed");
        }
    }

    private void initDeviceAndPeers(){
        if (listFragment != null){
            mDevice = listFragment.getDevice();
            peers = listFragment.getPeers();
        }else {
            Log.d(TAG, "ListFragment is null.");
        }

    }

    private WifiP2pConfig computeIntent(List<WifiP2pDevice> peers){
        WifiP2pConfig mP2pConfig = new WifiP2pConfig();
        if (peers.size() == 0){
            Log.d(TAG, "the device is no peers");
            return null;
        }else if (peers.size() == 1){
            mP2pConfig.deviceAddress = peers.get(0).deviceAddress;
            mP2pConfig.groupOwnerIntent = 0;
            mP2pConfig.wps.setup = WpsInfo.PBC;
            return mP2pConfig;
        }else if (peers.size() == 2){
            return null;
        }
        return null;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        activity = (WiFiDirectActivity) intent.getParcelableExtra("activity");
        initService();
        super.onStart(intent, startId);
        new DiscoverPeers().execute();
        //new Thread(new ServiceThread()).start();
        /**
        String action = intent.getAction();
        switch (action){
            case DISCOVER_PEERS_ACTION:
                activity.getDiscoverPeersService();
                initListFragment();
                initDeviceAndPeers();
                break;
            case CONNECT_ACTION:
                WifiP2pConfig config = computeIntent(peers);
                if (config != null){
                    activity.connect(config);
                }
                break;
            case ONESHOTACTION:
                new Thread(new ServiceThread()).start();
                break;
            default:
                Log.d(TAG, "onStartCommand Error Intent action.");
        }
         **/
    }

    /**
     * 对不同的Intent 执行不同操作
     * @return

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {

        String action = intent.getAction();
        switch (action){
            case DISCOVER_PEERS_ACTION:
                activity.getDiscoverPeersService();
                initListFragment();
                initDeviceAndPeers();
                break;
            case CONNECT_ACTION:
                WifiP2pConfig config = computeIntent(peers);
                if (config != null){
                    activity.connect(config);
                }
                break;
            case ONESHOTACTION:
                new Thread(new ServiceThread()).start();
                break;
            default:
                Log.d(TAG, "onStartCommand Error Intent action.");
        }
        return super.onStartCommand(intent, flags, startId);
    }
*/

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class ServiceThread extends Thread{
        @Override
        public void run() {
           // Toast.makeText(activity,"Service Thread is running", Toast.LENGTH_LONG).show();

            super.run();
            Log.d(TAG, "Service thread is running ");
            while (peers == null){
                activity.getDiscoverPeersService();
                initListFragment();
                initDeviceAndPeers();
            }
            while (detailFragment == null){
                WifiP2pConfig config = computeIntent(peers);
                if (config != null){
                    activity.connect(config);
                }
                initDetailFragment();
                 //goIp is null
            }
            //goIp = detailFragment.getGroupOwnerIp();

        }
    }

    private class DiscoverPeers extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /**
            if (!activity.isWifiP2pEnabled()) {
                Toast.makeText(activity, R.string.p2p_off_warning,
                        Toast.LENGTH_SHORT).show();
            }**/
            listFragment.onInitiateDiscovery();
        }

        @Override
        protected Void doInBackground(Void... params) {
            activity.discoverPeers(channel);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(activity, "Discover Peers success",
                    Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }

}
