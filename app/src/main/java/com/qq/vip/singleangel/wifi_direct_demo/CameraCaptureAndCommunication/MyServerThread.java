package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by singl on 2017/8/30.
 */

public class MyServerThread implements Runnable {
    private static String TAG = "MyServerThread";
    private Socket socket;
    private Handler mHandler;
    private Boolean mRunFlag = false;
    private BitmapFactory.Options bitmap_options = new BitmapFactory.Options();

    public MyServerThread(Handler handler){
        super();
        mHandler = handler;
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
    }
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(23333);
            socket = serverSocket.accept();
            mRunFlag = true;
        }catch (IOException e){
            e.printStackTrace();
            mRunFlag = false;
        }

        try {
                InputStream inStream = null;
                try {
                    inStream = socket.getInputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataInputStream is = new DataInputStream(inStream);
                while (mRunFlag) {
                    try {
                    //    int token = is.readInt();
                    //    if (token == 4) {
                            if (is.readUTF().equals("#@@#")) {
                                //System.out.println("before-token" + token);
                                int imgLength = is.readInt();
                                System.out.println("getLength:" + imgLength);
                                System.out.println("back-token" + is.readUTF());
                                byte[] buffer = new byte[imgLength];
                                int len = 0;
                                while (len < imgLength) {
                                    len += is.read(buffer, len, imgLength - len);
                                }
                                Message m = mHandler.obtainMessage();
                                m.obj = BitmapFactory.decodeByteArray(buffer, 0, buffer.length,bitmap_options);
                                if (m.obj != null) {
                                    mHandler.sendMessage(m);
                                } else {
                                    System.out.println("Decode Failed");
                                }
                            }
                    //    }else{
                      //      Log.d(TAG,"Skip Dirty bytes!!!!"+Integer.toString(token));
                    //    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
