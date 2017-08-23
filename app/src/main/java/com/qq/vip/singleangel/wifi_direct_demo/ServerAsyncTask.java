package com.qq.vip.singleangel.wifi_direct_demo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by singl on 2017/8/22.
 */

public class ServerAsyncTask extends AsyncTask<Void, Void, String> {

    private final Context context;
    private final TextView statusText;
    private DeviceDetailFragment fragment;
    private String clientIp;

    /**
     * @param context
     * @param statusText
     */
    public ServerAsyncTask(Context context, View statusText, DeviceDetailFragment deviceDetailFragment) {
        this.context = context;
        this.statusText = (TextView) statusText;
        this.fragment = (DeviceDetailFragment) deviceDetailFragment;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            clientIp = client.getInetAddress().getHostAddress();
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
            MyFile.copyFile(inputstream, new FileOutputStream(f));

            /**
             ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
             Object object = objectInputStream.readObject();
             if (object.getClass().equals(String.class) && ((String) object).equals("BROFIST")) {
             Log.d(TAG, "Client IP address: "+client.getInetAddress());
             inetAddress = client.getInetAddress();
             }
             **/

            serverSocket.close();
            fragment.setServer_running(false);
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

            /**
            Intent serviceIntent = new Intent(context, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, Uri.parse("file://" + result).toString());
            serviceIntent.putExtra("isGroupOwner",true);

            /**
            List<String> clientIps = fragment.getClientIps();
            for (String string : clientIps){
                if (!string.equals(clientIp)){
                    clientIp = string;
                }
            }
             **/

            SharedPreferences sharedPreferences = context.getSharedPreferences("wifi_direct",Context.MODE_PRIVATE);
            int Num = sharedPreferences.getInt("Num",0);
            for (int i = 0; i < Num; i++){
                if (!sharedPreferences.getString("item"+i,null).equals(clientIp)){
                    clientIp = sharedPreferences.getString("item"+i,null);
                }
            }

            File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/wificlientip-" + ".txt");
            try {
                InputStream inputStream = new FileInputStream(f);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ClientList clientIP = (ClientList) objectInputStream.readObject();
                List<String> restClient = clientIP.getRestClient(clientIp);

                for (String clientAddr : restClient){
                    Intent serviceIntent = new Intent(context, FileTransferService.class);
                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, Uri.parse("file://" + result).toString());
                    serviceIntent.putExtra("isGroupOwner",true);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientAddr);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, 8989);
                    context.startService(serviceIntent);
                }

            }catch (FileNotFoundException e){

            }catch (IOException e){

            }catch (ClassNotFoundException e){

            }



            /**
             if(localIP != null && localIP.equals(IP_SERVER)){
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
             }else{
             serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
             }

             **/





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
