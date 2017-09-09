package com.qq.vip.singleangel.wifi_direct_demo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
/**
 * Created by singl on 2017/8/6.
 */

public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "SEND_FILE";
    public static final String ACTION_SEND_IP = "SEND_IP";
    public static final String ACTION_START_RECEIVE_VIDEO = "START_RECEIVE_VIDEO";
    public static final String ACTION_SEND_VIDEO = "SEND_VIDEO";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_PORT = "go_port";
    public static final String EXTRAS_VIDEO = "video_byte";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            boolean isGroupOwner = intent.getExtras().getBoolean("isGroupOwner");
            Socket socket = new Socket();
            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(WiFiDirectActivity.TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream);


                // ObjectOutputStream oos = new ObjectOutputStream(stream);
                //oos.writeObject(new String("BROFIST"));


                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
            /**
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            boolean isGroupOwner = intent.getExtras().getBoolean("isGroupOwner");
            Socket socket = new Socket();
            if (isGroupOwner){
                File file = new File(Environment.getExternalStorageDirectory() + "/"
                        + "com.qq.singleangel.wifi_direct_demo" + "/wificlientip-" + ".txt");
                try {
                    InputStream inputStream = new FileInputStream(file);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    ClientList clientIP = (ClientList) objectInputStream.readObject();
                    List<String> clientIps = clientIP.getClientIps();

                    for (String clientAddr : clientIps){
                        try {
                            Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                            socket.bind(null);
                            socket.connect((new InetSocketAddress(clientAddr, port)), SOCKET_TIMEOUT);

                            Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                            OutputStream stream = socket.getOutputStream();
                            ContentResolver cr = context.getContentResolver();
                            InputStream is = null;
                            try {
                                is = cr.openInputStream(Uri.parse(fileUri));
                            } catch (FileNotFoundException e) {
                                Log.d(WiFiDirectActivity.TAG, e.toString());
                            }
                            DeviceDetailFragment.copyFile(is, stream);

                            Log.d(WiFiDirectActivity.TAG, "Client: Data written");
                        } catch (IOException e) {
                            Log.e(WiFiDirectActivity.TAG, e.getMessage());
                        } finally {
                            if (socket != null) {
                                if (socket.isConnected()) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        // Give up
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                }catch (FileNotFoundException e){

                }catch (IOException e){

                }catch (ClassNotFoundException e){

                }
            }else {
                try {
                    Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                    OutputStream stream = socket.getOutputStream();
                    ContentResolver cr = context.getContentResolver();
                    InputStream is = null;
                    try {
                        is = cr.openInputStream(Uri.parse(fileUri));
                    } catch (FileNotFoundException e) {
                        Log.d(WiFiDirectActivity.TAG, e.toString());
                    }
                    DeviceDetailFragment.copyFile(is, stream);


                    // ObjectOutputStream oos = new ObjectOutputStream(stream);
                    //oos.writeObject(new String("BROFIST"));


                    Log.d(WiFiDirectActivity.TAG, "Client: Data written");
                } catch (IOException e) {
                    Log.e(WiFiDirectActivity.TAG, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
             **/


        }else if (intent.getAction().equals(ACTION_SEND_IP)){
            /**
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }**/
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            boolean isGroupOwner = intent.getExtras().getBoolean("isGroupOwner");
            Socket socket = new Socket();
            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();

                ObjectOutputStream oos = new ObjectOutputStream(stream);
                oos.writeObject(new String("BROFIST"));


                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }else if (intent.getAction().equals(ACTION_START_RECEIVE_VIDEO)){
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            Socket socket = new Socket();
            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();

                ObjectOutputStream oos = new ObjectOutputStream(stream);
                oos.writeObject(new String("StartReceiveVideo"));

                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }else if (intent.getAction().equals(ACTION_SEND_VIDEO)){
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            byte[] parm = intent.getByteArrayExtra(EXTRAS_VIDEO);
            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(host,port), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                stream.write(parm);
            }catch (IOException e){

            }finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}
