package com.qq.vip.singleangel.wifi_direct_demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by singl on 2017/9/12.
 */

public class ServerVideoTask extends AsyncTask<Void, Void, String> {
    private final Context context;
    private final TextView statusText;
    private DeviceDetailFragment fragment;
    private String clientIp;

    public ServerVideoTask(Context context, View statusText, DeviceDetailFragment deviceDetailFragment){
        this.context = context;
        this.statusText = (TextView) statusText;
        this.fragment = (DeviceDetailFragment) deviceDetailFragment;
    }

    @Override
    protected void onPreExecute() {
        statusText.setText("Opening a server socket");
    }


    @Override
    protected String doInBackground(Void... params) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(25566);
            fragment.setServer_running(true);
            Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
        }catch (IOException e){
            Log.d(WiFiDirectActivity.TAG,"Open serversocket in port:8888 failed");
        }
        while (true){
            try {
                Socket client = serverSocket.accept();
                clientIp = client.getInetAddress().getHostAddress();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".mp4");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                MyFile.copyFile(inputstream, new FileOutputStream(f));

                // serverSocket.close();


                String result = f.getAbsolutePath();

                if (result != null) {
                    //      statusText.setText(inetAddress.getHostAddress());

                    ClientList clientList = MyFile.getClient();
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + result), "video/mp4");

                    context.startActivity(intent);
                }

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "video/mp4");

                context.startActivity(intent);


                //return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }
    }
}
