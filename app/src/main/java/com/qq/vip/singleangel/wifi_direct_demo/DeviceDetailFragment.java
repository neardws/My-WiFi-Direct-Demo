package com.qq.vip.singleangel.wifi_direct_demo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;


/**
 * Created by singl on 2017/8/6.
 */

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    public static final String IP_SERVER = "192.168.49.1";
    private static boolean server_running = false;

    public String deviceAddr;
    public String clientIp;

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    private boolean isGroupowner = false;
    ProgressDialog progressDialog = null;



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                config.groupOwnerIntent = 0; //meaning that who 
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                        //                        new DialogInterface.OnCancelListener() {
                        //
                        //                            @Override
                        //                            public void onCancel(DialogInterface dialog) {
                        //                                ((DeviceActionListener) getActivity()).cancelDisconnect();
                        //                            }
                        //                        }
                );
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);   //connect the peer

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        mContentView.findViewById(R.id.send_ipaddr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_IP);
                serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, info.groupOwnerAddress.getHostAddress());
                serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 8888);
                getActivity().startService(serviceIntent);
            }
        });

        mContentView.findViewById(R.id.btn_save_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView status = (TextView) mContentView.findViewById(R.id.status_text);
                clientIp = status.getText().toString();
            }
        });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (isGroupowner){  //sender is server\

          /**  String localIP = Utils.getLocalIPAddress();
            // Trick to find the ip in the file /proc/net/arp
            String client_mac_fixed = null;
            String clientIP = null;

            TextView view = (TextView) mContentView.findViewById(R.id.device_address);
            String deviceAddress = view.getText().toString();

            client_mac_fixed = deviceAddress.replace("99", "19");
            clientIP = Utils.getIPFromMac(client_mac_fixed);

           **/


            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            Uri uri = data.getData();
            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra("isGroupOwner",true);

            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIp);
            /**
             if(localIP != null && localIP.equals(IP_SERVER)){
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
             }else{
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
             }

             **/
            serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 8989);
            getActivity().startService(serviceIntent);

        }else {
            /**
            String localIP = Utils.getLocalIPAddress();
            // Trick to find the ip in the file /proc/net/arp
            String client_mac_fixed = null;
            String clientIP = null;
            if (device != null){
                client_mac_fixed = device.deviceAddress.replace("99", "19");
                clientIP = Utils.getIPFromMac(client_mac_fixed);
            }
             **/

            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            Uri uri = data.getData();
            TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra("isGroupOwner",false);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, info.groupOwnerAddress.getHostAddress());
            /**
             if(localIP != null && localIP.equals(IP_SERVER)){
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
             }else{
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
             }
             **/

            serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 8888);
            getActivity().startService(serviceIntent);
        }

    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));
        if (info.isGroupOwner){
            isGroupowner = true;
        }


        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

        if (!server_running){
            if (isGroupowner){
                new ServerIPAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
                server_running = true;
            }else {
                new ClientAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
                server_running = true;
            }
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        deviceAddr = device.deviceAddress;
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        private final Context context;
        private final TextView statusText;
        private InetAddress inetAddress;

        /**
         * @param context
         * @param statusText
         */
        public ServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));

                /**
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                Object object = objectInputStream.readObject();
                if (object.getClass().equals(String.class) && ((String) object).equals("BROFIST")) {
                    Log.d(TAG, "Client IP address: "+client.getInetAddress());
                    inetAddress = client.getInetAddress();
                }
                 **/

                serverSocket.close();
                server_running = false;
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
            /**catch (ClassNotFoundException e){
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            } **/
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
          //      statusText.setText(inetAddress.getHostAddress());
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                /**
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", new File(String.valueOf(Uri.parse("file://" + result))));
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.parse("file://" + result), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                 **/
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public static class ServerIPAsyncTask extends AsyncTask<Void, Void, InetAddress> {

        private final Context context;
        private final TextView statusText;
        private InetAddress inetAddress;

        /**
         * @param context
         * @param statusText
         */
        public ServerIPAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;

        }

        @Override
        protected InetAddress doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                InputStream inputstream = client.getInputStream();


                 ObjectInputStream objectInputStream = new ObjectInputStream(inputstream);
                 Object object = objectInputStream.readObject();
                 if (object.getClass().equals(String.class) && ((String) object).equals("BROFIST")) {
                    Log.d(TAG, "Client IP address: "+client.getInetAddress());
                    inetAddress = client.getInetAddress();
                 }
                serverSocket.close();
                server_running = false;
                return inetAddress;

            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            } catch (ClassNotFoundException e){
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
             }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(InetAddress result) {
            if (result != null) {
                statusText.setText(inetAddress.getHostAddress());
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }


    public static class ClientAsyncTask extends AsyncTask<Void, Void, String> {

        private final Context context;
        private final TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public ClientAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8989);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                server_running = false;
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                /**
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                 Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", new File(String.valueOf(Uri.parse("file://" + result))));
                 intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                 } else {
                 intent.setDataAndType(Uri.parse("file://" + result), "application/vnd.android.package-archive");
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 }
                 **/
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

}
