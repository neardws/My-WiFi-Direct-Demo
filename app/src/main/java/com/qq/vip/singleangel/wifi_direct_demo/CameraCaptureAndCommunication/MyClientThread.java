package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by singl on 2017/8/30.
 */

public class MyClientThread implements Runnable{
    private int mServerPort;
    private String mServerIP;
    private Context mContext;
    private static final int SOCKET_TIMEOUT = 5000;
    private CameraCaptureActivity cameraCaptureActivity;
    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mServerIP,mServerPort),SOCKET_TIMEOUT);
            Toast.makeText(cameraCaptureActivity,"Connect succesd",Toast.LENGTH_LONG);
            //new ServerSocketThread(socket).run();
            new Thread(new ServerSocketThread(socket)).start();
        }catch(Exception e){
            Log.d("ServerThread", "run: erro");
        }
    }

    public MyClientThread(Context context,String serverip,int serverport){
        super();
        mContext=context;
        mServerIP = serverip;
        mServerPort = serverport;
        cameraCaptureActivity = (CameraCaptureActivity) mContext;
    }

    public class ServerSocketThread implements Runnable{
        Socket s = null;
        // BufferedReader br = null;
        //BufferedWriter bw = null;
        OutputStream os = null;
        public ServerSocketThread(Socket s) throws IOException {
            this.s = s;
            //br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        }
        @Override
        public void run() {
            if(s !=null){
                String clientIp = s.getInetAddress().toString().replace("/", "");
                int clientPort = s.getPort();
                System.out.println("====client ip====="+clientIp);
                System.out.println("====client port====="+clientPort);
                try {

                   // s.setKeepAlive(true);
                    os = s.getOutputStream();
                    while(true){
                        //dos.write(mPreview.mFrameBuffer.);
                        DataOutputStream dos = new DataOutputStream(os);
                     //   dos.writeInt(4);
                        dos.writeUTF("#@@#");
                        dos.writeInt(cameraCaptureActivity.preview.mFrameBuffer.size());
                        dos.writeUTF("-@@-");
                        dos.flush();
                        System.out.println(cameraCaptureActivity.preview.mFrameBuffer.size());
                        dos.write(cameraCaptureActivity.preview.mFrameBuffer.toByteArray());
                        //System.out.println("outlength"+mPreview.mFrameBuffer.length);
                        dos.flush();
                    //    Thread.sleep(1000/15);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (os!= null)
                            os.close();

                    } catch (Exception e2) {
                        e.printStackTrace();
                    }

                }



            }
            else{
                System.out.println("socket is null");

            }
        }

    }
}
