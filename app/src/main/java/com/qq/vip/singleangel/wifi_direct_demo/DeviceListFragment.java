package com.qq.vip.singleangel.wifi_direct_demo;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qq.vip.singleangel.wifi_direct_demo.PopupWindows.DialogPopup;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by singl on 2017/8/6.
 */

public class DeviceListFragment extends ListFragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);

        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    public List<WifiP2pDevice> getPeers(){
        if (peers.size() != 0){
            return peers;
        }else {
            return null;
        }
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
      //  ((DeviceActionListener) getActivity()).showDetails(device);
        if (!device.isGroupOwner()){
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_IP);
            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, "192.168.49.1");
            serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 7777);
            getActivity().startService(serviceIntent);
        }
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            ImageView image_send = (ImageView) v.findViewById(R.id.img_send);
            ImageView image_disconnect = (ImageView) v.findViewById(R.id.img_disconnect);

            image_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogPopup dialogPopup = new DialogPopup(getActivity(),(DeviceDetailFragment)
                            getActivity().getFragmentManager().findFragmentById(R.id.frag_detail));
                    dialogPopup.showPopupWindow();
                }
            });

            image_disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                }
            });

            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                    if (device.status == WifiP2pDevice.CONNECTED){
                        image_disconnect.setVisibility(View.VISIBLE);
                        image_send.findViewById(R.id.img_send).setVisibility(View.VISIBLE);
                    }
                }
            }




            return v;

        }
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }

    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     *
     */
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    public void dismissDialog(){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }

}
