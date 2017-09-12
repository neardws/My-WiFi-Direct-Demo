package com.qq.vip.singleangel.wifi_direct_demo;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.qq.vip.singleangel.wifi_direct_demo.CameraCapture.CameraActivity;
import com.qq.vip.singleangel.wifi_direct_demo.PopupWindows.DialogPopup;

import org.opencv.android.OpenCVLoader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by singl on 2017/8/6.
 */

public class WiFiDirectActivity extends AppCompatActivity implements ChannelListener, DeviceListFragment.DeviceActionListener {

    public static final String TAG = "wifidirectdemo";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean isDiscover = false;
    private boolean isConnected = false;
    private boolean retryChannel = false;


    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    public boolean isWifiP2pEnabled(){
        return this.isWifiP2pEnabled;
    }

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    static {
        if (!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded.");
            //Toast.makeText(, "OpenCV not loaded.", Toast.LENGTH_LONG);
        }else {
            Log.d(TAG, "OpenCV loaded.");
            //Toast.makeText(, "OpenCV loaded.", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {     //fixed Android 7.0 FileUriExposedException
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }



    }

    public WifiP2pManager getManager(){
        return manager;
    }

    public Channel getChannel(){
        return channel;
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
       // MyFile.ClearFile();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
     //   MyFile.ClearFile();
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_item, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_start:
                if (!isWifiP2pEnabled()){ //wifi is not enabled
                    Toast.makeText(WiFiDirectActivity.this, "Please turn on the WiFi.",Toast.LENGTH_LONG).show();
                    return true;
                }else {  //wifi is enabled
                    new DiscoverPeers().execute();
                    return true;
                }
            case R.id.atn_start2:
                new ConnectPeers().execute();
                return true;
            case R.id.atn_start3:
                DialogPopup dialogPopup = new DialogPopup(WiFiDirectActivity.this,(DeviceDetailFragment)
                        WiFiDirectActivity.this.getFragmentManager().findFragmentById(R.id.frag_detail));
                dialogPopup.showPopupWindow();
                return true;
            case R.id.atn_video:
                Intent intent = new Intent(WiFiDirectActivity.this, CameraActivity.class);
                WiFiDirectActivity.this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }


    public void getDiscoverPeersService(){
        if (!isWifiP2pEnabled) {
            Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                    Toast.LENGTH_SHORT).show();
        }else {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.onInitiateDiscovery();
            discoverPeers(channel);
        }
    }


    public void discoverPeers(Channel channel){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
                isDiscover = true;
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
                isDiscover = false;
            }
        });
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                isConnected = true;
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                isConnected = false;
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    public void requestGroupInfo(WifiP2pManager.GroupInfoListener groupInfoListener){
        manager.requestGroupInfo(channel,groupInfoListener);
    }


    public class OneShot extends Thread{
        public OneShot(){
        }

        @Override
        public void run() {
            super.run();
        }



    }

    private class DiscoverPeers extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /**
             if (!activity.isWifiP2pEnabled()) {
             Toast.makeText(activity, R.string.p2p_off_warning,
             Toast.LENGTH_SHORT).show();
             }**/
            final DeviceListFragment listFragment = (DeviceListFragment) WiFiDirectActivity.this
                    .getFragmentManager().findFragmentById(R.id.frag_list);
            listFragment.onInitiateDiscovery();
           // WiFiDirectActivity.this.isDiscover = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!WiFiDirectActivity.this.isDiscover){
                WiFiDirectActivity.this.discoverPeers(channel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final DeviceListFragment listFragment = (DeviceListFragment) WiFiDirectActivity.this
                    .getFragmentManager().findFragmentById(R.id.frag_list);
            listFragment.dismissDialog();
            Toast.makeText(WiFiDirectActivity.this, "Discover Peers success",
                    Toast.LENGTH_SHORT).show();
           // new ConnectPeers().execute();
            super.onPostExecute(aVoid);
        }
    }

    private class ConnectPeers extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final DeviceDetailFragment detailFragment = (DeviceDetailFragment) WiFiDirectActivity.this.
                    getFragmentManager().findFragmentById(R.id.frag_detail);
            detailFragment.initConnect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!isConnected){
                final DeviceListFragment listFragment = (DeviceListFragment) WiFiDirectActivity.this.getFragmentManager().findFragmentById(R.id.frag_list);
                WifiP2pConfig config = computeIntent(listFragment.getPeers()); //notice that only discovery success ,the peers is exits
                if (config != null){
                    WiFiDirectActivity.this.connect(config);
                }else {
                    Toast.makeText(WiFiDirectActivity.this, "Peers List is null.",Toast.LENGTH_LONG).show();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final DeviceDetailFragment detailFragment = (DeviceDetailFragment) WiFiDirectActivity.this.
                    getFragmentManager().findFragmentById(R.id.frag_detail);
            detailFragment.dismissDialog();
            Toast.makeText(WiFiDirectActivity.this,"Connect is success,Please send message",Toast.LENGTH_LONG).show();
        }

        private WifiP2pConfig computeIntent(List<WifiP2pDevice> peers){
            WifiP2pConfig mP2pConfig = new WifiP2pConfig();
            if (peers == null){
                Toast.makeText(WiFiDirectActivity.this, "Peers is void.Please discover peers.", Toast.LENGTH_LONG);
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
    }

    public String getPacketName(){
        return this.getPackageName();
    }

}

